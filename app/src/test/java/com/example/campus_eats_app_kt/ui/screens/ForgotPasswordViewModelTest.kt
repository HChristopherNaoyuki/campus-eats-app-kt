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
 * SPEC v3.0.1 Section 2 compliance.
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
     * SPEC 2.1 - 2.8: Test successful password reset by User ID
     */
    @Test
    fun resetPassword_withValidUserIdAndPassword_emitsSuccessState() = runTest {
        // Given
        coEvery { authRepository.resetPasswordByUserId("USER123456789012", "Pass1234!") } returns Result.success(Unit)

        // Then
        viewModel.resetState.test {
            assertEquals(ResetState.Idle, awaitItem())

            // When
            viewModel.resetPassword("USER123456789012", "Pass1234!", "Pass1234!")

            assertEquals(ResetState.Loading, awaitItem())
            assertEquals(ResetState.Success, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * SPEC 2.7: Test password mismatch validation
     */
    @Test
    fun resetPassword_withMismatchedPasswords_emitsErrorState() = runTest {
        viewModel.resetState.test {
            assertEquals(ResetState.Idle, awaitItem())

            viewModel.resetPassword("USER123456789012", "Pass1234!", "DifferentPass!")

            val error = awaitItem()
            assertTrue(error is ResetState.Error)
            assertEquals("New password and confirm password must match.", (error as ResetState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * SPEC 2.2 - 2.6: Test weak password validation
     */
    @Test
    fun resetPassword_withWeakPassword_emitsErrorState() = runTest {
        viewModel.resetState.test {
            assertEquals(ResetState.Idle, awaitItem())

            viewModel.resetPassword("USER123456789012", "weak", "weak")

            val error = awaitItem()
            assertTrue(error is ResetState.Error)
            assertEquals(
                "Password must be at least 8 characters long and contain uppercase, lowercase, a digit, and a special character.",
                (error as ResetState.Error).message
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
