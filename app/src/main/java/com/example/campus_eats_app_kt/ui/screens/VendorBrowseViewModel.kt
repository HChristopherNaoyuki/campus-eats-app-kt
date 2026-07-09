package com.example.campus_eats_app_kt.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.entity.UserEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * VendorBrowseViewModel provides a reactive stream of all vendors for customer discovery.
 */
class VendorBrowseViewModel(repository: MenuRepository) : ViewModel()
{
    /**
     * Exposes a reactive stream of all vendors registered in the system.
     */
    val vendors: StateFlow<List<UserEntity>> = repository.getAllVendors()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
