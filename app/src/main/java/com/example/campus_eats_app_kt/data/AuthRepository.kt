package com.example.campus_eats_app_kt.data

import android.util.Log
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import com.example.campus_eats_app_kt.data.network.FakeRestaurantApiService
import com.example.campus_eats_app_kt.data.network.RegistrationRequest
import com.example.campus_eats_app_kt.util.IdGenerator
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.example.campus_eats_app_kt.util.ValidationEngine
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.GoogleAuthProvider
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
import java.security.SecureRandom
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
    @Suppress("UNUSED_PARAMETER") ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
)
{
    private val tag = "AuthRepository"



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
        return@coroutineScope runCatching()
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

            // Finding 1: Prevention of unauthorized role promotion
            if (role == UserRole.ADMINISTRATOR)
            {
                throw Exception("Self-registration as Administrator is strictly prohibited.")
            }

            if (userDao.getUserByEmail(email) != null)
            {
                throw Exception("An account with this email is already registered locally.")
            }

            // 1. Firebase Auth Account Creation
            val firebaseUser = try
            {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                result.user ?: throw Exception("Firebase user creation failed.")
            }
            catch (e: Exception)
            {
                throw Exception(FirebaseExceptionHandler.parse(e))
            }

            // 2. Parallel Remote API Synchronization (External "Fake Restaurant" provider)
            // Finding 4: Protect user credentials by using a per-user random secret for 3rd party sync.
            val apiSecret = generateRandomSecret()
            val apiSyncDeferred = async()
            {
                try
                {
                    val response = apiService.registerUser(RegistrationRequest(email, apiSecret))
                    if (response.isSuccessful) response.body()?.usercode else null
                }
                catch (_: Exception)
                {
                    null
                }
            }

            val remoteUsercode = apiSyncDeferred.await()

            // 3. Construct the Authoritative User Entity
            val campusUserId = IdGenerator.generateUserId()
            val user = UserEntity(
                userId = campusUserId,
                fullName = fullName,
                username = username,
                email = email,
                passwordHash = "[FIREBASE_AUTH]", // Passwords are NOT stored in our database.
                role = role,
                shopName = if (role == UserRole.VENDOR) shopName else null,
                shopStatus = if (role == UserRole.VENDOR) ShopStatus.OPEN else null,
                usercode = remoteUsercode,
            )

            // 4. Persistence - Cloud (Authoritative)
            try
            {
                // Finding 6: Use explicit Map mapping for Firebase to avoid serialization issues
                firebaseDatabase.getReference("users").child(campusUserId)
                    .setValue(mapToFirebase(user))
                    .await()
            }
            catch (e: Exception)
            {
                // Atomic cleanup: If RTDB write fails, remove the Auth account to prevent orphaned identities
                firebaseUser.delete().await()
                throw Exception("Cloud synchronization failed. Please try again.")
            }

            // 5. Persistence - Local (Cache)
            userDao.insertUser(user)
            user
        }
    }

    /**
     * Registers a new user via Google SSO.
     */
    suspend fun registerWithGoogle(
        idToken: String,
        role: UserRole,
        shopName: String? = null,
    ): Result<UserEntity> = coroutineScope()
    {
        Log.d(tag, "Initiating Google SSO registration")
        return@coroutineScope runCatching()
        {
            connectivityManager.ensureInternet()

            if (role == UserRole.ADMINISTRATOR)
            {
                throw Exception("Self-registration as Administrator is strictly prohibited.")
            }

            // 1. Firebase Authentication with Google Credential
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google Registration failed: User is null.")
            val email = firebaseUser.email ?: throw Exception("Google Registration failed: Email not provided.")

            // Check if user already exists in RTDB or local cache
            val existingUser = try { resolveUserRecord(email) } catch (_: Exception) { null }
            existingUser?.let { return@runCatching it }

            // 2. Parallel Remote API Synchronization
            val apiSecret = generateRandomSecret()
            val apiSyncDeferred = async()
            {
                try
                {
                    val response = apiService.registerUser(RegistrationRequest(email, apiSecret))
                    if (response.isSuccessful) response.body()?.usercode else null
                }
                catch (_: Exception)
                {
                    null
                }
            }

            val remoteUsercode = apiSyncDeferred.await()

            // 3. Construct the User Entity
            val campusUserId = IdGenerator.generateUserId()
            val user = UserEntity(
                userId = campusUserId,
                fullName = firebaseUser.displayName ?: "Google User",
                username = email.substringBefore("@"),
                email = email,
                passwordHash = "[FIREBASE_SSO]",
                role = role,
                shopName = if (role == UserRole.VENDOR) shopName else null,
                shopStatus = if (role == UserRole.VENDOR) ShopStatus.OPEN else null,
                usercode = remoteUsercode,
            )

            // 4. Persistence - Cloud then Local
            try
            {
                firebaseDatabase.getReference("users").child(campusUserId)
                    .setValue(mapToFirebase(user))
                    .await()
            }
            catch (e: Exception)
            {
                firebaseUser.delete().await()
                throw Exception("Cloud synchronization failed during Google Registration.")
            }

            userDao.insertUser(user)
            user
        }
    }

    /**
     * Authenticates a user and restores their account state.
     * 
     * Finding 2: Security hardening - Removal of the unvalidated local password bypass.
     * All logins must now be validated against Firebase Authentication.
     */
    suspend fun login(email: String, password: String): Result<UserEntity>
    {
        Log.d(tag, "Login attempt for: $email")
        return runCatching()
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

            // 2. Resolve Application User Record (Restores cache if missing)
            resolveUserRecord(email)
        }
    }

    /**
     * Authenticates a user with a Google ID Token.
     */
    suspend fun signInWithGoogle(idToken: String): Result<UserEntity>
    {
        Log.d(tag, "Initiating Google SSO exchange")
        return runCatching()
        {
            connectivityManager.ensureInternet()

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google SSO failed: User is null.")
            val email = firebaseUser.email ?: throw Exception("Google SSO failed: Email not provided.")

            resolveUserRecord(email)
        }
    }

    /**
     * Helper to resolve the application user record from RTDB or Cache.
     * Finding 6: Explicit Map mapping to handle Serialization and Role naming.
     */
    private suspend fun resolveUserRecord(email: String): UserEntity
    {
        var user = userDao.getUserByEmail(email)

        // Cross-Device Restoration Logic
        if (user == null)
        {
            Log.i(tag, "Local profile missing. Restoring from Firebase Realtime Database...")
            val snapshot = firebaseDatabase.getReference("users")
                .orderByChild("email")
                .equalTo(email)
                .get()
                .await()

            val map = snapshot.children.firstOrNull()?.value as? Map<Any?, Any?>
            user = map?.let { mapFromFirebase(it) }
            
            if (user != null)
            {
                Log.d(tag, "Profile restored for User ID: ${user.userId}")
                userDao.insertUser(user)
            }
        }

        return user ?: throw Exception("Profile record not found in system.")
    }

    /**
     * Finding 6: Mapping logic for Firebase RTDB compatibility.
     * Enforces the "ADMIN" role name in the database while using UserRole.ADMINISTRATOR in app.
     */
    private fun mapToFirebase(user: UserEntity): Map<String, Any?>
    {
        return mapOf(
            "userId" to user.userId,
            "fullName" to user.fullName,
            "username" to user.username,
            "email" to user.email,
            "passwordHash" to user.passwordHash,
            "role" to when (user.role)
            {
                UserRole.ADMINISTRATOR -> "ADMIN"
                else -> user.role.name
            },
            "status" to user.status.name,
            "walletBalance" to user.walletBalance,
            "shopName" to user.shopName,
            "shopStatus" to user.shopStatus?.name,
            "bankAccountInfo" to user.bankAccountInfo,
            "registrationDate" to user.registrationDate,
            "usercode" to user.usercode,
        )
    }

    private fun mapFromFirebase(map: Map<Any?, Any?>): UserEntity
    {
        val stringMap = map.mapKeys { it.key.toString() }
        return UserEntity(
            userId = stringMap["userId"] as? String ?: "",
            fullName = stringMap["fullName"] as? String ?: "",
            username = stringMap["username"] as? String ?: "",
            email = stringMap["email"] as? String ?: "",
            passwordHash = stringMap["passwordHash"] as? String ?: "[FIREBASE_SSO]",
            role = when (stringMap["role"] as? String)
            {
                "ADMIN" -> UserRole.ADMINISTRATOR
                "VENDOR" -> UserRole.VENDOR
                "STUDENT" -> UserRole.STUDENT
                "STANDARD" -> UserRole.STANDARD
                else -> UserRole.STANDARD
            },
            status = try { UserStatus.valueOf(stringMap["status"] as? String ?: "ACTIVE") } catch (_: Exception) { UserStatus.ACTIVE },
            walletBalance = (stringMap["walletBalance"] as? Number)?.toDouble() ?: 0.0,
            shopName = stringMap["shopName"] as? String,
            shopStatus = (stringMap["shopStatus"] as? String)?.let { try { ShopStatus.valueOf(it) } catch (_: Exception) { null } },
            bankAccountInfo = stringMap["bankAccountInfo"] as? String,
            registrationDate = (stringMap["registrationDate"] as? Number)?.toLong() ?: 0L,
            usercode = stringMap["usercode"] as? String,
        )
    }

    /**
     * Periodic background synchronization.
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
            val tokenResult: GetTokenResult = user.getIdToken(false).await()
            tokenResult.claims["admin"] == true
        }
        catch (e: Exception)
        {
            Log.e(tag, "Failed to check admin claims: ${e.message}")
            false
        }
    }

    fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null
    fun getCurrentUserEmail(): String? = firebaseAuth.currentUser?.email
    fun logout() = firebaseAuth.signOut()

    /**
     * Account recovery flow.
     * Finding 7: Corrected to handle signed-out password resets via email.
     */
    suspend fun sendRecoveryEmail(email: String): Result<Unit>
    {
        return runCatching()
        {
            connectivityManager.ensureInternet()
            firebaseAuth.sendPasswordResetEmail(email).await()
        }
    }

    /**
     * Internal password update (requires recent login).
     */
    suspend fun resetPassword(userId: String, newPassword: String): Result<Unit>
    {
        Log.d(tag, "Password reset initiated for User ID: $userId")
        return runCatching()
        {
            connectivityManager.ensureInternet()
            
            val currentUser = firebaseAuth.currentUser ?: throw Exception("You must be signed in to change your password.")
            
            try
            {
                currentUser.updatePassword(newPassword).await()
            }
            catch (e: Exception)
            {
                throw Exception(FirebaseExceptionHandler.parse(e))
            }
            
            // Password sync to 3rd party is deprecated in v3.1.0 for security compliance.
            // We only update the local cache status if needed.
        }
    }

    private fun generateRandomSecret(): String
    {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..16)
            .map { SecureRandom().nextInt(charPool.size) }
            .map(charPool::get)
            .joinToString("")
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
        return runCatching()
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
     */
    suspend fun updateProfile(
        userId: String,
        fullName: String,
        username: String,
        newPassword: String? = null,
    ): Result<Unit>
    {
        return runCatching()
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
        return runCatching()
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
