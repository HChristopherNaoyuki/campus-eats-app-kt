package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.UserStatus
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * AdminRepositoryTest verifies administrative actions like user suspension and credit issuance.
 */
class AdminRepositoryTest
{
    private lateinit var userDao: UserDao
    private lateinit var connectivityManager: NetworkConnectivityManager
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var repository: AdminRepository

    @Before
    fun setUp()
    {
        userDao = mockk(relaxed = true)
        connectivityManager = mockk(relaxed = true)
        firebaseDatabase = mockk(relaxed = true)

        // Mock successful RTDB task completion
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

        repository = AdminRepository(userDao, connectivityManager, firebaseDatabase)
    }

    /**
     * Requirement: Test user suspension
     */
    @Test
    fun suspendUser_callsDaoUpdate()
    {
        runTest()
        {
            val userId = "USER-123"
            repository.suspendUser(userId)
            coVerify { userDao.updateStatus(userId, UserStatus.SUSPENDED) }
        }
    }

    /**
     * Requirement: Test credit issuance
     */
    @Test
    fun issueCredits_callsDaoAddCredits()
    {
        runTest()
        {
            val userId = "USER-123"
            val amount = 150.0
            repository.issueCredits(userId, amount)
            coVerify { userDao.addCredits(userId, amount) }
        }
    }
}
