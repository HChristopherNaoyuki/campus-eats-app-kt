package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.CartDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.PaymentMethod
import com.example.campus_eats_app_kt.data.network.FakeRestaurantApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * OrderRepositoryTest verifies order placement and status management.
 */
class OrderRepositoryTest
{
    private lateinit var orderDao: OrderDao
    private lateinit var cartDao: CartDao
    private lateinit var userDao: UserDao
    private lateinit var apiService: FakeRestaurantApiService
    private lateinit var repository: OrderRepository

    @Before
    fun setUp()
    {
        orderDao = mockk(relaxed = true)
        cartDao = mockk(relaxed = true)
        userDao = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
        repository = OrderRepository(orderDao, cartDao, userDao, apiService)
    }

    /**
     * Requirement: Test Pending to Accepted transition
     */
    @Test
    fun placeOrder_persistsOrderAndClearsCart() = runTest {
        // Given
        val userId = "USER-001"
        val vendorId = "VENDOR-001"
        coEvery { orderDao.insertOrder(any()) } returns 123L

        // When
        repository.placeOrder(
            userId = userId,
            vendorId = vendorId,
            cartItems = emptyList(),
            totalAmount = 100.0,
            paymentMethod = PaymentMethod.DEBIT_CARD,
            pickupTime = "12:00"
        )

        // Then
        coVerify { orderDao.insertOrder(any()) }
        coVerify { cartDao.clearCart(userId) }
    }

    /**
     * Requirement: Test order status update
     */
    @Test
    fun updateOrderStatus_updatesInDao() = runTest {
        // Given
        val order = com.example.campus_eats_app_kt.data.entity.OrderEntity(
            orderId = 123L,
            customerId = "C1",
            vendorId = "V1",
            itemsJson = "[]",
            totalAmount = 100.0,
            status = OrderStatus.PENDING,
            paymentMethod = PaymentMethod.DEBIT_CARD,
            pickupTime = "12:00"
        )
        // Stub the update call even though it's relaxed, to be safe with match
        coEvery { orderDao.updateOrder(any()) } returns Unit

        // When
        repository.updateOrderStatus(order, OrderStatus.ACCEPTED)

        // Then
        coVerify { orderDao.updateOrder(match { it.orderId == 123L && it.status == OrderStatus.ACCEPTED }) }
    }
}
