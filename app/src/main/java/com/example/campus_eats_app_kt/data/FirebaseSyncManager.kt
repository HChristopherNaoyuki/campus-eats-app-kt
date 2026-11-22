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
 * FirebaseSyncManager synchronizes local Room storage and Firebase
 * Realtime Database.
 */
class FirebaseSyncManager(
    private val userDao: UserDao,
    private val orderDao: OrderDao,
    private val feedbackDao: FeedbackDao,
    private val couponDao: CouponDao,
    private val menuItemDao: MenuItemDao? = null,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase =
        FirebaseDatabaseProvider.instance,
    private val context: Context? = null,
)
{
    private val tag = "FirebaseSyncManager"

    private val baseIntervalSeconds = 10L
    private val maxBackoffSeconds = 60L
    private var syncJob: Job? = null

    /**
     * Starts continuous background synchronization loop.
     */
    fun startContinuousSync(scope: CoroutineScope)
    {
        if (syncJob?.isActive == true)
        {
            Log.d(
                tag,
                "Sync loop already active. Skipping duplicate start.",
            )
            return
        }

        syncJob = scope.launch {
            Log.i(
                tag,
                "Starting continuous Realtime Database sync loop.",
            )

            var currentBackoffSeconds = 1L

            setupInboundCloudListeners(scope)

            while (isActive)
            {
                var cycleHasErrors = false

                try
                {
                    if (firebaseAuth.currentUser == null)
                    {
                        Log.d(
                            tag,
                            "No authenticated user. Skipping cycle.",
                        )
                        delay(baseIntervalSeconds.seconds)
                        continue
                    }

                    if (connectivityManager.hasInternetConnection())
                    {
                        val uErr = syncUsersNode()
                        val oErr = syncOrdersNode()
                        val fErr = syncFeedbackNode()
                        val cErr = syncCouponsNode()
                        cycleHasErrors = uErr || oErr || fErr || cErr
                    }
                    else
                    {
                        Log.d(
                            tag,
                            "Device offline. Skipping sync cycle.",
                        )
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
                    Log.w(
                        tag,
                        "Sync cycle error: $parsedMessage",
                        e,
                    )
                }

                val delayTimeSeconds = if (cycleHasErrors)
                {
                    val activeDelay = currentBackoffSeconds
                    currentBackoffSeconds = (currentBackoffSeconds * 2L)
                        .coerceAtMost(maxBackoffSeconds)
                    Log.i(
                        tag,
                        "Backoff engaged. Waiting $activeDelay s.",
                    )
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
     * Attaches Realtime Database listeners to pull cloud updates.
     */
    private fun setupInboundCloudListeners(scope: CoroutineScope)
    {
        val currentUser = firebaseAuth.currentUser ?: return
        val uid = currentUser.uid

        val orderListener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                scope.launch {
                    for (child in snapshot.children)
                    {
                        try
                        {
                            val customerId = child.child("customerId")
                                .getValue(String::class.java) ?: ""
                            val vendorId = child.child("vendorId")
                                .getValue(String::class.java) ?: ""
                            val orderId = child.child("orderId")
                                .getValue(String::class.java)
                                ?: child.key ?: continue
                            val statusStr = child.child("status")
                                .getValue(String::class.java) ?: "PENDING"
                            val totalAmount = child.child("totalAmount")
                                .getValue(Double::class.java) ?: 0.0
                            val itemsJson = child.child("itemsJson")
                                .getValue(String::class.java) ?: "[]"
                            val paymentMethodStr = child
                                .child("paymentMethod")
                                .getValue(String::class.java)
                                ?: "CAMPUS_WALLET"
                            val pickupTime = child.child("pickupTime")
                                .getValue(String::class.java) ?: ""
                            val specialRequests = child
                                .child("specialRequests")
                                .getValue(String::class.java)
                            val timestamp = child.child("timestamp")
                                .getValue(Long::class.java)
                                ?: System.currentTimeMillis()

                            val status = try
                            {
                                OrderStatus.valueOf(statusStr)
                            }
                            catch (_: Exception)
                            {
                                OrderStatus.PENDING
                            }
                            val paymentMethod = try
                            {
                                PaymentMethod.valueOf(paymentMethodStr)
                            }
                            catch (_: Exception)
                            {
                                PaymentMethod.CAMPUS_WALLET
                            }

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
                                    specialRequests = specialRequests,
                                    timestamp = timestamp,
                                    isPendingSync = false,
                                )
                                orderDao.insertOrder(newOrder)
                            }
                            else if (shouldUpdateLocalOrder(
                                existing,
                                status,
                            ))
                            {
                                orderDao.updateOrder(
                                    existing.copy(
                                        status = status,
                                        specialRequests = specialRequests
                                            ?: existing.specialRequests,
                                        timestamp = timestamp,
                                        isPendingSync = false,
                                    ),
                                )

                                if (context != null && customerId == uid)
                                {
                                    val message = when (status)
                                    {
                                        OrderStatus.ACCEPTED ->
                                            "Your order is accepted."
                                        OrderStatus.PREPARING ->
                                            "Your meal is preparing!"
                                        OrderStatus.READY ->
                                            "Your order is ready!"
                                        OrderStatus.COMPLETED ->
                                            "Order completed. Thanks!"
                                        else ->
                                            "Order updated to $status."
                                    }
                                    NotificationHelper.showNotification(
                                        context = context,
                                        notificationId = orderId.hashCode(),
                                        title = "Order #$orderId Status",
                                        message = message,
                                    )
                                }
                            }
                        }
                        catch (e: Exception)
                        {
                            Log.w(
                                tag,
                                "Order processing error: ${e.message}",
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                Log.w(
                    tag,
                    "Orders listener cancelled: ${error.message}",
                )
            }
        }

        val ordersRef = firebaseDatabase.getReference("orders")
        ordersRef.orderByChild("customerId").equalTo(uid)
            .addValueEventListener(orderListener)
        ordersRef.orderByChild("vendorId").equalTo(uid)
            .addValueEventListener(orderListener)

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
                                val itemId = child.child("itemId")
                                    .getValue(Long::class.java) ?: continue
                                val name = child.child("name")
                                    .getValue(String::class.java) ?: ""
                                val description = child
                                    .child("description")
                                    .getValue(String::class.java) ?: ""
                                val price = child.child("price")
                                    .getValue(Double::class.java) ?: 0.0
                                val vendorId = child.child("vendorId")
                                    .getValue(String::class.java) ?: ""

                                val existingItem = menuItemDao
                                    .getMenuItemById(itemId)
                                val item = MenuItemEntity(
                                    itemId = itemId,
                                    vendorId = vendorId,
                                    name = name,
                                    description = description,
                                    price = price,
                                    stock = existingItem?.stock ?: 99,
                                    category = existingItem?.category
                                        ?: "General",
                                )
                                menuItemDao.insertMenuItem(item)
                            }
                            catch (_: Exception) {}
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Log.w(
                        tag,
                        "Menus listener cancelled: ${error.message}",
                    )
                }
            })
        }
    }

    /**
     * Synchronizes local unsynced user records to 'users/$userId' node.
     * Uses updateChildren to send only user-owned fields.
     */
    private suspend fun syncUsersNode(): Boolean
    {
        var hasError = false
        val unsyncedUsers = userDao.getUnsyncedUsers()
        if (unsyncedUsers.isEmpty())
        {
            return false
        }

        Log.d(
            tag,
            "Found ${unsyncedUsers.size} unsynced users. Syncing.",
        )

        for (user in unsyncedUsers)
        {
            try
            {
                val userUpdates = mapOf<String, Any?>(
                    "fullName" to user.fullName,
                    "username" to user.username,
                    "walletBalance" to user.walletBalance,
                    "shopName" to user.shopName,
                    "shopStatus" to user.shopStatus?.name,
                    "bankAccountInfo" to user.bankAccountInfo,
                )
                firebaseDatabase.getReference("users")
                    .child(user.userId)
                    .updateChildren(userUpdates)
                    .await()

                userDao.markAsSyncedIfWalletMatches(
                    user.userId,
                    user.walletBalance,
                )
                Log.d(
                    tag,
                    "Synced user record for ID: ${user.userId}",
                )
            }
            catch (e: Exception)
            {
                hasError = true
                val parsedMessage = FirebaseExceptionHandler.parse(e)
                Log.w(
                    tag,
                    "Failed sync user ID ${user.userId}: $parsedMessage",
                )
            }
        }
        return hasError
    }

    /**
     * Synchronizes orders that are pending sync to 'orders/$orderId'.
     */
    private suspend fun syncOrdersNode(): Boolean
    {
        var hasError = false
        val orders = orderDao.getAllOrders().first()
        val pendingOrders = orders.filter { it.isPendingSync }

        if (pendingOrders.isEmpty())
        {
            return false
        }

        Log.d(
            tag,
            "Found ${pendingOrders.size} pending orders. Syncing.",
        )

        for (order in pendingOrders)
        {
            try
            {
                val orderMap = mapOrderToFirebase(order)
                firebaseDatabase.getReference("orders")
                    .child(order.orderId)
                    .setValue(orderMap)
                    .await()

                orderDao.clearPendingSyncIfStatusMatches(
                    order.orderId,
                    order.status,
                )
                Log.d(
                    tag,
                    "Synced order record: ${order.orderId}",
                )
            }
            catch (e: Exception)
            {
                hasError = true
                val parsedMessage = FirebaseExceptionHandler.parse(e)
                Log.w(
                    tag,
                    "Failed sync order ${order.orderId}: $parsedMessage",
                )
            }
        }
        return hasError
    }

    /**
     * Synchronizes feedback records. Pushes local changes first, then
     * pulls remote entries using timestamp-based conflict resolution.
     */
    private suspend fun syncFeedbackNode(): Boolean
    {
        var hasError = false
        val currentUser = firebaseAuth.currentUser ?: return false

        try
        {
            val email = currentUser.email ?: ""
            val localUser = userDao.getUserByEmail(email)

            val feedbackList = feedbackDao.getAllFeedback().first()
            val userFeedback = if (localUser?.role ==
                UserRole.ADMINISTRATOR)
            {
                feedbackList
            }
            else
            {
                feedbackList.filter {
                    it.userId == currentUser.uid || it.userEmail == email
                }
            }

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
                catch (_: Exception)
                {
                    hasError = true
                }
            }

            if (localUser?.role == UserRole.ADMINISTRATOR)
            {
                try
                {
                    val snapshot = firebaseDatabase
                        .getReference("feedback")
                        .get()
                        .await()
                    for (child in snapshot.children)
                    {
                        val subject = child.child("subject")
                            .getValue(String::class.java) ?: continue
                        val message = child.child("message")
                            .getValue(String::class.java) ?: continue
                        val userId = child.child("userId")
                            .getValue(String::class.java) ?: ""
                        val userName = child.child("userName")
                            .getValue(String::class.java) ?: "User"
                        val userEmail = child.child("userEmail")
                            .getValue(String::class.java) ?: ""
                        val typeStr = child.child("type")
                            .getValue(String::class.java) ?: "compliment"
                        val statusStr = child.child("status")
                            .getValue(String::class.java) ?: "pending"
                        val createdAt = child.child("createdAt")
                            .getValue(String::class.java) ?: ""
                        val updatedAt = child.child("updatedAt")
                            .getValue(String::class.java) ?: createdAt

                        val type = if (typeStr.equals(
                                "complaint",
                                ignoreCase = true,
                            ))
                        {
                            FeedbackType.COMPLAINT
                        }
                        else
                        {
                            FeedbackType.COMPLIMENT
                        }
                        val status = if (statusStr.equals(
                                "resolved",
                                ignoreCase = true,
                            ))
                        {
                            FeedbackStatus.RESOLVED
                        }
                        else
                        {
                            FeedbackStatus.PENDING
                        }

                        val existing = feedbackDao.findByUserAndCreatedAt(
                            userId,
                            createdAt,
                        )
                        if (existing != null)
                        {
                            if (shouldUpdateLocalFeedback(
                                existing,
                                status,
                                updatedAt,
                            ))
                            {
                                feedbackDao.updateFeedbackStatus(
                                    existing.feedbackId,
                                    status,
                                    updatedAt,
                                )
                            }
                        }
                        else
                        {
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
                }
                catch (e: Exception)
                {
                    Log.d(
                        tag,
                        "Remote feedback pull skipped: ${e.message}",
                    )
                }
            }
        }
        catch (e: Exception)
        {
            hasError = true
            val parsedMessage = FirebaseExceptionHandler.parse(e)
            Log.w(tag, "Failed feedback sync cycle: $parsedMessage")
        }
        return hasError
    }

    /**
     * Synchronizes promotional coupons to 'coupons/$code' node.
     */
    private suspend fun syncCouponsNode(): Boolean
    {
        var hasError = false
        try
        {
            val currentUser = firebaseAuth.currentUser ?: return false
            val email = currentUser.email ?: ""
            val localUser = userDao.getUserByEmail(email)

            if (localUser?.role != UserRole.ADMINISTRATOR)
            {
                return false
            }

            val coupons = couponDao.getAllCoupons().first()
            if (coupons.isEmpty())
            {
                return false
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
                catch (_: Exception)
                {
                    hasError = true
                }
            }
        }
        catch (_: Exception)
        {
            hasError = true
        }
        return hasError
    }

    private fun handleDatabaseError(exception: DatabaseException)
    {
        val parsedMessage = FirebaseExceptionHandler.parse(exception)
        Log.e(
            tag,
            "Database error encountered: $parsedMessage",
            exception,
        )
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

    private fun mapFeedbackToFirebase(
        feedback: FeedbackEntity,
    ): Map<String, Any?>
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

    companion object
    {
        /**
         * Determines if inbound cloud order status should overwrite local order.
         */
        fun shouldUpdateLocalOrder(
            existing: OrderEntity?,
            remoteStatus: OrderStatus,
        ): Boolean
        {
            if (existing == null)
            {
                return false
            }
            return existing.status != remoteStatus && !existing.isPendingSync
        }

        /**
         * Determines if inbound cloud feedback should overwrite local feedback.
         */
        fun shouldUpdateLocalFeedback(
            existing: FeedbackEntity,
            remoteStatus: FeedbackStatus,
            remoteUpdatedAt: String,
        ): Boolean
        {
            if (existing.status == remoteStatus)
            {
                return false
            }
            return remoteUpdatedAt >= existing.updatedAt
        }
    }
}