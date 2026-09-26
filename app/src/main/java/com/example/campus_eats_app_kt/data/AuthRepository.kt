package com.example.campus_eats_app_kt.data

import android.util.Log
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import com.example.campus_eats_app_kt.data.network.FakeRestaurantApiService
import com.example.campus_eats_app_kt.data.network.RegistrationRequest
import com.example.campus_eats_app_kt.util.DatabaseSeeder
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
 * AuthRepository manages authentication and profile synchronization.
 * Requirement: All registration must be done offline and use Room Database.
 * Requirement: Backup to Firebase every 30 seconds.
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
     * Finding 1: Registration is now purely offline to satisfy project mandate.
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
            if (!ValidationEngine.isValidEmail(email)) throw Exception("Invalid email format.")
            if (!ValidationEngine.isStrongPassword(password)) throw Exception("Password too weak.")

            if (userDao.getUserByEmail(email) != null)
            {
                throw Exception("Email already exists.")
            }

            // SPEC v3.0.1 Section 1.5: User ID is the unique key for the account
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
     * Continuous 10-second background synchronization loop.
     */
    fun startBackgroundSync(userId: String, scope: CoroutineScope)
    {
        scope.launch()
        {
            while (isActive)
            {
                // Continuous 10-second backup interval
                delay(10.seconds)
                try
                {
                    if (connectivityManager.hasInternetConnection())
                    {
                        performUserBackup()
                        orderRepository.syncPendingOrders(userId)
                        syncCurrentProfile(userId)
                    }
                }
                catch (_: Exception) {}
            }
        }
    }

    /**
     * Finds unsynced users and backs them up to Firebase.
     */
    private suspend fun performUserBackup()
    {
        val unsynced = userDao.getUnsyncedUsers()
        Log.v(tag, "Scanning for unsynced users. Found: ${unsynced.size}")

        for (user in unsynced)
        {
            try
            {
                // Synchronize profile record to RTDB
                // Finding 6: Use explicit mapping for Firebase compatibility
                firebaseDatabase.getReference("users").child(user.userId)
                    .setValue(mapToFirebase(user))
                    .await()

                // Mark as synced locally
                userDao.markAsSynced(user.userId)
                Log.i(tag, "Successfully backed up user profile: ${user.email}")
            }
            catch (e: Exception)
            {
                Log.w(tag, "Backup failed for ${user.email}: ${e.message}")
            }
        }
    }

    private suspend fun syncCurrentProfile(userId: String)
    {
        if (firebaseAuth.currentUser != null)
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
                firebaseDatabase.getReference("users").child(userId).updateChildren(updates).await()
            }
        }
    }

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
            val firebaseUser = authResult.user ?: throw Exception("SSO failed.")
            val email = firebaseUser.email ?: throw Exception("Email missing.")

            val existingUser = try { resolveUserRecord(email) } catch (_: Exception) { null }
            if (existingUser != null) return@coroutineScope Result.success(existingUser)

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

    suspend fun login(email: String, password: String): Result<UserEntity>
    {
        return try
        {
            // Offline login check - compare hashed input with stored hash
            val localUser = userDao.getUserByEmail(email)
            val inputHash = DatabaseSeeder.encryptPassword(password)
            
            if ((localUser != null) && (localUser.passwordHash == inputHash))
            {
                if (localUser.status == UserStatus.SUSPENDED) throw Exception("Account suspended.")
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

    suspend fun signInWithGoogle(idToken: String): Result<UserEntity>
    {
        return try
        {
            connectivityManager.ensureInternet()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            val email = firebaseAuth.currentUser?.email ?: throw Exception("Email missing.")
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
        catch (_: Exception) { null }

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
            isSynced = true,
        )
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
            Result.failure(e)
        }
    }

    /**
     * SPEC v3.0.1 Section 2: Account Recovery by User ID
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
            Result.failure(e)
        }
    }

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
            Result.failure(e)
        }
    }

    fun getUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)
}
