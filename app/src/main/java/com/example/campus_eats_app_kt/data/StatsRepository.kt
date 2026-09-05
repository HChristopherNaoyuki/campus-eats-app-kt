package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

/**
 * VendorStats holds the performance metrics for a single shop owner.
 */
data class VendorStats(
    val allTimeEarnings: Double,
    val menuItemCount: Int,
    val activeOrders: Int,
    val todayRevenue: Double,
)

/**
 * AdminStats aggregates system-wide financial and user metrics.
 */
data class AdminStats(
    val allTimeEarnings: Double,
    val totalUsers: Int,
    val activeVendors: Int,
    val menuItemCount: Int,
    val orderCount: Int,
    val todayRevenue: Double,
    val weekRevenue: Double,
    val monthRevenue: Double,
)

/**
 * StatsRepository computes analytical data and financial reports by aggregating 
 * information from Users, Menu Items, and Orders.
 */
class StatsRepository(
    private val userDao: UserDao,
    private val menuItemDao: MenuItemDao,
    private val orderDao: OrderDao,
)
{
    companion object
    {
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
        private const val MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY
        private const val MILLIS_PER_MONTH = 30 * MILLIS_PER_DAY
    }

    /**
     * Aggregates real-time statistics for a vendor.
     */
    fun getVendorStats(vendorId: String): Flow<VendorStats>
    {
        return combine(
            orderDao.getOrdersByVendor(vendorId),
            menuItemDao.getMenuItemsByVendor(vendorId),
        ) { orders, menuItems ->
            val now = System.currentTimeMillis()
            val startOfDay = now - (now % MILLIS_PER_DAY)

            VendorStats(
                allTimeEarnings = orders.asSequence().filter { it.status == OrderStatus.COMPLETED }
                    .sumOf { it.totalAmount },
                menuItemCount = menuItems.size,
                activeOrders = orders.count() 
                {
                    (it.status != OrderStatus.COMPLETED) && (it.status != OrderStatus.CANCELLED)
                },
                todayRevenue = orders.asSequence().filter { (it.status == OrderStatus.COMPLETED) && (it.timestamp >= startOfDay) }
                    .sumOf { it.totalAmount },
            )
        }.flowOn(Dispatchers.Default)
    }

    /**
     * Aggregates system-wide analytics for administrators.
     */
    fun getAdminStats(): Flow<AdminStats>
    {
        return combine(
            userDao.getAllUsers(),
            menuItemDao.getAllMenuItems(),
            orderDao.getOrdersByStatus(OrderStatus.COMPLETED),
        ) { users, menuItems, completedOrders ->
            val now = System.currentTimeMillis()
            val startOfDay = now - (now % MILLIS_PER_DAY)
            val startOfWeek = now - MILLIS_PER_WEEK
            val startOfMonth = now - MILLIS_PER_MONTH

            AdminStats(
                allTimeEarnings = completedOrders.sumOf { it.totalAmount },
                totalUsers = users.size,
                activeVendors = users.count { it.role == UserRole.VENDOR },
                menuItemCount = menuItems.size,
                orderCount = completedOrders.size,
                todayRevenue = completedOrders.asSequence().filter { it.timestamp >= startOfDay }
                    .sumOf { it.totalAmount },
                weekRevenue = completedOrders.asSequence().filter { it.timestamp >= startOfWeek }
                    .sumOf { it.totalAmount },
                monthRevenue = completedOrders.asSequence().filter { it.timestamp >= startOfMonth }
                    .sumOf { it.totalAmount },
            )
        }.flowOn(Dispatchers.Default)
    }
}
