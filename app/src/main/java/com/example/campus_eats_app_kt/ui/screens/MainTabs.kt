package com.example.campus_eats_app_kt.ui.screens

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
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Inventory
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.campus_eats_app_kt.data.DebitCardRepository
import com.example.campus_eats_app_kt.data.FeedbackRepository
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.StatsRepository
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

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
    val vendors by menuRepository.getAllVendors().collectAsState(emptyList())
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
            Text(
                text = "User ID: $userId",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
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
            UserRole.STUDENT, UserRole.STANDARD ->
            {
                item {
                    Text(
                        text = "Top Vendors",
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
        }
    }
}

@Composable
fun StatRow(label: String, value: String)
{
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
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
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
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
                        title = "Receipts",
                        description = "View all your past receipts.",
                        icon = Icons.Rounded.Receipt,
                        onClick = { adminView = "Receipts" }
                    )
                    ServiceCard(
                        title = "Total Spending",
                        description = "View your lifetime spending.",
                        icon = Icons.Rounded.AccountBalance,
                        onClick = { adminView = "Spending" }
                    )
                }

                UserRole.VENDOR ->
                {
                    ServiceCard(
                        title = "Add New Item",
                        description = "Put a new meal on the menu.",
                        icon = Icons.Rounded.Add,
                        onClick = { onNavigateToAddMenuItem(userId, null) }
                    )
                    ServiceCard(
                        title = "Manage Inventory",
                        description = "View and edit your items.",
                        icon = Icons.Rounded.Inventory,
                        onClick = { onNavigateToVendorMenu(userId) }
                    )
                }

                UserRole.ADMIN ->
                {
                    ServiceCard(
                        title = "User Management",
                        description = "View and moderate all users.",
                        icon = Icons.Rounded.People,
                        onClick = { adminView = "Users" }
                    )
                    ServiceCard(
                        title = "Vendor Management",
                        description = "View and moderate all vendors.",
                        icon = Icons.Rounded.Store,
                        onClick = { adminView = "Vendors" }
                    )
                    ServiceCard(
                        title = "Order Management",
                        description = "View and update all orders.",
                        icon = Icons.Rounded.List,
                        onClick = { adminView = "Orders" }
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

            when (adminView)
            {
                "Users" -> AdminUserManagement(adminRepository = adminRepository)
                "Vendors" -> AdminVendorManagement(adminRepository = adminRepository)
                "Orders" -> AdminOrderManagement(orderRepository = orderRepository)
                "Receipts" -> StudentReceipts(userId = userId, orderRepository = orderRepository)
                "Spending" -> StudentTotalSpending(
                    userId = userId,
                    orderRepository = orderRepository
                )
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
    var currentHubView by remember { mutableStateOf("Main") }
    val userRole = remember(role) { UserRole.valueOf(role) }

    if (currentHubView == "Main")
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onReturnHome) { Text(text = "Home") }
            }
            Spacer(modifier = Modifier.height(16.dp))

            when (currentHubView)
            {
                "Current" -> StudentCurrentOrderHub(
                    userId = userId,
                    cartRepository = cartRepository,
                    onNavigateToCheckout = onNavigateToCheckout,
                    onReturnHome = onReturnHome
                )

                "ReceiptsHub" -> StudentReceipts(userId = userId, orderRepository = orderRepository)
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
                "AdminReports" -> AdminReportHub(orderRepository = orderRepository)
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Transaction Audit Report", style = MaterialTheme.typography.titleMedium)
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
    var statusFilter by remember { mutableStateOf<OrderStatus?>(null) }

    val filteredOrders = remember(orders, statusFilter) {
        if (statusFilter == null) orders
        else orders.filter { it.status == statusFilter }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                onClick = { statusFilter = null },
                label = { Text(text = "All") },
                selected = statusFilter == null
            )
            OrderStatus.values().forEach { status ->
                FilterChip(
                    selected = statusFilter == status,
                    onClick = { statusFilter = if (statusFilter == status) null else status },
                    label = { Text(text = status.name) }
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

        Button(
            onClick = { /* JSON Export */ },
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
                    Text(text = "Order #${order.orderId}", fontWeight = FontWeight.Bold)
                    Text(text = "Customer: ${order.customerId}")
                    Text(text = "Amount: R${String.format("%.2f", order.totalAmount)}")
                }
            }
        }
    }
}

