package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * MenuRepository manages food menu items and vendor discovery.
 * It integrates both local Room database records and remote API data.
 */
class MenuRepository(
    private val menuItemDao: MenuItemDao,
    private val userDao: UserDao,
)
{
    /**
     * Retrieves all menu items associated with a specific vendor, optionally sorted by price.
     */
    fun getMenuItemsByVendor(
        vendorId: String,
        sortOrder: String? = null
    ): Flow<List<MenuItemEntity>> = flow {
        // First emit local items (local sorting not implemented for simplicity here)
        menuItemDao.getMenuItemsByVendor(vendorId).collect { emit(it) }

        // Then attempt to fetch from network if it's a numeric ID (indicates Fake Restaurant API)
        val numericId = vendorId.toIntOrNull()
        if (numericId != null)
        {
            try
            {
                val response = if (sortOrder == null)
                {
                    RetrofitClient.instance.getRestaurantMenu(numericId)
                }
                else
                {
                    RetrofitClient.instance.getSortedMenu(numericId, sortOrder)
                }

                if (response.isSuccessful)
                {
                    val networkItems = response.body()?.map { networkItem ->
                        MenuItemEntity(
                            itemId = networkItem.itemID.toLong(),
                            vendorId = networkItem.restaurantID.toString(),
                            name = networkItem.itemName,
                            description = networkItem.itemDescription,
                            price = networkItem.itemPrice,
                            stock = 99,
                            category = "General",
                            imageUrl = networkItem.imageUrl
                        )
                    } ?: emptyList()
                    emit(networkItems)
                }
            }
            catch (_: Exception)
            {
                // Network failure
            }
        }
    }

    /**
     * Searches for menu items by name across both local and remote sources.
     */
    @Suppress("unused")
    fun searchMenuItems(query: String): Flow<List<MenuItemEntity>> = flow {
        // Search local items
        menuItemDao.searchMenuItems(query).collect { emit(it) }

        // Search remote items
        try
        {
            val response = RetrofitClient.instance.searchItemsByName(query)
            if (response.isSuccessful)
            {
                val networkItems = response.body()?.map { networkItem ->
                    MenuItemEntity(
                        itemId = networkItem.itemID.toLong(),
                        vendorId = networkItem.restaurantID.toString(),
                        name = networkItem.itemName,
                        description = networkItem.itemDescription,
                        price = networkItem.itemPrice,
                        stock = 99,
                        category = "General",
                        imageUrl = networkItem.imageUrl
                    )
                } ?: emptyList()
                emit(networkItems)
            }
        }
        catch (_: Exception)
        {
            // Network failure
        }
    }

    /**
     * Retrieves a list of all active vendors in the system.
     * Combines local registered vendors with restaurants from the Fake Restaurant API.
     */
    fun getAllVendors(): Flow<List<UserEntity>> = flow {
        // Emit local vendors first
        userDao.getAllUsers().map { users ->
            users.filter { it.role == UserRole.VENDOR }
        }.collect { emit(it) }

        // Fetch and emit network restaurants
        try
        {
            val response = RetrofitClient.instance.getAllRestaurants()
            if (response.isSuccessful)
            {
                val networkVendors = response.body()?.map { restaurant ->
                    UserEntity(
                        userId = restaurant.restaurantID.toString(),
                        fullName = restaurant.restaurantName,
                        username = restaurant.restaurantName.lowercase().replace(" ", "_"),
                        email = "contact@${
                            restaurant.restaurantName.lowercase().replace(" ", "")
                        }.com",
                        passwordHash = "",
                        role = UserRole.VENDOR,
                        shopName = restaurant.restaurantName
                    )
                } ?: emptyList()
                emit(networkVendors)
            }
        }
        catch (_: Exception)
        {
            // Network error, fall back to local only
        }
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
