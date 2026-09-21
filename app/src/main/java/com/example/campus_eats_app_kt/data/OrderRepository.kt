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
import com.example.campus_eats_app_kt.data.network.MasterOrder
import com.example.campus_eats_app_kt.data.network.OrderItemRequest
import com.example.campus_eats_app_kt.data.network.OrderRequest
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.example.campus_eats_app_kt.util.OrderStatusEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * OrderRepository manages the order lifecycle.
 * Hardened in Batch 2 with atomic transactions, wallet checks, and stock management.
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
     * Finding 12 & 13: Atomic order placement with financial and inventory checks.
     */
    suspend fun placeOrder(
        userId: String,
        vendorId: String,
        cartItems: List<CartItemEntity>,
        totalAmount: Double,
        paymentMethod: PaymentMethod,
        pickupTime: String,
        specialRequests: String? = null,
    ): Long = database.withTransaction()
    {
        Log.d(tag, "Placing order for $userId at $vendorId. Total: $totalAmount")

        // 1. Enforce Wallet Balance if using Campus Wallet
        if (paymentMethod == PaymentMethod.CAMPUS_WALLET)
        {
            val affected = userDao.debitWallet(userId, totalAmount)
            if (affected == 0) throw Exception("Insufficient Campus Wallet balance.")
        }

        // 2. Enforce Inventory Availability (Finding 12)
        for (item in cartItems)
        {
            val affected = menuItemDao.decrementStock(item.itemId, item.quantity)
            if (affected == 0) throw Exception("Item '${item.name}' is out of stock.")
        }

        // 3. Persist Order Record
        val order = OrderEntity(
            customerId = userId,
            vendorId = vendorId,
            itemsJson = Json.encodeToString(cartItems),
            totalAmount = totalAmount,
            status = OrderStatus.PENDING,
            paymentMethod = paymentMethod,
            pickupTime = pickupTime,
            specialRequests = specialRequests,
        )
        val orderId = orderDao.insertOrder(order)

        // 4. Remote API Synchronization (Best-effort prototyping)
        val numericVendorId = vendorId.toIntOrNull()
        val user = userDao.getUserById(userId)
        val apikey = user?.usercode

        if ((numericVendorId != null) && (apikey != null) && connectivityManager.hasInternetConnection())
        {
            try
            {
                val networkItems = cartItems.map { OrderItemRequest(it.name, it.quantity) }
                val response = apiService.createOrder(numericVendorId, apikey, OrderRequest(networkItems))
                if (!response.isSuccessful) Log.w(tag, "Remote sync warning: ${response.code()}")
            }
            catch (_: Exception) {}
        }

        // 5. Atomic Cart Clear (Finding 13)
        cartDao.clearCart(userId)

        orderId
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
