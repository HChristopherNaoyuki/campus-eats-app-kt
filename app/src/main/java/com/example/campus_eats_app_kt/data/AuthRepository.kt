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
import com.example.campus_eats_app_kt.util.ValidationEngine
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.seconds

/**
 * AuthRepository manages the authentication lifecycle and profile synchronization.
 * Optimized for low-latency registration and login (Target: < 3s).
 */
class AuthRepository(
    private val userDao: UserDao,
    private val apiService: FakeRestaurantApiService,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
)
{
    private val tag = "AuthRepository"
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Requirement: Registration process completes within 3 seconds.
     * Parallelizes Firebase Auth and Remote REST API sync.
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
        Log.d(tag, "Initiating optimized registration for: $email")
        return@coroutineScope kotlin.runCatching()
        {
            connectivityManager.ensureInternet()

            // 0. Pre-validation: Catch malformed data before it reaches Firebase
            if (!ValidationEngine.isValidEmail(email))
            {
                throw Exception("Invalid email format provided.")
            }
            if (!ValidationEngine.isStrongPassword(password))
            {
                throw Exception("Password must be at least 8 characters long.")
            }

            // 1. Fast local check to avoid unnecessary network calls
            if (userDao.getUserByEmail(email) != null)
            {
                throw Exception("An account with this email is already registered locally.")
            }

            // 2. Parallel Network Operations
            val authDeferred = async()
            {
                try
                {
                    val result =
                        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                    result.user?.uid ?: throw Exception("Firebase UID error")
                }
                catch (e: Exception)
                {
                    // Map technical error to user-friendly string
                    throw Exception(FirebaseExceptionHandler.parse(e))
                }
            }

            val apiSyncDeferred = async()
            {
                try
                {
                    val response = apiService.registerUser(RegistrationRequest(email, password))
                    if (response.isSuccessful) response.body()?.usercode else null
                }
                catch (_: Exception)
                {
                    null
                }
            }

            val firebaseUid = try
            {
                authDeferred.await()
            }
            catch (e: Exception)
            {
                throw e
            }
            val remoteUsercode = apiSyncDeferred.await()

            // 3. Construct and Persist Data
            val campusUserId = IdGenerator.generateUserId()
            val user = UserEntity(
                userId = campusUserId,
                fullName = fullName,
                username = username,
                email = email,
                passwordHash = "[FIREBASE_SSO]",
                role = role,
                shopName = if (role == UserRole.VENDOR) shopName else null,
                shopStatus = if (role == UserRole.VENDOR) ShopStatus.OPEN else null,
                usercode = remoteUsercode,
            )

            // 4. Background Sync to RTDB (True Fire and Forget)
            repositoryScope.launch()
            {
                try
                {
                    firebaseDatabase.getReference("users").child(campusUserId).setValue(user)
                        .await()
                    firebaseDatabase.getReference("users").child(campusUserId).keepSynced(true)
                }
                catch (e: Exception)
                {
                    Log.e(tag, "RTDB Sync failed for $campusUserId: ${e.message}")
                }
            }

            userDao.insertUser(user)
            Log.d(tag, "User $campusUserId (Firebase: $firebaseUid) successfully persisted")
            user
        }
    }

    /**
     * Requirement: Login process completes within 3 seconds.
     * Uses sequential execution for reliability; Room and Firebase Auth are high-performance.
     */
    suspend fun login(email: String, password: String): Result<UserEntity>
    {
        Log.d(tag, "Initiating optimized login for: $email")
        return kotlin.runCatching()
        {
            connectivityManager.ensureInternet()

            // 0. Pre-validation
            if (!ValidationEngine.isValidEmail(email))
            {
                throw Exception("Please enter a valid email address.")
            }

            // 1. Authenticate with Firebase
            try
            {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            }
            catch (e: Exception)
            {
                Log.e(tag, "Firebase login failed: ${e.message}")
                throw Exception(FirebaseExceptionHandler.parse(e))
            }

            // 2. Fetch local metadata
            var user = userDao.getUserByEmail(email)

            // 3. Reliability: Restore profile if local DB was cleared
            if (user == null)
            {
                Log.i(tag, "Local profile missing. Querying RTDB...")
                val snapshot = firebaseDatabase.getReference("users")
                    .orderByChild("email")
                    .equalTo(email)
                    .get()
                    .await()

                user = snapshot.children.firstOrNull()?.getValue(UserEntity::class.java)
                user?.let { userDao.insertUser(it) }
            }

            if (user == null)
            {
                throw Exception("Profile metadata missing in cloud. Account may be partially initialized.")
            }

            user
        }
    }

    /**
     * Requirement: Synchronize permitted data with the database every 10 seconds.
     */
    fun startBackgroundSync(userId: String, scope: CoroutineScope)
    {
        scope.launch()
        {
            while (isActive)
            {
                delay(10.seconds)
                try
                {
                    if (connectivityManager.hasInternetConnection())
                    {
                        userDao.getUserById(userId)?.let { user ->
                            firebaseDatabase.getReference("users").child(userId).setValue(user)
                        }
                    }
                }
                catch (_: Exception)
                {
                }
            }
        }
    }

    fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null
    fun getCurrentUserEmail(): String? = firebaseAuth.currentUser?.email
    fun logout() = firebaseAuth.signOut()

    /**
     * Re-authenticates the current user. Required for sensitive operations like 
     * password changes or account deletion if the session has expired.
     */
    suspend fun reauthenticate(password: String): Result<Unit>
    {
        return kotlin.runCatching()
        {
            try
            {
                val email = getCurrentUserEmail() ?: throw Exception("No active user session found.")
                val credential = EmailAuthProvider.getCredential(email, password)
                firebaseAuth.currentUser?.reauthenticate(credential)?.await()
                Log.d(tag, "User successfully re-authenticated.")
            }
            catch (e: Exception)
            {
                Log.e(tag, "Re-authentication failed: ${e.message}")
                throw Exception(FirebaseExceptionHandler.parse(e))
            }
        }
    }

    suspend fun resetPassword(userId: String, newPassword: String): Result<Unit>
    {
        Log.d(tag, "Password reset initiated for User ID: $userId")
        return kotlin.runCatching()
        {
            connectivityManager.ensureInternet()
            val user = userDao.getUserById(userId) ?: throw Exception("Invalid User ID")
            
            // Note: Firebase password reset usually via email, but we allow forced update here
            firebaseAuth.currentUser?.updatePassword(newPassword)?.await()
            
            if (user.usercode != null)
            {
                try
                {
                    apiService.updatePassword(user.usercode, newPassword)
                }
                catch (e: Exception)
                {
                    Log.e(tag, "Remote password sync failed: ${e.message}")
                }
            }
        }.onFailure { e ->
            throw Exception(FirebaseExceptionHandler.parse(e))
        }
    }

    suspend fun updateProfile(userId: String, email: String, password: String): Result<Unit>
    {
        Log.d(tag, "Profile update requested for User ID: $userId")
        return kotlin.runCatching()
        {
            connectivityManager.ensureInternet()
            val user = userDao.getUserById(userId) ?: throw Exception("User not found")
            val updatedUser = user.copy(email = email)
            if (password.isNotBlank())
            {
                try
                {
                    firebaseAuth.currentUser?.updatePassword(password)?.await()
                }
                catch (e: Exception)
                {
                    // If re-auth is needed, the exception is caught here and mapped
                    throw Exception(FirebaseExceptionHandler.parse(e))
                }

                if (user.usercode != null)
                {
                    try
                    {
                        apiService.updatePassword(user.usercode, password)
                    }
                    catch (e: Exception)
                    {
                        Log.e(tag, "Remote security sync failed: ${e.message}")
                    }
                }
            }
            userDao.updateUser(updatedUser)
            firebaseDatabase.getReference("users").child(userId).setValue(updatedUser).await()
            Log.d(tag, "Local and Cloud profile data successfully updated")
        }
    }

    fun getUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)
    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    suspend fun updateShopStatus(userId: String, status: ShopStatus): Result<Unit>
    {
        return kotlin.runCatching()
        {
            val user = userDao.getUserById(userId)
            if ((user != null) && (user.role == UserRole.VENDOR))
            {
                val updatedUser = user.copy(shopStatus = status)
                userDao.updateUser(updatedUser)
                try
                {
                    firebaseDatabase.getReference("users").child(userId).child("shopStatus")
                        .setValue(status).await()
                }
                catch (e: Exception)
                {
                    Log.e(tag, "Failed to sync shop status to RTDB: ${e.message}")
                }
            }
            else
            {
                throw Exception("User not found or not a vendor")
            }
        }
    }

    suspend fun linkBankAccount(userId: String, bankInfo: String): Result<Unit>
    {
        return kotlin.runCatching()
        {
            val user = userDao.getUserById(userId) ?: throw Exception("User not found")
            val updatedUser = user.copy(bankAccountInfo = bankInfo)
            userDao.updateUser(updatedUser)
            try
            {
                firebaseDatabase.getReference("users").child(userId).child("bankAccountInfo")
                    .setValue(bankInfo).await()
            }
            catch (e: Exception)
            {
                Log.e(tag, "Failed to sync bank info to RTDB: ${e.message}")
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
                try
                {
                    firebaseAuth.currentUser?.delete()?.await()
                }
                catch (e: Exception)
                {
                    throw Exception(FirebaseExceptionHandler.parse(e))
                }

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
                throw Exception("User not found locally. Account may already be deleted.")
            }
        }
    }
}
