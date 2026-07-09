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
 * RegistrationViewModelTest verifies the presentation logic for user onboarding.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationViewModelTest
{
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: RegistrationViewModel
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
        viewModel = RegistrationViewModel(authRepository)
    }

    @After
    fun tearDown()
    {
        Dispatchers.resetMain()
    }

    /**
     * Requirement: Test successful student registration
     */
    @Test
    fun register_withValidData_emitsSuccessState() = runTest {
        // Given
        coEvery {
            authRepository.register(
                "Full Name",
                "user",
                "email@test.com",
                "pass",
                UserRole.STUDENT,
                null
            )
        } returns Result.success(testUser)

        // When
        viewModel.register("Full Name", "user", "email@test.com", "pass", UserRole.STUDENT)

        // Then
        viewModel.registrationState.test {
            assertEquals(RegistrationState.Idle, awaitItem())
            assertEquals(RegistrationState.Loading, awaitItem())
            val success = awaitItem()
            assertTrue(success is RegistrationState.Success)
            assertEquals(testUser, (success as RegistrationState.Success).user)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Requirement: Test vendor registration validation (missing shop name)
     */
    @Test
    fun register_vendorWithoutShopName_emitsErrorState() = runTest {
        // When
        viewModel.register("Vendor Name", "vendor", "v@test.com", "pass", UserRole.VENDOR, null)

        // Then
        viewModel.registrationState.test {
            assertEquals(RegistrationState.Idle, awaitItem())
            val error = awaitItem()
            assertTrue(error is RegistrationState.Error)
            assertEquals(
                "Shop name is required for vendors",
                (error as RegistrationState.Error).message
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Requirement: Test validation for empty fields
     */
    @Test
    fun register_withEmptyFields_emitsErrorState() = runTest {
        // When
        viewModel.register("", "", "", "", UserRole.STUDENT)

        // Then
        viewModel.registrationState.test {
            assertEquals(RegistrationState.Idle, awaitItem())
            val error = awaitItem()
            assertTrue(error is RegistrationState.Error)
            assertEquals("Please fill in all fields", (error as RegistrationState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
