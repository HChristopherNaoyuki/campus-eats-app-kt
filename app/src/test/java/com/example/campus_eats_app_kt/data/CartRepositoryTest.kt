package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.CartDao
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * CartRepositoryTest verifies management of student shopping carts.
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

    @Test
    fun addToCart_newItem_insertsInDao() = runTest {
        val userId = "U1"
        val item = MenuItemEntity(1, "V1", "Item", "Desc", 10.0, 10, "Cat")
        
        // Mock current cart as empty
        every { cartDao.getCartByUserId(userId) } returns flowOf(emptyList())
        coEvery { cartDao.getCartItem(userId, 1L) } returns null

        repository.addToCart(userId, item)

        coVerify { cartDao.addToCart(any()) }
    }

    @Test
    fun addToCart_existingItem_updatesQuantity() = runTest {
        val userId = "U1"
        val item = MenuItemEntity(1, "V1", "Item", "Desc", 10.0, 10, "Cat")
        val existing = CartItemEntity(1, userId, 1, "V1", "Item", 10.0, 1)

        every { cartDao.getCartByUserId(userId) } returns flowOf(listOf(existing))
        coEvery { cartDao.getCartItem(userId, 1L) } returns existing

        repository.addToCart(userId, item)

        // Finding 14: Verification updated to use targeted update
        coVerify { cartDao.incrementQuantity(userId, 1L) }
    }

    @Test
    fun removeFromCart_quantityGreaterThanOne_decrements() = runTest {
        val cartItem = CartItemEntity(1, "U1", 1, "V1", "Item", 10.0, 2)

        repository.removeFromCart(cartItem)

        // Finding 14: Verification updated to use targeted update
        coVerify { cartDao.decrementQuantity("U1", 1L) }
    }
}
