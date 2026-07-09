package com.example.campus_eats_app_kt.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * VendorMenuViewModel handles menu item management logic for shop owners.
 */
class VendorMenuViewModel(
    private val repository: MenuRepository,
    val vendorId: String
) : ViewModel()
{
    /**
     * Exposes a reactive stream of menu items belonging to the vendor.
     */
    val menuItems: StateFlow<List<MenuItemEntity>> = repository.getMenuItemsByVendor(vendorId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Removes an item from the menu database.
     * 
     * @param item The MenuItemEntity to be deleted.
     */
    fun deleteItem(item: MenuItemEntity)
    {
        viewModelScope.launch {
            repository.deleteMenuItem(item)
        }
    }
}
