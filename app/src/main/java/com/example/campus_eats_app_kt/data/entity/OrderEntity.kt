package com.example.campus_eats_app_kt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * OrderStatus defines the various stages of an order lifecycle.
 */
@Serializable
enum class OrderStatus
{
    PENDING,
    ACCEPTED,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED
}

@Serializable
enum class PaymentMethod
{
    DEBIT_CARD,
    CAMPUS_WALLET,
    COUPON
}

/**
 * OrderEntity represents a customer's purchase from a specific vendor.
 */
@Serializable
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val orderId: Long = 0,
    val customerId: String,
    val vendorId: String,
    val itemsJson: String,
    val totalAmount: Double,
    val status: OrderStatus,
    val paymentMethod: PaymentMethod,
    val pickupTime: String,
    val specialRequests: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
