package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * VendorStats holds the performance metrics for a single shop owner.
 */
data class VendorStats(
    val allTimeEarnings: Double,
    val menuItemCount: Int,
    val activeOrders: Int,
    val todayRevenue: Double
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
    val monthRevenue: Double
)

data class DailyTrend(val date: String, val orderCount: Int, val revenue: Double)
data class VendorRevenue(
    val vendorName: String,
    val orderCount: Int,
    val revenue: Double,
    val percentage: Double
)

data class PopularItem(val itemName: String, val unitsSold: Int, val revenue: Double)

/**
 * StatsRepository computes analytical data and financial reports by aggregating 
 * information from Users, Menu Items, and Orders.
 */
class StatsRepository(
    private val userDao: UserDao,
    private val menuItemDao: MenuItemDao,
    private val orderDao: OrderDao
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
            menuItemDao.getMenuItemsByVendor(vendorId)
        ) { orders, menuItems ->
            val now = System.currentTimeMillis()
            val startOfDay = now - (now % MILLIS_PER_DAY)

            VendorStats(
                allTimeEarnings = orders.filter { it.status == OrderStatus.COMPLETED }
                    .sumOf { it.totalAmount },
                menuItemCount = menuItems.size,
                activeOrders = orders.count {
                    it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED
                },
                todayRevenue = orders.filter { it.status == OrderStatus.COMPLETED && it.timestamp >= startOfDay }
                    .sumOf { it.totalAmount }
            )
        }
    }

    /**
     * Aggregates system-wide analytics for administrators.
     */
    fun getAdminStats(): Flow<AdminStats>
    {
        return combine(
            userDao.getAllUsers(),
            menuItemDao.getAllMenuItems(),
            orderDao.getOrdersByStatus(OrderStatus.COMPLETED)
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
                todayRevenue = completedOrders.filter { it.timestamp >= startOfDay }
                    .sumOf { it.totalAmount },
                weekRevenue = completedOrders.filter { it.timestamp >= startOfWeek }
                    .sumOf { it.totalAmount },
                monthRevenue = completedOrders.filter { it.timestamp >= startOfMonth }
                    .sumOf { it.totalAmount }
            )
        }
    }

    /**
     * Generates a daily trend report for the past 7 days.
     */
    fun getDailyTrends(): Flow<List<DailyTrend>>
    {
        return orderDao.getOrdersByStatus(OrderStatus.COMPLETED).map { orders ->
            val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val now = System.currentTimeMillis()
            val pastWeek = now - MILLIS_PER_WEEK

            orders.filter { it.timestamp >= pastWeek }
                .groupBy { df.format(Date(it.timestamp)) }
                .map { (date, dayOrders) ->
                    DailyTrend(date, dayOrders.size, dayOrders.sumOf { it.totalAmount })
                }
                .sortedByDescending { it.date }
        }
    }

    /**
     * Calculates revenue distribution and rankings for all vendors.
     */
    fun getVendorRevenueRankings(): Flow<List<VendorRevenue>>
    {
        return combine(
            userDao.getAllUsers(),
            orderDao.getOrdersByStatus(OrderStatus.COMPLETED)
        ) { users, orders ->
            val totalRevenue = orders.sumOf { it.totalAmount }
            val vendors = users.filter { it.role == UserRole.VENDOR }

            vendors.map { vendor ->
                val vendorOrders = orders.filter { it.vendorId == vendor.userId }
                val revenue = vendorOrders.sumOf { it.totalAmount }
                val percentage = if (totalRevenue > 0) (revenue / totalRevenue) * 100 else 0.0
                VendorRevenue(
                    vendor.shopName ?: vendor.fullName,
                    vendorOrders.size,
                    revenue,
                    percentage
                )
            }.sortedByDescending { it.revenue }.take(10)
        }
    }

    /**
     * Identifies the top-selling items system-wide.
     */
    fun getPopularItems(): Flow<List<PopularItem>>
    {
        return orderDao.getOrdersByStatus(OrderStatus.COMPLETED).map { orders ->
            val itemUnitsMap = mutableMapOf<String, Int>()
            val itemRevenueMap = mutableMapOf<String, Double>()
            
            orders.forEach { order ->
                try
                {
                    val items = Json.decodeFromString<List<CartItemEntity>>(order.itemsJson)
                    items.forEach { item ->
                        itemUnitsMap[item.name] =
                            itemUnitsMap.getOrDefault(item.name, 0) + item.quantity
                        itemRevenueMap[item.name] = itemRevenueMap.getOrDefault(
                            item.name,
                            0.0
                        ) + (item.price * item.quantity)
                    }
                }
                catch (e: Exception)
                {
                    // Gracefully skip corrupted order items
                }
            }

            itemUnitsMap.map {
                PopularItem(it.key, it.value, itemRevenueMap[it.key] ?: 0.0)
            }.sortedByDescending { it.unitsSold }.take(3)
        }
    }
}
