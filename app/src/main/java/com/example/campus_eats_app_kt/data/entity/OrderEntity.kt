package com.example.campus_eats_app_kt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderStatus {
    PENDING,
    ACTIVE,
    COMPLETED,
    CANCELLED
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val orderId: Long = 0,
    val customerId: String,
    val vendorId: String,
    val itemsJson: String, // Storing items as JSON string for simplicity in offline-first
    val totalAmount: Double,
    val status: OrderStatus,
    val timestamp: Long = System.currentTimeMillis()
)
