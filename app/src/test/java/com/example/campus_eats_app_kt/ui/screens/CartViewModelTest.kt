package com.example.campus_eats_app_kt.ui.screens

import app.cash.turbine.test
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * CartViewModelTest verifies the management of the shopping cart state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest
{
    private lateinit var cartRepository: CartRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: CartViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val userId = "USER-001"
    private val testUser = UserEntity(
        userId = userId,
        fullName = "Test",
        username = "test",
        email = "t@t.com",
        passwordHash = "p",
        role = UserRole.STUDENT,
        status = UserStatus.ACTIVE
    )

    @Before
    fun setUp()
    {
        Dispatchers.setMain(testDispatcher)
        cartRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)

        every { cartRepository.getCart(userId) } returns flowOf(emptyList())
        every { authRepository.getUserFlow(userId) } returns flowOf(testUser)

        viewModel = CartViewModel(cartRepository, authRepository, userId)
    }

    @After
    fun tearDown()
    {
        Dispatchers.resetMain()
    }

    /**
     * Requirement: Test cart state loading
     */
    @Test
    fun cartItems_loadsFromRepository() = runTest {
        // Given
        val items = listOf(CartItemEntity(1, userId, 101, "V1", "Item", 10.0, 1))
        every { cartRepository.getCart(userId) } returns flowOf(items)

        // Re-init to pickup new flow
        val vm = CartViewModel(cartRepository, authRepository, userId)

        // Then
        vm.cartItems.test {
            assertEquals(items, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Requirement: Test item increment logic
     */
    @Test
    fun addItem_callsRepositoryIncrement() = runTest {
        // Given
        val item = CartItemEntity(1, userId, 101, "V1", "Item", 10.0, 1)

        // When
        viewModel.addItem(item)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { cartRepository.incrementCartItem(item) }
    }

    /**
     * Requirement: Test item removal logic (decrement)
     */
    @Test
    fun removeItem_quantityGreaterThanOne_decrements() = runTest {
        // Given
        val item = CartItemEntity(1, userId, 101, "V1", "Item", 10.0, 2)

        // When
        viewModel.removeItem(item)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { cartRepository.removeFromCart(item) }
    }

    /**
     * Requirement: Test item removal logic (delete when last one)
     */
    @Test
    fun removeItem_quantityIsOne_deletes() = runTest {
        // Given
        val item = CartItemEntity(1, userId, 101, "V1", "Item", 10.0, 1)

        // When
        viewModel.removeItem(item)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { cartRepository.deleteCartItem(item) }
    }

    /**
     * Requirement: Test clear cart logic
     */
    @Test
    fun clearCart_callsRepositoryClear() = runTest {
        // When
        viewModel.clearCart()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { cartRepository.clearCart(userId) }
    }
}
