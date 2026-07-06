package com.example.campus_eats_app_kt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * UserRole defines the access levels for the application.
 */
@Serializable
enum class UserRole
{
    STUDENT,
    STANDARD,
    VENDOR,
    ADMIN
}

/**
 * UserStatus defines the account state.
 */
@Serializable
enum class UserStatus
{
    ACTIVE,
    SUSPENDED
}

/**
 * ShopStatus defines the availability of a vendor.
 */
@Serializable
enum class ShopStatus
{
    OPEN,
    PREPARING_ORDERS,
    BUSY,
    CLOSED
}

/**
 * UserEntity represents a user record in the local database.
 * The primary key is a generated 16-character alphanumeric string.
 */
@Serializable
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String, // 16-character alphanumeric (XXXX-XXXX-XXXX-XXXX)
    val fullName: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val status: UserStatus = UserStatus.ACTIVE,
    val walletBalance: Double = 0.0,
    val shopName: String? = null,
    val shopStatus: ShopStatus? = null,
    val bankAccountInfo: String? = null,
    val registrationDate: Long = System.currentTimeMillis()
)
