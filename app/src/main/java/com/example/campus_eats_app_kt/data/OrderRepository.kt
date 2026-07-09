package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.CartDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

/**
 * OrderRepository manages the lifecycle of customer orders.
 * It handles order placement, retrieval for users and vendors, and status updates.
 */
class OrderRepository(
    private val orderDao: OrderDao,
    private val cartDao: CartDao
)
{
    /**
     * Persists a new order in the database and clears the user's cart.
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
     * Retrieves a list of all orders across the entire system (Admin restricted).
     */
    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()
}
