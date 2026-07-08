package com.example.campus_eats_app_kt.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RemoveShoppingCart
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
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
    onNavigateToMenuBrowse: (String, String) -> Unit
)
{
    val user by authRepository.getUserFlow(userId).collectAsState(null)
    val vendorStats by statsRepository.getVendorStats(userId).collectAsState(null)
    val adminStats by statsRepository.getAdminStats().collectAsState(null)
    val coroutineScope = rememberCoroutineScope()

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
                            Toast.makeText(context, "Failed to copy ID", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(44.dp) // Optimized touch target for mobile
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy User ID",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (role == UserRole.VENDOR && user?.shopName != null)
            {
                Text(
                    text = "Shop: ${user?.shopName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (role == UserRole.VENDOR)
        {
            item {
                Text(
                    text = "Shop Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShopStatus.values().forEach { shopStatus ->
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
                            )
                        )
                    }
                }
            }
        }

        when (role)
        {
            UserRole.VENDOR ->
            {
                item {
                    vendorStats?.let { stats ->
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            StatRow(
                                label = "All-time Earnings",
                                value = "R${String.format("%.2f", stats.allTimeEarnings)}"
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatCard(
                                    label = "Menu Items",
                                    value = "${stats.menuItemCount}",
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label = "Active Orders",
                                    value = "${stats.activeOrders}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            StatRow(
                                label = "Today's Revenue",
                                value = "R${String.format("%.2f", stats.todayRevenue)}"
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
                                label = "System Earnings",
                                value = "R${String.format("%.2f", stats.allTimeEarnings)}"
                            )
                            GridStats(stats = stats)
                            StatRow(
                                label = "Today's Revenue",
                                value = "R${String.format("%.2f", stats.todayRevenue)}"
                            )
                            StatRow(
                                label = "This Week",
                                value = "R${String.format("%.2f", stats.weekRevenue)}"
                            )
                            StatRow(
                                label = "This Month",
                                value = "R${String.format("%.2f", stats.monthRevenue)}"
                            )
                        }
                    }
                }
            }

            else ->
            {
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String)
{
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(DesignSystem.Spacing.large)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
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
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(DesignSystem.Spacing.medium)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun GridStats(stats: AdminStats)
{
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                label = "Users",
                value = "${stats.totalUsers}",
                modifier = Modifier.fillMaxWidth()
            )
            StatCard(
                label = "Items",
                value = "${stats.menuItemCount}",
                modifier = Modifier.fillMaxWidth()
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                label = "Vendors",
                value = "${stats.activeVendors}",
                modifier = Modifier.fillMaxWidth()
            )
            StatCard(
                label = "Orders",
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
    var adminView by remember { mutableStateOf("Main") }

    if (adminView == "Main")
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()) // Ensures accessibility on small 4.7" devices
        ) {
            Text(
                text = "Services",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(24.dp))

            when (role)
            {
                UserRole.STUDENT, UserRole.STANDARD ->
                {
                    ServiceCard(
                        title = "Vendors",
                        description = "Browse available campus vendors.",
                        icon = Icons.Rounded.Store,
                        onClick = { adminView = "VendorsList" })
                    ServiceCard(
                        title = "Receipts",
                        description = "View all your past receipts.",
                        icon = Icons.Rounded.Receipt,
                        onClick = { adminView = "Receipts" })
                    ServiceCard(
                        title = "Total Spending",
                        description = "View your lifetime spending.",
                        icon = Icons.Rounded.AccountBalance,
                        onClick = { adminView = "Spending" })
                }

                UserRole.VENDOR ->
                {
                    ServiceCard(
                        title = "Add New Item",
                        description = "Put a new meal on the menu.",
                        icon = Icons.Rounded.Add,
                        onClick = { onNavigateToAddMenuItem(userId, null) })
                    ServiceCard(
                        title = "Manage Inventory",
                        description = "View and edit your items.",
                        icon = Icons.Rounded.Inventory,
                        onClick = { onNavigateToVendorMenu(userId) })
                }

                UserRole.ADMIN ->
                {
                    ServiceCard(
                        title = "User Management",
                        description = "View and moderate all users.",
                        icon = Icons.Rounded.People,
                        onClick = { adminView = "Users" })
                    ServiceCard(
                        title = "Vendor Management",
                        description = "View and moderate all vendors.",
                        icon = Icons.Rounded.Store,
                        onClick = { adminView = "Vendors" })
                    ServiceCard(
                        title = "Order Management",
                        description = "View and update all orders.",
                        icon = Icons.Rounded.List,
                        onClick = { adminView = "Orders" })
                }
            }
        }
    }
    else
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { adminView = "Main" }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = adminView,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onReturnHome,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Return Home")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Sub-view container with vertical scrolling for non-lazy layouts
            // to ensure accessibility on devices as small as 4.7 inches.
            Box(modifier = Modifier.weight(1f)) {
                when (adminView)
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
fun StudentVendorList(
    userId: String,
    menuRepository: MenuRepository,
    onNavigateToMenuBrowse: (String, String) -> Unit
)
{
    val vendors by menuRepository.getAllVendors().collectAsState(emptyList())
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(vendors) { vendor ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMenuBrowse(userId, vendor.userId) },
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = vendor.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()) // Responsive adaptation for small viewports
        ) {
            Text(
                text = "Activity Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(24.dp))

            when (userRole)
            {
                UserRole.STUDENT, UserRole.STANDARD ->
                {
                    ServiceCard(
                        title = "Current Order",
                        description = "View cart and track delivery.",
                        icon = Icons.Rounded.ShoppingCart,
                        onClick = { currentHubView = "Current" }
                    )
                    ServiceCard(
                        title = "Receipts",
                        description = "Full history of your orders.",
                        icon = Icons.Rounded.History,
                        onClick = { currentHubView = "ReceiptsHub" }
                    )
                    ServiceCard(
                        title = "Reports",
                        description = "Generate transaction reports.",
                        icon = Icons.Rounded.Analytics,
                        onClick = { currentHubView = "ReportsHub" }
                    )
                }

                UserRole.VENDOR ->
                {
                    ServiceCard(
                        title = "Live Orders",
                        description = "Manage pending and active orders.",
                        icon = Icons.Rounded.ListAlt,
                        onClick = { currentHubView = "VendorOrders" }
                    )
                    ServiceCard(
                        title = "Sales Reports",
                        description = "View revenue analytics.",
                        icon = Icons.Rounded.BarChart,
                        onClick = { currentHubView = "VendorReports" }
                    )
                }

                UserRole.ADMIN ->
                {
                    ServiceCard(
                        title = "Global Receipts",
                        description = "All system transactions.",
                        icon = Icons.Rounded.ReceiptLong,
                        onClick = { currentHubView = "AdminReceipts" }
                    )
                    ServiceCard(
                        title = "Global Summary",
                        description = "Financial aggregates.",
                        icon = Icons.Rounded.AccountBalance,
                        onClick = { currentHubView = "AdminSummary" }
                    )
                    ServiceCard(
                        title = "System Reports",
                        description = "Revenue and user analytics.",
                        icon = Icons.Rounded.Assessment,
                        onClick = { currentHubView = "AdminReports" }
                    )
                }
            }
        }
    }
    else
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { currentHubView = "Main" }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = currentHubView,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = DesignSystem.Typography.titleSize
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onReturnHome) { Text(text = "Home") }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Sub-view container wrapper for role-specific activity modules.
            // Responsive viewport management ensures no content squishing.
            Box(modifier = Modifier.weight(1f)) {
                when (currentHubView)
                {
                    "Current" -> StudentCurrentOrderHub(
                        userId = userId,
                        cartRepository = cartRepository,
                        onNavigateToCheckout = onNavigateToCheckout,
                        onReturnHome = onReturnHome
                    )

                    "ReceiptsHub" -> StudentReceipts(
                        userId = userId,
                        orderRepository = orderRepository
                    )

                    "ReportsHub" -> StudentActivityReports(
                        userId = userId,
                        orderRepository = orderRepository
                    )

                    "VendorOrders" -> VendorOrderHub(
                        vendorId = userId,
                        orderRepository = orderRepository
                    )

                    "VendorReports" -> VendorReportHub(
                        vendorId = userId,
                        orderRepository = orderRepository
                    )

                    "AdminReceipts" -> AdminReceiptsHub(orderRepository = orderRepository)
                    "AdminSummary" -> AdminGlobalSummary(orderRepository = orderRepository)
                    "AdminReports" -> AdminReportHub(
                        statsRepository = statsRepository,
                        adminRepository = adminRepository,
                        onReturnHome = onReturnHome
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
    val completedOrders = orders.filter { it.status == OrderStatus.COMPLETED }
    val totalTransactions = completedOrders.size
    val totalRevenue = completedOrders.sumOf { it.totalAmount }
    val pendingPayments =
        orders.filter { it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED }
            .sumOf { it.totalAmount }
    val averageTransactionValue =
        if (totalTransactions > 0) totalRevenue / totalTransactions else 0.0

    // Administrator Global Summary Bar refactored to span full available width
    // with clean, minimalist card stacking for high visual impact and responsiveness.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.medium)
            .verticalScroll(rememberScrollState()), // Enables scrolling for smaller viewports
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
    ) {
        StatCard(
            label = "Total Transactions",
            value = "$totalTransactions",
            modifier = Modifier.fillMaxWidth() // Metric card expansion for visual balance
        )
        StatCard(
            label = "Total Revenue",
            value = "R${String.format("%.2f", totalRevenue)}",
            modifier = Modifier.fillMaxWidth()
        )
        StatCard(
            label = "Pending Payments",
            value = "R${String.format("%.2f", pendingPayments)}",
            modifier = Modifier.fillMaxWidth()
        )
        StatCard(
            label = "Average Transaction Value",
            value = "R${String.format("%.2f", averageTransactionValue)}",
            modifier = Modifier.fillMaxWidth()
        )
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
    val cartItems by cartRepository.getCart(userId).collectAsState(emptyList())
    if (cartItems.isEmpty())
    {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.RemoveShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Your cart is empty.")
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onReturnHome) {
                    Text(text = "Browse Items")
                }
            }
        }
    }
    else
    {
        Column {
            Text(text = "${cartItems.size} items in your cart.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToCheckout, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Proceed to Checkout")
            }
        }
    }
}

