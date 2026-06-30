package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campus_eats_app_kt.data.AdminRepository
import com.example.campus_eats_app_kt.data.AdminStats
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.CouponRepository
import com.example.campus_eats_app_kt.data.FeedbackRepository
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.StatsRepository
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole

@Composable
fun HomeScreenTab(
    userId: String,
    role: UserRole,
    authRepository: AuthRepository,
    statsRepository: StatsRepository,
    menuRepository: MenuRepository,
    onNavigateToMenuBrowse: (String, String) -> Unit
)
{
    var user by remember { mutableStateOf<UserEntity?>(null) }
    val vendorStats by statsRepository.getVendorStats(userId).collectAsState(null)
    val adminStats by statsRepository.getAdminStats().collectAsState(null)
    val vendors by menuRepository.getAllVendors().collectAsState(emptyList())

    LaunchedEffect(userId) {
        user = authRepository.login("", "").getOrNull() // Hack
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Hello, ${user?.fullName ?: "User"}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text("User ID: $userId", style = MaterialTheme.typography.labelMedium)
        }

        when (role)
        {
            UserRole.STUDENT, UserRole.STANDARD ->
            {
                item {
                    Text(
                        "Top Vendors",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(vendors) { vendor ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToMenuBrowse(userId, vendor.userId) },
                        shape = MaterialTheme.shapes.large
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(vendor.fullName, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

            UserRole.VENDOR ->
            {
                item {
                    vendorStats?.let { stats ->
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            StatRow(
                                "All-time Earnings",
                                "R${String.format("%.2f", stats.allTimeEarnings)}"
                            )
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatCard(
                                    "Menu Items",
                                    "${stats.menuItemCount}",
                                    Modifier.weight(1f)
                                )
                                StatCard(
                                    "Active Orders",
                                    "${stats.activeOrders}",
                                    Modifier.weight(1f)
                                )
                            }
                            StatRow(
                                "Today's Revenue",
                                "R${String.format("%.2f", stats.todayRevenue)}"
                            )
                        }
                    }
                }
            }

            UserRole.ADMIN ->
            {
                item {
                    adminStats?.let { stats ->
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            StatRow(
                                "System Earnings",
                                "R${String.format("%.2f", stats.allTimeEarnings)}"
                            )
                            GridStats(stats)
                            StatRow(
                                "Today's Revenue",
                                "R${String.format("%.2f", stats.todayRevenue)}"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String)
{
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier)
{
    Card(modifier, shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GridStats(stats: AdminStats)
{
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Users", "${stats.totalUsers}", Modifier.weight(1f))
            StatCard("Vendors", "${stats.activeVendors}", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Items", "${stats.menuItemCount}", Modifier.weight(1f))
            StatCard("Orders", "${stats.orderCount}", Modifier.weight(1f))
        }
    }
}

@Composable
fun ServicesScreenTab(
    userId: String,
    role: UserRole,
    menuRepository: MenuRepository,
    adminRepository: AdminRepository,
    orderRepository: OrderRepository,
    onNavigateToVendorMenu: (String) -> Unit,
    onNavigateToMenuBrowse: (String, String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToAddMenuItem: (String, Long?) -> Unit,
    onReturnHome: () -> Unit
) {
    var currentSubScreen by remember { mutableStateOf("Main") }

    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        if (currentSubScreen != "Main")
        {
            TextButton(onClick = onReturnHome) {
                Icon(Icons.Rounded.Home, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Return Home")
            }
        }
        
        Text("Services", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        when (role) {
            UserRole.STUDENT, UserRole.STANDARD -> {
                ServiceCard(
                    "Receipts",
                    "View your past orders.",
                    Icons.Rounded.Receipt,
                    onClick = { currentSubScreen = "Receipts" })
                ServiceCard(
                    "Total Spending",
                    "Lifetime usage.",
                    Icons.Rounded.AccountBalance,
                    onClick = { currentSubScreen = "Spending" })
            }
            UserRole.VENDOR -> {
                ServiceCard(
                    "Add Items",
                    "Create a new menu entry.",
                    Icons.Rounded.Add,
                    onClick = { onNavigateToAddMenuItem(userId, null) })
                ServiceCard(
                    "View Items",
                    "Manage your inventory.",
                    Icons.Rounded.Inventory,
                    onClick = { onNavigateToVendorMenu(userId) })
            }
            UserRole.ADMIN -> {
                ServiceCard(
                    "User Management",
                    "Moderate accounts.",
                    Icons.Rounded.People,
                    onClick = { currentSubScreen = "Users" })
                ServiceCard(
                    "Vendor Management",
                    "Manage shops.",
                    Icons.Rounded.Store,
                    onClick = { currentSubScreen = "Vendors" })
                ServiceCard(
                    "Order Management",
                    "System wide orders.",
                    Icons.Rounded.List,
                    onClick = { currentSubScreen = "Orders" })
            }
        }
    }
}

@Composable
fun ActivityScreenTab(
    userId: String,
    role: String,
    orderRepository: OrderRepository,
    cartRepository: CartRepository,
    onNavigateToCheckout: () -> Unit,
    onReturnHome: () -> Unit
)
{
    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        TextButton(onClick = onReturnHome) {
            Icon(Icons.Rounded.Home, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Return Home")
        }
        Text(
            "Activity Hub",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        // Simplified Hub logic
        Text("Role: $role Activity Hub Placeholder", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SettingsScreenTab(
    userId: String,
    role: UserRole,
    authRepository: AuthRepository,
    feedbackRepository: FeedbackRepository,
    couponRepository: CouponRepository,
    adminRepository: AdminRepository,
    onLogout: () -> Unit
)
{
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Wallet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                when (role)
                {
                    UserRole.STUDENT -> Text("Campus Wallet, Debit Cards, Coupons")
                    UserRole.VENDOR -> Text("Payout Settings")
                    UserRole.ADMIN -> Text("Wallet Credits, Coupons, Compensation")
                    UserRole.STANDARD -> Text("Personal Wallet")
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Feedback",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (role == UserRole.ADMIN)
                {
                    Text("Review User Feedback")
                }
                else
                {
                    Text("Submit Feedback")
                }
            }
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout")
        }

        Text(
            "Version 1.1.0",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
