package com.example.campus_eats_app_kt.data

import app.cash.turbine.test
import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.PaymentMethod
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * StatsRepositoryTest verifies the data aggregation logic for vendor and admin reports.
 */
class StatsRepositoryTest
{
    private lateinit var userDao: UserDao
    private lateinit var menuItemDao: MenuItemDao
    private lateinit var orderDao: OrderDao
    private lateinit var repository: StatsRepository

    @Before
    fun setUp()
    {
        userDao = mockk(relaxed = true)
        menuItemDao = mockk(relaxed = true)
        orderDao = mockk(relaxed = true)
        repository = StatsRepository(userDao, menuItemDao, orderDao)
    }

    /**
     * Requirement: Test vendor earnings aggregation
     */
    @Test
    fun getVendorStats_aggregatesEarningsAndOrders() = runTest {
        // Given
        val vendorId = "VENDOR-1"
        val orders = listOf(
            OrderEntity(
                orderId = 1,
                customerId = "C1",
                vendorId = vendorId,
                itemsJson = "[]",
                totalAmount = 100.0,
                status = OrderStatus.COMPLETED,
                paymentMethod = PaymentMethod.DEBIT_CARD,
                pickupTime = "12:00"
            ),
            OrderEntity(
                orderId = 2,
                customerId = "C1",
                vendorId = vendorId,
                itemsJson = "[]",
                totalAmount = 50.0,
                status = OrderStatus.PENDING,
                paymentMethod = PaymentMethod.DEBIT_CARD,
                pickupTime = "12:15"
            )
        )
        every { orderDao.getOrdersByVendor(vendorId) } returns flowOf(orders)
        every { menuItemDao.getMenuItemsByVendor(vendorId) } returns flowOf(emptyList())

        // When/Then
        repository.getVendorStats(vendorId).test {
            val stats = awaitItem()
            assertEquals(100.0, stats.allTimeEarnings, 0.001)
            assertEquals(1, stats.activeOrders) // The PENDING one
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Requirement: Test admin system stats aggregation
     */
    @Test
    fun getAdminStats_aggregatesSystemMetrics() = runTest {
        // Given
        val completedOrders = listOf(
            OrderEntity(
                orderId = 1,
                customerId = "C1",
                vendorId = "V1",
                itemsJson = "[]",
                totalAmount = 500.0,
                status = OrderStatus.COMPLETED,
                paymentMethod = PaymentMethod.DEBIT_CARD,
                pickupTime = "12:00"
            )
        )
        every { orderDao.getOrdersByStatus(OrderStatus.COMPLETED) } returns flowOf(completedOrders)
        every { userDao.getAllUsers() } returns flowOf(emptyList())
        every { menuItemDao.getAllMenuItems() } returns flowOf(emptyList())

        // When/Then
        repository.getAdminStats().test {
            val stats = awaitItem()
            assertEquals(500.0, stats.allTimeEarnings, 0.001)
            assertEquals(1, stats.orderCount)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
