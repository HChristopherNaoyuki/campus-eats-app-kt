package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * MenuRepository manages food menu items and vendor discovery.
 */
class MenuRepository(
    private val menuItemDao: MenuItemDao,
    private val userDao: UserDao
)
{
    /**
     * Retrieves all menu items associated with a specific vendor.
     */
    fun getMenuItemsByVendor(vendorId: String): Flow<List<MenuItemEntity>> =
        menuItemDao.getMenuItemsByVendor(vendorId)

    /**
     * Retrieves a list of all active vendors in the system.
     */
    fun getAllVendors(): Flow<List<UserEntity>> =
        userDao.getAllUsers().map { users ->
            users.filter { it.role == UserRole.VENDOR }
        }

    /**
     * Persists a new menu item.
     */
    suspend fun addMenuItem(item: MenuItemEntity)
    {
        menuItemDao.insertMenuItem(item)
    }

    /**
     * Updates an existing menu item record.
     */
    suspend fun updateMenuItem(item: MenuItemEntity)
    {
        menuItemDao.updateMenuItem(item)
    }

    /**
     * Removes a menu item from the system.
     */
    suspend fun deleteMenuItem(item: MenuItemEntity)
    {
        menuItemDao.deleteMenuItem(item)
    }
}
