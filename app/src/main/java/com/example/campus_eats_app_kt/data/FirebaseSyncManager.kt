package com.example.campus_eats_app_kt.data

import android.content.Context
import android.util.Log
import com.example.campus_eats_app_kt.data.dao.CouponDao
import com.example.campus_eats_app_kt.data.dao.FeedbackDao
import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.CouponEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackStatus
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.PaymentMethod
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.example.campus_eats_app_kt.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
 * 3. Applies error isolation per record so a single failure does not block the entire loop.
 * 4. Transmits unsynced user/order records identified by local pending flags.
 * 5. Pulls remote cloud orders, user profiles, feedback, and menu items into Room.
 * 6. Triggers local status notifications on customer devices when cloud order status changes.
 */
class FirebaseSyncManager(
    private val userDao: UserDao,
    private val orderDao: OrderDao,
    private val feedbackDao: FeedbackDao,
    private val couponDao: CouponDao,
    private val menuItemDao: MenuItemDao? = null,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabaseProvider.instance,
    private val context: Context? = null,
)
{
    private val tag = "FirebaseSyncManager"

    private val baseIntervalSeconds = 10L
    private val maxBackoffSeconds = 60L
    private var syncJob: Job? = null

    /**
     * Starts the continuous background synchronization loop in the provided CoroutineScope.
     */
    fun startContinuousSync(scope: CoroutineScope)
    {
        if (syncJob?.isActive == true)
        {
            Log.d(tag, "Synchronization loop already active. Skipping duplicate start request.")
            return
        }

        syncJob = scope.launch {
            Log.i(tag, "Starting continuous Realtime Database synchronization loop.")

            var currentBackoffSeconds = 1L

            // Attach inbound realtime cloud listeners
            setupInboundCloudListeners(scope)

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
                    Log.w(tag, "Synchronization cycle encountered error: $parsedMessage", e)
                }

                val delayTimeSeconds = if (cycleHasErrors)
                {
                    val activeDelay = currentBackoffSeconds
                    currentBackoffSeconds = (currentBackoffSeconds * 2L).coerceAtMost(maxBackoffSeconds)
                    Log.i(tag, "Exponential backoff engaged. Waiting $activeDelay seconds before retry.")
                    activeDelay
                }
                else
                {
                    currentBackoffSeconds = 1L
                    baseIntervalSeconds
                }

                delay(delayTimeSeconds.seconds)
            }
        }
    }

    /**
     * Attaches Realtime Database event listeners to pull cloud updates into Room.
     */
    private fun setupInboundCloudListeners(scope: CoroutineScope)
    {
        val currentUser = firebaseAuth.currentUser ?: return
        val uid = currentUser.uid
        val email = currentUser.email ?: ""

        val ordersRef = firebaseDatabase.getReference("orders")
        ordersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                scope.launch {
                    for (child in snapshot.children)
                    {
                        try
                        {
                            val customerId = child.child("customerId").getValue(String::class.java) ?: ""
                            val vendorId = child.child("vendorId").getValue(String::class.java) ?: ""

                            if (customerId == uid || customerId == email || vendorId == uid || vendorId == email)
                            {
                                val orderId = child.child("orderId").getValue(String::class.java) ?: child.key ?: continue
                                val statusStr = child.child("status").getValue(String::class.java) ?: "PENDING"
                                val totalAmount = child.child("totalAmount").getValue(Double::class.java) ?: 0.0
                                val itemsJson = child.child("itemsJson").getValue(String::class.java) ?: "[]"
                                val paymentMethodStr = child.child("paymentMethod").getValue(String::class.java) ?: "CAMPUS_WALLET"
                                val pickupTime = child.child("pickupTime").getValue(String::class.java) ?: ""

                                val status = try { OrderStatus.valueOf(statusStr) } catch (_: Exception) { OrderStatus.PENDING }
                                val paymentMethod = try { PaymentMethod.valueOf(paymentMethodStr) } catch (_: Exception) { PaymentMethod.CAMPUS_WALLET }

                                val existing = orderDao.getOrderById(orderId)
                                if (existing == null)
                                {
                                    val newOrder = OrderEntity(
                                        orderId = orderId,
                                        customerId = customerId,
                                        vendorId = vendorId,
                                        itemsJson = itemsJson,
                                        totalAmount = totalAmount,
                                        status = status,
                                        paymentMethod = paymentMethod,
                                        pickupTime = pickupTime,
                                        isPendingSync = false,
                                    )
                                    orderDao.insertOrder(newOrder)
                                }
                                else if (existing.status != status)
                                {
                                    orderDao.updateOrder(existing.copy(status = status, isPendingSync = false))

                                    // Trigger status notification on customer device (Finding 11)
                                    if (context != null && customerId == uid)
                                    {
                                        val message = when (status)
                                        {
                                            OrderStatus.ACCEPTED -> "Your order has been accepted by the vendor."
                                            OrderStatus.PREPARING -> "Your meal is currently being prepared!"
                                            OrderStatus.READY -> "Your order is ready for pickup!"
                                            OrderStatus.COMPLETED -> "Order completed. Thank you!"
                                            else -> "Order status updated to $status."
                                        }
                                        NotificationHelper.showNotification(
                                            context = context,
                                            notificationId = orderId.hashCode(),
                                            title = "Order #$orderId Status Update",
                                            message = message,
                                        )
                                    }
                                }
                            }
                        }
                        catch (_: Exception) {}
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Listen for menu updates
        if (menuItemDao != null)
        {
            val menusRef = firebaseDatabase.getReference("menus")
            menusRef.addValueEventListener(object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    scope.launch {
                        for (child in snapshot.children)
                        {
                            try
                            {
                                val itemId = child.child("itemId").getValue(Long::class.java) ?: continue
                                val name = child.child("name").getValue(String::class.java) ?: ""
                                val description = child.child("description").getValue(String::class.java) ?: ""
                                val price = child.child("price").getValue(Double::class.java) ?: 0.0
                                val vendorId = child.child("vendorId").getValue(String::class.java) ?: ""

                                val item = MenuItemEntity(
                                    itemId = itemId,
                                    vendorId = vendorId,
                                    name = name,
                                    description = description,
                                    price = price,
                                    stock = 99,
                                    category = "General",
                                )
                                menuItemDao.insertMenuItem(item)
                            }
                            catch (_: Exception) {}
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    /**
     * Synchronizes local unsynced user records to the 'users/$userId' node.
     * Applies per-record error isolation (Finding 4).
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
                Log.d(tag, "Successfully synchronized user record for ID: ${user.userId}")
            }
            catch (e: Exception)
            {
                val parsedMessage = FirebaseExceptionHandler.parse(e)
                Log.w(tag, "Failed to sync user ID ${user.userId}: $parsedMessage")
            }
        }
    }

    /**
     * Synchronizes orders that are pending sync to the 'orders/$orderId' node.
     * Applies per-record error isolation (Finding 4).
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
            }
        }
    }

    /**
     * Synchronizes feedback records to the 'feedback/$feedbackId' node.
     * Fixes admin feedback lookup by email (Finding 5).
     */
    private suspend fun syncFeedbackNode()
    {
        val currentUser = firebaseAuth.currentUser ?: return

        try
        {
            val email = currentUser.email ?: ""
            val localUser = userDao.getUserByEmail(email)

            if (localUser?.role == UserRole.ADMINISTRATOR)
            {
                try
                {
                    val snapshot = firebaseDatabase.getReference("feedback").get().await()
                    for (child in snapshot.children)
                    {
                        val subject = child.child("subject").getValue(String::class.java) ?: continue
                        val message = child.child("message").getValue(String::class.java) ?: continue
                        val userId = child.child("userId").getValue(String::class.java) ?: ""
                        val userName = child.child("userName").getValue(String::class.java) ?: "User"
                        val userEmail = child.child("userEmail").getValue(String::class.java) ?: ""
                        val typeStr = child.child("type").getValue(String::class.java) ?: "compliment"
                        val statusStr = child.child("status").getValue(String::class.java) ?: "pending"
                        val createdAt = child.child("createdAt").getValue(String::class.java) ?: ""
                        val updatedAt = child.child("updatedAt").getValue(String::class.java) ?: createdAt

                        val type = if (typeStr.equals("complaint", ignoreCase = true)) FeedbackType.COMPLAINT else FeedbackType.COMPLIMENT
                        val status = if (statusStr.equals("resolved", ignoreCase = true)) FeedbackStatus.RESOLVED else FeedbackStatus.PENDING

                        val feedbackEntity = FeedbackEntity(
                            userId = userId,
                            type = type,
                            subject = subject,
                            message = message,
                            userName = userName,
                            userEmail = userEmail,
                            status = status,
                            createdAt = createdAt,
                            updatedAt = updatedAt,
                        )
                        feedbackDao.insertFeedback(feedbackEntity)
                    }
                }
                catch (e: Exception)
                {
                    Log.d(tag, "Remote feedback pull skipped: ${e.message}")
                }
            }

            val feedbackList = feedbackDao.getAllFeedback().first()
            val userFeedback = if (localUser?.role == UserRole.ADMINISTRATOR) feedbackList else feedbackList.filter { it.userId == currentUser.uid || it.userEmail == email }

            for (feedback in userFeedback)
            {
                try
                {
                    val stableKey = FeedbackKeys.keyFor(feedback)
                    val feedbackMap = mapFeedbackToFirebase(feedback)
                    firebaseDatabase.getReference("feedback")
                        .child(stableKey)
                        .setValue(feedbackMap)
                        .await()
                }
                catch (_: Exception) {}
            }
        }
        catch (e: Exception)
        {
            val parsedMessage = FirebaseExceptionHandler.parse(e)
            Log.w(tag, "Failed feedback sync cycle: $parsedMessage")
        }
    }

    /**
     * Synchronizes promotional coupons to the 'coupons/$code' node.
     * Only admins execute coupon writes to prevent rule denial loops (Finding 4).
     */
    private suspend fun syncCouponsNode()
    {
        try
        {
            val currentUser = firebaseAuth.currentUser ?: return
            val email = currentUser.email ?: ""
            val localUser = userDao.getUserByEmail(email)

            if (localUser?.role != UserRole.ADMINISTRATOR)
            {
                return
            }

            val coupons = couponDao.getAllCoupons().first()
            if (coupons.isEmpty())
            {
                return
            }

            for (coupon in coupons)
            {
                try
                {
                    val couponMap = mapCouponToFirebase(coupon)
                    firebaseDatabase.getReference("coupons")
                        .child(coupon.code)
                        .setValue(couponMap)
                        .await()
                }
                catch (_: Exception) {}
            }
        }
        catch (e: Exception)
        {
            val parsedMessage = FirebaseExceptionHandler.parse(e)
            Log.w(tag, "Failed coupon sync cycle: $parsedMessage")
        }
    }

    private fun handleDatabaseError(exception: DatabaseException)
    {
        val parsedMessage = FirebaseExceptionHandler.parse(exception)
        Log.e(tag, "Firebase Realtime Database error encountered: $parsedMessage", exception)
    }

    @Suppress("unused")
    fun observeConnection(): Flow<Boolean> = callbackFlow {
        val ref = firebaseDatabase.getReference(".info/connected")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) == true)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

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

    private fun mapFeedbackToFirebase(feedback: FeedbackEntity): Map<String, Any?>
    {
        return FeedbackRepository.mapFeedbackToMap(feedback)
    }

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
