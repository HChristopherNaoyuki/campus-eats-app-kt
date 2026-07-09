package com.example.campus_eats_app_kt.ui.screens

import app.cash.turbine.test
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * LoginViewModelTest verifies the presentation logic for the login screen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest
{
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testUser = UserEntity(
        userId = "ID-123",
        fullName = "Test User",
        username = "test",
        email = "test@example.com",
        passwordHash = "pass",
        role = UserRole.STUDENT,
        status = UserStatus.ACTIVE
    )

    @Before
    fun setUp()
    {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown()
    {
        Dispatchers.resetMain()
    }

    /**
     * Requirement: Test successful login transition
     */
    @Test
    fun login_withValidCredentials_emitsSuccessState() = runTest {
        // Given
        coEvery {
            authRepository.login(
                "test@example.com",
                "pass"
            )
        } returns Result.success(testUser)

        // When
        viewModel.login("test@example.com", "pass")

        // Then
        viewModel.loginState.test {
            // Initial state is Idle
            assertEquals(LoginState.Idle, awaitItem())

            // Should transition to Loading then Success
            assertEquals(LoginState.Loading, awaitItem())
            val success = awaitItem()
            assertTrue(success is LoginState.Success)
            assertEquals(testUser, (success as LoginState.Success).user)

            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Requirement: Test validation failure (empty fields)
     */
    @Test
    fun login_withEmptyFields_emitsErrorState() = runTest {
        // When
        viewModel.login("", "")

        // Then
        viewModel.loginState.test {
            assertEquals(LoginState.Idle, awaitItem())
            val error = awaitItem()
            assertTrue(error is LoginState.Error)
            assertEquals("Please fill in all fields", (error as LoginState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Requirement: Test repository failure
     */
    @Test
    fun login_onRepositoryError_emitsErrorState() = runTest {
        // Given
        coEvery {
            authRepository.login(
                any(),
                any()
            )
        } returns Result.failure(Exception("Network error"))

        // When
        viewModel.login("test@example.com", "pass")

        // Then
        viewModel.loginState.test {
            assertEquals(LoginState.Idle, awaitItem())
            assertEquals(LoginState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is LoginState.Error)
            assertEquals("Network error", (error as LoginState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
