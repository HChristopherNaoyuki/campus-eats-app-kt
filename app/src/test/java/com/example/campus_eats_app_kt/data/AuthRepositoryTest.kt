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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * AuthRepositoryTest verifies the authentication and profile management logic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest
{
    private lateinit var userDao: UserDao
    private lateinit var apiService: FakeRestaurantApiService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var connectivityManager: NetworkConnectivityManager
    private lateinit var repository: AuthRepository

    private val testUser = UserEntity(
        userId = "TEST-USER-ID-X1234",
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
            userDao = userDao,
            apiService = apiService,
            connectivityManager = connectivityManager,
            firebaseAuth = firebaseAuth,
            firebaseDatabase = firebaseDatabase,
            orderRepository = mockk(relaxed = true),
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

    @Test
    fun isAdmin_withAdminClaim_returnsTrue() = runTest {
        val mockFirebaseUser = mockk<FirebaseUser>(relaxed = true)
        val tokenResult = mockk<GetTokenResult>()
        val task = mockSuccessfulTask(tokenResult)

        every { firebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.getIdToken(false) } returns task
        every { tokenResult.claims } returns mapOf("admin" to true)

        val isAdmin = repository.isAdmin()
        assertTrue("User should be identified as admin", isAdmin)
    }

    @Test
    fun updateProfile_updatesAllowedFields() = runTest {
        coEvery { userDao.getUserById(testUser.userId) } returns testUser
        val ref = mockk<DatabaseReference>(relaxed = true)
        every { firebaseDatabase.getReference("users").child(any()) } returns ref
        every { ref.updateChildren(any()) } returns mockSuccessfulTask(null)

        val result = repository.updateProfile(testUser.userId, "New Name", "newusername")

        assertTrue(result.isSuccess)
        // Finding 14: Verification updated to use targeted update
        coVerify { userDao.updateProfileFields(testUser.userId, "New Name", "newusername") }
        coVerify { ref.updateChildren(match { (it["fullName"] == "New Name") && (it["username"] == "newusername") }) }
    }

    @Test
    fun login_withCorrectCredentials_returnsSuccess() = runTest {
        val authResult = mockk<AuthResult>()
        val authTask = mockSuccessfulTask(authResult)

        every {
            firebaseAuth.signInWithEmailAndPassword("test@example.com", "password123")
        } returns authTask
        
        // Mock resolveUserRecord's cloud fetch
        val ref = mockk<DatabaseReference>(relaxed = true)
        val query = mockk<Query>(relaxed = true)
        val snapshot = mockk<DataSnapshot>(relaxed = true)
        val childSnapshot = mockk<DataSnapshot>(relaxed = true)
        
        every { firebaseDatabase.getReference("users") } returns ref
        every { ref.orderByChild("email") } returns query
        every { query.equalTo("test@example.com") } returns query
        every { query.get() } returns mockSuccessfulTask(snapshot)
        every { snapshot.children } returns listOf(childSnapshot)
        
        // Map data from cloud
        every { childSnapshot.value } returns mapOf(
            "userId" to testUser.userId,
            "fullName" to testUser.fullName,
            "username" to testUser.username,
            "email" to testUser.email,
            "role" to "STUDENT",
            "status" to "ACTIVE"
        )

        val result = repository.login("test@example.com", "password123")

        assertTrue(result.isSuccess)
        assertEquals(testUser.userId, result.getOrNull()?.userId)
    }
}
