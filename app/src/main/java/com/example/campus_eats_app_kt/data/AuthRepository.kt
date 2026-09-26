package com.example.campus_eats_app_kt.data

import android.util.Log
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import com.example.campus_eats_app_kt.data.network.FakeRestaurantApiService
import com.example.campus_eats_app_kt.util.DatabaseSeeder
import com.example.campus_eats_app_kt.util.IdGenerator
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.example.campus_eats_app_kt.util.ValidationEngine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/**
 * AuthRepository manages authentication, registration, user profile persistence, and credential security.
 *
 * Architecture principles:
 * 1. User registration uses local-first insertion into Room database with isSynced set to false.
 * 2. Background synchronization is delegated to FirebaseSyncManager to avoid duplicate sync loops.
 * 3. Supports Google Single Sign-In via Credential Manager token exchange with Firebase Auth.
 */
class AuthRepository(
    private val userDao: UserDao,
    private val apiService: FakeRestaurantApiService,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val orderRepository: OrderRepository,
    private val firebaseSyncManager: FirebaseSyncManager? = null,
)
{
    private val tag = "AuthRepository"

    /**
     * Registers a new user locally in Room storage (offline-first architecture).
     */
    suspend fun register(
        fullName: String,
        username: String,
        email: String,
        password: String,
        role: UserRole,
        shopName: String? = null,
        userId: String? = null,
    ): Result<UserEntity>
    {
        Log.d(tag, "Initiating offline-first registration for: $email")
        return try
        {
            if (!ValidationEngine.isValidEmail(email))
            {
                throw Exception("Invalid email format.")
            }
            if (!ValidationEngine.isStrongPassword(password))
            {
                throw Exception("Password too weak.")
            }

            if (userDao.getUserByEmail(email) != null)
            {
                throw Exception("Email already exists.")
            }

            val campusUserId = userId.takeIf { !it.isNullOrBlank() } ?: IdGenerator.generateUserId()
            val user = UserEntity(
                userId = campusUserId,
                fullName = fullName,
                username = username,
                email = email,
                passwordHash = DatabaseSeeder.encryptPassword(password),
                role = role,
                shopName = if (role == UserRole.VENDOR) shopName else null,
                shopStatus = if (role == UserRole.VENDOR) ShopStatus.OPEN else null,
                isSynced = false,
            )

            userDao.insertUser(user)
            Result.success(user)
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    /**
     * Delegation shim for continuous background synchronization.
     * Deprecated for internal sync loops, delegates directly to FirebaseSyncManager.
     */
    fun startBackgroundSync(userId: String, scope: CoroutineScope)
    {
        Log.i(tag, "Delegating continuous background sync loop to FirebaseSyncManager.")
        firebaseSyncManager?.startContinuousSync(scope)
    }

    /**
     * Registers a new user authenticated via Google SSO credential token exchange.
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
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("SSO authentication failed.")
            val email = firebaseUser.email ?: throw Exception("Google account missing email address.")

            val existingUser = try
            {
                resolveUserRecord(email)
            }
            catch (_: Exception)
            {
                null
            }

            if (existingUser != null)
            {
                return@coroutineScope Result.success(existingUser)
            }

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
                isSynced = true,
            )

            firebaseDatabase.getReference("users").child(campusUserId)
                .setValue(mapToFirebase(user))
                .await()

            userDao.insertUser(user)
            Result.success(user)
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    /**
     * Authenticates an existing user using email and password credentials.
     */
    suspend fun login(email: String, password: String): Result<UserEntity>
    {
        return try
        {
            val localUser = userDao.getUserByEmail(email)
            val inputHash = DatabaseSeeder.encryptPassword(password)

            if ((localUser != null) && (localUser.passwordHash == inputHash))
            {
                if (localUser.status == UserStatus.SUSPENDED)
                {
                    throw Exception("Account suspended.")
                }
                return Result.success(localUser)
            }

            connectivityManager.ensureInternet()
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
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
            Result.failure(e)
        }
    }

    /**
     * Signs in an existing user using Google ID token credentials.
     */
    suspend fun signInWithGoogle(idToken: String): Result<UserEntity>
    {
        return try
        {
            connectivityManager.ensureInternet()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            val email = firebaseAuth.currentUser?.email ?: throw Exception("Google account missing email address.")
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
            Result.failure(e)
        }
    }

    /**
     * Resolves user entity by checking Firebase Realtime Database and local database.
     */
    private suspend fun resolveUserRecord(email: String): UserEntity
    {
        val snapshot = try
        {
            firebaseDatabase.getReference("users")
                .orderByChild("email")
                .equalTo(email)
                .get()
                .await()
        }
        catch (_: Exception)
        {
            null
        }

        val cloudMap = snapshot?.children?.firstOrNull()?.value as? Map<Any?, Any?>
        val cloudUser = cloudMap?.let { mapFromFirebase(it) }

        if (cloudUser != null)
        {
            userDao.insertUser(cloudUser)
            return cloudUser
        }

        val localUser = userDao.getUserByEmail(email)
        return localUser ?: throw Exception("Profile record not found.")
    }

    /**
     * Maps UserEntity to a map suitable for Firebase Realtime Database serialization.
     */
    private fun mapToFirebase(user: UserEntity): Map<String, Any?>
    {
        return mapOf(
            "userId" to user.userId,
            "fullName" to user.fullName,
            "username" to user.username,
            "email" to user.email,
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

    /**
     * Maps Firebase Realtime Database dictionary map back into a local UserEntity.
     */
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
            status = try
            {
                UserStatus.valueOf(stringMap["status"] as? String ?: "ACTIVE")
            }
            catch (_: Exception)
            {
                UserStatus.ACTIVE
            },
            walletBalance = (stringMap["walletBalance"] as? Number)?.toDouble() ?: 0.0,
            shopName = stringMap["shopName"] as? String,
            shopStatus = (stringMap["shopStatus"] as? String)?.let
            { statusStr ->
                try
                {
                    ShopStatus.valueOf(statusStr)
                }
                catch (_: Exception)
                {
                    null
                }
            },
            bankAccountInfo = stringMap["bankAccountInfo"] as? String,
            registrationDate = (stringMap["registrationDate"] as? Number)?.toLong() ?: 0L,
            usercode = stringMap["usercode"] as? String,
            isSynced = true,
        )
    }

    /**
     * Determines if current authenticated Firebase user holds administrator claim privileges.
     */
    suspend fun isAdmin(): Boolean
    {
        return try
        {
            val user = firebaseAuth.currentUser ?: return false
            val tokenResult: GetTokenResult = user.getIdToken(false).await()
            tokenResult.claims["admin"] == true
        }
        catch (_: Exception)
        {
            false
        }
    }

    fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null

    fun getCurrentUserEmail(): String? = firebaseAuth.currentUser?.email

    fun logout() = firebaseAuth.signOut()

    /**
     * Initiates password recovery email via Firebase Auth.
     */
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
            Result.failure(e)
        }
    }

    /**
     * Resets password locally by User ID.
     */
    suspend fun resetPasswordByUserId(userId: String, newPassword: String): Result<Unit>
    {
        return try
        {
            val user = userDao.getUserById(userId) ?: throw Exception("User ID not found.")
            val newHash = DatabaseSeeder.encryptPassword(newPassword)
            userDao.updateUser(user.copy(passwordHash = newHash, isSynced = false))
            Result.success(Unit)
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    /**
     * Updates user profile name and username in local Room database and Firebase Realtime Database.
     */
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
            Result.failure(e)
        }
    }

    /**
     * Links banking information to user account.
     */
    suspend fun linkBankAccount(userId: String, bankInfo: String): Result<Unit>
    {
        return try
        {
            connectivityManager.ensureInternet()
            val user = userDao.getUserById(userId) ?: throw Exception("User not found.")
            userDao.updateUser(user.copy(bankAccountInfo = bankInfo))

            firebaseDatabase.getReference("users").child(userId).child("bankAccountInfo")
                .setValue(bankInfo).await()
            Result.success(Unit)
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    /**
     * Updates vendor shop operating status.
     */
    suspend fun updateShopStatus(userId: String, status: ShopStatus): Result<Unit>
    {
        return try
        {
            connectivityManager.ensureInternet()
            val user = userDao.getUserById(userId) ?: throw Exception("User not found.")
            userDao.updateUser(user.copy(shopStatus = status))
            firebaseDatabase.getReference("users").child(userId).child("shopStatus").setValue(status.name).await()
            Result.success(Unit)
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    fun getUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)
}
