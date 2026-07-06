package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.util.IdGenerator
import kotlinx.coroutines.flow.Flow

/**
 * AuthRepository handles user-related authentication and profile management operations.
 * It interacts with the local Room database via the UserDao.
 */
class AuthRepository(private val userDao: UserDao)
{
    /**
     * Registers a new user in the system.
     * Generates a unique 16-character alphanumeric User ID.
     */
    suspend fun register(
        fullName: String,
        username: String,
        email: String,
        password: String,
        role: UserRole,
        shopName: String? = null
    ): Result<UserEntity>
    {
        return kotlin.runCatching {
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null)
            {
                throw Exception("Email already registered")
            }

            val userId = IdGenerator.generateUserId()
            val user = UserEntity(
                userId = userId,
                fullName = fullName,
                username = username,
                email = email,
                passwordHash = password,
                role = role,
                shopName = if (role == UserRole.VENDOR) shopName else null,
                shopStatus = if (role == UserRole.VENDOR) ShopStatus.OPEN else null
            )

            userDao.insertUser(user)
            user
        }
    }

    /**
     * Attempts to log in a user with the provided email and password.
     * Returns a Result containing the UserEntity if successful, or a failure if not.
     */
    suspend fun login(email: String, password: String): Result<UserEntity>
    {
        return kotlin.runCatching {
            val user = userDao.getUserByEmail(email)
            if (user != null && user.passwordHash == password)
            {
                user
            }
            else
            {
                throw Exception("Invalid email or password")
            }
        }
    }

    /**
     * Resets the password for a user identified by their unique User ID.
     */
    suspend fun resetPassword(userId: String, newPassword: String): Result<Unit>
    {
        return kotlin.runCatching {
            val user = userDao.getUserById(userId)
            if (user != null)
            {
                val updatedUser = user.copy(passwordHash = newPassword)
                userDao.updateUser(updatedUser)
            }
            else
            {
                throw Exception("Invalid User ID")
            }
        }
    }

    /**
     * Updates the user's profile information.
     */
    suspend fun updateProfile(userId: String, email: String, password: String): Result<Unit>
    {
        return kotlin.runCatching {
            val user = userDao.getUserById(userId)
            if (user == null) throw Exception("User not found")

            if (user.email != email)
            {
                val existing = userDao.getUserByEmail(email)
                if (existing != null) throw Exception("Email already taken")
            }

            var updatedUser = user.copy(email = email)
            if (password.isNotBlank())
            {
                updatedUser = updatedUser.copy(passwordHash = password)
            }
            userDao.updateUser(updatedUser)
        }
    }

    /**
     * Returns a Flow of the UserEntity for the given User ID.
     */
    fun getUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)

    /**
     * Updates the shop status for a vendor.
     */
    suspend fun updateShopStatus(userId: String, status: ShopStatus)
    {
        val user = userDao.getUserById(userId)
        if (user != null && user.role == UserRole.VENDOR)
        {
            userDao.updateUser(user.copy(shopStatus = status))
        }
    }

    /**
     * Links a bank account to a vendor's profile for payouts.
     */
    suspend fun linkBankAccount(userId: String, bankInfo: String): Result<Unit>
    {
        return kotlin.runCatching {
            val user = userDao.getUserById(userId)
            if (user != null)
            {
                userDao.updateUser(user.copy(bankAccountInfo = bankInfo))
            }
            else
            {
                throw Exception("User not found")
            }
        }
    }
}
