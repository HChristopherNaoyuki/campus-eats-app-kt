package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import com.example.campus_eats_app_kt.data.network.FakeRestaurantApiService
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * AuthRepositoryTest verifies the authentication and profile management logic.
 * It has been updated to integrate Firebase Auth mocking and ensure coroutine completion.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest
{
    private lateinit var userDao: UserDao
    private lateinit var apiService: FakeRestaurantApiService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: com.google.firebase.database.FirebaseDatabase
    private lateinit var connectivityManager: NetworkConnectivityManager
    private lateinit var repository: AuthRepository
    
    // Using UnconfinedTestDispatcher for immediate execution in tests.
    // This dispatcher ensures that any coroutine launched in the repository (e.g. background sync)
    // is executed predictably within the test scope.
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testUser = UserEntity(
        userId = "TEST-USER-ID-0001",
        fullName = "Test User",
        username = "testuser",
        email = "test@example.com",
        passwordHash = "[FIREBASE_SSO]",
        role = UserRole.STUDENT,
        status = UserStatus.ACTIVE,
        usercode = "TEST-USER-CODE",
    )

    @Before
    fun setUp()
    {
        userDao = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        firebaseDatabase = mockk(relaxed = true)
        connectivityManager = mockk(relaxed = true)
        
        // Injecting the test dispatcher to ensure all coroutines (including background sync)
        // are controlled by the test environment and do not outlive the test scope.
        repository = AuthRepository(
            userDao, 
            apiService, 
            connectivityManager, 
            firebaseAuth, 
            firebaseDatabase,
            testDispatcher
        )
    }

    /**
     * Utility to mock a successful Firebase Task completion.
     * Resumes any coroutine suspended by .await().
     */
    private fun <T> mockSuccessfulTask(result: T?): Task<T>
    {
        val task = mockk<Task<T>>()
        every { task.isComplete } returns true
        every { task.isSuccessful } returns true
        every { task.isCanceled } returns false
        every { task.result } returns result
        every { task.exception } returns null
        every { task.addOnCompleteListener(any()) } answers {
            @Suppress("UNCHECKED_CAST")
            val listener = invocation.args[0] as OnCompleteListener<T>
            listener.onComplete(task)
            task
        }
        return task
    }

    /**
     * Utility to mock a failed Firebase Task completion.
     */
    private fun <T> mockFailedTask(exception: Exception): Task<T>
    {
        val task = mockk<Task<T>>()
        every { task.isComplete } returns true
        every { task.isSuccessful } returns false
        every { task.isCanceled } returns false
        every { task.result } throws exception
        every { task.exception } returns exception
        every { task.addOnCompleteListener(any()) } answers {
            @Suppress("UNCHECKED_CAST")
            val listener = invocation.args[0] as OnCompleteListener<T>
            listener.onComplete(task)
            task
        }
        return task
    }

    /**
     * Requirement: Test successful login with Firebase SSO.
     */
    @Test
    fun login_withCorrectCredentials_returnsSuccess() = runTest {
        // Given
        val authResult = mockk<AuthResult>()
        val task = mockSuccessfulTask(authResult)

        every {
            firebaseAuth.signInWithEmailAndPassword("test@example.com", "password123")
        } returns task
        coEvery { userDao.getUserByEmail("test@example.com") } returns testUser

        // When
        val result = repository.login("test@example.com", "password123")

        // Then
        assertTrue("Expected success but got ${result.exceptionOrNull()}", result.isSuccess)
        assertEquals(testUser, result.getOrNull())
    }

    /**
     * Requirement: Test failed login with Firebase exception.
     */
    @Test
    fun login_withIncorrectPassword_returnsFailure() = runTest {
        // Given
        val task = mockFailedTask<AuthResult>(Exception("Firebase Auth Error"))
        every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns task

        // When
        val result = repository.login("test@example.com", "wrongpassword")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Firebase Auth Error", result.exceptionOrNull()?.message)
    }

    /**
     * Requirement: Test password reset (Fake API sync)
     * FIX: Mocks FirebaseUser and Task completion to prevent UncompletedCoroutinesError.
     * This traces the execution through AuthRepository and ensures the .await() call on the
     * Firebase updatePassword Task is properly resumed by mocking the listener invocation.
     */
    @Test
    fun resetPassword_withValidId_updatesRemote() = runTest {
        // Given
        val mockFirebaseUser = mockk<FirebaseUser>(relaxed = true)
        val updateTask = mockSuccessfulTask<Void>(null)
        
        // Mock current user session to avoid early exit in resetPassword
        every { firebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.updatePassword(any()) } returns updateTask
        coEvery { userDao.getUserById(testUser.userId) } returns testUser

        // When
        val result = repository.resetPassword(testUser.userId, "newpassword")

        // Then
        assertTrue("Reset should be successful: ${result.exceptionOrNull()?.message}", result.isSuccess)
        
        // Verify both Firebase and Remote API were updated sequentially as per implementation.
        coVerify { mockFirebaseUser.updatePassword("newpassword") }
        coVerify { apiService.updatePassword(any(), "newpassword") }
    }
}
