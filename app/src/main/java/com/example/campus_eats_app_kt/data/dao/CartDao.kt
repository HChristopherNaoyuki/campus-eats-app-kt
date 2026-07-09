package com.example.campus_eats_app_kt.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * CartDao defines database interactions for the shopping cart.
 */
@Dao
interface CartDao
{
    /**
     * Persists or overwrites a cart item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(cartItem: CartItemEntity)

    /**
     * Updates an item's quantity or price in the cart.
     */
    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)

    /**
     * Removes a single item record from the user's cart.
     */
    @Delete
    suspend fun removeFromCart(cartItem: CartItemEntity)

    /**
     * Retrieves all items currently in a user's cart.
     */
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartByUserId(userId: String): Flow<List<CartItemEntity>>

    /**
     * Finds a specific food item in a user's cart.
     */
    @Query("SELECT * FROM cart_items WHERE userId = :userId AND itemId = :itemId LIMIT 1")
    suspend fun getCartItem(userId: String, itemId: Long): CartItemEntity?

    /**
     * Wipes the cart for a user (called after successful order placement).
     */
    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: String)
}