@Composable
fun StudentActivityReports(userId: String, orderRepository: OrderRepository)
{
    val orders by orderRepository.getOrdersForUser(userId).collectAsState(emptyList())
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Transaction Audit Report",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val json = Json.encodeToString(orders)
                val file = File(context.getExternalFilesDir(null), "order_history.json")
                file.writeText(json)
            }
        ) {
            Icon(imageVector = Icons.Rounded.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Export History (JSON)")
        }
    }
}

@Composable
fun VendorOrderHub(vendorId: String, orderRepository: OrderRepository)
{
    val orders by orderRepository.getOrdersForVendor(vendorId).collectAsState(emptyList())

    // UI State for Advanced Filtering and Search Logic
    var statusFilter by remember { mutableStateOf<OrderStatus?>(null) }

    // Requirement: Current year must be pre-selected by default to show relevant live context.
    val initialYear = Calendar.getInstance().get(Calendar.YEAR)
    var selectedMonth by remember { mutableIntStateOf(-1) }
    var selectedYear by remember { mutableIntStateOf(initialYear) }

    val months = listOf(
        "All Months", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    // Requirement: Year range from 2019 to current year, updating automatically.
    // The range calculation ensures that as the system enters a new year, it is added dynamically.
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = listOf("All Years") + (currentYear downTo 2019).map { it.toString() }
    
    val statusOptions = listOf("All Status", "Accepted", "Preparing", "Ready", "Completed")

    // Robust filter module: Combinatorial logic for status, month, and year constraints.
    val filteredOrders = remember(orders, statusFilter, selectedMonth, selectedYear) {
        orders.filter { order ->
            val cal = Calendar.getInstance().apply { timeInMillis = order.timestamp }
            val statusMatch = statusFilter == null || order.status == statusFilter
            val monthMatch = selectedMonth == -1 || cal.get(Calendar.MONTH) == selectedMonth
            val yearMatch = selectedYear == -1 || cal.get(Calendar.YEAR) == selectedYear
            statusMatch && monthMatch && yearMatch
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
    ) {
        // Multi-Picker Filter Grid - Adaptive for mobile viewports
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)
        ) {
            MinimalDropdown(
                label = "Month",
                selectedOption = if (selectedMonth == -1) "All Months" else months[selectedMonth + 1],
                options = months,
                onOptionSelected = { option ->
                    selectedMonth = months.indexOf(option) - 1
                },
                modifier = Modifier.weight(1f)
            )
            MinimalDropdown(
                label = "Year",
                selectedOption = if (selectedYear == -1) "All Years" else selectedYear.toString(),
                options = years,
                onOptionSelected = { option ->
                    selectedYear = if (option == "All Years") -1 else option.toInt()
                },
                modifier = Modifier.weight(1f)
            )
        }

        MinimalDropdown(
            label = "Order Status",
            selectedOption = statusFilter?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
                ?: "All Status",
            options = statusOptions,
            onOptionSelected = { option ->
                statusFilter =
                    if (option == "All Status") null else OrderStatus.valueOf(option.uppercase())
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Polished Order Stream - Responsive Lazy List with scrolling and padding
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = DesignSystem.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)
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
                        Text(
                            "No orders match the selected filters.",
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            items(filteredOrders) { order -> VendorOrderCard(order = order) }
        }
    }
}

@Composable
fun VendorOrderCard(order: OrderEntity)
{
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Order #${order.orderId}", fontWeight = FontWeight.Bold)
            Text(text = "Customer: ${order.customerId}")
            Text(text = "Amount: R${String.format("%.2f", order.totalAmount)}")
        }
    }
}

