package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserStatus
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import android.util.Log
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
    private val tag = "AdminRepository"

    /**
     * Retrieves all registered users in the system.
     * Firebase rules restrict read access to /users if not admin or own record.
     */
    fun getAllUsers(): Flow<List<UserEntity>>
    {
        return userDao.getAllUsers()
    }

    /**
     * Temporarily disables a user's access to the application.
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
     * Restores a suspended user's access to the application.
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
     * Manually adds credit to a specific user's Campus Wallet.
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

            connectivityManager.ensureInternet()
            userDao.addCredits(userId, amount)

            userDao.getUserById(userId)?.let()
            { updatedUser ->
                firebaseDatabase.getReference("users").child(userId).child("walletBalance")
                    .setValue(updatedUser.walletBalance).await()
            }
        }
    }

    /**
     * Permanently removes a user record from the database.
     * Restricted to authenticated administrators in Firebase rules.
     */
    suspend fun deleteUser(user: UserEntity): Result<Unit>
    {
        return kotlin.runCatching()
        {
            if (!authRepository.isAdmin())
            {
                throw Exception("Unauthorized: Administrator privileges required.")
            }

            connectivityManager.ensureInternet()

            try
            {
                firebaseDatabase.getReference("users").child(user.userId).removeValue().await()
            }
            catch (e: Exception)
            {
                Log.e(tag, "Failed to delete user from RTDB: ${e.message}")
                throw e
            }

            userDao.deleteUser(user)
        }
    }
}
