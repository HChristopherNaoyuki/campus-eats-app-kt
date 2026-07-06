package com.example.campus_eats_app_kt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserStatus
import kotlinx.coroutines.flow.Flow

/**
 * UserDao provides the data access methods for the 'users' table.
 */
@Dao
interface UserDao
{
    /**
     * Inserts a new user. Aborts if the user already exists.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    /**
     * Retrieves a user by their email address.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    /**
     * Retrieves a user by their unique 16-character User ID.
     */
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    /**
     * Returns a Flow of a user record for reactive UI updates.
     */
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    /**
     * Updates an existing user record.
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * Returns a Flow of all users in the system.
     */
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    /**
     * Adds credits to a user's wallet balance.
     */
    @Query("UPDATE users SET walletBalance = walletBalance + :amount WHERE userId = :userId")
    suspend fun addCredits(userId: String, amount: Double)

    /**
     * Updates the activation status of a user.
     */
    @Query("UPDATE users SET status = :status WHERE userId = :userId")
    suspend fun updateStatus(userId: String, status: UserStatus)

    /**
     * Deletes a user record.
     */
    @androidx.room.Delete
    suspend fun deleteUser(user: UserEntity)
}
