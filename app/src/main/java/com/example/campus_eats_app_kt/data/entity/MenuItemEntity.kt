package com.example.campus_eats_app_kt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MenuItemEntity represents a food or drink item available for purchase.
 */
@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,
    val vendorId: String,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val category: String,
    val imageUrl: String? = null
)
