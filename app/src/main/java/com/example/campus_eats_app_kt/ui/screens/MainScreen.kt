package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import com.example.campus_eats_app_kt.ui.theme.DesignSystem
import com.example.campus_eats_app_kt.util.LanguageManager

/**
 * MainScreen is the primary navigation hub after authentication.
 * It manages the bottom navigation bar and displays the corresponding role-based tabs.
 * This version supports dynamic language toggling between English and Afrikaans.
 */
@Composable
fun MainScreen(
    userId: String,
    role: String,
    authRepository: AuthRepository,
    menuRepository: MenuRepository,
    cartRepository: CartRepository,
    orderRepository: OrderRepository,
    adminViewModel: AdminViewModel,
    statsRepository: StatsRepository,
    feedbackRepository: FeedbackRepository,
    couponRepository: CouponRepository,
    debitCardRepository: DebitCardRepository,
    onLogout: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToVendorMenu: (String) -> Unit,
    onNavigateToAddMenuItem: (String, Long?) -> Unit,
    onNavigateToMenuBrowse: (String, String) -> Unit,
)
{
    var selectedTab by remember { mutableIntStateOf(0) }
    val userRole = remember(role)
    {
        UserRole.entries.find { it.name == role } ?: UserRole.STANDARD
    }

    // Requirement: Synchronize permitted data with the database every 10 seconds.
    LaunchedEffect(userId)
    {
        authRepository.startBackgroundSync(userId, this)
    }

    Scaffold(
        topBar = {
            val title = when (selectedTab)
            {
                0 -> LanguageManager.getString("Home", "Tuis")
                1 -> LanguageManager.getString("Browse", "Snuffel")
                2 -> LanguageManager.getString("Orders", "Bestellings")
                3 -> LanguageManager.getString("Reports", "Verslae")
                else -> LanguageManager.getString("Settings", "Instellings")
            }
            HIGTopAppBar(title = title)
        },
        bottomBar = {
            // Requirement: Dynamic Tab Bars with a horizontal margin of 3.7 mm (~23.3 dp).
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = DesignSystem.Spacing.tabBarHorizontalMargin,
                        vertical = 8.dp,
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 4.dp,
            )
            {
                NavigationBar(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                )
                {
                    // Home Tab
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                        label = { Text(LanguageManager.getString("Home", "Tuis")) },
                    )
                    // Browse Tab
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Rounded.Search, contentDescription = "Browse") },
                        label = { Text(LanguageManager.getString("Browse", "Snuffel")) },
                    )
                    // Orders Tab
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Rounded.History, contentDescription = "Orders") },
                        label = { Text(LanguageManager.getString("Orders", "Bestellings")) },
                    )
                    // Reports Tab
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Reports") },
                        label = { Text(LanguageManager.getString("Reports", "Verslae")) },
                    )
                    // Settings Tab
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                        label = { Text(LanguageManager.getString("Settings", "Instellings")) },
                    )
                }
            }
        },
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
                    menuRepository = menuRepository,
                    onNavigateToMenuBrowse = onNavigateToMenuBrowse,
                )
                {
                    selectedTab = 1
                }

                1 -> ServicesScreenTab(
                    userId = userId,
                    role = userRole,
                    menuRepository = menuRepository,
                    adminViewModel = adminViewModel,
                    orderRepository = orderRepository,
                    onNavigateToVendorMenu = onNavigateToVendorMenu,
                    onNavigateToMenuBrowse = onNavigateToMenuBrowse,
                    onNavigateToAddMenuItem = onNavigateToAddMenuItem,
                    onReturnHome = { selectedTab = 0 },
                )

                2 -> ActivityScreenTab(
                    userId = userId,
                    role = role,
                    orderRepository = orderRepository,
                    cartRepository = cartRepository,
                    onNavigateToCheckout = onNavigateToCheckout,
                    onReturnHome = { selectedTab = 0 },
                )

                3 -> ActivityScreenTab(
                    userId = userId,
                    role = role,
                    orderRepository = orderRepository,
                    cartRepository = cartRepository,
                    onNavigateToCheckout = onNavigateToCheckout,
                    onReturnHome = { selectedTab = 0 },
                )

                4 -> SettingsScreenTab(
                    userId = userId,
                    role = userRole,
                    authRepository = authRepository,
                    feedbackRepository = feedbackRepository,
                    couponRepository = couponRepository,
                    adminViewModel = adminViewModel,
                    debitCardRepository = debitCardRepository,
                    onLogout = onLogout,
                )
            }
        }
    }
}
