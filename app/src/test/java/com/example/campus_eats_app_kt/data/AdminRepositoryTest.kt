package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.UserStatus
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertTrue

/**
 * AdminRepositoryTest verifies administrative actions like user suspension and credit issuance.
 * Includes authorization verification using admin custom claims.
 */
class AdminRepositoryTest
{
    private lateinit var userDao: UserDao
    private lateinit var authRepository: AuthRepository
    private lateinit var connectivityManager: NetworkConnectivityManager
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var repository: AdminRepository

    @Before
    fun setUp()
    {
        userDao = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        connectivityManager = mockk(relaxed = true)
        firebaseDatabase = mockk(relaxed = true)

        val task = mockk<Task<Void>>()
        every { task.isComplete } returns true
        every { task.isSuccessful } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns null

        val ref = mockk<DatabaseReference>(relaxed = true)
        every { firebaseDatabase.getReference(any()) } returns ref
        every { ref.child(any()) } returns ref
        every { ref.setValue(any()) } returns task
        every { ref.removeValue() } returns task

        repository = AdminRepository(userDao, authRepository, connectivityManager, firebaseDatabase)
    }

    /**
     * Requirement: Test user suspension with admin authorization.
     */
    @Test
    fun suspendUser_withAdminPrivileges_callsDaoUpdate() = runTest {
        val userId = "USER-123"
        coEvery { authRepository.isAdmin() } returns true
        
        val result = repository.suspendUser(userId)
        
        assertTrue(result.isSuccess)
        coVerify { userDao.updateStatus(userId, UserStatus.SUSPENDED) }
    }

    /**
     * Requirement: Test user suspension without admin authorization.
     */
    @Test
    fun suspendUser_withoutAdminPrivileges_returnsFailure() = runTest {
        val userId = "USER-123"
        coEvery { authRepository.isAdmin() } returns false
        
        val result = repository.suspendUser(userId)
        
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { userDao.updateStatus(any(), any()) }
    }

    /**
     * Requirement: Test credit issuance with admin authorization.
     */
    @Test
    fun issueCredits_withAdminPrivileges_callsDaoAddCredits() = runTest {
        val userId = "USER-123"
        val amount = 150.0
        coEvery { authRepository.isAdmin() } returns true
        
        val result = repository.issueCredits(userId, amount)
        
        assertTrue(result.isSuccess)
        coVerify { userDao.addCredits(userId, amount) }
    }
}
