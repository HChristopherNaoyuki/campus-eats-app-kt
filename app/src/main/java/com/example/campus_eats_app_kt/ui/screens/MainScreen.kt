package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.MiscellaneousServices
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.campus_eats_app_kt.data.AdminRepository
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.CouponRepository
import com.example.campus_eats_app_kt.data.DebitCardRepository
import com.example.campus_eats_app_kt.data.FeedbackRepository
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.StatsRepository
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.ui.components.HIGTopAppBar

/**
 * MainScreen is the primary navigation hub after authentication.
 * It manages the bottom navigation bar and displays the corresponding role-based tabs.
 */
@Composable
fun MainScreen(
    userId: String,
    role: String,
    authRepository: AuthRepository,
    menuRepository: MenuRepository,
    cartRepository: CartRepository,
    orderRepository: OrderRepository,
    adminRepository: AdminRepository,
    statsRepository: StatsRepository,
    feedbackRepository: FeedbackRepository,
    couponRepository: CouponRepository,
    debitCardRepository: DebitCardRepository,
    onLogout: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToVendorMenu: (String) -> Unit,
    onNavigateToAddMenuItem: (String, Long?) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToMenuBrowse: (String, String) -> Unit,
    onNavigateToVendorBrowse: (String) -> Unit
)
{
    var selectedTab by remember { mutableIntStateOf(0) }
    val userRole = remember(role) { UserRole.valueOf(role) }

    Scaffold(
        topBar = {
            val title = when (selectedTab)
            {
                0 -> "Home"
                1 -> "Services"
                2 -> "Activity"
                else -> "Settings"
            }
            HIGTopAppBar(title = title)
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                // Home Tab
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unselectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                // Services Tab
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Rounded.MiscellaneousServices, contentDescription = "Services") },
                    label = { Text("Services") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )
                // Activity Tab
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Rounded.List, contentDescription = "Activity") },
                    label = { Text("Activity") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )
                // Settings Tab
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab)
            {
                0 -> HomeScreenTab(
                    userId = userId,
                    role = userRole,
                    authRepository = authRepository,
                    statsRepository = statsRepository,
                    menuRepository = menuRepository,
                    onNavigateToMenuBrowse = onNavigateToMenuBrowse,
                    onExploreVendors = { onNavigateToVendorBrowse(userId) }
                )
                1 -> ServicesScreenTab(
                    userId = userId,
                    role = userRole,
                    menuRepository = menuRepository,
                    adminRepository = adminRepository,
                    orderRepository = orderRepository,
                    statsRepository = statsRepository,
                    onNavigateToVendorMenu = onNavigateToVendorMenu,
                    onNavigateToMenuBrowse = onNavigateToMenuBrowse,
                    onNavigateToCart = onNavigateToCart,
                    onNavigateToAddMenuItem = onNavigateToAddMenuItem,
                    onReturnHome = { selectedTab = 0 }
                )
                2 -> ActivityScreenTab(
                    userId = userId,
                    role = role,
                    orderRepository = orderRepository,
                    cartRepository = cartRepository,
                    statsRepository = statsRepository,
                    adminRepository = adminRepository,
                    onNavigateToCheckout = onNavigateToCheckout,
                    onReturnHome = { selectedTab = 0 }
                )
                3 -> SettingsScreenTab(
                    userId = userId,
                    role = userRole,
                    authRepository = authRepository,
                    feedbackRepository = feedbackRepository,
                    couponRepository = couponRepository,
                    adminRepository = adminRepository,
                    debitCardRepository = debitCardRepository,
                    onLogout = onLogout
                )
            }
        }
    }
}
