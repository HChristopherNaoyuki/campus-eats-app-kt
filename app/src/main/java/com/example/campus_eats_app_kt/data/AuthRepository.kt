package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.network.FakeRestaurantApiService
import com.example.campus_eats_app_kt.data.network.RegistrationRequest
import com.example.campus_eats_app_kt.util.IdGenerator
import kotlinx.coroutines.flow.Flow

/**
 * AuthRepository handles user-related authentication and profile management operations.
 * It interacts with the local Room database and the remote Fake Restaurant API.
 */
class AuthRepository(
    private val userDao: UserDao,
    private val apiService: FakeRestaurantApiService
)
{
    /**
     * Registers a new user in the system.
     * Synchronizes with the remote Fake Restaurant API to obtain a usercode (API Key).
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
        return kotlin.runCatching {
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null)
            {
                throw Exception("Email already registered")
            }

            // Attempt to register on the remote API first
            var remoteUsercode: String? = null
            try
            {
                val response =
                    apiService.registerUser(RegistrationRequest(email, password))
                if (response.isSuccessful)
                {
                    remoteUsercode = response.body()?.usercode
                }
            }
            catch (_: Exception)
            {
                // Network failure during registration, we'll continue with local only for now
            }

            val userId = IdGenerator.generateUserId()
            val user = UserEntity(
                userId = userId,
                fullName = fullName,
                username = username,
                email = email,
                passwordHash = password,
                role = role,
                shopName = if (role == UserRole.VENDOR) shopName else null,
                shopStatus = if (role == UserRole.VENDOR) ShopStatus.OPEN else null,
                usercode = remoteUsercode
            )

            userDao.insertUser(user)
            user
        }
    }

    /**
     * Attempts to log in a user with the provided email and password.
     * Updates the user's API key from the remote server if missing.
     */
    suspend fun login(email: String, password: String): Result<UserEntity>
    {
        return kotlin.runCatching {
            val user = userDao.getUserByEmail(email)
            if ((user != null) && (user.passwordHash == password))
            {
                // If usercode is missing locally, try to fetch it from the API
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
                    catch (_: Exception)
                    {
                        // Network error, return local user as is
                    }
                }
                user
            }
            else
            {
                throw Exception("Invalid email or password")
            }
        }
    }

    /**
     * Resets the password for a user identified by their unique User ID.
     */
    suspend fun resetPassword(userId: String, newPassword: String): Result<Unit>
    {
        return kotlin.runCatching {
            val user = userDao.getUserById(userId)
            if (user != null)
            {
                // Attempt to update on remote API if usercode exists
                if (user.usercode != null)
                {
                    try
                    {
                        apiService.updatePassword(user.usercode, newPassword)
                    }
                    catch (_: Exception)
                    {
                        // Ignore network failure for local password reset
                    }
                }
                val updatedUser = user.copy(passwordHash = newPassword)
                userDao.updateUser(updatedUser)
            }
            else
            {
                throw Exception("Invalid User ID")
            }
        }
    }

    /**
     * Updates the user's profile information.
     */
    suspend fun updateProfile(userId: String, email: String, password: String): Result<Unit>
    {
        return kotlin.runCatching {
            val user = userDao.getUserById(userId) ?: throw Exception("User not found")

            if (user.email != email)
            {
                val existing = userDao.getUserByEmail(email)
                if (existing != null) throw Exception("Email already taken")
            }

            var updatedUser = user.copy(email = email)
            if (password.isNotBlank())
            {
                // Attempt to update on remote API if usercode exists
                if (user.usercode != null)
                {
                    try
                    {
                        apiService.updatePassword(user.usercode, password)
                    }
                    catch (_: Exception)
                    {
                        // Ignore network failure for local password reset
                    }
                }
                updatedUser = updatedUser.copy(passwordHash = password)
            }
            userDao.updateUser(updatedUser)
        }
    }

    /**
     * Returns a Flow of the UserEntity for the given User ID.
     */
    fun getUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)

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
        return kotlin.runCatching {
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
     * Deletes a user account from both local and remote systems.
     */
    @Suppress("unused")
    suspend fun deleteAccount(userId: String): Result<Unit>
    {
        return kotlin.runCatching {
            val user = userDao.getUserById(userId)
            if (user != null)
            {
                // Attempt to delete on remote API if usercode exists
                if (user.usercode != null)
                {
                    try
                    {
                        apiService.deleteUser(user.usercode)
                    }
                    catch (_: Exception)
                    {
                        // Ignore network failure
                    }
                }
                userDao.deleteUser(user)
            }
            else
            {
                throw Exception("User not found")
            }
        }
    }
}
