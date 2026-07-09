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
     * Requirement: Test successful password reset
     */
    @Test
    fun resetPassword_withValidId_emitsSuccessState() = runTest {
        // Given
        coEvery { authRepository.resetPassword("ID-001", "newpass") } returns Result.success(Unit)

        // When
        viewModel.resetPassword("ID-001", "newpass")

        // Then
        viewModel.resetState.test {
            assertEquals(ResetState.Idle, awaitItem())
            assertEquals(ResetState.Loading, awaitItem())
            assertEquals(ResetState.Success, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Requirement: Test reset failure due to invalid User ID
     */
    @Test
    fun resetPassword_withInvalidId_emitsErrorState() = runTest {
        // Given
        coEvery {
            authRepository.resetPassword(
                "WRONG",
                "any"
            )
        } returns Result.failure(Exception("Invalid User ID"))

        // When
        viewModel.resetPassword("WRONG", "any")

        // Then
        viewModel.resetState.test {
            assertEquals(ResetState.Idle, awaitItem())
            assertEquals(ResetState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is ResetState.Error)
            assertEquals("Invalid User ID", (error as ResetState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Requirement: Test empty field validation
     */
    @Test
    fun resetPassword_withEmptyFields_emitsErrorState() = runTest {
        // When
        viewModel.resetPassword("", "")

        // Then
        viewModel.resetState.test {
            assertEquals(ResetState.Idle, awaitItem())
            val error = awaitItem()
            assertTrue(error is ResetState.Error)
            assertEquals("Please fill in all fields", (error as ResetState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
