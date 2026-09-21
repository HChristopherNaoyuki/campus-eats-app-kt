package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.OrderDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.util.Calendar

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
 * StatsRepository computes analytical data.
 * Hardened in Batch 2 with SQL-based aggregation and local time boundaries.
 */
class StatsRepository(
    private val userDao: UserDao,
    private val menuItemDao: MenuItemDao,
    private val orderDao: OrderDao,
)
{
    private fun getStartTime(daysBack: Int): Long
    {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysBack)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Aggregates real-time statistics for a vendor.
     * Finding 18: Uses SQL aggregation for performance.
     */
    fun getVendorStats(vendorId: String): Flow<VendorStats>
    {
        val today = getStartTime(0)
        return combine(
            orderDao.getVendorTotalEarnings(vendorId),
            menuItemDao.getMenuItemCountByVendor(vendorId),
            orderDao.getVendorActiveOrderCount(vendorId),
            orderDao.getVendorRevenueSince(vendorId, today),
        ) { earnings, count, active, todayRev ->
            VendorStats(
                allTimeEarnings = earnings ?: 0.0,
                menuItemCount = count,
                activeOrders = active,
                todayRevenue = todayRev ?: 0.0,
            )
        }.flowOn(Dispatchers.Default)
    }

    /**
     * Aggregates system-wide analytics for administrators.
     */
    fun getAdminStats(): Flow<AdminStats>
    {
        val today = getStartTime(0)
        val week = getStartTime(7)
        val month = getStartTime(30)

        // Using combine with varargs since we have > 5 flows
        return combine(
            orderDao.getGlobalTotalEarnings(),
            userDao.getTotalUserCount(),
            userDao.getVendorCount(),
            menuItemDao.getGlobalMenuItemCount(),
            orderDao.getGlobalCompletedOrderCount(),
            orderDao.getRevenueSince(today),
            orderDao.getRevenueSince(week),
            orderDao.getRevenueSince(month),
        ) { args: Array<*> ->
            AdminStats(
                allTimeEarnings = args[0] as? Double ?: 0.0,
                totalUsers = args[1] as Int,
                activeVendors = args[2] as Int,
                menuItemCount = args[3] as Int,
                orderCount = args[4] as Int,
                todayRevenue = args[5] as? Double ?: 0.0,
                weekRevenue = args[6] as? Double ?: 0.0,
                monthRevenue = args[7] as? Double ?: 0.0,
            )
        }.flowOn(Dispatchers.Default)
    }
}
