package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.dao.MenuItemDao
import com.example.campus_eats_app_kt.data.dao.UserDao
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.network.FakeRestaurantApiService
import com.example.campus_eats_app_kt.data.network.MenuItemRequest
import com.example.campus_eats_app_kt.util.NetworkConnectivityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * MenuRepository manages food menu items and vendor discovery.
 */
class MenuRepository(
    private val menuItemDao: MenuItemDao,
    private val userDao: UserDao,
    private val apiService: FakeRestaurantApiService,
    private val connectivityManager: NetworkConnectivityManager,
)
{
    /**
     * Retrieves all menu items for a vendor.
     */
    fun getMenuItemsByVendor(
        vendorId: String,
        sortOrder: String? = null,
    ): Flow<List<MenuItemEntity>> = combine(
        menuItemDao.getMenuItemsByVendor(vendorId),
        fetchRemoteMenu(vendorId, sortOrder),
    ) { local, remote ->
        (local + remote).distinctBy { it.itemId }
    }

    private fun fetchRemoteMenu(vendorId: String, sortOrder: String?): Flow<List<MenuItemEntity>> = flow()
    {
        val numericId = vendorId.toIntOrNull()
        if ((numericId != null) && connectivityManager.hasInternetConnection())
        {
            try
            {
                val response = if (sortOrder == null)
                {
                    apiService.getRestaurantMenu(numericId)
                }
                else
                {
                    apiService.getSortedMenu(numericId, sortOrder)
                }

                if (response.isSuccessful)
                {
                    val items = response.body()?.map()
                    { networkItem ->
                        MenuItemEntity(
                            itemId = networkItem.itemID.toLong(),
                            vendorId = networkItem.restaurantID.toString(),
                            name = networkItem.itemName,
                            description = networkItem.itemDescription,
                            price = networkItem.itemPrice,
                            stock = 99,
                            category = "General",
                            imageUrl = networkItem.imageUrl,
                        )
                    } ?: emptyList()
                    emit(items)
                }
            }
            catch (_: Exception) {}
        }
        else
        {
            emit(emptyList())
        }
    }.onStart { emit(emptyList()) }

    /**
     * Searches for menu items.
     */
    @Suppress("unused")
    fun searchMenuItems(query: String): Flow<List<MenuItemEntity>> = combine(
        menuItemDao.searchMenuItems(query),
        fetchRemoteSearch(query),
    ) { local, remote ->
        (local + remote).distinctBy { it.itemId }
    }

    private fun fetchRemoteSearch(query: String): Flow<List<MenuItemEntity>> = flow()
    {
        if (connectivityManager.hasInternetConnection())
        {
            try
            {
                val response = apiService.searchItemsByName(query)
                if (response.isSuccessful)
                {
                    val items = response.body()?.map()
                    { networkItem ->
                        MenuItemEntity(
                            itemId = networkItem.itemID.toLong(),
                            vendorId = networkItem.restaurantID.toString(),
                            name = networkItem.itemName,
                            description = networkItem.itemDescription,
                            price = networkItem.itemPrice,
                            stock = 99,
                            category = "General",
                            imageUrl = networkItem.imageUrl,
                        )
                    } ?: emptyList()
                    emit(items)
                }
            }
            catch (_: Exception) {}
        }
        else
        {
            emit(emptyList())
        }
    }.onStart { emit(emptyList()) }

    /**
     * Retrieves all vendors.
     */
    fun getAllVendors(): Flow<List<UserEntity>> = combine(
        userDao.getAllUsers().map { users -> users.filter { it.role == UserRole.VENDOR } },
        fetchRemoteVendors(),
    ) { local, remote ->
        (local + remote).distinctBy { it.userId }
    }

    private fun fetchRemoteVendors(): Flow<List<UserEntity>> = flow()
    {
        if (connectivityManager.hasInternetConnection())
        {
            try
            {
                val response = apiService.getAllRestaurants()
                if (response.isSuccessful)
                {
                    val vendors = response.body()?.map()
                    { restaurant ->
                        UserEntity(
                            userId = restaurant.restaurantID.toString(),
                            fullName = restaurant.restaurantName,
                            username = restaurant.restaurantName.lowercase().replace(" ", "_"),
                            email = "contact@${restaurant.restaurantName.lowercase().replace(" ", "")}.com",
                            passwordHash = "[REMOTE]",
                            role = UserRole.VENDOR,
                            shopName = restaurant.restaurantName,
                        )
                    } ?: emptyList()
                    emit(vendors)
                }
            }
            catch (_: Exception) {}
        }
        else
        {
            emit(emptyList())
        }
    }.onStart { emit(emptyList()) }

    suspend fun addMenuItem(item: MenuItemEntity)
    {
        val numericVendorId = item.vendorId.toIntOrNull()
        if (numericVendorId != null)
        {
            try
            {
                apiService.addMenuItem(
                    numericVendorId,
                    MenuItemRequest(
                        itemName = item.name,
                        itemPrice = item.price,
                        itemDescription = item.description,
                        imageUrl = item.imageUrl,
                    ),
                )
            }
            catch (_: Exception) {}
        }
        menuItemDao.insertMenuItem(item)
    }

    suspend fun updateMenuItem(item: MenuItemEntity) = menuItemDao.updateMenuItem(item)
    suspend fun deleteMenuItem(item: MenuItemEntity) = menuItemDao.deleteMenuItem(item)
}