@Composable
fun AdminReportHub(orderRepository: OrderRepository)
{
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ServiceCard(
                title = "Daily Trends",
                description = "Orders and revenue.",
                icon = Icons.Rounded.TrendingUp,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            ServiceCard(
                title = "Vendor Revenue",
                description = "Vendor rankings.",
                icon = Icons.Rounded.AttachMoney,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ServiceCard(
                title = "Popular Items",
                description = "Top sold foods.",
                icon = Icons.Rounded.Star,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            ServiceCard(
                title = "User Analytics",
                description = "User type breakdown.",
                icon = Icons.Rounded.PieChart,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
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
                    Text(text = "Shop: ${vendor.fullName}", fontWeight = FontWeight.Bold)
                    Text(text = "Email: ${vendor.email}")
                    Text(text = "Status: ${vendor.status.name}")
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
                    Text(text = "Current Status: ${order.status.name}")

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
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(orders) { order ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Order #${order.orderId}", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Date: ${
                                java.text.SimpleDateFormat("dd/MM/yyyy")
                                    .format(java.util.Date(order.timestamp))
                            }"
                        )
                    }
                    Text(
                        text = "R${String.format("%.2f", order.totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
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
    var newPassword by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showAddCardDialog by remember { mutableStateOf(false) }
    var showApplyCouponDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    if (showAddCardDialog)
    {
        // ... (existing dialog)
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
                                // In real app, persist this to user's session or cart
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
                    else Text(
                        text = "Apply"
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyCouponDialog = false }) { Text(text = "Cancel") }
            }
        )
    }

    if (showFeedbackDialog)
    {
        var subject by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }
        var submitted by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showFeedbackDialog = false },
            title = { Text(text = "Submit Feedback", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (submitted)
                    {
                        Text(
                            text = "Thank you! Your feedback has been saved successfully.",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    else
                    {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text(text = "Subject") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text(text = "Message") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            },
            confirmButton = {
                if (submitted)
                {
                    Button(onClick = { showFeedbackDialog = false }) { Text(text = "Close") }
                }
                else
                {
                    Button(
                        onClick = {
                            isSubmitting = true
                            coroutineScope.launch {
                                feedbackRepository.submitFeedback(userId, subject, message)
                                isSubmitting = false
                                submitted = true
                            }
                        },
                        enabled = !isSubmitting && subject.isNotBlank() && message.isNotBlank()
                    ) {
                        if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        else Text(
                            text = "Submit"
                        )
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
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
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Update Password",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(text = "Enter New Password") }, 
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        coroutineScope.launch {
                            val result = authRepository.resetPassword(userId, newPassword)
                            if (result.isSuccess)
                            {
                                newPassword = ""
                            }
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(text = "Save Changes", fontWeight = FontWeight.Bold) 
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
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
                            text = "Campus Wallet Balance: R0.00",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = {
                            showAddCardDialog = true
                        }) { Text(text = "Add Debit Card") }
                        TextButton(onClick = {
                            showApplyCouponDialog = true
                        }) { Text(text = "Apply Coupons") }
                    }

                    UserRole.VENDOR ->
                    {
                        Text(text = "Payout Settings", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = {}) { Text(text = "Link Bank Account") }
                    }

                    UserRole.ADMIN ->
                    {
                        Text(text = "System Treasury", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = {}) { Text(text = "Issue Wallet Credits") }
                        TextButton(onClick = {}) { Text(text = "Create Coupons") }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Feedback",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (role == UserRole.ADMIN)
                {
                    TextButton(onClick = {}) { Text(text = "Review Complaints") }
                    TextButton(onClick = {}) { Text(text = "Review Compliments") }
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
            Icon(imageVector = Icons.Rounded.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Logout Session", fontWeight = FontWeight.Bold)
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
                text = "Campus Eats v$versionName",
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.large
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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
