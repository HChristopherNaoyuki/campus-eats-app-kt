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
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/**
 * AuthRepository manages authentication, registration, user profile
 * persistence, and credential security.
 */
class AuthRepository(
    private val userDao: UserDao,
    @Suppress("UNUSED_PARAMETER") apiService: FakeRestaurantApiService,
    private val connectivityManager: NetworkConnectivityManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    @Suppress("UNUSED_PARAMETER") orderRepository: OrderRepository,
    private val firebaseSyncManager: FirebaseSyncManager? = null,
)
{
    private val tag = "AuthRepository"

    /**
     * Registers a new user locally in Room storage (offline-first).
     * Attempts online Firebase Auth creation and Realtime Database mirror
     * within a single 5-second timeout budget.
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
        Log.d(tag, "Initiating registration for user: $email")
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

            val campusUserId = userId.takeIf { !it.isNullOrBlank() }
                ?: IdGenerator.generateUserId()
            var isSyncedCloud = false
            var firebaseUid: String? = null

            if (connectivityManager.hasInternetConnection())
            {
                try
                {
                    withTimeout(5000)
                    {
                        val authResult = firebaseAuth
                            .createUserWithEmailAndPassword(email, password)
                            .await()
                        firebaseUid = authResult.user?.uid
                        val effectiveUid = firebaseUid ?: campusUserId

                        val roleStr = if (role == UserRole.ADMINISTRATOR)
                        {
                            "ADMIN"
                        }
                        else
                        {
                            role.name
                        }

                        val userMap = mapOf(
                            "userId" to effectiveUid,
                            "fullName" to fullName,
                            "username" to username,
                            "email" to email,
                            "role" to roleStr,
                            "passwordHash" to "[FIREBASE_SSO]",
                            "status" to UserStatus.ACTIVE.name,
                            "walletBalance" to 0.0,
                            "shopName" to if (role == UserRole.VENDOR)
                            {
                                shopName
                            }
                            else
                            {
                                null
                            },
                            "shopStatus" to if (role == UserRole.VENDOR)
                            {
                                ShopStatus.OPEN.name
                            }
                            else
                            {
                                null
                            },
                            "bankAccountInfo" to null,
                            "registrationDate" to System.currentTimeMillis(),
                            "usercode" to null,
                        )

                        firebaseDatabase.getReference("users")
                            .child(effectiveUid)
                            .setValue(userMap)
                            .await()
                    }
                    isSyncedCloud = true
                }
                catch (e: Exception)
                {
                    Log.w(
                        tag,
                        "Online registration skipped: ${e.message}",
                    )
                }
            }

            val salt = DatabaseSeeder.generateSalt()
            val user = UserEntity(
                userId = firebaseUid ?: campusUserId,
                fullName = fullName,
                username = username,
                email = email,
                passwordHash = DatabaseSeeder.encryptPassword(
                    password,
                    salt,
                ),
                role = role,
                shopName = if (role == UserRole.VENDOR) shopName else null,
                shopStatus = if (role == UserRole.VENDOR)
                {
                    ShopStatus.OPEN
                }
                else
                {
                    null
                },
                isSynced = isSyncedCloud,
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
     */
    fun startBackgroundSync(
        @Suppress("UNUSED_PARAMETER") userId: String,
        scope: CoroutineScope,
    )
    {
        Log.i(
            tag,
            "Delegating background sync loop to FirebaseSyncManager.",
        )
        firebaseSyncManager?.startContinuousSync(scope)
    }

    /**
     * Registers a new user authenticated via Google SSO credentials.
     * Enforces single 5-second timeout budget.
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
            val user = withTimeout(5000)
            {
                val authResult = firebaseAuth
                    .signInWithCredential(credential)
                    .await()
                val firebaseUser = authResult.user
                    ?: throw Exception("SSO authentication failed.")

                val email = firebaseUser.email
                    ?: throw Exception("Google account missing email.")
                val firebaseUid = firebaseUser.uid

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
                    return@withTimeout existingUser
                }

                val newUser = UserEntity(
                    userId = firebaseUid,
                    fullName = firebaseUser.displayName ?: "Google User",
                    username = email.substringBefore("@"),
                    email = email,
                    passwordHash = "[FIREBASE_SSO]",
                    role = role,
                    shopName = if (role == UserRole.VENDOR)
                    {
                        shopName
                    }
                    else
                    {
                        null
                    },
                    shopStatus = if (role == UserRole.VENDOR)
                    {
                        ShopStatus.OPEN
                    }
                    else
                    {
                        null
                    },
                    isSynced = true,
                )

                firebaseDatabase.getReference("users")
                    .child(firebaseUid)
                    .setValue(mapToFirebase(newUser))
                    .await()

                userDao.insertUser(newUser)
                newUser
            }

            Result.success(user)
        }
        catch (e: Exception)
        {
            firebaseAuth.signOut()
            Result.failure(e)
        }
    }

    /**
     * Authenticates an existing user using email and password credentials.
     * Restores missing Firebase Auth account for offline-registered users
     * within a single 5-second timeout budget.
     */
    suspend fun login(
        email: String,
        password: String,
    ): Result<UserEntity>
    {
        return try
        {
            var localUser = userDao.getUserByEmail(email)
            val storedHash = localUser?.passwordHash ?: ""
            val salt = if (storedHash.contains(":"))
            {
                storedHash.substringBefore(":")
            }
            else
            {
                null
            }

            val inputHash = DatabaseSeeder.encryptPassword(password, salt)
            val legacyHash = DatabaseSeeder.legacySha256Password(password)

            val passwordMatches = (localUser != null) &&
                ((localUser.passwordHash == inputHash) ||
                 (localUser.passwordHash == legacyHash))

            if (passwordMatches && localUser != null)
            {
                if (localUser.status == UserStatus.SUSPENDED)
                {
                    throw Exception("Account suspended.")
                }

                if (localUser.passwordHash == legacyHash || salt == null)
                {
                    val newSalt = DatabaseSeeder.generateSalt()
                    val newHash = DatabaseSeeder.encryptPassword(
                        password,
                        newSalt,
                    )
                    localUser = localUser.copy(passwordHash = newHash)
                    userDao.insertUser(localUser)
                }

                if (connectivityManager.hasInternetConnection())
                {
                    val currentUser = firebaseAuth.currentUser
                    val needSignIn = currentUser == null ||
                        !currentUser.email.equals(email, ignoreCase = true)

                    if (needSignIn)
                    {
                        try
                        {
                            withTimeout(5000)
                            {
                                try
                                {
                                    firebaseAuth
                                        .signInWithEmailAndPassword(
                                            email,
                                            password,
                                        )
                                        .await()
                                }
                                catch (e: FirebaseAuthInvalidUserException)
                                {
                                    val authResult = firebaseAuth
                                        .createUserWithEmailAndPassword(
                                            email,
                                            password,
                                        )
                                        .await()
                                    val newUid = authResult.user?.uid
                                    if (newUid != null &&
                                        localUser.userId != newUid)
                                    {
                                        userDao.migrateUserId(
                                            localUser.userId,
                                            newUid,
                                        )
                                        localUser = userDao.getUserById(
                                            newUid,
                                        ) ?: localUser.copy(userId = newUid)
                                    }
                                }
                            }
                        }
                        catch (_: Exception)
                        {
                            Log.w(
                                tag,
                                "Firebase Auth session creation skipped.",
                            )
                        }
                    }
                }

                return Result.success(localUser)
            }

            connectivityManager.ensureInternet()
            val user = withTimeout(5000)
            {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .await()
                resolveUserRecord(email)
            }

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
     * Enforces single 5-second timeout budget.
     */
    suspend fun signInWithGoogle(idToken: String): Result<UserEntity>
    {
        return try
        {
            connectivityManager.ensureInternet()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val user = withTimeout(5000)
            {
                firebaseAuth.signInWithCredential(credential).await()
                val email = firebaseAuth.currentUser?.email
                    ?: throw Exception("Google account missing email.")
                resolveUserRecord(email)
            }

            if (user.status == UserStatus.SUSPENDED)
            {
                firebaseAuth.signOut()
                throw Exception("Account suspended.")
            }

            Result.success(user)
        }
        catch (e: Exception)
        {
            firebaseAuth.signOut()
            Result.failure(e)
        }
    }

    /**
     * Resolves user entity from Firebase Realtime Database and Room.
     * Preserves cloud wallet balance top-ups while respecting local spend.
     */
    private suspend fun resolveUserRecord(email: String): UserEntity
    {
        val localUser = userDao.getUserByEmail(email)

        val snapshot = try
        {
            withTimeout(5000)
            {
                firebaseDatabase.getReference("users")
                    .orderByChild("email")
                    .equalTo(email)
                    .get()
                    .await()
            }
        }
        catch (_: Exception)
        {
            null
        }

        @Suppress("UNCHECKED_CAST")
        val cloudMap = snapshot?.children?.firstOrNull()?.value
            as? Map<Any?, Any?>
        val cloudUser = cloudMap?.let { mapFromFirebase(it) }

        if (cloudUser != null)
        {
            val mergedUser = if (localUser != null && !localUser.isSynced)
            {
                val effectiveWallet = if (cloudUser.walletBalance >
                    localUser.walletBalance)
                {
                    cloudUser.walletBalance
                }
                else
                {
                    localUser.walletBalance
                }
                cloudUser.copy(
                    walletBalance = effectiveWallet,
                    isSynced = false,
                )
            }
            else
            {
                cloudUser
            }
            userDao.insertUser(mergedUser)
            return mergedUser
        }

        return localUser ?: throw Exception("Profile record not found.")
    }

    /**
     * Maps UserEntity instance into Map for Firebase Realtime Database.
     */
    private fun mapToFirebase(user: UserEntity): Map<String, Any?>
    {
        val roleStr = if (user.role == UserRole.ADMINISTRATOR)
        {
            "ADMIN"
        }
        else
        {
            user.role.name
        }

        return mapOf(
            "userId" to user.userId,
            "fullName" to user.fullName,
            "username" to user.username,
            "email" to user.email,
            "role" to roleStr,
            "passwordHash" to "[FIREBASE_SSO]",
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
     * Converts a raw Firebase Realtime Database Map into UserEntity.
     */
    private fun mapFromFirebase(map: Map<Any?, Any?>): UserEntity
    {
        val stringMap = map.mapKeys { it.key.toString() }
        val roleVal = when (stringMap["role"] as? String)
        {
            "ADMIN" -> UserRole.ADMINISTRATOR
            "VENDOR" -> UserRole.VENDOR
            "STUDENT" -> UserRole.STUDENT
            else -> UserRole.STANDARD
        }
        val statusVal = try
        {
            UserStatus.valueOf(
                (stringMap["status"] as? String) ?: "ACTIVE",
            )
        }
        catch (_: Exception)
        {
            UserStatus.ACTIVE
        }
        val shopStatusStr = stringMap["shopStatus"] as? String
        val shopStatusVal = if (shopStatusStr != null)
        {
            try
            {
                ShopStatus.valueOf(shopStatusStr)
            }
            catch (_: Exception)
            {
                null
            }
        }
        else
        {
            null
        }

        return UserEntity(
            userId = (stringMap["userId"] as? String) ?: "",
            fullName = (stringMap["fullName"] as? String) ?: "",
            username = (stringMap["username"] as? String) ?: "",
            email = (stringMap["email"] as? String) ?: "",
            passwordHash = (stringMap["passwordHash"] as? String)
                ?: "[FIREBASE_SSO]",
            role = roleVal,
            status = statusVal,
            walletBalance = (stringMap["walletBalance"] as? Number)
                ?.toDouble() ?: 0.0,
            shopName = stringMap["shopName"] as? String,
            shopStatus = shopStatusVal,
            bankAccountInfo = stringMap["bankAccountInfo"] as? String,
            registrationDate = (stringMap["registrationDate"] as? Number)
                ?.toLong() ?: 0L,
            usercode = stringMap["usercode"] as? String,
            isSynced = true,
        )
    }

    /**
     * Determines whether the currently authenticated user is an admin.
     */
    suspend fun isAdmin(): Boolean
    {
        val email = getCurrentUserEmail()
        if (email != null)
        {
            val user = userDao.getUserByEmail(email)
            if (user?.role == UserRole.ADMINISTRATOR)
            {
                return true
            }
        }
        return try
        {
            val firebaseUser = firebaseAuth.currentUser ?: return false
            val tokenResult: GetTokenResult = firebaseUser
                .getIdToken(false)
                .await()
            tokenResult.claims["admin"] == true
        }
        catch (_: Exception)
        {
            false
        }
    }

    /**
     * Checks if Firebase Auth session is currently active.
     */
    fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null

    /**
     * Returns email address of active Firebase user session.
     */
    fun getCurrentUserEmail(): String? = firebaseAuth.currentUser?.email

    /**
     * Returns UID of active Firebase user session.
     */
    fun getCurrentUserUid(): String? = firebaseAuth.currentUser?.uid

    /**
     * Signs out the active user session.
     */
    fun logout() = firebaseAuth.signOut()

    /**
     * Sends password recovery email to specified user address.
     */
    suspend fun sendRecoveryEmail(email: String): Result<Unit>
    {
        return try
        {
            connectivityManager.ensureInternet()
            withTimeout(5000)
            {
                firebaseAuth.sendPasswordResetEmail(email).await()
            }
            Result.success(Unit)
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    /**
     * Resets password for specified User ID in local Room database.
     */
    suspend fun resetPasswordByUserId(
        userId: String,
        newPassword: String,
    ): Result<Unit>
    {
        return try
        {
            val user = userDao.getUserById(userId)
                ?: throw Exception("User ID not found.")
            val newHash = DatabaseSeeder.encryptPassword(newPassword)
            userDao.updateUser(
                user.copy(passwordHash = newHash, isSynced = false),
            )
            Result.success(Unit)
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    /**
     * Updates user profile name, username, and password in Room and Firebase.
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
                withTimeout(5000)
                {
                    firebaseAuth.currentUser
                        ?.updatePassword(newPassword)
                        ?.await()
                }

                val localUser = userDao.getUserById(userId)
                val newSalt = DatabaseSeeder.generateSalt()
                val newHash = DatabaseSeeder.encryptPassword(
                    newPassword,
                    newSalt,
                )
                if (localUser != null)
                {
                    userDao.updateUser(
                        localUser.copy(
                            fullName = fullName,
                            username = username,
                            passwordHash = newHash,
                            isSynced = false,
                        ),
                    )
                }
            }
            else
            {
                userDao.updateProfileFields(userId, fullName, username)
            }

            val updates = mapOf<String, Any>(
                "fullName" to fullName,
                "username" to username,
            )
            withTimeout(5000)
            {
                firebaseDatabase.getReference("users")
                    .child(userId)
                    .updateChildren(updates)
                    .await()
            }
            Result.success(Unit)
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    /**
     * Links bank account information to user profile in Room and Firebase.
     */
    suspend fun linkBankAccount(
        userId: String,
        bankInfo: String,
    ): Result<Unit>
    {
        return try
        {
            connectivityManager.ensureInternet()
            val user = userDao.getUserById(userId)
                ?: throw Exception("User not found.")
            userDao.updateUser(
                user.copy(bankAccountInfo = bankInfo, isSynced = false),
            )

            withTimeout(5000)
            {
                firebaseDatabase.getReference("users")
                    .child(userId)
                    .child("bankAccountInfo")
                    .setValue(bankInfo)
                    .await()
            }
            Result.success(Unit)
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    /**
     * Updates shop operational status for vendor profile.
     */
    suspend fun updateShopStatus(
        userId: String,
        status: ShopStatus,
    ): Result<Unit>
    {
        return try
        {
            connectivityManager.ensureInternet()
            val user = userDao.getUserById(userId)
                ?: throw Exception("User not found.")
            userDao.updateUser(
                user.copy(shopStatus = status, isSynced = false),
            )
            withTimeout(5000)
            {
                firebaseDatabase.getReference("users")
                    .child(userId)
                    .child("shopStatus")
                    .setValue(status.name)
                    .await()
            }
            Result.success(Unit)
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    /**
     * Returns Flow of user profile from Room for specified User ID.
     */
    fun getUserFlow(userId: String): Flow<UserEntity?> =
        userDao.getUserByIdFlow(userId)

    /**
     * Retrieves UserEntity record by email address.
     */
    suspend fun getUserByEmail(email: String): UserEntity? =
        userDao.getUserByEmail(email)
}