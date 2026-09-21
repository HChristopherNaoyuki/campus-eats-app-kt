package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.CartDao
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class VendorConflictException(message: String) : Exception(message)

/**
 * CartRepository manages shopping cart logic.
 * Hardened in Batch 2 with single-vendor enforcement and atomic updates.
 */
class CartRepository(private val cartDao: CartDao)
{
    private val mutex = Mutex()

    /**
     * Retrieves the current cart items.
     */
    fun getCart(userId: String): Flow<List<CartItemEntity>> = cartDao.getCartByUserId(userId)

    /**
     * Adds an item to the cart, enforcing that all items belong to the same vendor.
     * Finding 11: Single-vendor enforcement.
     * Finding 14: Uses Mutex to prevent duplicate row insertions.
     */
    suspend fun addToCart(userId: String, item: MenuItemEntity) = mutex.withLock()
    {
        val currentCart = cartDao.getCartByUserId(userId).first()
        
        if (currentCart.isNotEmpty() && (currentCart.first().vendorId != item.vendorId))
        {
            throw VendorConflictException("Your cart contains items from another vendor. Clear it first?")
        }

        val existing = cartDao.getCartItem(userId, item.itemId)
        if (existing != null)
        {
            cartDao.incrementQuantity(userId, item.itemId)
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
                    quantity = 1,
                ),
            )
        }
    }

    /**
     * Decrements the quantity of a cart item.
     */
    suspend fun removeFromCart(cartItem: CartItemEntity)
    {
        if (cartItem.quantity > 1)
        {
            cartDao.decrementQuantity(cartItem.userId, cartItem.itemId)
        }
        else
        {
            cartDao.removeFromCart(cartItem)
        }
    }

    /**
     * Increments the quantity of an item.
     */
    suspend fun incrementCartItem(cartItem: CartItemEntity)
    {
        cartDao.incrementQuantity(cartItem.userId, cartItem.itemId)
    }

    /**
     * Removes an item regardless of quantity.
     */
    suspend fun deleteCartItem(cartItem: CartItemEntity) = cartDao.removeFromCart(cartItem)

    /**
     * Clears all items from the cart.
     */
    suspend fun clearCart(userId: String) = cartDao.clearCart(userId)
}
