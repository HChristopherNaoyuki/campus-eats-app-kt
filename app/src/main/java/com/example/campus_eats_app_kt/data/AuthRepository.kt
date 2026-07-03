package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.util.IdGenerator
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val userDao: UserDao) {

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: UserRole,
        shopName: String? = null
    ): Result<UserEntity>
    {
        val existingUser = userDao.getUserByEmail(email)
        if (existingUser != null) {
            return Result.failure(Exception("Email already registered"))
        }

        val userId = IdGenerator.generateUserId()
        val user = UserEntity(
            userId = userId,
            fullName = fullName,
            email = email,
            passwordHash = password,
            role = role,
            shopName = if (role == UserRole.VENDOR) shopName else null,
            shopStatus = if (role == UserRole.VENDOR) ShopStatus.OPEN else null
        )

        return try {
            userDao.insertUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val user = userDao.getUserByEmail(email)
        return if (user != null && user.passwordHash == password) {
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    suspend fun resetPassword(userId: String, newPassword: String): Result<Unit> {
        val user = userDao.getUserById(userId)
        return if (user != null) {
            val updatedUser = user.copy(passwordHash = newPassword)
            userDao.updateUser(updatedUser)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid User ID"))
        }
    }

    fun getUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)

    suspend fun updateShopStatus(userId: String, status: ShopStatus)
    {
        val user = userDao.getUserById(userId)
        if (user != null && user.role == UserRole.VENDOR)
        {
            userDao.updateUser(user.copy(shopStatus = status))
        }
    }

    suspend fun linkBankAccount(userId: String, bankInfo: String): Result<Unit>
    {
        val user = userDao.getUserById(userId)
        return if (user != null)
        {
            userDao.updateUser(user.copy(bankAccountInfo = bankInfo))
            Result.success(Unit)
        }
        else
        {
            Result.failure(Exception("User not found"))
        }
    }
}
