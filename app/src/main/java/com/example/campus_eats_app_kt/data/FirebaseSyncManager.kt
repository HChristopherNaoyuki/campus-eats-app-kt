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
 * FirebaseSyncManager handles continuous online data synchronization with Firebase Realtime Database.
 * Operates on a 10-second interval, checking connectivity and handling security rules and permission errors.
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

    /**
     * Starts the continuous background synchronization loop.
     * The loop runs every 10 seconds while the provided CoroutineScope remains active.
     */
    fun startContinuousSync(scope: CoroutineScope)
    {
        scope.launch()
        {
            Log.i(tag, "Starting continuous 10-second Realtime Database synchronization loop.")

            while (isActive)
            {
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
                    handleDatabaseError(e)
                }
                catch (e: Exception)
                {
                    Log.w(tag, "Unexpected error during synchronization cycle: ${e.message}")
                }

                // Wait for the 10-second interval before next sync iteration
                delay(10.seconds)
            }
        }
    }

    /**
     * Synchronizes local unsynced user records to the 'users/$campus_user_id' node.
     * Enforces security rules: 'ADMIN' role string, passwordHash, and email constraints.
     */
    private suspend fun syncUsersNode()
    {
        val unsyncedUsers = userDao.getUnsyncedUsers()
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
            catch (e: DatabaseException)
            {
                handleDatabaseError(e)
            }
            catch (e: Exception)
            {
                Log.w(tag, "Failed to sync user ${user.email}: ${e.message}")
            }
        }
    }

    /**
     * Synchronizes orders to the 'orders' node in Realtime Database.
     */
    private suspend fun syncOrdersNode()
    {
        try
        {
            val orders = orderDao.getAllOrders().first()
            for (order in orders)
            {
                if (order.isPendingSync)
                {
                    val orderMap = mapOrderToFirebase(order)
                    firebaseDatabase.getReference("orders")
                        .child(order.orderId)
                        .setValue(orderMap)
                        .await()

                    orderDao.updateOrder(order.copy(isPendingSync = false))
                    Log.d(tag, "Successfully synchronized order: ${order.orderId}")
                }
            }
        }
        catch (e: DatabaseException)
        {
            handleDatabaseError(e)
        }
        catch (e: Exception)
        {
            Log.w(tag, "Failed during order synchronization: ${e.message}")
        }
    }

    /**
     * Synchronizes user feedback entries to the 'feedback' node in Realtime Database.
     * Enforces rule constraints: userId matches auth.uid, lowercase type and status.
     */
    private suspend fun syncFeedbackNode()
    {
        val currentUser = firebaseAuth.currentUser ?: return
        try
        {
            val feedbackList = feedbackDao.getAllFeedback().first()
            for (feedback in feedbackList)
            {
                if (feedback.userId == currentUser.uid)
                {
                    val feedbackMap = mapFeedbackToFirebase(feedback)
                    firebaseDatabase.getReference("feedback")
                        .child(feedback.feedbackId.toString())
                        .setValue(feedbackMap)
                        .await()
                }
            }
        }
        catch (e: DatabaseException)
        {
            handleDatabaseError(e)
        }
        catch (e: Exception)
        {
            Log.w(tag, "Failed during feedback synchronization: ${e.message}")
        }
    }

    /**
     * Synchronizes promotional coupons to the 'coupons' node.
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
        catch (e: DatabaseException)
        {
            handleDatabaseError(e)
        }
        catch (e: Exception)
        {
            Log.w(tag, "Failed during coupon synchronization: ${e.message}")
        }
    }

    /**
     * Handles PERMISSION_DENIED and other DatabaseExceptions gracefully without throwing.
     */
    private fun handleDatabaseError(exception: DatabaseException)
    {
        val message = exception.message ?: ""
        if (message.contains("Permission denied", ignoreCase = true))
        {
            Log.e(tag, "PERMISSION_DENIED: Current authenticated credentials lack write access for this node.")
        }
        else
        {
            Log.w(tag, "Realtime Database Exception: ${exception.localizedMessage}")
        }
    }

    /**
     * Maps UserEntity to Map matching Firebase validation rules.
     * Sets 'role' to 'ADMIN' for ADMINISTRATOR, passwordHash to '[FIREBASE_SSO]'.
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
     * Maps OrderEntity to Map for Realtime Database persistence.
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
     * Maps FeedbackEntity to Map matching 'feedback' node validation rules.
     * Enforces lowercase 'type' ("complaint" or "compliment") and 'status' ("pending").
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
     * Maps CouponEntity to Map for Realtime Database persistence.
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
