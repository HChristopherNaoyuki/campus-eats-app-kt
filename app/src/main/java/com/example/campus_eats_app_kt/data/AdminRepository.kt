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
 * Integrates local persistence with Realtime Database synchronization.
 */
class AdminRepository(
    private val userDao: UserDao,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseDatabase: FirebaseDatabase,
)
{
    private val tag = "AdminRepository"

    /**
     * Retrieves all registered users in the system.
     */
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    /**
     * Temporarily disables a user's access to the application.
     */
    suspend fun suspendUser(userId: String): Result<Unit>
    {
        return kotlin.runCatching()
        {
            connectivityManager.ensureInternet()
            userDao.updateStatus(userId, UserStatus.SUSPENDED)

            // Sync to RTDB
            firebaseDatabase.getReference("users").child(userId).child("status")
                .setValue(UserStatus.SUSPENDED).await()
        }
    }

    /**
     * Restores a suspended user's access to the application.
     */
    suspend fun activateUser(userId: String): Result<Unit>
    {
        return kotlin.runCatching()
        {
            connectivityManager.ensureInternet()
            userDao.updateStatus(userId, UserStatus.ACTIVE)

            // Sync to RTDB
            firebaseDatabase.getReference("users").child(userId).child("status")
                .setValue(UserStatus.ACTIVE).await()
        }
    }

    /**
     * Manually adds credit to a specific user's Campus Wallet.
     */
    suspend fun issueCredits(userId: String, amount: Double): Result<Unit>
    {
        return kotlin.runCatching()
        {
            connectivityManager.ensureInternet()
            userDao.addCredits(userId, amount)

            // Fetch updated balance and sync to RTDB
            userDao.getUserById(userId)?.let { updatedUser ->
                firebaseDatabase.getReference("users").child(userId).child("walletBalance")
                    .setValue(updatedUser.walletBalance).await()
            }
        }
    }

    /**
     * Permanently removes a user record from the database.
     */
    suspend fun deleteUser(user: UserEntity): Result<Unit>
    {
        return kotlin.runCatching()
        {
            connectivityManager.ensureInternet()

            // Delete from RTDB first to ensure cloud state is updated
            try
            {
                firebaseDatabase.getReference("users").child(user.userId).removeValue().await()
            }
            catch (e: Exception)
            {
                Log.e(tag, "Failed to delete user from RTDB: ${e.message}")
                throw e
            }

            // Then delete locally
            userDao.deleteUser(user)
        }
    }
}
