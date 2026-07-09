package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserStatus
import kotlinx.coroutines.flow.Flow

/**
 * AdminRepository provides administrative oversight over user accounts and system finances.
 */
class AdminRepository(private val userDao: UserDao)
{
    /**
     * Retrieves all registered users in the system.
     */
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    /**
     * Temporarily disables a user's access to the application.
     */
    suspend fun suspendUser(userId: String)
    {
        userDao.updateStatus(userId, UserStatus.SUSPENDED)
    }

    /**
     * Restores a suspended user's access to the application.
     */
    suspend fun activateUser(userId: String)
    {
        userDao.updateStatus(userId, UserStatus.ACTIVE)
    }

    /**
     * Manually adds credit to a specific user's Campus Wallet.
     */
    suspend fun issueCredits(userId: String, amount: Double)
    {
        userDao.addCredits(userId, amount)
    }

    /**
     * Permanently removes a user record from the database.
     */
    suspend fun deleteUser(user: UserEntity)
    {
        userDao.deleteUser(user)
    }
}
