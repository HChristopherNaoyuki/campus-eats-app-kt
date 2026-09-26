package com.example.campus_eats_app_kt.data

import android.util.Log
import com.example.campus_eats_app_kt.data.dao.CouponDao
import com.example.campus_eats_app_kt.data.dao.FeedbackDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.CouponEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.seconds

/**
 * FirebaseSyncManager serves as the single, authoritative background data synchronizer
 * between local Room storage and Firebase Realtime Database.
 *
 * Operational features:
 * 1. Executes a continuous loop targeted at approximately 10 second intervals.
 * 2. Checks active network connectivity before initiating database writes.
 * 3. Applies exponential backoff on failure (1s, 2s, 4s, 8s, up to a 60s cap).
 * 4. Only transmits unsynced records identified by flags (isSynced == false / isPendingSync == true).
 * 5. Clears local unsynced flags only after successful write completion in Firebase.
 * 6. Utilizes centralized exception handling via FirebaseExceptionHandler.
 */
class FirebaseSyncManager(
    private val userDao: UserDao,
    private val orderDao: OrderDao,
    private val feedbackDao: FeedbackDao,
    private val couponDao: CouponDao,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabaseProvider.instance,
)
{
    private val tag = "FirebaseSyncManager"

    // Base interval for regular periodic synchronization loops in seconds
    private val baseIntervalSeconds = 10L

    // Maximum backoff duration allowed during repeated failure recovery
    private val maxBackoffSeconds = 60L

    /**
     * Starts the continuous background synchronization loop in the provided CoroutineScope.
     * Operates continuously until the underlying CoroutineScope is cancelled.
     */
    fun startContinuousSync(scope: CoroutineScope)
    {
        scope.launch()
        {
            Log.i(tag, "Starting continuous Realtime Database synchronization loop.")

            var currentBackoffSeconds = 1L

            while (isActive)
            {
                var cycleHasErrors = false

                try
                {
                    if (connectivityManager.hasInternetConnection())
                    {
                        syncUsersNode()
                        syncOrdersNode()
                        syncFeedbackNode()
                        syncCouponsNode()
                    }
                    else
                    {
                        Log.d(tag, "Device is offline. Skipping current synchronization cycle.")
                    }
                }
                catch (e: DatabaseException)
                {
                    cycleHasErrors = true
                    handleDatabaseError(e)
                }
                catch (e: Exception)
                {
                    cycleHasErrors = true
                    val parsedMessage = FirebaseExceptionHandler.parse(e)
                    Log.w(tag, "Synchronization error encountered: $parsedMessage", e)
                }

                // Calculate next loop delay based on execution success or failure
                val delayTimeSeconds = if (cycleHasErrors)
                {
                    val activeDelay = currentBackoffSeconds
                    // Exponential backoff doubling up to maximum cap
                    currentBackoffSeconds = (currentBackoffSeconds * 2L).coerceAtMost(maxBackoffSeconds)
                    Log.i(tag, "Exponential backoff engaged. Waiting $activeDelay seconds before retry.")
                    activeDelay
                }
                else
                {
                    // Reset exponential backoff state after a clean cycle
                    currentBackoffSeconds = 1L
                    baseIntervalSeconds
                }

                delay(delayTimeSeconds.seconds)
            }
        }
    }

    /**
     * Synchronizes local unsynced user records to the 'users/$userId' node.
     * Sets local isSynced flag to true upon successful Firebase write.
     */
    private suspend fun syncUsersNode()
    {
        val unsyncedUsers = userDao.getUnsyncedUsers()
        if (unsyncedUsers.isEmpty())
        {
            return
        }

        Log.d(tag, "Found ${unsyncedUsers.size} unsynced user records. Initiating cloud sync.")

        for (user in unsyncedUsers)
        {
            try
            {
                val userMap = mapUserToFirebase(user)
                firebaseDatabase.getReference("users")
                    .child(user.userId)
                    .setValue(userMap)
                    .await()

                userDao.markAsSynced(user.userId)
                Log.d(tag, "Successfully synchronized user record for: ${user.email}")
            }
            catch (e: Exception)
            {
                val parsedMessage = FirebaseExceptionHandler.parse(e)
                Log.w(tag, "Failed to sync user ${user.email}: $parsedMessage")
                throw e
            }
        }
    }

    /**
     * Synchronizes orders that are pending sync to the 'orders/$orderId' node.
     * Updates isPendingSync flag to false upon successful Firebase write.
     */
    private suspend fun syncOrdersNode()
    {
        val orders = orderDao.getAllOrders().first()
        val pendingOrders = orders.filter { it.isPendingSync }

        if (pendingOrders.isEmpty())
        {
            return
        }

        Log.d(tag, "Found ${pendingOrders.size} pending orders. Initiating cloud sync.")

        for (order in pendingOrders)
        {
            try
            {
                val orderMap = mapOrderToFirebase(order)
                firebaseDatabase.getReference("orders")
                    .child(order.orderId)
                    .setValue(orderMap)
                    .await()

                orderDao.updateOrder(order.copy(isPendingSync = false))
                Log.d(tag, "Successfully synchronized order record: ${order.orderId}")
            }
            catch (e: Exception)
            {
                val parsedMessage = FirebaseExceptionHandler.parse(e)
                Log.w(tag, "Failed to sync order ${order.orderId}: $parsedMessage")
                throw e
            }
        }
    }

    /**
     * Synchronizes feedback records to the 'feedback/$feedbackId' node.
     */
    private suspend fun syncFeedbackNode()
    {
        val currentUser = firebaseAuth.currentUser ?: return
        try
        {
            val feedbackList = feedbackDao.getAllFeedback().first()
            val userFeedback = feedbackList.filter { it.userId == currentUser.uid }

            for (feedback in userFeedback)
            {
                val feedbackMap = mapFeedbackToFirebase(feedback)
                firebaseDatabase.getReference("feedback")
                    .child(feedback.feedbackId.toString())
                    .setValue(feedbackMap)
                    .await()
            }
        }
        catch (e: Exception)
        {
            val parsedMessage = FirebaseExceptionHandler.parse(e)
            Log.w(tag, "Failed feedback sync cycle: $parsedMessage")
            throw e
        }
    }

    /**
     * Synchronizes promotional coupons to the 'coupons/$code' node.
     */
    private suspend fun syncCouponsNode()
    {
        try
        {
            val coupons = couponDao.getAllCoupons().first()
            for (coupon in coupons)
            {
                val couponMap = mapCouponToFirebase(coupon)
                firebaseDatabase.getReference("coupons")
                    .child(coupon.code)
                    .setValue(couponMap)
                    .await()
            }
        }
        catch (e: Exception)
        {
            val parsedMessage = FirebaseExceptionHandler.parse(e)
            Log.w(tag, "Failed coupon sync cycle: $parsedMessage")
            throw e
        }
    }

    /**
     * Handles Firebase DatabaseException instances by parsing error messages and logging appropriately.
     */
    private fun handleDatabaseError(exception: DatabaseException)
    {
        val parsedMessage = FirebaseExceptionHandler.parse(exception)
        Log.e(tag, "Firebase Realtime Database error encountered: $parsedMessage", exception)
    }

    /**
     * Maps UserEntity into a Firebase Realtime Database map representation.
     * Passwords are never sent to the remote database and are mapped to [FIREBASE_SSO].
     */
    private fun mapUserToFirebase(user: UserEntity): Map<String, Any?>
    {
        return mapOf(
            "userId" to user.userId,
            "fullName" to user.fullName,
            "username" to user.username,
            "email" to user.email,
            "role" to if (user.role == UserRole.ADMINISTRATOR) "ADMIN" else user.role.name,
            "passwordHash" to "[FIREBASE_SSO]",
            "status" to user.status.name,
            "walletBalance" to user.walletBalance,
            "shopName" to user.shopName,
            "shopStatus" to user.shopStatus?.name,
            "bankAccountInfo" to user.bankAccountInfo,
            "registrationDate" to user.registrationDate,
            "usercode" to user.usercode,
        )
    }

    /**
     * Maps OrderEntity into a Firebase Realtime Database map representation.
     */
    private fun mapOrderToFirebase(order: OrderEntity): Map<String, Any?>
    {
        return mapOf(
            "orderId" to order.orderId,
            "customerId" to order.customerId,
            "vendorId" to order.vendorId,
            "itemsJson" to order.itemsJson,
            "totalAmount" to order.totalAmount,
            "status" to order.status.name,
            "paymentMethod" to order.paymentMethod.name,
            "pickupTime" to order.pickupTime,
            "specialRequests" to order.specialRequests,
            "timestamp" to order.timestamp,
        )
    }

    /**
     * Maps FeedbackEntity into a Firebase Realtime Database map representation.
     */
    private fun mapFeedbackToFirebase(feedback: FeedbackEntity): Map<String, Any?>
    {
        return mapOf(
            "userId" to feedback.userId,
            "type" to feedback.type.name.lowercase(),
            "subject" to feedback.subject,
            "message" to feedback.message,
            "userName" to feedback.userName,
            "userEmail" to feedback.userEmail,
            "status" to feedback.status.name.lowercase(),
            "createdAt" to feedback.createdAt,
            "updatedAt" to feedback.updatedAt,
        )
    }

    /**
     * Maps CouponEntity into a Firebase Realtime Database map representation.
     */
    private fun mapCouponToFirebase(coupon: CouponEntity): Map<String, Any?>
    {
        return mapOf(
            "code" to coupon.code,
            "discountPercent" to coupon.discountPercent,
            "isActive" to coupon.isActive,
            "expiryDate" to coupon.expiryDate,
            "assignedUserId" to coupon.assignedUserId,
        )
    }
}
