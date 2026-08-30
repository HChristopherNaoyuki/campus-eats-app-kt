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
 * AuthRepository manages the authentication lifecycle, cross-device account access, 
 * and profile synchronization between local Room cache and Firebase Realtime Database.
 * 
 * Architecture Principles:
 * 1. Authoritative Identity: Firebase Authentication.
 * 2. Authoritative Data: Firebase Realtime Database.
 * 3. Performance/Offline: Room Database (Local Cache).
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

    /**
     * repositoryScope is used for background synchronization tasks that should 
     * outlive the immediate UI operation but are bound to the repository's lifecycle.
     */
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    /**
     * Registers a new user.
     * 
     * Requirement: User email must match auth.token.email.
     * Requirement: userId must be exactly 19 characters (XXXX-XXXX-XXXX-XXXX).
     * Security: ADMIN role selection is verified by Firebase Security Rules via custom claims.
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
        Log.d(tag, "Initiating registration for: $email with role: ${role.name}")
        return@coroutineScope kotlin.runCatching()
        {
            connectivityManager.ensureInternet()

            // Pre-validation to avoid unnecessary network calls
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

            // 1. Firebase Auth Account Creation
            // Credentials are encrypted and handled exclusively by the Firebase SDK.
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

            // 2. Parallel Remote API Synchronization (External "Fake Restaurant" provider)
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

            // 3. Construct the Authoritative User Entity
            // Note: campusUserId is the primary key in both Room and Realtime Database.
            val campusUserId = IdGenerator.generateUserId()
            val user = UserEntity(
                userId = campusUserId,
                fullName = fullName,
                username = username,
                email = email,
                passwordHash = "[FIREBASE_SSO]", // Security: Passwords are NOT stored in RTDB.
                role = role,
                shopName = if (role == UserRole.VENDOR) shopName else null,
                shopStatus = if (role == UserRole.VENDOR) ShopStatus.OPEN else null,
                usercode = remoteUsercode,
            )

            // 4. Persistence - Cloud (Authoritative) then Local (Cache)
            repositoryScope.launch()
            {
                try
                {
                    // Security: This write will be rejected if UserRole.ADMIN is selected 
                    // and the user lacks the 'admin' custom claim.
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
     * Authenticates a user and restores their account state.
     * 
     * Requirement: Cross-device access. If a user logs in on a new device, 
     * their profile is retrieved from Firebase Realtime Database and cached in Room.
     */
    suspend fun login(email: String, password: String): Result<UserEntity>
    {
        Log.d(tag, "Login attempt for: $email")
        return kotlin.runCatching()
        {
            connectivityManager.ensureInternet()

            // 1. Firebase Authentication (Auth Identity)
            try
            {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            }
            catch (e: Exception)
            {
                throw Exception(FirebaseExceptionHandler.parse(e))
            }

            // 2. Resolve Application User Record
            var user = userDao.getUserByEmail(email)

            // 3. Cross-Device Restoration Logic
            if (user == null)
            {
                Log.i(tag, "Local profile missing. Restoring from Firebase Realtime Database...")
                val snapshot = firebaseDatabase.getReference("users")
                    .orderByChild("email")
                    .equalTo(email)
                    .get()
                    .await()

                user = snapshot.children.firstOrNull()?.getValue(UserEntity::class.java)
                
                if (user != null)
                {
                    Log.d(tag, "Profile restored for User ID: ${user.userId}")
                    userDao.insertUser(user)
                }
            }

            user ?: throw Exception("Profile record not found in system.")
        }
    }

    /**
     * Periodic background synchronization.
     * Synchronizes non-restricted fields from local cache to cloud to ensure multi-device consistency.
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
                            // Requirement: Selective update. 
                            // Avoid syncing immutable fields (email, role) to prevent rule violations.
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
     * Authority: Firebase Authentication Token Claims.
     */
    suspend fun isAdmin(): Boolean
    {
        return try
        {
            val user = firebaseAuth.currentUser ?: return false
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
     * Essential for administrative authorization compliance when roles are granted.
     */
    @Suppress("unused")
    suspend fun refreshUserClaims()
    {
        try
        {
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

    /**
     * Resets the user's password.
     * Note: This operation requires a recent login session in Firebase.
     */
    suspend fun resetPassword(userId: String, newPassword: String): Result<Unit>
    {
        Log.d(tag, "Password reset initiated for User ID: $userId")
        return kotlin.runCatching()
        {
            connectivityManager.ensureInternet()
            val user = userDao.getUserById(userId) ?: throw Exception("Invalid User ID")
            
            val currentUser = firebaseAuth.currentUser ?: throw Exception("You must be signed in to change your password.")
            
            try
            {
                currentUser.updatePassword(newPassword).await()
            }
            catch (e: Exception)
            {
                throw Exception(FirebaseExceptionHandler.parse(e))
            }
            
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

    /**
     * Re-authenticates the current user. Required for sensitive operations like 
     * password changes or account deletion if the session has expired.
     */
    @Suppress("unused")
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
     * Retrieves a user by their email address from the local cache.
     */
    suspend fun getUserByEmail(email: String): UserEntity?
    {
        return userDao.getUserByEmail(email)
    }

    /**
     * Links bank account info while syncing to Realtime Database.
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

            val updatedUser = user.copy(fullName = fullName, username = username)
            userDao.updateUser(updatedUser)
            
            val updates = mapOf<String, Any>(
                "fullName" to fullName,
                "username" to username,
            )
            
            firebaseDatabase.getReference("users").child(userId)
                .updateChildren(updates).await()
        }
    }

    /**
     * Synchronizes shop availability status for vendors.
     */
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

    /**
     * Provides a reactive stream of the user record for UI observers.
     */
    fun getUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)
}
