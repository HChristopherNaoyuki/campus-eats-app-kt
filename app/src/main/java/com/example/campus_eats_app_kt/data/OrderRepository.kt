package com.example.campus_eats_app_kt.data

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
import com.example.campus_eats_app_kt.util.OrderStatusEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * OrderRepository manages the order lifecycle.
 * Hardened in Batch 3 for architectural alignment with Online-First mandate.
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
     * Places a new order. 
     * Finding 1: Enforces Online-First single source of truth (Firebase/REST).
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
        Log.d(tag, "Placing order for $userId. Online Status: ${connectivityManager.hasInternetConnection()}")

        // 1. Local Balance & Stock Check
        if (paymentMethod == PaymentMethod.CAMPUS_WALLET)
        {
            val affected = userDao.debitWallet(userId, totalAmount)
            if (affected == 0) throw Exception("Insufficient Campus Wallet balance.")
        }

        for (item in cartItems)
        {
            val menuItem = menuItemDao.getMenuItemById(item.itemId)
            if (menuItem?.isInventoryTracked == true)
            {
                val affected = menuItemDao.decrementStock(item.itemId, item.quantity)
                if (affected == 0) throw Exception("Item '${item.name}' is out of stock.")
            }
        }

        // 2. Immediate Remote Sync (Online-First mandate)
        var remoteSyncSuccess = false
        val numericVendorId = vendorId.toIntOrNull()
        val apikey = userDao.getUserById(userId)?.usercode

        if ((numericVendorId != null) && (apikey != null) && connectivityManager.hasInternetConnection())
        {
            try
            {
                val networkItems = cartItems.map { OrderItemRequest(it.name, it.quantity) }
                val response = apiService.createOrder(numericVendorId, apikey, OrderRequest(networkItems))
                remoteSyncSuccess = response.isSuccessful
            }
            catch (_: Exception) { remoteSyncSuccess = false }
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
            isPendingSync = !remoteSyncSuccess
        )
        orderDao.insertOrder(order)
        cartDao.clearCart(userId)

        order.orderId
    }

    /**
     * Flushes any pending local orders to the remote server.
     * Finding 1: Synchronization logic for offline-to-online transitions.
     */
    suspend fun syncPendingOrders(userId: String)
    {
        if (!connectivityManager.hasInternetConnection()) return

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
            catch (_: Exception) {}
        }
    }

    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>> = orderDao.getOrdersByCustomer(userId)

    fun getOrdersForVendor(vendorId: String): Flow<List<OrderEntity>> = orderDao.getOrdersByVendor(vendorId)

    suspend fun updateOrderStatus(order: OrderEntity, status: OrderStatus)
    {
        if (OrderStatusEngine.isValidTransition(order.status, status))
        {
            orderDao.updateOrder(order.copy(status = status))
        }
        else
        {
            throw Exception("Invalid status transition from ${order.status} to $status")
        }
    }

    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()
}
