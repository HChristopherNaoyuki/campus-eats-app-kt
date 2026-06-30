package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MenuRepository(
    private val menuItemDao: MenuItemDao,
    private val userDao: UserDao
) {
    fun getMenuItemsByVendor(vendorId: String): Flow<List<MenuItemEntity>> =
        menuItemDao.getMenuItemsByVendor(vendorId)

    fun getAllVendors(): Flow<List<UserEntity>> =
        userDao.getAllUsers().map { users ->
            users.filter { it.role == UserRole.VENDOR }
        }

    suspend fun addMenuItem(item: MenuItemEntity) {
        menuItemDao.insertMenuItem(item)
    }

    suspend fun updateMenuItem(item: MenuItemEntity) {
        menuItemDao.updateMenuItem(item)
    }

    suspend fun deleteMenuItem(item: MenuItemEntity) {
        menuItemDao.deleteMenuItem(item)
    }
}
