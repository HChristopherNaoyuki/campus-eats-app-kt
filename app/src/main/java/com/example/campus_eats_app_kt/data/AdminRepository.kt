package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserStatus
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/**
 * AdminRepository provides administrative oversight over user accounts and system finances.
 * Enforces compliance with Firebase Realtime Database security rules using custom claims.
 */
class AdminRepository(
    private val userDao: UserDao,
    private val authRepository: AuthRepository,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseDatabase: FirebaseDatabase,
)
{
    /**
     * Retrieves all registered users in the system.
     * Firebase rules restrict read access to /users if not admin or own record.
     */
    fun getAllUsers(): Flow<List<UserEntity>>
    {
        return userDao.getAllUsers()
    }

    /**
     * Temporarily disables a user account access to the application.
     * Restricted to authenticated administrators in Firebase rules.
     */
    suspend fun suspendUser(userId: String): Result<Unit>
    {
        return kotlin.runCatching()
        {
            if (!authRepository.isAdmin())
            {
                throw Exception("Unauthorized: Administrator privileges required.")
            }
            
            connectivityManager.ensureInternet()
            userDao.updateStatus(userId, UserStatus.SUSPENDED)

            firebaseDatabase.getReference("users").child(userId).child("status")
                .setValue(UserStatus.SUSPENDED).await()
        }
    }

    /**
     * Restores a suspended user account access to the application.
     * Restricted to authenticated administrators in Firebase rules.
     */
    suspend fun activateUser(userId: String): Result<Unit>
    {
        return kotlin.runCatching()
        {
            if (!authRepository.isAdmin())
            {
                throw Exception("Unauthorized: Administrator privileges required.")
            }

            connectivityManager.ensureInternet()
            userDao.updateStatus(userId, UserStatus.ACTIVE)

            firebaseDatabase.getReference("users").child(userId).child("status")
                .setValue(UserStatus.ACTIVE).await()
        }
    }

    /**
     * Manually adds credit to a specific user wallet balance.
     * Enforces numeric boundaries to prevent invalid zero or negative credit values (SEC-017).
     * Restricted to authenticated administrators in Firebase rules.
     */
    suspend fun issueCredits(userId: String, amount: Double): Result<Unit>
    {
        return kotlin.runCatching()
        {
            if (!authRepository.isAdmin())
            {
                throw Exception("Unauthorized: Administrator privileges required.")
            }

            if ((amount <= 0.0) || (amount > 10000.0))
            {
                throw IllegalArgumentException("Credit amount must be greater than 0.00 and less than or equal to 10000.00.")
            }

            connectivityManager.ensureInternet()
            userDao.addCredits(userId, amount)

            userDao.getUserById(userId)?.let()
            { updatedUser ->
                firebaseDatabase.getReference("users").child(userId).child("walletBalance")
                    .setValue(updatedUser.walletBalance).await()
            }
        }
    }
}
