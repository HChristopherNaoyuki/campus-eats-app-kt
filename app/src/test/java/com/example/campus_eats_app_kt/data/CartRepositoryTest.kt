package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.CartDao
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * CartRepositoryTest verifies cart item management logic.
 */
class CartRepositoryTest
{
    private lateinit var cartDao: CartDao
    private lateinit var repository: CartRepository

    @Before
    fun setUp()
    {
        cartDao = mockk(relaxed = true)
        repository = CartRepository(cartDao)
    }

    /**
     * Requirement: Test adding to cart (New item)
     */
    @Test
    fun addToCart_newItem_insertsInDao() = runTest {
        // Given
        val userId = "USER-001"
        val item = MenuItemEntity(
            itemId = 1,
            vendorId = "V1",
            name = "Burger",
            description = "Tasty",
            price = 50.0,
            stock = 10,
            category = "Food"
        )
        coEvery { cartDao.getCartItem(userId, 1) } returns null

        // When
        repository.addToCart(userId, item)

        // Then
        coVerify { cartDao.addToCart(match { it.itemId == 1L && it.quantity == 1 }) }
    }

    /**
     * Requirement: Test adding to cart (Existing item increments)
     */
    @Test
    fun addToCart_existingItem_updatesQuantity() = runTest {
        // Given
        val userId = "USER-001"
        val item = MenuItemEntity(
            itemId = 1,
            vendorId = "V1",
            name = "Burger",
            description = "Tasty",
            price = 50.0,
            stock = 10,
            category = "Food"
        )
        val existing = CartItemEntity(
            cartItemId = 10,
            userId = userId,
            itemId = 1,
            vendorId = "V1",
            name = "Burger",
            price = 50.0,
            quantity = 1
        )
        coEvery { cartDao.getCartItem(userId, 1) } returns existing

        // When
        repository.addToCart(userId, item)

        // Then
        coVerify { cartDao.updateCartItem(match { it.cartItemId == 10L && it.quantity == 2 }) }
    }

    /**
     * Requirement: Test removal from cart (Decrement)
     */
    @Test
    fun removeFromCart_quantityGreaterThanOne_decrements() = runTest {
        // Given
        val item = CartItemEntity(
            cartItemId = 10,
            userId = "U1",
            itemId = 1,
            vendorId = "V1",
            name = "Burger",
            price = 50.0,
            quantity = 2
        )

        // When
        repository.removeFromCart(item)

        // Then
        coVerify { cartDao.updateCartItem(match { it.quantity == 1 }) }
    }

    /**
     * Requirement: Test removal from cart (Delete)
     */
    @Test
    fun removeFromCart_quantityIsOne_deletes() = runTest {
        // Given
        val item = CartItemEntity(
            cartItemId = 10,
            userId = "U1",
            itemId = 1,
            vendorId = "V1",
            name = "Burger",
            price = 50.0,
            quantity = 1
        )

        // When
        repository.removeFromCart(item)

        // Then
        coVerify { cartDao.removeFromCart(item) }
    }
}
