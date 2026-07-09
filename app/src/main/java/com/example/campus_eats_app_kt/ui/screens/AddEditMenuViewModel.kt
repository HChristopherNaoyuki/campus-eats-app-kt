package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * AddEditMenuViewModel manages the temporary state of a menu item being created or updated.
 * It pre-fills data if an existing item ID is provided.
 */
class AddEditMenuViewModel(
    private val repository: MenuRepository,
    val vendorId: String,
    val itemId: Long?
) : ViewModel()
{
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var price by mutableStateOf("")
    var stock by mutableStateOf("")
    var category by mutableStateOf("")
    var imageUrl by mutableStateOf("")

    init
    {
        if (itemId != null)
        {
            viewModelScope.launch {
                val item = repository.getMenuItemsByVendor(vendorId).firstOrNull()
                    ?.find { it.itemId == itemId }
                item?.let {
                    name = it.name
                    description = it.description
                    price = it.price.toString()
                    stock = it.stock.toString()
                    category = it.category
                    imageUrl = it.imageUrl ?: ""
                }
            }
        }
    }

    /**
     * Persists the item to the database after validation.
     */
    fun saveItem(onSuccess: () -> Unit)
    {
        if (name.isBlank() || price.toDoubleOrNull() == null || stock.toIntOrNull() == null || category.isBlank())
        {
            return
        }

        viewModelScope.launch {
            val item = MenuItemEntity(
                itemId = itemId ?: 0,
                vendorId = vendorId,
                name = name,
                description = description,
                price = price.toDoubleOrNull() ?: 0.0,
                stock = stock.toIntOrNull() ?: 0,
                category = category,
                imageUrl = imageUrl.ifBlank { null }
            )

            if (itemId == null)
            {
                repository.addMenuItem(item)
            }
            else
            {
                repository.updateMenuItem(item)
            }
            onSuccess()
        }
    }
}
