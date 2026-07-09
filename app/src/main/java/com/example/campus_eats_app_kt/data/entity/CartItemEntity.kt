package com.example.campus_eats_app_kt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * CartItemEntity represents a transient item stored in the user's shopping cart.
 */
@Serializable
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val cartItemId: Long = 0,
    val userId: String,
    val itemId: Long,
    val vendorId: String,
    val name: String,
    val price: Double,
    val quantity: Int
)