@Composable
fun VendorReportHub(vendorId: String, orderRepository: OrderRepository)
{
    val orders by orderRepository.getOrdersForVendor(vendorId).collectAsState(emptyList())
    val totalRevenue = orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.totalAmount }
    val avgValue = if (orders.isNotEmpty()) totalRevenue / orders.size else 0.0
    val uniqueCustomers = orders.distinctBy { it.customerId }.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                label = "Total Orders",
                value = "${orders.size}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Total Revenue",
                value = "R${String.format("%.2f", totalRevenue)}",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                label = "Avg Order Value",
                value = "R${String.format("%.2f", avgValue)}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Unique Customers",
                value = "$uniqueCustomers",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val context = LocalContext.current
        Button(
            onClick = {
                val json = Json.encodeToString(orders)
                val file = File(context.getExternalFilesDir(null), "vendor_sales_report.json")
                file.writeText(json)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(imageVector = Icons.Rounded.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Export Report (JSON)")
        }
    }
}

@Composable
fun AdminReceiptsHub(orderRepository: OrderRepository)
{
    val orders by orderRepository.getAllOrders().collectAsState(emptyList())
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(orders) { order ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(Date(order.timestamp)),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(text = "Order #${order.orderId}", fontWeight = FontWeight.Bold)
                    Text(text = "Customer: ${order.customerId}")
                    Text(text = "Vendor: ${order.vendorId}")
                    Text(text = "Amount: R${String.format("%.2f", order.totalAmount)}")
                    Text(text = "Payment: ${order.paymentMethod.name}")
                    Text(text = "Status: ${order.status.name}")
                }
            }
        }
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

    // Administrator System Reports view refactored for vertical scrolling
    // to ensure all content remains accessible across various device viewports.
    if (reportType == "Main")
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.medium)
                .verticalScroll(rememberScrollState()), // Enables window-level scrolling
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
        ) {
            ServiceCard(
                title = "Daily Trends",
                description = "Orders and revenue (Past Week).",
                icon = Icons.Rounded.TrendingUp,
                onClick = { reportType = "DailyTrends" }
            )
            ServiceCard(
                title = "Vendor Revenue",
                description = "Top 10 vendors by revenue.",
                icon = Icons.Rounded.AttachMoney,
                onClick = { reportType = "VendorRevenue" }
            )
            ServiceCard(
                title = "Popular Items",
                description = "Top 3 food items.",
                icon = Icons.Rounded.Star,
                onClick = { reportType = "PopularItems" }
            )
            ServiceCard(
                title = "Analytics",
                description = "User type breakdown.",
                icon = Icons.Rounded.PieChart,
                onClick = { reportType = "Analytics" }
            )

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onReturnHome,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Return Home", fontWeight = FontWeight.Bold)
            }
        }
    }
    else
    {
        Column(Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(DesignSystem.Spacing.medium)
            ) {
                IconButton(onClick = { reportType = "Main" }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                }
                Text(
                    text = when (reportType)
                    {
                        "DailyTrends" -> "Daily Trends (Past Week)"
                        "VendorRevenue" -> "Top 10 Vendor Revenue"
                        "PopularItems" -> "Top 3 Popular Items"
                        else -> reportType
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onReturnHome) { Text("Home") }
            }

            Box(Modifier.weight(1f)) {
                when (reportType)
                {
                    "DailyTrends" -> AdminDailyTrendsReport(statsRepository)
                    "VendorRevenue" -> AdminVendorRevenueReport(statsRepository)
                    "PopularItems" -> AdminPopularItemsReport(statsRepository)
                    "Analytics" -> AdminAnalyticsReport(adminRepository)
                }
            }
        }
    }
}

