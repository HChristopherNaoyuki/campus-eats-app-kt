package com.example.campus_eats_app_kt.data

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.campus_eats_app_kt.data.dao.CartDao
import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.PaymentMethod
import com.example.campus_eats_app_kt.data.network.FakeRestaurantApiService
import com.example.campus_eats_app_kt.data.network.OrderItemRequest
import com.example.campus_eats_app_kt.data.network.OrderRequest
import com.example.campus_eats_app_kt.util.IdGenerator
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.example.campus_eats_app_kt.util.NotificationHelper
import com.example.campus_eats_app_kt.util.OrderStatusEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

/**
 * OrderRepository manages order persistence, state progression, and synchronization.
 * Follows an online-first architecture with offline fallback capabilities.
 */
class OrderRepository(
    private val database: CampusEatsDatabase,
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
    private val userDao: UserDao,
    private val menuItemDao: MenuItemDao,
    private val apiService: FakeRestaurantApiService,
    private val connectivityManager: NetworkConnectivityManager,
)
{
    private val tag = "OrderRepository"

    /**
     * Places a new order into the database.
     * Verifies balance and stock locally, attempts remote API synchronization when online,
     * and flags the entity for cloud sync if remote API execution fails or is offline.
     */
    suspend fun placeOrder(
        userId: String,
        vendorId: String,
        cartItems: List<CartItemEntity>,
        totalAmount: Double,
        paymentMethod: PaymentMethod,
        pickupTime: String,
        specialRequests: String? = null,
    ): String = database.withTransaction()
    {
        Log.d(tag, "Placing order for user ID: $userId. Online Status: ${connectivityManager.hasInternetConnection()}")

        // 1. Local Balance and Stock Check
        if (paymentMethod == PaymentMethod.CAMPUS_WALLET)
        {
            val affected = userDao.debitWallet(userId, totalAmount)
            if (affected == 0)
            {
                throw Exception("Insufficient Campus Wallet balance.")
            }
        }

        for (item in cartItems)
        {
            val menuItem = menuItemDao.getMenuItemById(item.itemId)
            if (menuItem?.isInventoryTracked == true)
            {
                val affected = menuItemDao.decrementStock(item.itemId, item.quantity)
                if (affected == 0)
                {
                    throw Exception("Item '${item.name}' is out of stock.")
                }
            }
        }

        // 2. Immediate Remote Sync Attempt (Online-First Mandate)
        val numericVendorId = vendorId.toIntOrNull()
        val apikey = userDao.getUserById(userId)?.usercode

        val remoteSyncSuccess = if ((numericVendorId != null) && (apikey != null) && connectivityManager.hasInternetConnection())
        {
            try
            {
                val networkItems = cartItems.map { OrderItemRequest(it.name, it.quantity) }
                val response = apiService.createOrder(numericVendorId, apikey, OrderRequest(networkItems))
                response.isSuccessful
            }
            catch (_: Exception)
            {
                false
            }
        }
        else
        {
            false
        }

        // 3. Persist Order Record with Sync Status
        val order = OrderEntity(
            orderId = IdGenerator.generateOrderId(),
            customerId = userId,
            vendorId = vendorId,
            itemsJson = Json.encodeToString(cartItems),
            totalAmount = totalAmount,
            status = OrderStatus.PENDING,
            paymentMethod = paymentMethod,
            pickupTime = pickupTime,
            specialRequests = specialRequests,
            isPendingSync = !remoteSyncSuccess,
        )
        orderDao.insertOrder(order)
        cartDao.clearCart(userId)

        order.orderId
    }

    /**
     * Flushes any pending local orders to the remote server.
     */
    @Suppress("unused")
    suspend fun syncPendingOrders(userId: String)
    {
        if (!connectivityManager.hasInternetConnection())
        {
            return
        }

        val pending = orderDao.getOrdersByCustomer(userId).first().filter { it.isPendingSync }
        val apikey = userDao.getUserById(userId)?.usercode ?: return

        for (order in pending)
        {
            val numericVendorId = order.vendorId.toIntOrNull() ?: continue
            try
            {
                val items = Json.decodeFromString<List<CartItemEntity>>(order.itemsJson)
                val networkItems = items.map { OrderItemRequest(it.name, it.quantity) }
                val response = apiService.createOrder(numericVendorId, apikey, OrderRequest(networkItems))
                
                if (response.isSuccessful)
                {
                    orderDao.updateOrder(order.copy(isPendingSync = false))
                }
            }
            catch (_: Exception)
            {
                Log.w(tag, "Pending order sync skipped for order ID: ${order.orderId}")
            }
        }
    }

    /**
     * Retrieves a reactive stream of orders for a specific customer.
     */
    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>> = orderDao.getOrdersByCustomer(userId)

    /**
     * Retrieves a reactive stream of orders assigned to a specific vendor.
     */
    fun getOrdersForVendor(vendorId: String): Flow<List<OrderEntity>> = orderDao.getOrdersByVendor(vendorId)

    /**
     * Updates order status after validating state transitions.
     * Flags the order entity for cloud synchronization and triggers system notifications.
     */
    suspend fun updateOrderStatus(order: OrderEntity, status: OrderStatus, context: Context? = null)
    {
        if (OrderStatusEngine.isValidTransition(order.status, status))
        {
            orderDao.updateOrder(order.copy(status = status, isPendingSync = true))

            if (context != null)
            {
                val title = "Order #${order.orderId} Update"
                val message = when (status)
                {
                    OrderStatus.ACCEPTED -> "Your order has been accepted by the vendor."
                    OrderStatus.PREPARING -> "Your meal is currently being prepared!"
                    OrderStatus.READY -> "Your order is ready for pickup!"
                    OrderStatus.COMPLETED -> "Order completed. Thank you for dining with Campus Eats!"
                    OrderStatus.CANCELLED -> "Order #${order.orderId} was cancelled."
                    else -> "Order status updated to $status."
                }
                NotificationHelper.showNotification(
                    context = context,
                    notificationId = order.orderId.hashCode(),
                    title = title,
                    message = message,
                )
            }
        }
        else
        {
            throw Exception("Invalid status transition from ${order.status} to $status")
        }
    }

    /**
     * Returns a reactive stream of all orders in the database.
     */
    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()
}
