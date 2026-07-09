package com.example.campus_eats_app_kt.ui.screens

import app.cash.turbine.test
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.PaymentMethod
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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * CheckoutViewModelTest verifies the order finalization logic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest
{
    private lateinit var cartRepository: CartRepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: CheckoutViewModel
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
        orderRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)

        every { authRepository.getUserFlow(userId) } returns flowOf(testUser)
        every { cartRepository.getCart(userId) } returns flowOf(emptyList())

        viewModel = CheckoutViewModel(cartRepository, orderRepository, authRepository, userId)
    }

    @After
    fun tearDown()
    {
        Dispatchers.resetMain()
    }

    /**
     * Requirement: Test summary calculation for student role
     */
    @Test
    fun summary_calculatesCorrectlyForStudent() = runTest {
        // Given
        val items = listOf(CartItemEntity(1, userId, 101, "V1", "Item", 100.0, 1))
        every { cartRepository.getCart(userId) } returns flowOf(items)

        // When
        val vm = CheckoutViewModel(cartRepository, orderRepository, authRepository, userId)

        // Then
        vm.summary.test {
            val summary = awaitItem()
            assertNotNull(summary)
            // Subtotal 100
            // Tax 20
            // Fee 10 (under 500)
            // Discount 2.5
            // Total 127.5 -> Rounded 130
            assertEquals(130.0, summary?.total ?: 0.0, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Requirement: Test order placement logic
     */
    @Test
    fun placeOrder_callsRepositoryWithCalculatedTotal() = runTest {
        // Given
        val items = listOf(CartItemEntity(1, userId, 101, "V1", "Item", 100.0, 1))
        every { cartRepository.getCart(userId) } returns flowOf(items)
        val vm = CheckoutViewModel(cartRepository, orderRepository, authRepository, userId)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        vm.placeOrder(PaymentMethod.CAMPUS_WALLET, "12:00", "None") { }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            orderRepository.placeOrder(
                userId = userId,
                vendorId = "V1",
                cartItems = items,
                totalAmount = 130.0,
                paymentMethod = PaymentMethod.CAMPUS_WALLET,
                pickupTime = "12:00",
                specialRequests = "None"
            )
        }
    }
}
