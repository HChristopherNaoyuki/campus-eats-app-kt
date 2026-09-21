package com.example.campus_eats_app_kt.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.VendorConflictException
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * MenuBrowseViewModel handles data retrieval for a vendor's menu.
 * Hardened in Batch 2 to handle vendor conflicts in the cart.
 */
class MenuBrowseViewModel(
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository,
    val userId: String,
    val vendorId: String,
) : ViewModel()
{
    private val _error = MutableStateFlow<String?>(value = null)
    val error = _error.asStateFlow()

    /**
     * Exposes a reactive stream of menu items.
     */
    val menuItems: StateFlow<List<MenuItemEntity>> = menuRepository.getMenuItemsByVendor(vendorId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    /**
     * Adds an item to the cart.
     */
    fun addToCart(item: MenuItemEntity)
    {
        viewModelScope.launch()
        {
            try
            {
                cartRepository.addToCart(userId, item)
                _error.value = null
            }
            catch (e: VendorConflictException)
            {
                _error.value = e.message
            }
            catch (_: Exception)
            {
                _error.value = "Unable to add item to cart."
            }
        }
    }

    fun clearError()
    {
        _error.value = null
    }

    fun clearCartAndAdd(item: MenuItemEntity)
    {
        viewModelScope.launch()
        {
            cartRepository.clearCart(userId)
            cartRepository.addToCart(userId, item)
            _error.value = null
        }
    }
}
