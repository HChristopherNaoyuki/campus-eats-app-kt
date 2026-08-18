package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import com.example.campus_eats_app_kt.data.network.FakeRestaurantApiService
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * AuthRepositoryTest verifies the authentication and profile management logic.
 * It has been updated to integrate Firebase Auth mocking.
 */
class AuthRepositoryTest
{
    private lateinit var userDao: UserDao
    private lateinit var apiService: FakeRestaurantApiService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: com.google.firebase.database.FirebaseDatabase
    private lateinit var connectivityManager: NetworkConnectivityManager
    private lateinit var repository: AuthRepository

    private val testUser = UserEntity(
        userId = "TEST-USER-ID-0001",
        fullName = "Test User",
        username = "testuser",
        email = "test@example.com",
        passwordHash = "[FIREBASE_SSO]",
        role = UserRole.STUDENT,
        status = UserStatus.ACTIVE,
        usercode = "TEST-USER-CODE"
    )

    @Before
    fun setUp()
    {
        userDao = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        firebaseDatabase = mockk(relaxed = true)
        connectivityManager = mockk(relaxed = true)
        repository =
            AuthRepository(userDao, apiService, connectivityManager, firebaseAuth, firebaseDatabase)
    }

    /**
     * Requirement: Test successful login with Firebase SSO.
     */
    @Test
    fun login_withCorrectCredentials_returnsSuccess()
    {
        runTest()
        {
            // Given: Mock Firebase Success with Task completion handling
            val authResult = mockk<AuthResult>()
            val task = mockk<Task<AuthResult>>()

            every { task.isComplete } returns true
            every { task.isSuccessful } returns true
            every { task.isCanceled } returns false
            every { task.result } returns authResult
            every { task.exception } returns null

            // Mocking the listeners that .await() might use
            every { task.addOnCompleteListener(any()) } answers {
                val listener =
                    it.invocation.args[0] as com.google.android.gms.tasks.OnCompleteListener<AuthResult>
                listener.onComplete(task)
                task
            }

            every {
                firebaseAuth.signInWithEmailAndPassword(
                    "test@example.com",
                    "password123"
                )
            } returns task
            coEvery { userDao.getUserByEmail("test@example.com") } returns testUser

            // When
            val result = repository.login("test@example.com", "password123")

            // Then
            assertTrue("Expected success but got ${result.exceptionOrNull()}", result.isSuccess)
            assertEquals(testUser, result.getOrNull())
        }
    }

    /**
     * Requirement: Test failed login with Firebase exception.
     */
    @Test
    fun login_withIncorrectPassword_returnsFailure()
    {
        runTest()
        {
            // Given: Mock Firebase Failure
            val task = mockk<Task<AuthResult>>()
            every { task.isComplete } returns true
            every { task.isSuccessful } returns false
            every { task.exception } returns Exception("Firebase Auth Error")
            every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns task

            // When
            val result = repository.login("test@example.com", "wrongpassword")

            // Then
            assertTrue(result.isFailure)
            assertEquals("Firebase Auth Error", result.exceptionOrNull()?.message)
        }
    }

    /**
     * Requirement: Test password reset (Fake API sync)
     */
    @Test
    fun resetPassword_withValidId_updatesRemote()
    {
        runTest()
        {
            // Given
            coEvery { userDao.getUserById(testUser.userId) } returns testUser

            // When
            val result = repository.resetPassword(testUser.userId, "newpassword")

            // Then
            assertTrue(result.isSuccess)
            // Verify remote sync was attempted
            coVerify { apiService.updatePassword(any(), "newpassword") }
        }
    }
}
