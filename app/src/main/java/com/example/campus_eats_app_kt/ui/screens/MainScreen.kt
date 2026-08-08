package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    onNavigateToMenuBrowse: (String, String) -> Unit
)
{
    var selectedTab by remember { mutableIntStateOf(0) }
    val userRole = remember(role) {
        UserRole.entries.find { it.name == role } ?: UserRole.STANDARD
    }

    Scaffold(
        topBar = {
            val title = when (selectedTab)
            {
                0 -> "Home"
                1 -> "Browse"
                2 -> "Orders"
                3 -> "Reports"
                else -> "Settings"
            }
            HIGTopAppBar(title = title)
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp
            )
            {
                // Home Tab
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                // Browse Tab
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Rounded.Search, contentDescription = "Browse") },
                    label = { Text("Browse") }
                )
                // Orders Tab
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Rounded.History, contentDescription = "Orders") },
                    label = { Text("Orders") }
                )
                // Reports Tab
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Reports") },
                    label = { Text("Reports") }
                )
                // Settings Tab
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    )
    { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding))
        {
            when (selectedTab)
            {
                0 -> HomeScreenTab(
                    userId = userId,
                    role = userRole,
                    authRepository = authRepository,
                    statsRepository = statsRepository,
                    onExploreVendors = { selectedTab = 1 }
                )
                1 -> ServicesScreenTab(
                    userId = userId,
                    role = userRole,
                    menuRepository = menuRepository,
                    adminRepository = adminRepository,
                    orderRepository = orderRepository,
                    onNavigateToVendorMenu = onNavigateToVendorMenu,
                    onNavigateToMenuBrowse = onNavigateToMenuBrowse,
                    onNavigateToAddMenuItem = onNavigateToAddMenuItem,
                    onReturnHome = { selectedTab = 0 }
                )
                2 -> ActivityScreenTab(
                    userId = userId,
                    role = role,
                    orderRepository = orderRepository,
                    cartRepository = cartRepository,
                    statsRepository = statsRepository,
                    onNavigateToCheckout = onNavigateToCheckout,
                    onReturnHome = { selectedTab = 0 }
                )
                3 -> ActivityScreenTab( // Reports Tab reused same tab logic with different active view
                    userId = userId,
                    role = role,
                    orderRepository = orderRepository,
                    cartRepository = cartRepository,
                    statsRepository = statsRepository,
                    onNavigateToCheckout = onNavigateToCheckout,
                    onReturnHome = { selectedTab = 0 }
                )

                4 -> SettingsScreenTab(
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
