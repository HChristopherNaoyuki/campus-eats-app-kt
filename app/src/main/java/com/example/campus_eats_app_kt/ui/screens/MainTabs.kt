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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
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
        user = authRepository.login("", "").getOrNull() 
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                value,
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
    var adminView by remember { mutableStateOf("Main") }

    if (adminView == "Main")
    {
        Column(Modifier
            .fillMaxSize()
            .padding(24.dp)) {
            Text(
                "Services",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(24.dp))

            when (role)
            {
                UserRole.STUDENT, UserRole.STANDARD ->
                {
                    ServiceCard(
                        "Receipts",
                        "View all your past receipts.",
                        Icons.Rounded.Receipt,
                        onClick = { adminView = "Receipts" })
                    ServiceCard(
                        "Total Spending",
                        "View your lifetime spending.",
                        Icons.Rounded.AccountBalance,
                        onClick = { adminView = "Spending" })
                }

                UserRole.VENDOR ->
                {
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

                UserRole.ADMIN ->
                {
                    ServiceCard(
                        "User Management",
                        "View and moderate all users.",
                        Icons.Rounded.People,
                        onClick = { adminView = "Users" })
                    ServiceCard(
                        "Vendor Management",
                        "View and moderate all vendors.",
                        Icons.Rounded.Store,
                        onClick = { adminView = "Vendors" })
                    ServiceCard(
                        "Order Management",
                        "View and update all orders.",
                        Icons.Rounded.List,
                        onClick = { adminView = "Orders" })
                }
            }
        }
    }
    else
    {
        Column(Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { adminView = "Main" }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Text(
                    adminView,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onReturnHome,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Return Home")
                }
            }
            Spacer(Modifier.height(16.dp))

            when (adminView)
            {
                "Users" -> AdminUserManagement(adminRepository)
                "Vendors" -> AdminVendorManagement(adminRepository)
                "Orders" -> AdminOrderManagement(orderRepository)
                "Receipts" -> StudentReceipts(userId, orderRepository)
                "Spending" -> StudentTotalSpending(userId, orderRepository)
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
        Column(Modifier
            .fillMaxSize()
            .padding(24.dp)) {
            Text(
                "Activity Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(24.dp))

            when (userRole)
            {
                UserRole.STUDENT, UserRole.STANDARD ->
                {
                    ServiceCard(
                        "Current Order",
                        "View cart and track delivery.",
                        Icons.Rounded.ShoppingCart,
                        onClick = { currentHubView = "Current" })
                    ServiceCard(
                        "Receipts",
                        "Full history of your orders.",
                        Icons.Rounded.History,
                        onClick = { currentHubView = "ReceiptsHub" })
                    ServiceCard(
                        "Reports",
                        "Generate transaction reports.",
                        Icons.Rounded.Analytics,
                        onClick = { currentHubView = "ReportsHub" })
                }

                UserRole.VENDOR ->
                {
                    ServiceCard(
                        "Live Orders",
                        "Manage pending and active orders.",
                        Icons.Rounded.ListAlt,
                        onClick = { currentHubView = "VendorOrders" })
                    ServiceCard(
                        "Sales Reports",
                        "View revenue analytics.",
                        Icons.Rounded.BarChart,
                        onClick = { currentHubView = "VendorReports" })
                }

                UserRole.ADMIN ->
                {
                    ServiceCard(
                        "Global Receipts",
                        "All system transactions.",
                        Icons.Rounded.ReceiptLong,
                        onClick = { currentHubView = "AdminReceipts" })
                    ServiceCard(
                        "System Reports",
                        "Revenue and user analytics.",
                        Icons.Rounded.Assessment,
                        onClick = { currentHubView = "AdminReports" })
                }
            }
        }
    }
    else
    {
        Column(Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { currentHubView = "Main" }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Text(
                    currentHubView,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Button(onClick = onReturnHome) { Text("Home") }
            }
            Spacer(Modifier.height(16.dp))

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
                "AdminReports" -> AdminReportHub(orderRepository)
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
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Rounded.RemoveShoppingCart,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(16.dp))
                Text("Your cart is empty.")
                Spacer(Modifier.height(24.dp))
                Button(onClick = onReturnHome) {
                    Text("Browse Items")
                }
            }
        }
    }
    else
    {
        Column {
            Text("${cartItems.size} items in your cart.")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onNavigateToCheckout, modifier = Modifier.fillMaxWidth()) {
                Text("Proceed to Checkout")
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
        Text("Transaction Audit Report", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            val json = Json.encodeToString(orders)
            val file = File(context.getExternalFilesDir(null), "order_history.json")
            file.writeText(json)
        }) {
            Icon(Icons.Rounded.Download, null)
            Spacer(Modifier.width(8.dp))
            Text("Export History (JSON)")
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
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                onClick = { statusFilter = null },
                label = { Text("All") },
                selected = statusFilter == null
            )
            OrderStatus.values().forEach { status ->
                FilterChip(
                    selected = statusFilter == status,
                    onClick = { statusFilter = if (statusFilter == status) null else status },
                    label = { Text(status.name) }
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredOrders) { order -> VendorOrderCard(order) }
        }
    }
}

