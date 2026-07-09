package com.example.campus_eats_app_kt.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * MenuItemDao provides methods to manage menu items in the database.
 */
@Dao
interface MenuItemDao
{
    /**
     * Persists a new food item.
     */
    @Insert
    suspend fun insertMenuItem(item: MenuItemEntity)

    /**
     * Updates an existing menu item's details.
     */
    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    /**
     * Removes an item from the menu.
     */
    @Delete
    suspend fun deleteMenuItem(item: MenuItemEntity)

    /**
     * Retrieves all items offered by a specific vendor.
     */
    @Query("SELECT * FROM menu_items WHERE vendorId = :vendorId")
    fun getMenuItemsByVendor(vendorId: String): Flow<List<MenuItemEntity>>

    /**
     * Retrieves all menu items currently available system-wide.
     */
    @Query("SELECT * FROM menu_items")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>
}
