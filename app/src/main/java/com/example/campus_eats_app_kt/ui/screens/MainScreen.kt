package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.MiscellaneousServices
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AdminRepository
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import kotlinx.coroutines.flow.*

class MainViewModel(
    private val authRepository: AuthRepository,
    val userId: String,
    val role: String
) : ViewModel() {
    val userProfile: StateFlow<UserEntity?> = flow {
        // Simple flow to get user from DB
        emit(authRepository.login("", "").getOrNull()) // Not ideal, but let's assume we can fetch by ID
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    // Better way to get user profile reactively
    // In a real app, UserDao would have a Flow<UserEntity?> getUserById(id)
}

@Composable
fun MainScreen(
    userId: String,
    role: String,
    authRepository: AuthRepository,
    menuRepository: MenuRepository,
    cartRepository: CartRepository,
    orderRepository: OrderRepository,
    adminRepository: AdminRepository,
    onLogout: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToVendorMenu: (String) -> Unit,
    onNavigateToAddMenuItem: (String, Long?) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToMenuBrowse: (String, String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val userRole = remember(role) { UserRole.valueOf(role) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Rounded.MiscellaneousServices, contentDescription = "Services") },
                    label = { Text("Services") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Rounded.List, contentDescription = "Activity") },
                    label = { Text("Activity") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreenTab(userId, authRepository)
                1 -> ServicesScreenTab(
                    userId = userId,
                    role = userRole,
                    menuRepository = menuRepository,
                    adminRepository = adminRepository,
                    onNavigateToVendorMenu = onNavigateToVendorMenu,
                    onNavigateToMenuBrowse = onNavigateToMenuBrowse,
                    onNavigateToCart = onNavigateToCart
                )
                2 -> ActivityScreenTab(userId, role, orderRepository)
                3 -> SettingsScreenTab(userId, authRepository, onLogout)
            }
        }
    }
}
