package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.UserStatus
import io.mockk.coVerify
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
    private lateinit var repository: AdminRepository

    @Before
    fun setUp()
    {
        userDao = mockk(relaxed = true)
        repository = AdminRepository(userDao)
    }

    /**
     * Requirement: Test user suspension
     */
    @Test
    fun suspendUser_callsDaoUpdate() = runTest {
        val userId = "USER-123"
        repository.suspendUser(userId)
        coVerify { userDao.updateStatus(userId, UserStatus.SUSPENDED) }
    }

    /**
     * Requirement: Test credit issuance
     */
    @Test
    fun issueCredits_callsDaoAddCredits() = runTest {
        val userId = "USER-123"
        val amount = 150.0
        repository.issueCredits(userId, amount)
        coVerify { userDao.addCredits(userId, amount) }
    }
}
