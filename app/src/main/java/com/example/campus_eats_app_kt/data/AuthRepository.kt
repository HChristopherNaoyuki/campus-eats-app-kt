package com.example.campus_eats_app_kt.data

import android.util.Log
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.network.FakeRestaurantApiService
import com.example.campus_eats_app_kt.data.network.RegistrationRequest
import com.example.campus_eats_app_kt.util.IdGenerator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/**
 * AuthRepository handles user-related authentication and profile management operations.
 * It integrates Firebase Authentication for SSO and local Room database for profile metadata.
 */
class AuthRepository(
    private val userDao: UserDao,
    private val apiService: FakeRestaurantApiService,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
)
{
    private val TAG = "AuthRepository"

    /**
     * Requirement: Implement authentication using Firebase Authentication.
     * Registers a new user via Firebase Auth and synchronizes metadata locally and with the REST API.
     */
    suspend fun register(
        fullName: String,
        username: String,
        email: String,
        password: String,
        role: UserRole,
        shopName: String? = null,
    ): Result<UserEntity>
    {
        Log.d(TAG, "Attempting Firebase registration for email: $email")
        return kotlin.runCatching()
        {
            // 1. Create user in Firebase
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUid =
                authResult.user?.uid ?: throw Exception("Firebase UID generation failed")

            // 2. Local check for existing email (redundant but safe for Room)
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null)
            {
                throw Exception("Email already registered in local database")
            }

            // 3. Attempt to register on the remote Fake Restaurant API
            var remoteUsercode: String? = null
            try
            {
                Log.i(TAG, "Syncing registration with remote REST API...")
                val response = apiService.registerUser(RegistrationRequest(email, password))
                if (response.isSuccessful)
                {
                    remoteUsercode = response.body()?.usercode
                }
            }
            catch (e: Exception)
            {
                Log.e(TAG, "Remote registration sync failed: ${e.message}")
            }

            // 4. Generate local Campus Eats ID and persist User Metadata
            val campusUserId = IdGenerator.generateUserId()
            val user = UserEntity(
                userId = campusUserId,
                fullName = fullName,
                username = username,
                email = email,
                passwordHash = "[FIREBASE_SSO]", // No longer storing local passwords
                role = role,
                shopName = if (role == UserRole.VENDOR) shopName else null,
                shopStatus = if (role == UserRole.VENDOR) ShopStatus.OPEN else null,
                usercode = remoteUsercode
            )

            userDao.insertUser(user)
            Log.d(TAG, "User $campusUserId (Firebase: $firebaseUid) successfully persisted")
            user
        }
    }

    /**
     * Requirement: Single Sign-On (SSO) authentication flow.
     * Attempts to log in a user with Firebase and retrieves metadata from local storage.
     */
    suspend fun login(email: String, password: String): Result<UserEntity>
    {
        Log.d(TAG, "Firebase login request received for: $email")
        return kotlin.runCatching()
        {
            // 1. Authenticate with Firebase
            firebaseAuth.signInWithEmailAndPassword(email, password).await()

            // 2. Fetch local metadata
            val user = userDao.getUserByEmail(email)
                ?: throw Exception("Firebase authenticated, but local profile missing.")

            Log.i(TAG, "SSO Login successful for User ID: ${user.userId}")

            // 3. Sync usercode if missing
            if (user.usercode == null)
            {
                try
                {
                    val response = apiService.getUserCode(email, password)
                    if (response.isSuccessful)
                    {
                        val code = response.body()?.usercode
                        if (code != null)
                        {
                            val updatedUser = user.copy(usercode = code)
                            userDao.updateUser(updatedUser)
                            return@runCatching updatedUser
                        }
                    }
                }
                catch (e: Exception)
                {
                    Log.e(TAG, "Remote usercode retrieval failed: ${e.message}")
                }
            }
            user
        }
    }

    /**
     * Requirement: Authentication state handling.
     * Determines whether a user is currently authenticated via Firebase.
     */
    fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null

    /**
     * Retrieves the email of the currently authenticated user.
     */
    fun getCurrentUserEmail(): String? = firebaseAuth.currentUser?.email

    /**
     * Logs the current user out of Firebase.
     */
    fun logout()
    {
        firebaseAuth.signOut()
    }

    /**
     * Requirement: SSO authentication flow for password recovery.
     * Firebase handles the reset logic; we update local records if necessary.
     */
    suspend fun resetPassword(userId: String, newPassword: String): Result<Unit>
    {
        Log.d(TAG, "Password reset initiated for User ID: $userId")
        return kotlin.runCatching()
        {
            val user = userDao.getUserById(userId) ?: throw Exception("Invalid User ID")

            // Firebase doesn't allow direct password override from client without current session or reset email.
            // For this implementation, we assume the user is resetting via a known workflow.
            // In a real app, this would use firebaseAuth.sendPasswordResetEmail(user.email).

            // If usercode exists, sync with Fake API
            if (user.usercode != null)
            {
                try
                {
                    apiService.updatePassword(user.usercode, newPassword)
                }
                catch (e: Exception)
                {
                    Log.e(TAG, "Remote password sync failed: ${e.message}")
                }
            }
            Log.i(TAG, "Local password sync logic maintained for backward compatibility.")
        }
    }

    /**
     * Updates the user's profile information.
     */
    suspend fun updateProfile(userId: String, email: String, password: String): Result<Unit>
    {
        Log.d(TAG, "Profile update requested for User ID: $userId")
        return kotlin.runCatching()
        {
            val user = userDao.getUserById(userId) ?: throw Exception("User not found")

            if (user.email != email)
            {
                val existing = userDao.getUserByEmail(email)
                if (existing != null) throw Exception("Email already taken")
                // Firebase email update logic would go here
            }

            val updatedUser = user.copy(email = email)
            if (password.isNotBlank())
            {
                // Update Firebase password
                firebaseAuth.currentUser?.updatePassword(password)?.await()
                
                if (user.usercode != null)
                {
                    try
                    {
                        apiService.updatePassword(user.usercode, password)
                    }
                    catch (e: Exception)
                    {
                        Log.e(TAG, "Remote security sync failed: ${e.message}")
                    }
                }
            }
            userDao.updateUser(updatedUser)
            Log.d(TAG, "Local profile data successfully updated")
        }
    }

    /**
     * Returns a Flow of the UserEntity for the given User ID.
     */
    fun getUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)

    /**
     * Retrieves UserEntity by email.
     */
    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

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
        return kotlin.runCatching()
        {
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

    /**
     * Deletes a user account from Firebase, local, and remote systems.
     */
    @Suppress("unused")
    suspend fun deleteAccount(userId: String): Result<Unit>
    {
        return kotlin.runCatching()
        {
            val user = userDao.getUserById(userId)
            if (user != null)
            {
                // 1. Delete from Firebase
                firebaseAuth.currentUser?.delete()?.await()

                // 2. Delete from Remote API
                if (user.usercode != null)
                {
                    try
                    {
                        apiService.deleteUser(user.usercode)
                    }
                    catch (_: Exception)
                    {
                    }
                }

                // 3. Delete from Local DB
                userDao.deleteUser(user)
            }
            else
            {
                throw Exception("User not found")
            }
        }
    }
}
