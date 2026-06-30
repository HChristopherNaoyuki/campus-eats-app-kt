package com.example.campus_eats_app_kt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey
    val code: String,
    val discountPercent: Double,
    val isActive: Boolean = true
)