@Composable
fun AdminAnalyticsReport(adminRepository: AdminRepository)
{
    val users by adminRepository.getAllUsers().collectAsState(emptyList())
    val students = users.count { it.role == UserRole.STUDENT }
    val standards = users.count { it.role == UserRole.STANDARD }
    val vendors = users.count { it.role == UserRole.VENDOR }
    val admins = users.count { it.role == UserRole.ADMIN }

    // User Analytics report refactored with full-width cards and scrolling
    // to maintain readability and data hierarchy.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSystem.Spacing.medium)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
    ) {
        StatCard(label = "Students", value = "$students", modifier = Modifier.fillMaxWidth())
        StatCard(label = "Standard Users", value = "$standards", modifier = Modifier.fillMaxWidth())
        StatCard(label = "Vendors", value = "$vendors", modifier = Modifier.fillMaxWidth())
        StatCard(label = "Admins", value = "$admins", modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun AdminDailyTrendsReport(statsRepository: StatsRepository)
{
    val trends by statsRepository.getDailyTrends().collectAsState(emptyList())
    if (trends.isEmpty())
    {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No data available") }
    }
    else
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.medium)
        ) {
            ReportHeader(listOf("Date", "Orders", "Revenue"))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)) {
                items(trends) { trend ->
                    ReportRow(
                        listOf(
                            trend.date,
                            trend.orderCount.toString(),
                            "R${String.format("%.2f", trend.revenue)}"
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AdminVendorRevenueReport(statsRepository: StatsRepository)
{
    val rankings by statsRepository.getVendorRevenueRankings().collectAsState(emptyList())
    if (rankings.isEmpty())
    {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No data available") }
    }
    else
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.medium)
        ) {
            ReportHeader(listOf("Vendor", "Orders", "Revenue", "%"))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)) {
                items(rankings) { rank ->
                    ReportRow(
                        listOf(
                            rank.vendorName,
                            rank.orderCount.toString(),
                            "R${String.format("%.2f", rank.revenue)}",
                            "${String.format("%.1f", rank.percentage)}%"
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AdminPopularItemsReport(statsRepository: StatsRepository)
{
    val items by statsRepository.getPopularItems().collectAsState(emptyList())
    if (items.isEmpty())
    {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No data available") }
    }
    else
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.medium)
        ) {
            ReportHeader(listOf("Item Name", "Units Sold", "Revenue"))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)) {
                items(items) { item ->
                    ReportRow(
                        listOf(
                            item.itemName,
                            item.unitsSold.toString(),
                            "R${String.format("%.2f", item.revenue)}"
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ReportHeader(labels: List<String>)
{
    Row(Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        labels.forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ReportRow(values: List<String>)
{
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            values.forEach { value ->
                Text(
                    text = value,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AdminUserManagement(adminRepository: AdminRepository)
{
    val users by adminRepository.getAllUsers().collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(users) { user ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "ID: ${user.userId}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Name: ${user.fullName}", fontWeight = FontWeight.Bold)
                    Text(text = "Username: ${user.username}")
                    Text(text = "Email: ${user.email}")
                    Text(text = "Role: ${user.role.name} | Status: ${user.status.name}")
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
                                coroutineScope.launch {
                                    adminRepository.deleteUser(user)
                                }
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
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(vendors) { vendor ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Shop: ${vendor.shopName ?: vendor.fullName}",
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Email: ${vendor.email}")
                    Text(text = "Status: ${vendor.status.name}")
                    Text(
                        text = "Registered: ${
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(Date(vendor.registrationDate))
                        }",
                        style = MaterialTheme.typography.bodySmall
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

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(orders) { order ->
            var expanded by remember { mutableStateOf(false) }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Order #${order.orderId}", fontWeight = FontWeight.Bold)
                    Text(text = "Customer: ${order.customerId}")
                    Text(text = "Vendor: ${order.vendorId}")
                    Text(text = "Total: R${String.format("%.2f", order.totalAmount)}")
                    Text(text = "Status: ${order.status.name}")
                    Text(
                        text = "Placed: ${
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                .format(Date(order.timestamp))
                        }"
                    )

                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(text = "Update Status")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            OrderStatus.values().forEach { status ->
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

    // Enhanced State Management for Receipt Filtering and Sorting
    // State is preserved during sub-view transitions within the Activity Tab.
    var selectedMonth by remember { mutableIntStateOf(-1) }
    var selectedYear by remember { mutableIntStateOf(-1) }
    var sortBy by remember { mutableStateOf("Date (Newest to Oldest)") }

    val months = listOf(
        "All Months", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    // Requirement: Numeric Year Picker supporting current and previous 5 years for auditing.
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = listOf("All Years") + (currentYear downTo currentYear - 5).map { it.toString() }

    // Sort Options supporting minimalist financial tracking
    val sortOptions = listOf(
        "Amount (Ascending)",
        "Amount (Descending)",
        "Date (Newest to Oldest)",
        "Date (Oldest to Newest)"
    )

    // Unified combinatorial filtering and sorting module for accurate record display.
    val filteredOrders = remember(orders, selectedMonth, selectedYear, sortBy) {
        orders.filter { order ->
            val cal = Calendar.getInstance().apply { timeInMillis = order.timestamp }
            val monthMatch = selectedMonth == -1 || cal.get(Calendar.MONTH) == selectedMonth
            val yearMatch = selectedYear == -1 || cal.get(Calendar.YEAR) == selectedYear
            monthMatch && yearMatch
        }.let { list ->
            when (sortBy)
            {
                "Amount (Ascending)" -> list.sortedBy { it.totalAmount }
                "Amount (Descending)" -> list.sortedByDescending { it.totalAmount }
                "Date (Newest to Oldest)" -> list.sortedByDescending { it.timestamp }
                "Date (Oldest to Newest)" -> list.sortedBy { it.timestamp }
                else -> list
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
    ) {
        // Minimalist Filter & Sort Control Grid - High Touch Targets (44dp+)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)
        ) {
            MinimalDropdown(
                label = "Month",
                selectedOption = if (selectedMonth == -1) "All Months" else months[selectedMonth + 1],
                options = months,
                onOptionSelected = { option ->
                    selectedMonth = months.indexOf(option) - 1
                },
                modifier = Modifier.weight(1f)
            )
            MinimalDropdown(
                label = "Year",
                selectedOption = if (selectedYear == -1) "All Years" else selectedYear.toString(),
                options = years,
                onOptionSelected = { option ->
                    selectedYear = if (option == "All Years") -1 else option.toInt()
                },
                modifier = Modifier.weight(1f)
            )
        }

        MinimalDropdown(
            label = "Sort By",
            selectedOption = sortBy,
            options = sortOptions,
            onOptionSelected = { sortBy = it },
            modifier = Modifier.fillMaxWidth()
        )

        // Responsive Receipt Stream with clean typography and spacing.
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)
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
                        Text(
                            "No receipts match your current selection.",
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            items(filteredOrders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(DesignSystem.Spacing.large),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Order #${order.orderId}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                                    Date(order.timestamp)
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            text = "R${String.format("%.2f", order.totalAmount)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
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
            Text(text = "Lifetime Spending", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "R${String.format("%.2f", total)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
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
    val scrollState = rememberScrollState()
    val user by authRepository.getUserFlow(userId).collectAsState(null)
    var newPassword by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf(user?.email ?: "") }

    LaunchedEffect(user) {
        if (newEmail.isEmpty())
        {
            newEmail = user?.email ?: ""
        }
    }
    
    val coroutineScope = rememberCoroutineScope()

    var showAddCardDialog by remember { mutableStateOf(false) }
    var showApplyCouponDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showLinkBankDialog by remember { mutableStateOf(false) }
    var showIssueCreditsDialog by remember { mutableStateOf(false) }
    var showCreateCouponDialog by remember { mutableStateOf(false) }
    var showFeedbackReviewType by remember { mutableStateOf<FeedbackType?>(null) }

    if (showAddCardDialog)
    {
        var cardNumber by remember { mutableStateOf("") }
        var expiryDate by remember { mutableStateOf("") }
        var cvv by remember { mutableStateOf("") }
        var isSaving by remember { mutableStateOf(false) }
        var statusMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showAddCardDialog = false },
            title = { Text(text = "Add Debit Card", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text(text = "Card Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            label = { Text(text = "MM/YY") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { cvv = it },
                            label = { Text(text = "CVV") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    statusMsg?.let {
                        Text(
                            it,
                            color = if (it.contains("Success")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cardNumber.length < 16 || expiryDate.isEmpty() || cvv.length < 3)
                        {
                            statusMsg = "Please enter valid card details"
                            return@Button
                        }
                        isSaving = true
                        coroutineScope.launch {
                            debitCardRepository.addCard(userId, cardNumber, expiryDate, cvv)
                            isSaving = false
                            statusMsg = "Success! Card saved."
                            kotlinx.coroutines.delay(1000)
                            showAddCardDialog = false
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else Text(text = "Save Card")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) { Text(text = "Cancel") }
            }
        )
    }

    if (showLinkBankDialog)
    {
        var bankName by remember {
            mutableStateOf(
                user?.bankAccountInfo?.split(" - ")?.getOrNull(0) ?: ""
            )
        }
        var accountNumber by remember {
            mutableStateOf(
                user?.bankAccountInfo?.split(" - ")?.getOrNull(1) ?: ""
            )
        }
        var holderName by remember { mutableStateOf(user?.fullName ?: "") }
        var isSaving by remember { mutableStateOf(false) }
        var statusMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showLinkBankDialog = false },
            title = {
                Text(
                    text = if (user?.bankAccountInfo != null) "Edit Bank Account" else "Link Bank Account",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = holderName,
                        onValueChange = { holderName = it },
                        label = { Text("Account Holder Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text(text = "Bank Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text(text = "Account Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    statusMsg?.let {
                        Text(
                            it,
                            color = if (it.contains("Success")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    if (user?.bankAccountInfo != null)
                    {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    authRepository.linkBankAccount(userId, "")
                                    showLinkBankDialog = false
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Unlink Account")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bankName.isBlank() || accountNumber.isBlank() || holderName.isBlank())
                        {
                            statusMsg = "All fields required"
                            return@Button
                        }
                        if (accountNumber.length < 8)
                        {
                            statusMsg = "Invalid account number"
                            return@Button
                        }
                        isSaving = true
                        coroutineScope.launch {
                            val res =
                                authRepository.linkBankAccount(userId, "$bankName - $accountNumber")
                            isSaving = false
                            if (res.isSuccess)
                            {
                                statusMsg = "Success! Bank account linked."
                                kotlinx.coroutines.delay(1000)
                                showLinkBankDialog = false
                            }
                            else
                            {
                                statusMsg = res.exceptionOrNull()?.message ?: "Error"
                            }
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else Text(text = "Save Details")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkBankDialog = false }) { Text(text = "Cancel") }
            }
        )
    }

    if (showApplyCouponDialog)
    {
        var code by remember { mutableStateOf("") }
        var isValidating by remember { mutableStateOf(false) }
        var validationMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isValidating) showApplyCouponDialog = false },
            title = { Text(text = "Apply Coupon", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text(text = "Coupon Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    validationMsg?.let {
                        Text(
                            text = it,
                            color = if (it.contains("Success")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isValidating = true
                        coroutineScope.launch {
                            val coupon = couponRepository.validateCoupon(code)
                            isValidating = false
                            if (coupon != null)
                            {
                                validationMsg =
                                    "Success! ${coupon.discountPercent}% discount applied."
                            }
                            else
                            {
                                validationMsg = "Invalid or inactive coupon code."
                            }
                        }
                    },
                    enabled = !isValidating && code.isNotBlank()
                ) {
                    if (isValidating) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else Text(text = "Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyCouponDialog = false }) { Text(text = "Cancel") }
            }
        )
    }

    if (showCreateCouponDialog)
    {
        var code by remember { mutableStateOf("") }
        var discount by remember { mutableStateOf("") }
        var isSaving by remember { mutableStateOf(false) }
        var statusMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showCreateCouponDialog = false },
            title = { Text(text = "Create Coupon", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Coupon Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = discount,
                        onValueChange = { discount = it },
                        label = { Text("Discount %") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    statusMsg?.let {
                        Text(
                            it,
                            color = if (it.contains("Success")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val d = discount.toDoubleOrNull()
                    if (code.isBlank() || d == null)
                    {
                        statusMsg = "Invalid input"; return@Button
                    }
                    isSaving = true
                    coroutineScope.launch {
                        couponRepository.createCoupon(code, d)
                        isSaving = false
                        statusMsg = "Success! Coupon created."
                        kotlinx.coroutines.delay(1000)
                        showCreateCouponDialog = false
                    }
                }, enabled = !isSaving) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else Text(
                        "Create"
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateCouponDialog = false
                }) { Text("Cancel") }
            }
        )
    }

    if (showIssueCreditsDialog)
    {
        var targetId by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var isSaving by remember { mutableStateOf(false) }
        var statusMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showIssueCreditsDialog = false },
            title = { Text(text = "Issue Wallet Credits", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = targetId,
                        onValueChange = { targetId = it },
                        label = { Text("User ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (R)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    statusMsg?.let {
                        Text(
                            it,
                            color = if (it.contains("Success")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val a = amount.toDoubleOrNull()
                    if (targetId.isBlank() || a == null)
                    {
                        statusMsg = "Invalid input"; return@Button
                    }
                    isSaving = true
                    coroutineScope.launch {
                        adminRepository.issueCredits(targetId, a)
                        isSaving = false
                        statusMsg = "Success! Credits issued."
                        kotlinx.coroutines.delay(1000)
                        showIssueCreditsDialog = false
                    }
                }, enabled = !isSaving) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else Text(
                        "Issue"
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showIssueCreditsDialog = false
                }) { Text("Cancel") }
            }
        )
    }

    if (showFeedbackReviewType != null)
    {
        val feedbacks by (if (showFeedbackReviewType == FeedbackType.COMPLAINT) feedbackRepository.getComplaints() else feedbackRepository.getCompliments()).collectAsState(
            emptyList()
        )
        AlertDialog(
            onDismissRequest = { showFeedbackReviewType = null },
            title = {
                Text(
                    if (showFeedbackReviewType == FeedbackType.COMPLAINT) "Review Complaints" else "Review Compliments",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(feedbacks) { fb ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(8.dp)) {
                                Text(fb.subject, fontWeight = FontWeight.Bold)
                                Text(fb.message, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "From: ${fb.userId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showFeedbackReviewType = null
                }) { Text("Close") }
            }
        )
    }

    if (showFeedbackDialog)
    {
        var subject by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var type by remember { mutableStateOf(FeedbackType.COMPLIMENT) }
        var isSubmitting by remember { mutableStateOf(false) }
        var submitted by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showFeedbackDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showFeedbackDialog = false }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Text(text = "Submit Feedback", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (submitted)
                    {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Thank you! Your feedback has been saved successfully.",
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else
                    {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "What kind of feedback is this?",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilterChip(
                                    selected = type == FeedbackType.COMPLIMENT,
                                    onClick = { type = FeedbackType.COMPLIMENT },
                                    label = { Text("Compliment") },
                                    leadingIcon = if (type == FeedbackType.COMPLIMENT)
                                    {
                                        { Icon(Icons.Rounded.ThumbUp, null, Modifier.size(16.dp)) }
                                    }
                                    else null
                                )
                                Spacer(Modifier.width(8.dp))
                                FilterChip(
                                    selected = type == FeedbackType.COMPLAINT,
                                    onClick = { type = FeedbackType.COMPLAINT },
                                    label = { Text("Complaint") },
                                    leadingIcon = if (type == FeedbackType.COMPLAINT)
                                    {
                                        {
                                            Icon(
                                                Icons.Rounded.ThumbDown,
                                                null,
                                                Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text(text = "Subject") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text(text = "Message") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )
                    }
                }
            },
            confirmButton = {
                if (submitted)
                {
                    Button(
                        onClick = { showFeedbackDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(text = "Close") }
                }
                else
                {
                    Button(
                        onClick = {
                            if (subject.isBlank() || message.isBlank()) return@Button
                            isSubmitting = true
                            coroutineScope.launch {
                                feedbackRepository.submitFeedback(userId, subject, message, type)
                                isSubmitting = false
                                submitted = true
                            }
                        },
                        enabled = !isSubmitting && subject.isNotBlank() && message.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSubmitting) CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        else Text(text = "Submit Feedback")
                    }
                }
            }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Settings",
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
                    text = "Update Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    coroutineScope.launch {
                        authRepository.updateProfile(
                            userId,
                            newEmail,
                            newPassword
                        ); newPassword = ""
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Save Changes") }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "Wallet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                when (role)
                {
                    UserRole.STUDENT, UserRole.STANDARD ->
                    {
                        Text(
                            text = "Campus Wallet Balance: R${
                                String.format(
                                    "%.2f",
                                    user?.walletBalance ?: 0.0
                                )
                            }", style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = {
                            showAddCardDialog = true
                        }) { Text("Add Debit Card") }
                        TextButton(onClick = {
                            showApplyCouponDialog = true
                        }) { Text("Apply Coupons") }
                    }

                    UserRole.VENDOR ->
                    {
                        Text(
                            text = if (user?.bankAccountInfo != null) "Linked: ${user?.bankAccountInfo}" else "No bank account linked.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = {
                            showLinkBankDialog = true
                        }) { Text(if (user?.bankAccountInfo != null) "Edit Payout Settings" else "Link Bank Account") }
                    }

                    UserRole.ADMIN ->
                    {
                        Text(text = "System Treasury", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = {
                            showIssueCreditsDialog = true
                        }) { Text("Issue Wallet Credits") }
                        TextButton(onClick = {
                            showCreateCouponDialog = true
                        }) { Text("Create Coupons") }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "Feedback",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (role == UserRole.ADMIN)
                {
                    TextButton(onClick = {
                        showFeedbackReviewType = FeedbackType.COMPLAINT
                    }) { Text("Review Complaints") }
                    TextButton(onClick = {
                        showFeedbackReviewType = FeedbackType.COMPLIMENT
                    }) { Text("Review Compliments") }
                }
                else
                {
                    Button(
                        onClick = { showFeedbackDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Share Your Thoughts")
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
            Icon(Icons.Rounded.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Logout Session", fontWeight = FontWeight.Bold)
        }
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                "Campus Eats v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
{
    // Minimalist Card following Apple-inspired design principles:
    // Large corner radii, generous padding, and subtle interactive states.
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = DesignSystem.Spacing.extraSmall),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
    ) {
        Row(
            Modifier.padding(DesignSystem.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.width(DesignSystem.Spacing.medium))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp // Apple-like tight kerning for headers
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
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

    Box(modifier = modifier)
    {
        Surface(
            onClick = { expanded = true },
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = MaterialTheme.shapes.large
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
