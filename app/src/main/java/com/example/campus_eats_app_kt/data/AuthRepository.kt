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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * AuthRepository manages the authentication lifecycle and profile synchronization.
 * Hardened in Batch 2 and 3 to enforce account suspension and Online-First sync.
 */
class AuthRepository(
    private val userDao: UserDao,
    private val apiService: FakeRestaurantApiService,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val orderRepository: OrderRepository,
)
{
    private val tag = "AuthRepository"

    /**
     * Registers a new user.
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
        try
        {
            connectivityManager.ensureInternet()

            if (!ValidationEngine.isValidEmail(email)) throw Exception("Invalid email format.")
            if (!ValidationEngine.isStrongPassword(password)) throw Exception("Password too weak.")
            if (role == UserRole.ADMINISTRATOR) throw Exception("Unauthorized role selection.")

            if (userDao.getUserByEmail(email) != null)
            {
                throw Exception("Email already exists locally.")
            }

            // 1. Firebase Auth Creation
            val firebaseUser = try
            {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                result.user ?: throw Exception("Auth creation failed.")
            }
            catch (e: Exception)
            {
                if (e is CancellationException) throw e
                throw Exception(FirebaseExceptionHandler.parse(e))
            }

            // 2. Remote API Sync with random secret
            val apiSecret = generateRandomSecret()
            val apiSyncDeferred = async()
            {
                try
                {
                    val response = apiService.registerUser(RegistrationRequest(email, apiSecret))
                    if (response.isSuccessful) response.body()?.usercode else null
                }
                catch (_: Exception) { null }
            }

            val remoteUsercode = apiSyncDeferred.await()

            // 3. User Entity Construction
            val campusUserId = IdGenerator.generateUserId()
            val user = UserEntity(
                userId = campusUserId,
                fullName = fullName,
                username = username,
                email = email,
                passwordHash = "[FIREBASE_AUTH]",
                role = role,
                shopName = if (role == UserRole.VENDOR) shopName else null,
                shopStatus = if (role == UserRole.VENDOR) ShopStatus.OPEN else null,
                usercode = remoteUsercode,
            )

            // 4. Authoritative Cloud Write
            try
            {
                firebaseDatabase.getReference("users").child(campusUserId)
                    .setValue(mapToFirebase(user))
                    .await()
            }
            catch (e: Exception)
            {
                if (e is CancellationException) throw e
                firebaseUser.delete().await()
                throw Exception("Cloud sync failed. Account creation rolled back.")
            }

            // 5. Local Cache
            userDao.insertUser(user)
            Result.success(user)
        }
        catch (e: Exception)
        {
            if (e is CancellationException) throw e
            Result.failure(e)
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
        try
        {
            connectivityManager.ensureInternet()
            if (role == UserRole.ADMINISTRATOR) throw Exception("Unauthorized role selection.")

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("SSO failed.")
            val email = firebaseUser.email ?: throw Exception("Email missing from SSO.")

            val existingUser = try { resolveUserRecord(email) } catch (_: Exception) { null }
            if (existingUser != null) return@coroutineScope Result.success(existingUser)

            val apiSecret = generateRandomSecret()
            val apiSyncDeferred = async()
            {
                try
                {
                    val response = apiService.registerUser(RegistrationRequest(email, apiSecret))
                    if (response.isSuccessful) response.body()?.usercode else null
                }
                catch (_: Exception) { null }
            }

            val remoteUsercode = apiSyncDeferred.await()

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

            try
            {
                firebaseDatabase.getReference("users").child(campusUserId)
                    .setValue(mapToFirebase(user))
                    .await()
            }
            catch (e: Exception)
            {
                if (e is CancellationException) throw e
                firebaseUser.delete().await()
                throw Exception("Cloud sync failed.")
            }

            userDao.insertUser(user)
            Result.success(user)
        }
        catch (e: Exception)
        {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    /**
     * Authenticates a user and enforces suspension checks.
     */
    suspend fun login(email: String, password: String): Result<UserEntity>
    {
        return try
        {
            connectivityManager.ensureInternet()
            
            try
            {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            }
            catch (e: Exception)
            {
                if (e is CancellationException) throw e
                throw Exception(FirebaseExceptionHandler.parse(e))
            }

            val user = resolveUserRecord(email)
            
            // Finding 8: Enforce account suspension immediately after login
            if (user.status == UserStatus.SUSPENDED)
            {
                firebaseAuth.signOut()
                throw Exception("Account suspended. Please contact administration.")
            }
            
            Result.success(user)
        }
        catch (e: Exception)
        {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<UserEntity>
    {
        return try
        {
            connectivityManager.ensureInternet()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("SSO failed.")
            val email = firebaseUser.email ?: throw Exception("Email missing.")

            val user = resolveUserRecord(email)
            
            if (user.status == UserStatus.SUSPENDED)
            {
                firebaseAuth.signOut()
                throw Exception("Account suspended.")
            }
            
            Result.success(user)
        }
        catch (e: Exception)
        {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    /**
     * Helper to resolve the user record, now including a cloud-check for status.
     */
    private suspend fun resolveUserRecord(email: String): UserEntity
    {
        Log.i(tag, "Resolving user record for: $email")
        
        // 1. Fetch from cloud (Authoritative for status/sync)
        val snapshot = try
        {
            firebaseDatabase.getReference("users")
                .orderByChild("email")
                .equalTo(email)
                .get()
                .await()
        }
        catch (e: Exception)
        {
            if (e is CancellationException) throw e
            null
        }

        val cloudMap = snapshot?.children?.firstOrNull()?.value as? Map<Any?, Any?>
        val cloudUser = cloudMap?.let { mapFromFirebase(it) }

        if (cloudUser != null)
        {
            // Sync cloud state to local cache (Finding 8: ensure suspension is cached)
            userDao.insertUser(cloudUser)
            return cloudUser
        }

        // 2. Fallback to local cache if offline but already registered
        val localUser = userDao.getUserByEmail(email)
        return localUser ?: throw Exception("Profile record not found.")
    }

    private fun mapToFirebase(user: UserEntity): Map<String, Any?>
    {
        return mapOf(
            "userId" to user.userId,
            "fullName" to user.fullName,
            "username" to user.username,
            "email" to user.email,
            "passwordHash" to user.passwordHash,
            "role" to if (user.role == UserRole.ADMINISTRATOR) "ADMIN" else user.role.name,
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
                        // Finding 1: Explicitly flush pending orders on a interval
                        orderRepository.syncPendingOrders(userId)

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
                catch (_: Exception) {}
            }
        }
    }

    suspend fun isAdmin(): Boolean
    {
        return try
        {
            val user = firebaseAuth.currentUser ?: return false
            val tokenResult: GetTokenResult = user.getIdToken(false).await()
            tokenResult.claims["admin"] == true
        }
        catch (_: Exception) { false }
    }

    fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null
    fun getCurrentUserEmail(): String? = firebaseAuth.currentUser?.email
    fun logout() = firebaseAuth.signOut()

    suspend fun sendRecoveryEmail(email: String): Result<Unit>
    {
        return try
        {
            connectivityManager.ensureInternet()
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        }
        catch (e: Exception)
        {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        userId: String,
        fullName: String,
        username: String,
        newPassword: String? = null,
    ): Result<Unit>
    {
        return try
        {
            connectivityManager.ensureInternet()
            
            if (!newPassword.isNullOrBlank())
            {
                firebaseAuth.currentUser?.updatePassword(newPassword)?.await()
            }

            userDao.updateProfileFields(userId, fullName, username)
            
            val updates = mapOf<String, Any>("fullName" to fullName, "username" to username)
            firebaseDatabase.getReference("users").child(userId).updateChildren(updates).await()
            Result.success(Unit)
        }
        catch (e: Exception)
        {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun linkBankAccount(userId: String, bankInfo: String): Result<Unit>
    {
        return try
        {
            connectivityManager.ensureInternet()
            val user = userDao.getUserById(userId) ?: throw Exception("User not found")
            userDao.updateUser(user.copy(bankAccountInfo = bankInfo))
            
            firebaseDatabase.getReference("users").child(userId).child("bankAccountInfo")
                .setValue(bankInfo).await()
            Result.success(Unit)
        }
        catch (e: Exception)
        {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    private fun generateRandomSecret(): String
    {
        val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..16).map { SecureRandom().nextInt(charPool.size) }.map(charPool::get).joinToString("")
    }

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    suspend fun updateShopStatus(userId: String, status: ShopStatus): Result<Unit>
    {
        return try
        {
            connectivityManager.ensureInternet()
            val user = userDao.getUserById(userId) ?: throw Exception("User not found")
            userDao.updateUser(user.copy(shopStatus = status))
            firebaseDatabase.getReference("users").child(userId).child("shopStatus").setValue(status.name).await()
            Result.success(Unit)
        }
        catch (e: Exception)
        {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    fun getUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)
}
