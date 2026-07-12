package com.example.campus_eats_app_kt.ui.screens

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RemoveShoppingCart
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campus_eats_app_kt.data.AdminRepository
import com.example.campus_eats_app_kt.data.AdminStats
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.CouponRepository
import com.example.campus_eats_app_kt.data.DebitCardRepository
import com.example.campus_eats_app_kt.data.FeedbackRepository
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.StatsRepository
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import com.example.campus_eats_app_kt.ui.components.HIGButton
import com.example.campus_eats_app_kt.ui.components.HIGCard
import com.example.campus_eats_app_kt.ui.components.HIGServiceRow
import com.example.campus_eats_app_kt.ui.theme.DesignSystem
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * HomeScreenTab provides the primary landing page for authenticated users,
 * displaying greetings and role-specific dashboard metrics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenTab(
    userId: String,
    role: UserRole,
    authRepository: AuthRepository,
    statsRepository: StatsRepository,
    menuRepository: MenuRepository,
    onNavigateToMenuBrowse: (String, String) -> Unit,
    onExploreVendors: () -> Unit
)
{
    val user by authRepository.getUserFlow(userId).collectAsState(null)
    val vendorStats by statsRepository.getVendorStats(userId).collectAsState(null)
    val adminStats by statsRepository.getAdminStats().collectAsState(null)
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(DesignSystem.Spacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.large)
    ) {
        // Welcome Header Section
        item {
            Column {
                Text(
                    text = "Hello, ${user?.fullName ?: "User"}.",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-1).sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "User ID: $userId",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            try
                            {
                                val clipboard =
                                    context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("User ID", userId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(
                                    context,
                                    "User ID copied to clipboard",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            catch (e: Exception)
                            {
                                Toast.makeText(context, "Failed to copy ID", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy User ID",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (role == UserRole.VENDOR && user?.shopName != null)
                {
                    Text(
                        text = "Operating as ${user?.shopName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Shop Status Module (Vendor-Exclusive)
        if (role == UserRole.VENDOR)
        {
            item {
                Text(
                    text = "Shop Visibility",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)
                ) {
                    ShopStatus.entries.forEach { shopStatus ->
                        val isSelected = user?.shopStatus == shopStatus
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                coroutineScope.launch {
                                    authRepository.updateShopStatus(userId, shopStatus)
                                }
                            },
                            label = {
                                Text(
                                    text = shopStatus.name.replace("_", " ").lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }
            }
        }

        // Analytical Dashboard Metrics
        when (role)
        {
            UserRole.VENDOR ->
            {
                item {
                    vendorStats?.let { stats ->
                        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
                            StatCardFull(
                                label = "All-time Earnings",
                                value = "R${String.format("%.2f", stats.allTimeEarnings)}"
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
                            ) {
                                StatCardHalf(
                                    label = "Menu Items",
                                    value = "${stats.menuItemCount}",
                                    modifier = Modifier.weight(1f)
                                )
                                StatCardHalf(
                                    label = "Active Orders",
                                    value = "${stats.activeOrders}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            StatCardFull(
                                label = "Today's Revenue",
                                value = "R${String.format("%.2f", stats.todayRevenue)}",
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }
                    }
                }
            }

            UserRole.ADMIN ->
            {
                item {
                    adminStats?.let { stats ->
                        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
                            StatCardFull(
                                label = "System-wide Earnings",
                                value = "R${String.format("%.2f", stats.allTimeEarnings)}"
                            )
                            AdminGridStats(stats = stats)
                            StatCardFull(
                                label = "Today's Summary",
                                value = "R${String.format("%.2f", stats.todayRevenue)}",
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }
                    }
                }
            }

            UserRole.STUDENT, UserRole.STANDARD ->
            {
                item {
                    Text(
                        text = "Browse Campus Dining",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                }
                item {
                    HIGServiceRow(
                        title = "Explore Vendors",
                        description = "View all available shops and their menus.",
                        icon = Icons.Rounded.Store,
                        onClick = onExploreVendors
                    )
                }
            }
        }
    }
}

@Composable
fun StatCardFull(
    label: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer
)
{
    HIGCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = containerColor
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                letterSpacing = (-1).sp
            )
        }
    }
}

@Composable
fun StatCardHalf(label: String, value: String, modifier: Modifier = Modifier)
{
    HIGCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdminGridStats(stats: AdminStats)
{
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
        ) {
            StatCardHalf(
                label = "Users",
                value = "${stats.totalUsers}",
                modifier = Modifier.fillMaxWidth()
            )
            StatCardHalf(
                label = "Vendors",
                value = "${stats.activeVendors}",
                modifier = Modifier.fillMaxWidth()
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
        ) {
            StatCardHalf(
                label = "Menu Items",
                value = "${stats.menuItemCount}",
                modifier = Modifier.fillMaxWidth()
            )
            StatCardHalf(
                label = "Total Orders",
                value = "${stats.orderCount}",
                modifier = Modifier.fillMaxWidth()
            )
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
    statsRepository: StatsRepository,
    onNavigateToVendorMenu: (String) -> Unit,
    onNavigateToMenuBrowse: (String, String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToAddMenuItem: (String, Long?) -> Unit,
    onReturnHome: () -> Unit
)
{
    var activeView by remember { mutableStateOf("Main") }

    if (activeView == "Main")
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.screenPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Principle: Aesthetic Integrity - Removed redundant heading to reduce visual clutter.

            when (role)
            {
                UserRole.STUDENT, UserRole.STANDARD ->
                {
                    HIGServiceRow(
                        title = "Vendors",
                        description = "Browse available campus dining options.",
                        icon = Icons.Rounded.Store,
                        onClick = { activeView = "VendorsList" })
                    HIGServiceRow(
                        title = "Order Receipts",
                        description = "Audit your past transaction history.",
                        icon = Icons.Rounded.Receipt,
                        onClick = { activeView = "Receipts" })
                    HIGServiceRow(
                        title = "Cumulative Spending",
                        description = "Monitor your total platform expenditure.",
                        icon = Icons.Rounded.AccountBalance,
                        onClick = { activeView = "Spending" })
                }

                UserRole.VENDOR ->
                {
                    HIGServiceRow(
                        title = "New Menu Item",
                        description = "Add a fresh food offering to your shop.",
                        icon = Icons.Rounded.Add,
                        onClick = { onNavigateToAddMenuItem(userId, null) })
                    HIGServiceRow(
                        title = "Inventory Manager",
                        description = "Update and monitor your current stock.",
                        icon = Icons.Rounded.Inventory,
                        onClick = { onNavigateToVendorMenu(userId) })
                }

                UserRole.ADMIN ->
                {
                    HIGServiceRow(
                        title = "User Directory",
                        description = "Manage and moderate platform accounts.",
                        icon = Icons.Rounded.People,
                        onClick = { activeView = "Users" })
                    HIGServiceRow(
                        title = "Vendor Directory",
                        description = "Manage campus shop registry and status.",
                        icon = Icons.Rounded.Store,
                        onClick = { activeView = "Vendors" })
                    HIGServiceRow(
                        title = "System Orders",
                        description = "Supervise all active and past transactions.",
                        icon = Icons.Rounded.List,
                        onClick = { activeView = "Orders" })
                }
            }
        }
    }
    else
    {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignSystem.Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeView = "Main" }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = activeView,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onReturnHome) {
                    Text(text = "Home")
                }
            }

            Box(modifier = Modifier
                .weight(1f)
                .padding(horizontal = DesignSystem.Spacing.medium)) {
                when (activeView)
                {
                    "VendorsList" -> StudentVendorList(
                        userId,
                        menuRepository,
                        onNavigateToMenuBrowse
                    )
                    "Users" -> AdminUserManagement(adminRepository = adminRepository)
                    "Vendors" -> AdminVendorManagement(adminRepository = adminRepository)
                    "Orders" -> AdminOrderManagement(orderRepository = orderRepository)
                    "Receipts" -> StudentReceipts(
                        userId = userId,
                        orderRepository = orderRepository
                    )

                    "Spending" -> StudentTotalSpending(
                        userId = userId,
                        orderRepository = orderRepository
                    )
                }
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
    statsRepository: StatsRepository,
    adminRepository: AdminRepository,
    onNavigateToCheckout: () -> Unit,
    onReturnHome: () -> Unit
)
{
    var currentHubView by remember { mutableStateOf("Main") }
    val userRole = remember(role) { UserRole.valueOf(role) }

    if (currentHubView == "Main")
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.screenPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Principle: Aesthetic Integrity - Removed redundant heading.

            when (userRole)
            {
                UserRole.STUDENT, UserRole.STANDARD ->
                {
                    HIGServiceRow(
                        title = "Active Cart",
                        description = "Complete your purchase and track status.",
                        icon = Icons.Rounded.ShoppingCart,
                        onClick = { currentHubView = "Current" }
                    )
                    HIGServiceRow(
                        title = "Order History",
                        description = "Comprehensive list of all past meals.",
                        icon = Icons.Rounded.History,
                        onClick = { currentHubView = "ReceiptsHub" }
                    )
                    HIGServiceRow(
                        title = "Analytics",
                        description = "Personal consumption trends and reports.",
                        icon = Icons.Rounded.Analytics,
                        onClick = { currentHubView = "ReportsHub" }
                    )
                }

                UserRole.VENDOR ->
                {
                    HIGServiceRow(
                        title = "Live Orders",
                        description = "Fulfill pending and active customer tasks.",
                        icon = Icons.Rounded.ListAlt,
                        onClick = { currentHubView = "VendorOrders" }
                    )
                    HIGServiceRow(
                        title = "Financial Reports",
                        description = "Detailed revenue and growth analytics.",
                        icon = Icons.Rounded.BarChart,
                        onClick = { currentHubView = "VendorReports" }
                    )
                }

                UserRole.ADMIN ->
                {
                    HIGServiceRow(
                        title = "Global Receipts",
                        description = "Audit every transaction on the platform.",
                        icon = Icons.Rounded.ReceiptLong,
                        onClick = { currentHubView = "AdminReceipts" }
                    )
                    HIGServiceRow(
                        title = "System Summary",
                        description = "High-level financial aggregates.",
                        icon = Icons.Rounded.AccountBalance,
                        onClick = { currentHubView = "AdminSummary" }
                    )
                    HIGServiceRow(
                        title = "Insight Reports",
                        description = "Advanced user and revenue analytics.",
                        icon = Icons.Rounded.Assessment,
                        onClick = { currentHubView = "AdminReports" }
                    )
                }
            }
        }
    }
    else
    {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignSystem.Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentHubView = "Main" }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = currentHubView,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onReturnHome) { Text(text = "Home") }
            }

            Box(modifier = Modifier
                .weight(1f)
                .padding(horizontal = DesignSystem.Spacing.medium)) {
                when (currentHubView)
                {
                    "Current" -> StudentCurrentOrderHub(
                        userId,
                        cartRepository,
                        onNavigateToCheckout,
                        onReturnHome
                    )

                    "ReceiptsHub" -> StudentReceipts(userId, orderRepository)
                    "ReportsHub" -> StudentActivityReports(userId, orderRepository)
                    "VendorOrders" -> VendorOrderHub(userId, orderRepository)
                    "VendorReports" -> VendorReportHub(userId, orderRepository)
                    "AdminReceipts" -> AdminReceiptsHub(orderRepository)
                    "AdminSummary" -> AdminGlobalSummary(orderRepository)
                    "AdminReports" -> AdminReportHub(statsRepository, adminRepository, onReturnHome)
                }
            }
        }
    }
}

@Composable
fun StudentVendorList(
    userId: String,
    menuRepository: MenuRepository,
    onNavigateToMenuBrowse: (String, String) -> Unit
)
{
    val vendors by menuRepository.getAllVendors().collectAsState(emptyList())
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
        contentPadding = PaddingValues(bottom = DesignSystem.Spacing.large)
    ) {
        items(vendors) { vendor ->
            HIGCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigateToMenuBrowse(userId, vendor.userId) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.medium))
                    Column {
                        Text(
                            text = vendor.shopName ?: vendor.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to browse menu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserManagement(adminRepository: AdminRepository)
{
    val users by adminRepository.getAllUsers().collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
        contentPadding = PaddingValues(bottom = DesignSystem.Spacing.large)
    ) {
        items(users) { user ->
            HIGCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "ID: ${user.userId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${user.role.name} • ${user.status.name}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (user.status == UserStatus.ACTIVE) adminRepository.suspendUser(
                                        user.userId
                                    )
                                    else adminRepository.activateUser(user.userId)
                                }
                            }
                        ) {
                            Text(text = if (user.status == UserStatus.ACTIVE) "Suspend" else "Activate")
                        }

                        TextButton(
                            onClick = {
                                coroutineScope.launch { adminRepository.deleteUser(user) }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminVendorManagement(adminRepository: AdminRepository)
{
    val users by adminRepository.getAllUsers().collectAsState(emptyList())
    val vendors = users.filter { it.role == UserRole.VENDOR }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
        contentPadding = PaddingValues(bottom = DesignSystem.Spacing.large)
    ) {
        items(vendors) { vendor ->
            HIGCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = vendor.shopName ?: vendor.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = vendor.email, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "Status: ${vendor.status.name}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun AdminOrderManagement(orderRepository: OrderRepository)
{
    val orders by orderRepository.getAllOrders().collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
        contentPadding = PaddingValues(bottom = DesignSystem.Spacing.large)
    ) {
        items(orders) { order ->
            var expanded by remember { mutableStateOf(false) }
            HIGCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Order #${order.orderId}", fontWeight = FontWeight.Bold)
                        Text(
                            text = "R${String.format("%.2f", order.totalAmount)}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "Status: ${order.status.name}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Box(modifier = Modifier.align(Alignment.End)) {
                        TextButton(onClick = { expanded = true }) {
                            Text(text = "Update Status")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            OrderStatus.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(text = status.name) },
                                    onClick = {
                                        coroutineScope.launch {
                                            orderRepository.updateOrderStatus(order, status)
                                            expanded = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentReceipts(userId: String, orderRepository: OrderRepository)
{
    val orders by orderRepository.getOrdersForUser(userId).collectAsState(emptyList())

    var selectedMonth by remember { mutableIntStateOf(-1) }
    var selectedYear by remember { mutableIntStateOf(-1) }
    var sortBy by remember { mutableStateOf("Date (Newest)") }

    val months = listOf(
        "All Months",
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec"
    )
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = listOf("All Years") + (currentYear downTo currentYear - 5).map { it.toString() }
    val sortOptions =
        listOf("Amount (High to Low)", "Amount (Low to High)", "Date (Newest)", "Date (Oldest)")

    val filteredOrders = remember(orders, selectedMonth, selectedYear, sortBy) {
        orders.filter { order ->
            val cal = Calendar.getInstance().apply { timeInMillis = order.timestamp }
            val monthMatch = selectedMonth == -1 || cal.get(Calendar.MONTH) == selectedMonth
            val yearMatch = selectedYear == -1 || cal.get(Calendar.YEAR) == selectedYear
            monthMatch && yearMatch
        }.let { list ->
            when (sortBy)
            {
                "Amount (High to Low)" -> list.sortedByDescending { it.totalAmount }
                "Amount (Low to High)" -> list.sortedBy { it.totalAmount }
                "Date (Newest)" -> list.sortedByDescending { it.timestamp }
                "Date (Oldest)" -> list.sortedBy { it.timestamp }
                else -> list
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
        Row(horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)) {
            MinimalDropdown(
                label = "Month",
                selectedOption = if (selectedMonth == -1) "All" else months[selectedMonth + 1],
                options = months,
                onOptionSelected = { selectedMonth = months.indexOf(it) - 1 },
                modifier = Modifier.weight(1f)
            )
            MinimalDropdown(
                label = "Year",
                selectedOption = if (selectedYear == -1) "All" else selectedYear.toString(),
                options = years,
                onOptionSelected = { selectedYear = if (it == "All Years") -1 else it.toInt() },
                modifier = Modifier.weight(1f)
            )
        }

        MinimalDropdown(
            label = "Sort",
            selectedOption = sortBy,
            options = sortOptions,
            onOptionSelected = { sortBy = it },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small),
            contentPadding = PaddingValues(bottom = DesignSystem.Spacing.large)
        ) {
            if (filteredOrders.isEmpty())
            {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(DesignSystem.Spacing.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No records found.", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            items(filteredOrders) { order ->
                HIGCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = "Order #${order.orderId}", fontWeight = FontWeight.Bold)
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
                                    Date(order.timestamp)
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            text = "R${String.format("%.2f", order.totalAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentTotalSpending(userId: String, orderRepository: OrderRepository)
{
    val orders by orderRepository.getOrdersForUser(userId).collectAsState(emptyList())
    val total = orders.sumOf { it.totalAmount }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Lifetime Spending",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "R${String.format("%.2f", total)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-1).sp
            )
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
    debitCardRepository: DebitCardRepository,
    onLogout: () -> Unit
)
{
    var activeSettingView by remember { mutableStateOf("Main") }
    val user by authRepository.getUserFlow(userId).collectAsState(null)
    val coroutineScope = rememberCoroutineScope()

    if (activeSettingView == "Main")
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.screenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.large)
        ) {
            // Principle: Aesthetic Integrity - Removed redundant heading.

            // Account Group
            HIGCard(modifier = Modifier.fillMaxWidth()) {
                var newEmail by remember { mutableStateOf(user?.email ?: "") }
                var newPassword by remember { mutableStateOf("") }

                LaunchedEffect(user) { if (newEmail.isEmpty()) newEmail = user?.email ?: "" }

                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
                    Text(
                        text = "Profile Identity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Security Key") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        shape = MaterialTheme.shapes.medium
                    )
                    Button(onClick = {
                        coroutineScope.launch {
                            authRepository.updateProfile(
                                userId,
                                newEmail,
                                newPassword
                            ); newPassword = ""
                        }
                    }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                        Text("Update Credentials")
                    }
                }
            }

            // Financial Group
            HIGCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)) {
                    Text(
                        text = "Financial Controls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    when (role)
                    {
                        UserRole.STUDENT, UserRole.STANDARD ->
                        {
                            Text(
                                text = "Balance: R${
                                    String.format(
                                        "%.2f",
                                        user?.walletBalance ?: 0.0
                                    )
                                }",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(onClick = {
                                activeSettingView = "AddCard"
                            }) { Text("Link Debit Card") }
                            TextButton(onClick = {
                                activeSettingView = "Redeem"
                            }) { Text("Redeem Coupon") }
                        }

                        UserRole.VENDOR ->
                        {
                            Text(text = if (user?.bankAccountInfo != null) "Payout Enabled" else "Payout Not Configured")
                            TextButton(onClick = {
                                activeSettingView = "Bank"
                            }) { Text("Update Bank Details") }
                        }

                        UserRole.ADMIN ->
                        {
                            TextButton(onClick = {
                                activeSettingView = "Credits"
                            }) { Text("Issue System Credits") }
                            TextButton(onClick = {
                                activeSettingView = "Coupons"
                            }) { Text("Generate Coupons") }
                        }
                    }
                }
            }

            // Support Group
            HIGCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)) {
                    Text(
                        text = "System Support",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (role == UserRole.ADMIN)
                    {
                        TextButton(onClick = {
                            activeSettingView = "Complaints"
                        }) { Text("Review Complaints") }
                        TextButton(onClick = {
                            activeSettingView = "Compliments"
                        }) { Text("Review Compliments") }
                    }
                    else
                    {
                        Button(
                            onClick = { activeSettingView = "Feedback" },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Submit Feedback")
                        }
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
                Icon(Icons.Rounded.Logout, null)
                Spacer(Modifier.width(DesignSystem.Spacing.small))
                Text("Logout Session", fontWeight = FontWeight.Bold)
            }

            Text(
                "Campus Eats v1.0.0 Stable",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
    else
    {
        // Sub-view Controller
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(DesignSystem.Spacing.medium)
            ) {
                IconButton(onClick = {
                    activeSettingView = "Main"
                }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                Text(
                    text = activeSettingView.replace(Regex("([a-z])([A-Z])"), "$1 $2"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(modifier = Modifier
                .weight(1f)
                .padding(horizontal = DesignSystem.Spacing.medium)) {
                when (activeSettingView)
                {
                    "Credits" -> AdminIssueCreditsWindow(adminRepository)
                    "Coupons" -> AdminGenerateCouponsWindow(couponRepository)
                    "Complaints" -> AdminFeedbackWindow(feedbackRepository, FeedbackType.COMPLAINT)
                    "Compliments" -> AdminFeedbackWindow(
                        feedbackRepository,
                        FeedbackType.COMPLIMENT
                    )

                    "Redeem" -> StudentRedeemCouponWindow(couponRepository)
                    "AddCard" -> StudentAddCardWindow(debitCardRepository, userId)
                    "Bank" -> VendorBankDetailsWindow(authRepository, userId, user?.bankAccountInfo)
                    "Feedback" -> UserFeedbackWindow(feedbackRepository, userId)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimalDropdown(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
)
{
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(DesignSystem.Spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedOption,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onOptionSelected(option); expanded = false })
            }
        }
    }
}

@Composable
fun StudentCurrentOrderHub(
    userId: String,
    cartRepository: CartRepository,
    onNavigateToCheckout: () -> Unit,
    onReturnHome: () -> Unit
)
{
    val items by cartRepository.getCart(userId).collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()

    if (items.isEmpty())
    {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Rounded.RemoveShoppingCart,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.medium))
                Text(
                    text = "Your active cart is empty.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
                TextButton(onClick = onReturnHome) { Text("Start Shopping") }
            }
        }
    }
    else
    {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = DesignSystem.Spacing.large),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)
            ) {
                items(items) { item ->
                    HIGCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "R${String.format("%.2f", item.price)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        if (item.quantity > 1) cartRepository.removeFromCart(item)
                                        else cartRepository.deleteCartItem(item)
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Decrease"
                                    )
                                }

                                Text(
                                    text = "${item.quantity}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )

                                IconButton(onClick = {
                                    coroutineScope.launch { cartRepository.incrementCartItem(item) }
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowUp,
                                        contentDescription = "Increase"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Principle: User Control - Explicit primary action for checkout.
            Button(
                onClick = onNavigateToCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                val subtotal = items.sumOf { it.price * it.quantity }
                Text(
                    text = "Proceed to Checkout • R${String.format("%.2f", subtotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StudentActivityReports(userId: String, orderRepository: OrderRepository)
{
    val orders by orderRepository.getOrdersForUser(userId).collectAsState(emptyList())
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Generate financial activity report.", color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(DesignSystem.Spacing.large))
        Button(onClick = {
            val json = Json.encodeToString(orders)
            File(context.getExternalFilesDir(null), "order_history.json").writeText(json)
            Toast.makeText(context, "Report saved to local storage", Toast.LENGTH_SHORT).show()
        }) {
            Icon(Icons.Rounded.Download, null)
            Text("Export as JSON")
        }
    }
}

@Composable
fun VendorOrderHub(vendorId: String, orderRepository: OrderRepository)
{
    val orders by orderRepository.getOrdersForVendor(vendorId).collectAsState(emptyList())
    LazyColumn(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
        items(orders) { order ->
            HIGCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = "Order #${order.orderId}", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Status: ${order.status.name}",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Time: ${
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                                Date(
                                    order.timestamp
                                )
                            )
                        }"
                    )
                }
            }
        }
    }
}

@Composable
fun VendorReportHub(vendorId: String, orderRepository: OrderRepository)
{
    val orders by orderRepository.getOrdersForVendor(vendorId).collectAsState(emptyList())
    val total = orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.totalAmount }
    HIGCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column {
            Text("Total Revenue", style = MaterialTheme.typography.labelMedium)
            Text(
                "R${String.format("%.2f", total)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun AdminReceiptsHub(orderRepository: OrderRepository)
{
    val orders by orderRepository.getAllOrders().collectAsState(emptyList())
    LazyColumn(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)) {
        items(orders) { order ->
            HIGCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Order #${order.orderId} • ${order.status}", fontWeight = FontWeight.Bold)
                    Text(
                        "Customer: ${order.customerId} | Vendor: ${order.vendorId}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        "Amount: R${String.format("%.2f", order.totalAmount)}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun AdminGlobalSummary(orderRepository: OrderRepository)
{
    val orders by orderRepository.getAllOrders().collectAsState(emptyList())
    val total = orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.totalAmount }
    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
        StatCardFull(label = "Platform Revenue", value = "R${String.format("%.2f", total)}")
        StatCardHalf(
            label = "Total Transactions",
            value = "${orders.size}",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AdminReportHub(
    statsRepository: StatsRepository,
    adminRepository: AdminRepository,
    onReturnHome: () -> Unit
)
{
    var reportType by remember { mutableStateOf("Main") }
    if (reportType == "Main")
    {
        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
            HIGServiceRow(
                "Daily Trends",
                "View platform activity over time.",
                Icons.Rounded.TrendingUp,
                { reportType = "Trends" })
            HIGServiceRow(
                "Vendor Rankings",
                "Top performers by revenue.",
                Icons.Rounded.AttachMoney,
                { reportType = "Vendors" })
        }
    }
    else
    {
        Column {
            IconButton(onClick = {
                reportType = "Main"
            }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) }
            Text(
                "Report: $reportType",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Data aggregation in progress...") }
        }
    }
}

@Composable
fun AdminIssueCreditsWindow(adminRepository: AdminRepository)
{
    var targetId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var successMsg by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
        Text(
            "Manually add credits to a user's campus wallet for support or refunds.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        OutlinedTextField(
            value = targetId,
            onValueChange = { targetId = it; successMsg = "" },
            label = { Text("Target User ID") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it; successMsg = "" },
            label = { Text("Amount (R)") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        if (successMsg.isNotEmpty()) Text(
            successMsg,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))
        HIGButton(
            onClick = {
                val a = amount.toDoubleOrNull() ?: 0.0
                if (targetId.isNotBlank() && a > 0)
                {
                    coroutineScope.launch { adminRepository.issueCredits(targetId, a) }
                    successMsg = "Success: R${String.format("%.2f", a)} issued to $targetId"
                }
            },
            text = "Finalize Credit Issue",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AdminGenerateCouponsWindow(couponRepository: CouponRepository)
{
    var code by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var successMsg by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
        Text(
            "Create unique promotional codes for student discounts.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        OutlinedTextField(
            value = code,
            onValueChange = { code = it; successMsg = "" },
            label = { Text("Coupon Code") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        OutlinedTextField(
            value = discount,
            onValueChange = { discount = it; successMsg = "" },
            label = { Text("Discount Percentage") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        if (successMsg.isNotEmpty()) Text(
            successMsg,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))
        HIGButton(
            onClick = {
                val d = discount.toDoubleOrNull() ?: 0.0
                if (code.isNotBlank() && d > 0)
                {
                    coroutineScope.launch { couponRepository.createCoupon(code, d) }
                    successMsg = "Success: Coupon $code (${d}%) generated."
                }
            },
            text = "Generate Promo Code",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AdminFeedbackWindow(feedbackRepository: FeedbackRepository, type: FeedbackType)
{
    val feedbacks by (if (type == FeedbackType.COMPLAINT) feedbackRepository.getComplaints() else feedbackRepository.getCompliments()).collectAsState(
        emptyList()
    )

    if (feedbacks.isEmpty())
    {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No feedback items in this category.", color = MaterialTheme.colorScheme.outline)
        }
    }
    else
    {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
            items(feedbacks) { fb ->
                var response by remember { mutableStateOf("") }
                var resolved by remember { mutableStateOf(false) }

                HIGCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(fb.subject, fontWeight = FontWeight.Black)
                            if (resolved) Icon(
                                Icons.Rounded.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(fb.message, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "From: ${fb.userId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = DesignSystem.Spacing.small))

                        OutlinedTextField(
                            value = response,
                            onValueChange = { response = it },
                            label = { Text("Administrator Response") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small
                        )
                        val context = LocalContext.current
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { resolved = !resolved }) {
                                Text(if (resolved) "Reopen" else "Mark Resolved")
                            }
                            TextButton(onClick = {
                                if (response.isNotBlank())
                                {
                                    Toast.makeText(
                                        context,
                                        "Response sent to user.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    response = ""
                                    resolved = true
                                }
                            }) {
                                Text("Send Response")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentRedeemCouponWindow(couponRepository: CouponRepository)
{
    var code by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
        Text(
            "Enter a promotional code to apply a discount to your next order.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        OutlinedTextField(
            value = code,
            onValueChange = { code = it; status = "" },
            label = { Text("Promo Code") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        if (status.isNotEmpty()) Text(
            status,
            color = if (status.startsWith("Valid")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))
        HIGButton(
            onClick = {
                coroutineScope.launch {
                    val c = couponRepository.validateCoupon(code)
                    status =
                        if (c != null) "Valid: ${c.discountPercent}% discount activated." else "Error: Code not found or inactive."
                }
            },
            text = "Verify Code",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StudentAddCardWindow(debitCardRepository: DebitCardRepository, userId: String)
{
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var successMsg by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
        Text(
            "Link a debit card for secure campus wallet top-ups.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { cardNumber = it; successMsg = "" },
            label = { Text("16-Digit Card Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
            OutlinedTextField(
                value = expiryDate,
                onValueChange = { expiryDate = it },
                label = { Text("MM/YY") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value = cvv,
                onValueChange = { cvv = it },
                label = { Text("CVV") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            )
        }

        if (successMsg.isNotEmpty()) Text(
            successMsg,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))
        HIGButton(
            onClick = {
                if (cardNumber.length == 16)
                {
                    coroutineScope.launch {
                        debitCardRepository.addCard(
                            userId,
                            cardNumber,
                            expiryDate,
                            cvv
                        )
                    }
                    successMsg = "Success: Card ending in ${cardNumber.takeLast(4)} linked."
                }
            },
            text = "Securely Save Card",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun VendorBankDetailsWindow(authRepository: AuthRepository, userId: String, currentInfo: String?)
{
    var bankName by remember { mutableStateOf(currentInfo?.split(" | ")?.getOrNull(0) ?: "") }
    var accNum by remember { mutableStateOf(currentInfo?.split(" | ")?.getOrNull(1) ?: "") }
    var holder by remember { mutableStateOf(currentInfo?.split(" | ")?.getOrNull(2) ?: "") }
    var branch by remember { mutableStateOf(currentInfo?.split(" | ")?.getOrNull(3) ?: "") }
    val coroutineScope = rememberCoroutineScope()
    var successMsg by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
        Text(
            "Provide your banking information to receive periodic revenue payouts.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        OutlinedTextField(
            value = bankName,
            onValueChange = { bankName = it; successMsg = "" },
            label = { Text("Financial Institution (Bank Name)") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        OutlinedTextField(
            value = accNum,
            onValueChange = { accNum = it },
            label = { Text("Account Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        OutlinedTextField(
            value = holder,
            onValueChange = { holder = it },
            label = { Text("Account Holder Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        OutlinedTextField(
            value = branch,
            onValueChange = { branch = it },
            label = { Text("Branch Code") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        if (successMsg.isNotEmpty()) Text(
            successMsg,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))
        HIGButton(
            onClick = {
                val info = "$bankName | $accNum | $holder | $branch"
                coroutineScope.launch { authRepository.linkBankAccount(userId, info) }
                successMsg = "Success: Banking details updated."
            },
            text = "Update Payout Details",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun UserFeedbackWindow(feedbackRepository: FeedbackRepository, userId: String)
{
    var subj by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(FeedbackType.COMPLIMENT) }
    val coroutineScope = rememberCoroutineScope()
    var successMsg by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)) {
        Text(
            "Your feedback helps us improve the campus dining experience.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Row(horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)) {
            FilterChip(
                selected = type == FeedbackType.COMPLIMENT,
                onClick = { type = FeedbackType.COMPLIMENT },
                label = { Text("Compliment") },
                shape = MaterialTheme.shapes.medium
            )
            FilterChip(
                selected = type == FeedbackType.COMPLAINT,
                onClick = { type = FeedbackType.COMPLAINT },
                label = { Text("Complaint") },
                shape = MaterialTheme.shapes.medium
            )
        }
        OutlinedTextField(
            value = subj,
            onValueChange = { subj = it; successMsg = "" },
            label = { Text("Topic / Subject") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        OutlinedTextField(
            value = msg,
            onValueChange = { msg = it },
            label = { Text("Detailed Message") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            shape = MaterialTheme.shapes.medium
        )

        if (successMsg.isNotEmpty()) Text(
            successMsg,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))
        HIGButton(
            onClick = {
                if (subj.isNotBlank() && msg.isNotBlank())
                {
                    coroutineScope.launch {
                        feedbackRepository.submitFeedback(
                            userId,
                            subj,
                            msg,
                            type
                        )
                    }
                    successMsg = "Thank you: Feedback submitted successfully."
                    subj = ""; msg = ""
                }
            },
            text = "Send Feedback",
            modifier = Modifier.fillMaxWidth()
        )
    }
}