@Composable
fun VendorOrderCard(order: OrderEntity)
{
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Order #${order.orderId}", fontWeight = FontWeight.Bold)
            Text("Customer: ${order.customerId}")
            Text("Amount: R${String.format("%.2f", order.totalAmount)}")
        }
    }
}

@Composable
fun VendorReportHub(vendorId: String, orderRepository: OrderRepository)
{
    val orders by orderRepository.getOrdersForVendor(vendorId).collectAsState(emptyList())
    val totalRevenue = orders.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.totalAmount }
    Column(Modifier.padding(16.dp)) {
        StatCard("Total Orders", "${orders.size}")
        Spacer(Modifier.height(8.dp))
        StatCard("Total Revenue", "R${String.format("%.2f", totalRevenue)}")
    }
}

@Composable
fun AdminReceiptsHub(orderRepository: OrderRepository)
{
    Text("Admin Global Receipts Placeholder")
}

@Composable
fun AdminReportHub(orderRepository: OrderRepository)
{
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ServiceCard(
            "Daily Trends",
            "Orders and revenue over the past week.",
            Icons.Rounded.TrendingUp,
            onClick = {})
        ServiceCard(
            "Vendor Revenue",
            "Top 10 vendors by earnings.",
            Icons.Rounded.AttachMoney,
            onClick = {})
        ServiceCard(
            "Popular Items",
            "Top 3 most sold food items.",
            Icons.Rounded.Star,
            onClick = {})
        ServiceCard(
            "User Analytics",
            "Breakdown by user type.",
            Icons.Rounded.PieChart,
            onClick = {})
    }
}

@Composable
fun AdminUserManagement(adminRepository: AdminRepository)
{
    val users by adminRepository.getAllUsers().collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(users) { user ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("ID: ${user.userId}", style = MaterialTheme.typography.bodySmall)
                    Text("Name: ${user.fullName}", fontWeight = FontWeight.Bold)
                    Text("Email: ${user.email}")
                    Text("Role: ${user.role.name} | Status: ${user.status.name}")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                if (user.status == UserStatus.ACTIVE) adminRepository.suspendUser(
                                    user.userId
                                )
                                else adminRepository.activateUser(user.userId)
                            }
                        }) {
                            Text(if (user.status == UserStatus.ACTIVE) "Suspend" else "Activate")
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
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Shop: ${vendor.fullName}", fontWeight = FontWeight.Bold)
                    Text("Email: ${vendor.email}")
                    Text("Status: ${vendor.status.name}")
                }
            }
        }
    }
}

@Composable
fun AdminOrderManagement(orderRepository: OrderRepository)
{
    val orders by orderRepository.getOrdersForVendor("").collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(orders) { order ->
            var expanded by remember { mutableStateOf(false) }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Order #${order.orderId}", fontWeight = FontWeight.Bold)
                    Text("Customer: ${order.customerId}")
                    Text("Current Status: ${order.status.name}")

                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text("Update Status")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            OrderStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.name) },
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
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Order #${order.orderId}", fontWeight = FontWeight.Bold)
                        Text(
                            "Date: ${
                                java.text.SimpleDateFormat("dd/MM/yyyy")
                                    .format(java.util.Date(order.timestamp))
                            }"
                        )
                    }
                    Text(
                        "R${String.format("%.2f", order.totalAmount)}",
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
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Lifetime Spending", style = MaterialTheme.typography.titleMedium)
            Text(
                "R${String.format("%.2f", total)}",
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
    onLogout: () -> Unit
)
{
    val scrollState = rememberScrollState()
    var newPassword by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Wallet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                when (role)
                {
                    UserRole.STUDENT, UserRole.STANDARD ->
                    {
                        Text("Balance: R0.00", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = {}) { Text("Add Debit Card") }
                        TextButton(onClick = {}) { Text("Apply Coupons") }
                    }

                    UserRole.VENDOR ->
                    {
                        Text("Payout Settings", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = {}) { Text("Link Bank Account") }
                    }

                    UserRole.ADMIN ->
                    {
                        Text("System Treasury", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = {}) { Text("Issue Wallet Credits") }
                        TextButton(onClick = {}) { Text("Create Coupons") }
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
                    "Feedback",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                if (role == UserRole.ADMIN)
                {
                    TextButton(onClick = {}) { Text("Review Complaints") }
                    TextButton(onClick = {}) { Text("Review Compliments") }
                }
                else
                {
                    TextButton(onClick = {}) { Text("Submit Complaint") }
                    TextButton(onClick = {}) { Text("Submit Compliment") }
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
