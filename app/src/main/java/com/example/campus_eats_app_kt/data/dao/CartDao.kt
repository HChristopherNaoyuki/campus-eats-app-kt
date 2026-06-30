package com.example.campus_eats_app_kt.data.dao

import androidx.room.*
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(cartItem: CartItemEntity)

    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)

    @Delete
    suspend fun removeFromCart(cartItem: CartItemEntity)

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartByUserId(userId: String): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND itemId = :itemId LIMIT 1")
    suspend fun getCartItem(userId: String, itemId: Long): CartItemEntity?

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: String)
}
