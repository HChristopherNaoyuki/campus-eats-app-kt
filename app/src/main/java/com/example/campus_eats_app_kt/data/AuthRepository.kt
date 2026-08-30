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
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineDispatcher
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
 * Enforces compliance with Firebase Realtime Database security rules.
 */
class AuthRepository(
    private val userDao: UserDao,
    private val apiService: FakeRestaurantApiService,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
)
{
    private val tag = "AuthRepository"

    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    /**
     * Registers a new user. 
     * Requirement: User email must match auth.token.email.
     * Requirement: userId must be exactly 19 characters.
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
        Log.d(tag, "Initiating registration for: $email")
        return@coroutineScope kotlin.runCatching()
        {
            connectivityManager.ensureInternet()

            if (!ValidationEngine.isValidEmail(email))
            {
                throw Exception("Invalid email format provided.")
            }
            if (!ValidationEngine.isStrongPassword(password))
            {
                throw Exception("Password must be at least 8 characters long.")
            }

            if (userDao.getUserByEmail(email) != null)
            {
                throw Exception("An account with this email is already registered locally.")
            }

            // 1. Firebase Auth Creation
            val authDeferred = async()
            {
                try
                {
                    val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                    result.user?.uid ?: throw Exception("Firebase UID generation failed.")
                }
                catch (e: Exception)
                {
                    throw Exception(FirebaseExceptionHandler.parse(e))
                }
            }

            // 2. Remote API Sync
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

            authDeferred.await()
            val remoteUsercode = apiSyncDeferred.await()

            // 3. Construct Entity with 19-character ID
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

            // 4. Sync to RTDB
            repositoryScope.launch()
            {
                try
                {
                    // Ensure the initial write complies with rules (email matches auth token)
                    firebaseDatabase.getReference("users").child(campusUserId).setValue(user)
                        .await()
                }
                catch (e: Exception)
                {
                    Log.e(tag, "RTDB Registration Sync failed: ${e.message}")
                }
            }

            userDao.insertUser(user)
            user
        }
    }

    /**
     * Authenticates existing user.
     */
    suspend fun login(email: String, password: String): Result<UserEntity>
    {
        return kotlin.runCatching()
        {
            connectivityManager.ensureInternet()

            try
            {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            }
            catch (e: Exception)
            {
                throw Exception(FirebaseExceptionHandler.parse(e))
            }

            var user = userDao.getUserByEmail(email)

            if (user == null)
            {
                val snapshot = firebaseDatabase.getReference("users")
                    .orderByChild("email")
                    .equalTo(email)
                    .get()
                    .await()

                user = snapshot.children.firstOrNull()?.getValue(UserEntity::class.java)
                user?.let { userDao.insertUser(it) }
            }

            user ?: throw Exception("Profile metadata missing in cloud.")
        }
    }

    /**
     * Background sync with restricted field awareness.
     * Only updates fields that normal users are permitted to change.
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
                    if ((firebaseAuth.currentUser != null) && connectivityManager.hasInternetConnection())
                    {
                        userDao.getUserById(userId)?.let()
                        { user ->
                            val updates = mapOf<String, Any?>(
                                "fullName" to user.fullName,
                                "username" to user.username,
                                "shopName" to user.shopName,
                                "shopStatus" to user.shopStatus?.name,
                                "bankAccountInfo" to user.bankAccountInfo,
                            )
                            
                            firebaseDatabase.getReference("users").child(userId)
                                .updateChildren(updates).await()
                        }
                    }
                }
                catch (e: Exception)
                {
                    Log.w(tag, "Periodic background sync failed: ${e.message}")
                }
            }
        }
    }

    /**
     * Checks if the current user has administrative privileges via custom claims.
     */
    suspend fun isAdmin(): Boolean
    {
        return try
        {
            val user = firebaseAuth.currentUser ?: return false
            // Use getIdToken(false) which returns Task<GetTokenResult> in modern Firebase SDKs
            val tokenResult: GetTokenResult = user.getIdToken(false).await()
            tokenResult.claims["admin"] == true
        }
        catch (e: Exception)
        {
            Log.e(tag, "Failed to check admin claims: ${e.message}")
            false
        }
    }

    /**
     * Refreshes the current user's token to pick up new custom claims.
     */
    suspend fun refreshUserClaims()
    {
        try
        {
            // Forcing refresh to ensure latest claims are retrieved
            firebaseAuth.currentUser?.getIdToken(true)?.await()
        }
        catch (e: Exception)
        {
            Log.e(tag, "Failed to refresh user token: ${e.message}")
        }
    }

    fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null
    fun getCurrentUserEmail(): String? = firebaseAuth.currentUser?.email
    fun logout() = firebaseAuth.signOut()

    suspend fun reauthenticate(password: String): Result<Unit>
    {
        return kotlin.runCatching()
        {
            val email = getCurrentUserEmail() ?: throw Exception("No active session.")
            val credential = EmailAuthProvider.getCredential(email, password)
            firebaseAuth.currentUser?.reauthenticate(credential)?.await()
        }
    }

    /**
     * Requirement: Reliability - Restore profile if local DB was cleared.
     */
    suspend fun getUserByEmail(email: String): UserEntity?
    {
        return userDao.getUserByEmail(email)
    }

    /**
     * Requirement: Security - Links bank account info while syncing to RTDB.
     */
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
     * Updates user profile while respecting immutable field rules.
     * Password changes are applied to Firebase Auth only; the database stores 
     * a constant placeholder to indicate SSO usage.
     */
    suspend fun updateProfile(
        userId: String,
        fullName: String,
        username: String,
        newPassword: String? = null,
    ): Result<Unit>
    {
        return kotlin.runCatching()
        {
            connectivityManager.ensureInternet()
            val user = userDao.getUserById(userId) ?: throw Exception("User not found")
            
            // 1. Update Password in Firebase Auth if provided
            if (!newPassword.isNullOrBlank())
            {
                try
                {
                    firebaseAuth.currentUser?.updatePassword(newPassword)?.await()
                }
                catch (e: Exception)
                {
                    throw Exception(FirebaseExceptionHandler.parse(e))
                }
            }

            // 2. Update local profile (email, role, status are immutable for users)
            val updatedUser = user.copy(fullName = fullName, username = username)
            userDao.updateUser(updatedUser)
            
            // 3. Sync permitted fields to RTDB
            val updates = mapOf<String, Any>(
                "fullName" to fullName,
                "username" to username
            )
            
            firebaseDatabase.getReference("users").child(userId)
                .updateChildren(updates).await()
        }
    }

    suspend fun updateShopStatus(userId: String, status: ShopStatus): Result<Unit>
    {
        return kotlin.runCatching()
        {
            val user = userDao.getUserById(userId)
            if ((user != null) && (user.role == UserRole.VENDOR))
            {
                val updatedUser = user.copy(shopStatus = status)
                userDao.updateUser(updatedUser)
                
                firebaseDatabase.getReference("users").child(userId).child("shopStatus")
                    .setValue(status).await()
            }
            else
            {
                throw Exception("Invalid vendor profile.")
            }
        }
    }

    fun getUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)
}
