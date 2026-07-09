package com.example.campus_eats_app_kt.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * MenuBrowseViewModel handles data retrieval for a vendor's menu.
 * It exposes a reactive list of menu items.
 */
class MenuBrowseViewModel(
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository,
    val userId: String,
    val vendorId: String
) : ViewModel()
{
    /**
     * Exposes a reactive stream of menu items belonging to the vendor.
     */
    val menuItems: StateFlow<List<MenuItemEntity>> = menuRepository.getMenuItemsByVendor(vendorId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Adds an item to the persistent cart in the background.
     * 
     * @param item The MenuItemEntity to be added.
     */
    fun addToCart(item: MenuItemEntity)
    {
        viewModelScope.launch {
            cartRepository.addToCart(userId, item)
        }
    }
}
