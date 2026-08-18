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
 * Integrates local persistence with Realtime Database synchronization.
 */
class AdminRepository(
    private val userDao: UserDao,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabaseProvider.instance
)
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
        connectivityManager.ensureInternet()
        userDao.updateStatus(userId, UserStatus.SUSPENDED)

        // Sync to RTDB
        firebaseDatabase.getReference("users").child(userId).child("status")
            .setValue(UserStatus.SUSPENDED).await()
    }

    /**
     * Restores a suspended user's access to the application.
     */
    suspend fun activateUser(userId: String)
    {
        connectivityManager.ensureInternet()
        userDao.updateStatus(userId, UserStatus.ACTIVE)

        // Sync to RTDB
        firebaseDatabase.getReference("users").child(userId).child("status")
            .setValue(UserStatus.ACTIVE).await()
    }

    /**
     * Manually adds credit to a specific user's Campus Wallet.
     */
    suspend fun issueCredits(userId: String, amount: Double)
    {
        connectivityManager.ensureInternet()
        userDao.addCredits(userId, amount)

        // Fetch updated balance and sync to RTDB
        val updatedUser = userDao.getUserById(userId)
        if (updatedUser != null)
        {
            firebaseDatabase.getReference("users").child(userId).child("walletBalance")
                .setValue(updatedUser.walletBalance).await()
        }
    }

    /**
     * Permanently removes a user record from the database.
     */
    suspend fun deleteUser(user: UserEntity)
    {
        connectivityManager.ensureInternet()
        userDao.deleteUser(user)

        // Sync to RTDB
        firebaseDatabase.getReference("users").child(user.userId).removeValue().await()
    }
}
