package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * AuthRepositoryTest verifies the authentication and profile management logic.
 */
class AuthRepositoryTest
{
    private lateinit var userDao: UserDao
    private lateinit var repository: AuthRepository

    private val testUser = UserEntity(
        userId = "TEST-USER-ID-0001",
        fullName = "Test User",
        username = "testuser",
        email = "test@example.com",
        passwordHash = "password123",
        role = UserRole.STUDENT,
        status = UserStatus.ACTIVE
    )

    @Before
    fun setUp()
    {
        userDao = mockk(relaxed = true)
        repository = AuthRepository(userDao)
    }

    /**
     * Requirement: Test successful login
     */
    @Test
    fun login_withCorrectCredentials_returnsSuccess() = runTest {
        // Given
        coEvery { userDao.getUserByEmail("test@example.com") } returns testUser

        // When
        val result = repository.login("test@example.com", "password123")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(testUser, result.getOrNull())
    }

    /**
     * Requirement: Test failed login with incorrect password
     */
    @Test
    fun login_withIncorrectPassword_returnsFailure() = runTest {
        // Given
        coEvery { userDao.getUserByEmail("test@example.com") } returns testUser

        // When
        val result = repository.login("test@example.com", "wrongpassword")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Invalid email or password", result.exceptionOrNull()?.message)
    }

    /**
     * Requirement: Test failed login with non-existent email
     */
    @Test
    fun login_withNonExistentEmail_returnsFailure() = runTest {
        // Given
        coEvery { userDao.getUserByEmail("unknown@example.com") } returns null

        // When
        val result = repository.login("unknown@example.com", "any")

        // Then
        assertTrue(result.isFailure)
    }

    /**
     * Requirement: Test password reset functionality
     */
    @Test
    fun resetPassword_withValidId_updatesPassword() = runTest {
        // Given
        coEvery { userDao.getUserById(testUser.userId) } returns testUser

        // When
        val result = repository.resetPassword(testUser.userId, "newpassword")

        // Then
        assertTrue(result.isSuccess)
        coVerify { userDao.updateUser(match { it.passwordHash == "newpassword" }) }
    }

    /**
     * Requirement: Test profile update
     */
    @Test
    fun updateProfile_withValidData_updatesUser() = runTest {
        // Given
        coEvery { userDao.getUserById(testUser.userId) } returns testUser
        coEvery { userDao.getUserByEmail("new@example.com") } returns null

        // When
        val result = repository.updateProfile(testUser.userId, "new@example.com", "newpass")

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            userDao.updateUser(match {
                it.email == "new@example.com" && it.passwordHash == "newpass"
            })
        }
    }
}
