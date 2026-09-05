package com.example.campus_eats_app_kt.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * UserRole defines the access levels for the application.
 * Values must match Firebase Realtime Database security rules.
 */
@Serializable
enum class UserRole
{
    @SerialName("STUDENT")
    STUDENT,
    @SerialName("STANDARD")
    STANDARD,
    @SerialName("VENDOR")
    VENDOR,
    @SerialName("ADMIN")
    ADMINISTRATOR
}

/**
 * UserStatus defines the account state.
 * Values must match Firebase Realtime Database security rules.
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
 * UserEntity represents a user record in the local database and Firebase Realtime Database.
 * The primary key is a 19-character formatted string: XXXX-XXXX-XXXX-XXXX.
 */
@Serializable
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String, // 19-character formatted (XXXX-XXXX-XXXX-XXXX)
    val fullName: String,
    val username: String,
    val email: String,
    val passwordHash: String = "[FIREBASE_SSO]",
    val role: UserRole,
    val status: UserStatus = UserStatus.ACTIVE,
    val walletBalance: Double = 0.0,
    val shopName: String? = null,
    val shopStatus: ShopStatus? = null,
    val bankAccountInfo: String? = null,
    val registrationDate: Long = System.currentTimeMillis(),
    val usercode: String? = null, // API key for external "Fake Restaurant" API
)
