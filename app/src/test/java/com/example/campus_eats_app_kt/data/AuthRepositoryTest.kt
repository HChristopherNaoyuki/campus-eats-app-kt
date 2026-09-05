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
import com.google.firebase.auth.GetTokenResult
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
 * Enforces security rule compliance and administrative authorization.
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
    
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testUser = UserEntity(
        userId = "TEST-USER-ID-X1234", // 19 chars: XXXX-XXXX-XXXX-XXXX
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
        
        repository = AuthRepository(
            userDao, 
            apiService, 
            connectivityManager, 
            firebaseAuth, 
            firebaseDatabase,
            testDispatcher,
        )
    }

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
     * Requirement: Verify isAdmin correctly identifies administrative custom claims.
     */
    @Test
    fun isAdmin_withAdminClaim_returnsTrue() = runTest {
        // Given
        val mockFirebaseUser = mockk<FirebaseUser>(relaxed = true)
        val tokenResult = mockk<GetTokenResult>()
        val task = mockSuccessfulTask(tokenResult)

        every { firebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.getIdToken(false) } returns task
        every { tokenResult.claims } returns mapOf("admin" to true)

        // When
        val isAdmin = repository.isAdmin()

        // Then
        assertTrue("User should be identified as admin", isAdmin)
    }

    /**
     * Requirement: Test successful profile update with restricted fields.
     */
    @Test
    fun updateProfile_updatesAllowedFields() = runTest {
        // Given
        coEvery { userDao.getUserById(testUser.userId) } returns testUser
        val ref = mockk<com.google.firebase.database.DatabaseReference>(relaxed = true)
        every { firebaseDatabase.getReference("users").child(any()) } returns ref
        every { ref.updateChildren(any()) } returns mockSuccessfulTask(null)

        // When
        val result = repository.updateProfile(testUser.userId, "New Name", "newusername")

        // Then
        assertTrue(result.isSuccess)
        coVerify { userDao.updateUser(match { (it.fullName == "New Name") && (it.username == "newusername") }) }
        coVerify { ref.updateChildren(match { (it["fullName"] == "New Name") && (it["username"] == "newusername") }) }
    }

    @Test
    fun login_withCorrectCredentials_returnsSuccess() = runTest {
        val authResult = mockk<AuthResult>()
        val task = mockSuccessfulTask(authResult)

        every {
            firebaseAuth.signInWithEmailAndPassword("test@example.com", "password123")
        } returns task
        coEvery { userDao.getUserByEmail("test@example.com") } returns testUser

        val result = repository.login("test@example.com", "password123")

        assertTrue(result.isSuccess)
        assertEquals(testUser, result.getOrNull())
    }
}
