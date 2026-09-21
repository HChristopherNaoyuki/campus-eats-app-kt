package com.example.campus_eats_app_kt.data

import app.cash.turbine.test
import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * StatsRepositoryTest verifies data aggregation using SQL-optimized DAOs.
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

    @Test
    fun getVendorStats_aggregatesEarningsAndOrders() = runTest {
        val vendorId = "VENDOR-1"
        
        every { orderDao.getVendorTotalEarnings(vendorId) } returns flowOf(100.0)
        every { menuItemDao.getMenuItemCountByVendor(vendorId) } returns flowOf(5)
        every { orderDao.getVendorActiveOrderCount(vendorId) } returns flowOf(1)
        every { orderDao.getVendorRevenueSince(vendorId, any()) } returns flowOf(20.0)

        repository.getVendorStats(vendorId).test {
            val stats = awaitItem()
            assertEquals(100.0, stats.allTimeEarnings, 0.001)
            assertEquals(5, stats.menuItemCount)
            assertEquals(1, stats.activeOrders)
            assertEquals(20.0, stats.todayRevenue, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAdminStats_aggregatesSystemMetrics() = runTest {
        every { orderDao.getGlobalTotalEarnings() } returns flowOf(500.0)
        every { userDao.getTotalUserCount() } returns flowOf(10)
        every { userDao.getVendorCount() } returns flowOf(2)
        every { menuItemDao.getGlobalMenuItemCount() } returns flowOf(20)
        every { orderDao.getGlobalCompletedOrderCount() } returns flowOf(15)
        every { orderDao.getRevenueSince(any()) } returns flowOf(100.0)

        repository.getAdminStats().test {
            val stats = awaitItem()
            assertEquals(500.0, stats.allTimeEarnings, 0.001)
            assertEquals(10, stats.totalUsers)
            assertEquals(2, stats.activeVendors)
            assertEquals(20, stats.menuItemCount)
            assertEquals(15, stats.orderCount)
            assertEquals(100.0, stats.todayRevenue, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
