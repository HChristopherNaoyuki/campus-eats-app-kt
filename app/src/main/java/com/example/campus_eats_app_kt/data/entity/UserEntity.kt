package com.example.campus_eats_app_kt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    STUDENT,
    STANDARD,
    VENDOR,
    ADMIN
}

@Serializable
enum class UserStatus {
    ACTIVE,
    SUSPENDED
}

@Serializable
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String, // 16-character alphanumeric (XXXX-XXXX-XXXX-XXXX)
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val status: UserStatus = UserStatus.ACTIVE,
    val walletBalance: Double = 0.0
)
