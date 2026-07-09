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
}
