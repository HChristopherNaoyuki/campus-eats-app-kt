package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.CartDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class OrderRepository(
    private val orderDao: OrderDao,
    private val cartDao: CartDao
) {
    suspend fun placeOrder(
        userId: String,
        vendorId: String,
        cartItems: List<CartItemEntity>,
        totalAmount: Double
    ): Long
    {
        val order = OrderEntity(
            customerId = userId,
            vendorId = vendorId,
            itemsJson = Json.encodeToString(cartItems),
            totalAmount = totalAmount,
            status = OrderStatus.PENDING
        )
        val id = orderDao.insertOrder(order)
        cartDao.clearCart(userId)
        return id
    }

    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersByCustomer(userId)

    fun getOrdersForVendor(vendorId: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersByVendor(vendorId)

    suspend fun updateOrderStatus(order: OrderEntity, status: OrderStatus) {
        orderDao.updateOrder(order.copy(status = status))
    }
}
