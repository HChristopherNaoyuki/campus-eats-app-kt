package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

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
        user = authRepository.login("", "").getOrNull() // In real app, fetch from DB by ID properly
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Hello, ${user?.fullName ?: "User"}.",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "User ID: $userId",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
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
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Row(
                            Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Store,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            Spacer(Modifier.width(20.dp))
                            Text(
                                vendor.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier)
{
    Card(
        modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
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
    Column(Modifier
        .fillMaxSize()
        .padding(24.dp)) {
        Text(
            "Services",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(24.dp))

        when (role) {
            UserRole.STUDENT, UserRole.STANDARD -> {
                ServiceCard(
                    "Order Food",
                    "Browse available campus vendors.",
                    Icons.Rounded.Restaurant,
                    onClick = onReturnHome
                )
                ServiceCard(
                    "My Cart",
                    "Review and checkout items.",
                    Icons.Rounded.ShoppingCart,
                    onClick = onNavigateToCart
                )
            }
            UserRole.VENDOR -> {
                ServiceCard(
                    "Add New Item",
                    "Put a new meal on the menu.",
                    Icons.Rounded.Add,
                    onClick = { onNavigateToAddMenuItem(userId, null) })
                ServiceCard(
                    "Manage Inventory",
                    "View and edit your items.",
                    Icons.Rounded.Inventory,
                    onClick = { onNavigateToVendorMenu(userId) })
            }
            UserRole.ADMIN -> {
                ServiceCard(
                    "User Accounts",
                    "Suspend or activate users.",
                    Icons.Rounded.People,
                    onClick = { /* Implementation */ })
                ServiceCard(
                    "Vendor Shops",
                    "Manage registered vendors.",
                    Icons.Rounded.Store,
                    onClick = { /* Implementation */ })
                ServiceCard(
                    "System Orders",
                    "Full order oversight.",
                    Icons.Rounded.List,
                    onClick = { /* Implementation */ })
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
    val orders by orderRepository.getOrdersForUser(userId).collectAsState(initial = emptyList())
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    Column(Modifier
        .fillMaxSize()
        .padding(24.dp)) {
        Text(
            "Activity Hub",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(24.dp))

        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Receipts", fontWeight = FontWeight.Bold) })
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Reports", fontWeight = FontWeight.Bold) })
        }

        Spacer(Modifier.height(24.dp))

        if (selectedSubTab == 0)
        {
            var filterQuery by remember { mutableStateOf("") }
            val filteredOrders = remember(orders, filterQuery) {
                if (filterQuery.isBlank()) orders
                else orders.filter {
                    it.orderId.toString().contains(filterQuery) || it.status.name.contains(
                        filterQuery,
                        ignoreCase = true
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    label = { Text("Search by ID or Status") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    shape = MaterialTheme.shapes.large
                )

                if (filteredOrders.isEmpty())
                {
                    Box(Modifier
                        .fillMaxSize()
                        .weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            "No records found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                else
                {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredOrders) { order ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Order #${order.orderId}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            "Status: ${order.status.name}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Text(
                                        "R${String.format("%.2f", order.totalAmount)}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        else
        {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Analytics,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "Generate Activity Report",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Export all transaction data for auditing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = {
                        val historyJson = Json.encodeToString(orders)
                        val file = File(
                            context.getExternalFilesDir(null),
                            "campus_eats_activity_${userId}.json"
                        )
                        file.writeText(historyJson)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Export History (JSON)", fontWeight = FontWeight.Bold)
                }
            }
        }
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
    var newPassword by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Update Password",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Enter New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result = authRepository.resetPassword(userId, newPassword)
                            if (result.isSuccess)
                            {
                                newPassword = ""
                                // In real app, show success
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold) 
                }
            }
        }

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Rounded.Logout, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Logout Session", fontWeight = FontWeight.Bold)
        }

        val versionName = try
        {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }
        catch (e: Exception)
        {
            "1.0.0"
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                "Campus Eats v$versionName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.large
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
