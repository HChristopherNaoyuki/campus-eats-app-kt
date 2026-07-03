package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.CartDao
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartDao: CartDao) {
    fun getCart(userId: String): Flow<List<CartItemEntity>> = cartDao.getCartByUserId(userId)

    suspend fun addToCart(userId: String, item: MenuItemEntity) {
        val existing = cartDao.getCartItem(userId, item.itemId)
        if (existing != null) {
            cartDao.updateCartItem(existing.copy(quantity = existing.quantity + 1))
        } else {
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

    suspend fun removeFromCart(cartItem: CartItemEntity) {
        if (cartItem.quantity > 1) {
            cartDao.updateCartItem(cartItem.copy(quantity = cartItem.quantity - 1))
        } else {
            cartDao.removeFromCart(cartItem)
        }
    }

    suspend fun incrementCartItem(cartItem: CartItemEntity) {
        cartDao.updateCartItem(cartItem.copy(quantity = cartItem.quantity + 1))
    }

    suspend fun deleteCartItem(cartItem: CartItemEntity)
    {
        cartDao.removeFromCart(cartItem)
    }

    suspend fun clearCart(userId: String) {
        cartDao.clearCart(userId)
    }
}
