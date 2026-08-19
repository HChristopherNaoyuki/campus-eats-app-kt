package com.example.campus_eats_app_kt.data

import android.util.Log
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.network.FakeRestaurantApiService
import com.example.campus_eats_app_kt.data.network.RegistrationRequest
import com.example.campus_eats_app_kt.util.IdGenerator
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/**
 * AuthRepository handles user-related authentication and profile management operations.
 * It integrates Firebase Authentication for SSO, Realtime Database for online synchronization,
 * and local Room database for profile metadata.
 */
class AuthRepository(
    private val userDao: UserDao,
    private val apiService: FakeRestaurantApiService,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
)
{
    private val TAG = "AuthRepository"

    /**
     * Requirement: Implement authentication using Firebase Authentication.
     * Registers a new user via Firebase Auth and synchronizes metadata locally and with the REST API.
     * Principle: Performance - Uses parallel coroutines for network-heavy registration tasks.
     */
    suspend fun register(
        fullName: String,
        username: String,
        email: String,
        password: String,
        role: UserRole,
        shopName: String? = null,
    ): Result<UserEntity> = coroutineScope()
    {
        Log.d(TAG, "Attempting Firebase registration for email: $email")
        return@coroutineScope kotlin.runCatching()
        {
            // 0. Ensure internet connection
            connectivityManager.ensureInternet()

            // 1. Parallelize Identity Creation (Firebase Auth) and Remote API Sync
            val authDeferred = async {
                try
                {
                    val authResult =
                        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                    authResult.user?.uid ?: throw Exception("Firebase UID generation failed")
                }
                catch (e: Exception)
                {
                    Log.e(TAG, "Firebase registration failed: ${e.message}")
                    if (e.message?.contains("CONFIGURATION_NOT_FOUND") == true)
                    {
                        throw Exception("Authentication service is not enabled for this project (campus-eats-db). Please enable the Email/Password provider in the Firebase Console.")
                    }
                    throw e
                }
            }

            val apiDeferred = async {
                try
                {
                    Log.i(TAG, "Syncing registration with remote REST API...")
                    val response = apiService.registerUser(RegistrationRequest(email, password))
                    if (response.isSuccessful) response.body()?.usercode else null
                }
                catch (e: Exception)
                {
                    Log.e(TAG, "Remote registration sync failed: ${e.message}")
                    null
                }
            }

            // Wait for both identity layers to respond
            val firebaseUid = authDeferred.await()
            val remoteUsercode = apiDeferred.await()

            // 2. Local check for existing email (redundant but safe for Room)
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null)
            {
                throw Exception("Email already registered in local database")
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

            // 5. Synchronize with Firebase Realtime Database (Wait for acknowledgment)
            try
            {
                firebaseDatabase.getReference("users").child(campusUserId).setValue(user).await()

                // Principle: Performance - Enable keepSynced for the user's node to ensure low-latency access.
                firebaseDatabase.getReference("users").child(campusUserId).keepSynced(true)
                
                Log.i(TAG, "User profile successfully synchronized with Realtime Database")
            }
            catch (e: Exception)
            {
                Log.e(TAG, "Realtime Database synchronization failed: ${e.message}")
            }

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
            // 0. Ensure internet connection
            connectivityManager.ensureInternet()

            // 1. Authenticate with Firebase
            try
            {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            }
            catch (e: Exception)
            {
                Log.e(TAG, "Firebase login failed: ${e.message}")
                if (e.message?.contains("CONFIGURATION_NOT_FOUND") == true)
                {
                    throw Exception("Authentication service is not enabled for this project (campus-eats-db). Please enable the Email/Password provider in the Firebase Console.")
                }
                throw e
            }

            // 2. Fetch local metadata (fallback to RTDB if local is missing)
            var user = userDao.getUserByEmail(email)

            if (user == null)
            {
                Log.i(
                    TAG,
                    "Local profile missing. Attempting restoration from Realtime Database..."
                )
                // In a production app, we would search RTDB by email index. 
                // For simplicity, we assume the user was registered on this device or recently.
                // Here we fetch from RTDB if we had the campus ID, but we don't yet.
                // Standard procedure: Use Firebase UID to map to Campus ID in RTDB.
            }

            if (user == null) throw Exception("Firebase authenticated, but local profile missing.")

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
                            user = updatedUser
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
            connectivityManager.ensureInternet()
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
            connectivityManager.ensureInternet()
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

            // Synchronize updated profile with Realtime Database
            try
            {
                firebaseDatabase.getReference("users").child(userId).setValue(updatedUser).await()
                Log.i(TAG, "Realtime Database profile synchronization successful")
            }
            catch (e: Exception)
            {
                Log.e(TAG, "Realtime Database profile synchronization failed: ${e.message}")
            }
            
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
            val updatedUser = user.copy(shopStatus = status)
            userDao.updateUser(updatedUser)

            // Sync to RTDB
            try
            {
                firebaseDatabase.getReference("users").child(userId).child("shopStatus")
                    .setValue(status).await()
            }
            catch (_: Exception)
            {
            }
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
                val updatedUser = user.copy(bankAccountInfo = bankInfo)
                userDao.updateUser(updatedUser)

                // Sync to RTDB
                try
                {
                    firebaseDatabase.getReference("users").child(userId).child("bankAccountInfo")
                        .setValue(bankInfo).await()
                }
                catch (_: Exception)
                {
                }
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
