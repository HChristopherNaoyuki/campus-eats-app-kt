package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserStatus
import kotlinx.coroutines.flow.Flow

class AdminRepository(private val userDao: UserDao) {
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun suspendUser(userId: String) {
        userDao.updateStatus(userId, UserStatus.SUSPENDED)
    }

    suspend fun activateUser(userId: String) {
        userDao.updateStatus(userId, UserStatus.ACTIVE)
    }

    suspend fun issueCredits(userId: String, amount: Double) {
        userDao.addCredits(userId, amount)
    }

    suspend fun deleteUser(user: UserEntity)
    {
        userDao.deleteUser(user)
    }
}
