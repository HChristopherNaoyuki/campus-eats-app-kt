package com.example.campus_eats_app_kt.ui.screens

import app.cash.turbine.test
import com.example.campus_eats_app_kt.data.AuthRepository
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
 * ForgotPasswordViewModelTest verifies the account recovery logic.
 * Finding 7: Updated to verify Email-based recovery.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest
{
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: ForgotPasswordViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp()
    {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        viewModel = ForgotPasswordViewModel(authRepository)
    }

    @After
    fun tearDown()
    {
        Dispatchers.resetMain()
    }

    /**
     * Requirement: Test successful password reset email dispatch
     */
    @Test
    fun sendRecoveryEmail_withValidEmail_emitsSuccessState() = runTest {
        // Given
        coEvery { authRepository.sendRecoveryEmail("aisha.govender@campuseats.test") } returns Result.success(Unit)

        // Then
        viewModel.resetState.test {
            assertEquals(ResetState.Idle, awaitItem())

            // When
            viewModel.sendRecoveryEmail("aisha.govender@campuseats.test")

            assertEquals(ResetState.Loading, awaitItem())
            assertEquals(ResetState.Success, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Requirement: Test reset failure due to network or missing account
     */
    @Test
    fun sendRecoveryEmail_withFailure_emitsErrorState() = runTest {
        // Given
        coEvery {
            authRepository.sendRecoveryEmail("missing@test.com")
        } returns Result.failure(Exception("Failed to send email"))

        // Then
        viewModel.resetState.test {
            assertEquals(ResetState.Idle, awaitItem())

            // When
            viewModel.sendRecoveryEmail("missing@test.com")

            assertEquals(ResetState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is ResetState.Error)
            assertEquals("Failed to send email", (error as ResetState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Requirement: Test empty email validation
     */
    @Test
    fun sendRecoveryEmail_withEmptyEmail_emitsErrorState() = runTest {
        // Then
        viewModel.resetState.test {
            assertEquals(ResetState.Idle, awaitItem())

            // When
            viewModel.sendRecoveryEmail("")

            val error = awaitItem()
            assertTrue(error is ResetState.Error)
            assertEquals("Please enter your registered email address.", (error as ResetState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
