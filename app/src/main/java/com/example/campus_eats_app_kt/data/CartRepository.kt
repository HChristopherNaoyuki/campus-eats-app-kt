package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.CartDao
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * CartRepository handles the logic for managing shopping cart items.
 * It provides methods for adding, removing, and updating items in the cart.
 */
class CartRepository(private val cartDao: CartDao)
{
    /**
     * Retrieves the current cart items for a specific user as a Flow.
     */
    fun getCart(userId: String): Flow<List<CartItemEntity>> = cartDao.getCartByUserId(userId)

    /**
     * Adds a menu item to the user's cart. If the item already exists, its quantity is incremented.
     */
    suspend fun addToCart(userId: String, item: MenuItemEntity)
    {
        val existing = cartDao.getCartItem(userId, item.itemId)
        if (existing != null)
        {
            cartDao.updateCartItem(existing.copy(quantity = existing.quantity + 1))
        }
        else
        {
            cartDao.addToCart(
                CartItemEntity(
                    userId = userId,
                    itemId = item.itemId,
                    vendorId = item.vendorId,
                    name = item.name,
                    price = item.price,
                    quantity = 1
                )
            )
        }
    }

    /**
     * Decrements the quantity of a cart item or removes it entirely if quantity reaches zero.
     */
    suspend fun removeFromCart(cartItem: CartItemEntity)
    {
        if (cartItem.quantity > 1)
        {
            cartDao.updateCartItem(cartItem.copy(quantity = cartItem.quantity - 1))
        }
        else
        {
            cartDao.removeFromCart(cartItem)
        }
    }

    /**
     * Increments the quantity of an item already in the cart.
     */
    suspend fun incrementCartItem(cartItem: CartItemEntity)
    {
        cartDao.updateCartItem(cartItem.copy(quantity = cartItem.quantity + 1))
    }

    /**
     * Removes an item from the cart regardless of its quantity.
     */
    suspend fun deleteCartItem(cartItem: CartItemEntity)
    {
        cartDao.removeFromCart(cartItem)
    }

    /**
     * Clears all items from the user's cart.
     */
    suspend fun clearCart(userId: String)
    {
        cartDao.clearCart(userId)
    }
}
