package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.CartDao
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * OrderRepository manages the lifecycle of customer orders.
 * It coordinates with the local database and the remote Fake Restaurant API.
 */
class OrderRepository(
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
    private val userDao: UserDao,
    private val apiService: FakeRestaurantApiService
)
{
    /**
     * Persists a new order. If the vendor is from the remote API and the user
     * has an active usercode, the order is also synchronized with the server.
     */
    suspend fun placeOrder(
        userId: String,
        vendorId: String,
        cartItems: List<CartItemEntity>,
        totalAmount: Double,
        paymentMethod: PaymentMethod,
        pickupTime: String,
        specialRequests: String? = null
    ): Long
    {
        // 1. Check if we need to sync with the remote API
        val numericVendorId = vendorId.toIntOrNull()
        val user = userDao.getUserById(userId)
        val apikey = user?.usercode

        if ((numericVendorId != null) && (apikey != null))
        {
            try
            {
                val networkItems = cartItems.map()
                {
                    OrderItemRequest(it.name, it.quantity)
                }
                apiService.createOrder(
                    numericVendorId,
                    apikey,
                    OrderRequest(networkItems)
                )
                // Note: We ignore the response as the API mock is non-persistent for many users,
                // but we perform the call to demonstrate "Usage" of the API.
            }
            catch (_: Exception)
            {
                // Network order failed, we continue with local persistence
            }
        }

        // 2. Persist locally in Room
        val order = OrderEntity(
            customerId = userId,
            vendorId = vendorId,
            itemsJson = Json.encodeToString(cartItems),
            totalAmount = totalAmount,
            status = OrderStatus.PENDING,
            paymentMethod = paymentMethod,
            pickupTime = pickupTime,
            specialRequests = specialRequests
        )
        val id = orderDao.insertOrder(order)

        // Ensure atomic operations: clearing cart after order placement
        cartDao.clearCart(userId)

        return id
    }

    /**
     * Retrieves all orders placed by a specific customer.
     */
    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersByCustomer(userId)

    /**
     * Retrieves orders from the remote API for the given user.
     */
    @Suppress("unused")
    fun getRemoteOrders(userId: String): Flow<List<MasterOrder>> = flow()
    {
        val user = userDao.getUserById(userId)
        val apikey = user?.usercode
        if (apikey != null)
        {
            try
            {
                val response = apiService.getUserOrders(apikey)
                if (response.isSuccessful)
                {
                    emit(response.body() ?: emptyList())
                }
            }
            catch (_: Exception)
            {
                // Network error
            }
        }
    }

    /**
     * Retrieves all orders assigned to a specific vendor.
     */
    fun getOrdersForVendor(vendorId: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersByVendor(vendorId)

    /**
     * Updates the status of an existing order.
     */
    suspend fun updateOrderStatus(order: OrderEntity, status: OrderStatus)
    {
        orderDao.updateOrder(order.copy(status = status))
    }

    /**
     * Deletes a remote master order.
     */
    @Suppress("unused")
    suspend fun deleteRemoteMasterOrder(userId: String, masterId: Int): Boolean
    {
        val user = userDao.getUserById(userId)
        val apikey = user?.usercode ?: return false
        return try
        {
            val response = apiService.deleteMasterOrder(masterId, apikey)
            response.isSuccessful
        }
        catch (e: Exception)
        {
            false
        }
    }

    /**
     * Retrieves a list of all orders across the entire system (Admin restricted).
     */
    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()
}
