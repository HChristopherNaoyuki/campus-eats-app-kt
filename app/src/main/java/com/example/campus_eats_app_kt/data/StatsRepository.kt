package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class VendorStats(
    val allTimeEarnings: Double,
    val menuItemCount: Int,
    val activeOrders: Int,
    val todayRevenue: Double
)

data class AdminStats(
    val allTimeEarnings: Double,
    val totalUsers: Int,
    val activeVendors: Int,
    val menuItemCount: Int,
    val orderCount: Int,
    val todayRevenue: Double,
    val weekRevenue: Double,
    val monthRevenue: Double
)

class StatsRepository(
    private val userDao: UserDao,
    private val menuItemDao: MenuItemDao,
    private val orderDao: OrderDao
)
{
    fun getVendorStats(vendorId: String): Flow<VendorStats>
    {
        return combine(
            orderDao.getOrdersByVendor(vendorId),
            menuItemDao.getMenuItemsByVendor(vendorId)
        ) { orders, menuItems ->
            val now = System.currentTimeMillis()
            val startOfDay = now - (now % (24 * 60 * 60 * 1000))

            VendorStats(
                allTimeEarnings = orders.filter { it.status == OrderStatus.COMPLETED }
                    .sumOf { it.totalAmount },
                menuItemCount = menuItems.size,
                activeOrders = orders.count { it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED },
                todayRevenue = orders.filter { it.status == OrderStatus.COMPLETED && it.timestamp >= startOfDay }
                    .sumOf { it.totalAmount }
            )
        }
    }

    fun getAdminStats(): Flow<AdminStats>
    {
        return combine(
            userDao.getAllUsers(),
            menuItemDao.getAllMenuItems(),
            orderDao.getOrdersByStatus(OrderStatus.COMPLETED)
        ) { users, menuItems, completedOrders ->
            val now = System.currentTimeMillis()
            val startOfDay = now - (now % (24 * 60 * 60 * 1000))
            val startOfWeek = now - (7L * 24 * 60 * 60 * 1000)
            val startOfMonth = now - (30L * 24 * 60 * 60 * 1000)

            AdminStats(
                allTimeEarnings = completedOrders.sumOf { it.totalAmount },
                totalUsers = users.size,
                activeVendors = users.count { it.role == UserRole.VENDOR },
                menuItemCount = menuItems.size,
                orderCount = completedOrders.size,
                todayRevenue = completedOrders.filter { it.timestamp >= startOfDay }
                    .sumOf { it.totalAmount },
                weekRevenue = completedOrders.filter { it.timestamp >= startOfWeek }
                    .sumOf { it.totalAmount },
                monthRevenue = completedOrders.filter { it.timestamp >= startOfMonth }
                    .sumOf { it.totalAmount }
            )
        }
    }
}
