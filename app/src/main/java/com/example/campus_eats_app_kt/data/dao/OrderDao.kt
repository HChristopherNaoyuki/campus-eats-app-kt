package com.example.campus_eats_app_kt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import kotlinx.coroutines.flow.Flow

/**
 * OrderDao defines the database operations for the 'orders' table.
 */
@Dao
interface OrderDao
{
    /**
     * Persists a new order.
     */
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    /**
     * Updates an existing order.
     */
    @Update
    suspend fun updateOrder(order: OrderEntity)

    /**
     * Retrieves all orders for a specific customer, ordered by time.
     */
    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getOrdersByCustomer(customerId: String): Flow<List<OrderEntity>>

    /**
     * Retrieves all orders assigned to a specific vendor.
     */
    @Query("SELECT * FROM orders WHERE vendorId = :vendorId ORDER BY timestamp DESC")
    fun getOrdersByVendor(vendorId: String): Flow<List<OrderEntity>>

    /**
     * Retrieves orders filtered by their current status.
     */
    @Query("SELECT * FROM orders WHERE status = :status")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<OrderEntity>>

    /**
     * Returns a stream of all orders in the system.
     */
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT SUM(totalAmount) FROM orders WHERE vendorId = :vendorId AND status = 'COMPLETED'")
    fun getVendorTotalEarnings(vendorId: String): Flow<Double?>

    @Query("SELECT COUNT(*) FROM orders WHERE vendorId = :vendorId AND status NOT IN ('COMPLETED', 'CANCELLED')")
    fun getVendorActiveOrderCount(vendorId: String): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM orders WHERE status = 'COMPLETED'")
    fun getGlobalTotalEarnings(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED'")
    fun getGlobalCompletedOrderCount(): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM orders WHERE status = 'COMPLETED' AND timestamp >= :startTime")
    fun getRevenueSince(startTime: Long): Flow<Double?>

    @Query("SELECT SUM(totalAmount) FROM orders WHERE vendorId = :vendorId AND status = 'COMPLETED' AND timestamp >= :startTime")
    fun getVendorRevenueSince(vendorId: String, startTime: Long): Flow<Double?>
}
