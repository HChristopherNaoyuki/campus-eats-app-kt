package com.example.campus_eats_app_kt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
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
 * OrderEntity represents a customer's purchase.
 * Finding 22 & 27: Enforces relationships and unique ID formatting.
 */
@Serializable
@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["customerId"])]
)
data class OrderEntity(
    @PrimaryKey
    val orderId: String,
    val customerId: String,
    val vendorId: String,
    val itemsJson: String,
    val totalAmount: Double,
    val status: OrderStatus,
    val paymentMethod: PaymentMethod,
    val pickupTime: String,
    val specialRequests: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isPendingSync: Boolean = false,
)
