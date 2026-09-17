package com.example.campus_eats_app_kt.ui.screens

import android.graphics.Paint
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.RemoveShoppingCart
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campus_eats_app_kt.data.AdminStats
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.CouponRepository
import com.example.campus_eats_app_kt.data.DebitCardRepository
import com.example.campus_eats_app_kt.data.FeedbackRepository
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.StatsRepository
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import com.example.campus_eats_app_kt.ui.components.HIGButton
import com.example.campus_eats_app_kt.ui.components.HIGCard
import com.example.campus_eats_app_kt.ui.components.HIGSegmentedControl
import com.example.campus_eats_app_kt.ui.components.HIGServiceRow
import com.example.campus_eats_app_kt.ui.theme.CampusOrange
import com.example.campus_eats_app_kt.ui.theme.DesignSystem
import com.example.campus_eats_app_kt.util.LanguageManager
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SelectableDates
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.runtime.mutableLongStateOf
import com.example.campus_eats_app_kt.data.entity.UserEntity
import kotlin.math.cos
import kotlin.math.sin

private val prettyJson = Json { prettyPrint = true }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenTab(
    userId: String,
    role: UserRole,
    authRepository: AuthRepository,
    statsRepository: StatsRepository,
    menuRepository: MenuRepository,
    onNavigateToMenuBrowse: (String, String) -> Unit,
    onExploreVendors: () -> Unit,
)
{
    val user by authRepository.getUserFlow(userId).collectAsState(null)
    val vendorStats by statsRepository.getVendorStats(userId).collectAsState(null)
    val adminStats by statsRepository.getAdminStats().collectAsState(null)
    val vendors by menuRepository.getAllVendors().collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()
    val locale = LocalConfiguration.current.locales[0]
    val responsivePadding = DesignSystem.Spacing.responsiveHorizontalPadding()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = responsivePadding, vertical = DesignSystem.Spacing.large),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.large),
    )
    {
        // Welcoming Header Card - Professional Minimalist Refinement
        item()
        {
            if (role != UserRole.ADMINISTRATOR)
            {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraLarge,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                {
                    Column(
                        modifier = Modifier.padding(DesignSystem.Spacing.large),
                    )
                    {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        {
                            Column()
                            {
                                val greeting =
                                    when (Calendar.getInstance()[Calendar.HOUR_OF_DAY])
                                    {
                                        in 0..11 -> "Good morning"
                                        in 12..16 -> "Good afternoon"
                                        else -> "Good evening"
                                    }
                                Text(
                                    text = "$greeting,",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                )
                                Text(
                                    text = user?.fullName?.split(" ")?.firstOrNull() ?: "User",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically)
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(28.dp),
                                )
                                Spacer(modifier = Modifier.width(DesignSystem.Spacing.medium))
                                Surface(
                                    shape = CircleShape,
                                    color = CampusOrange.copy(alpha = 0.1f),
                                    modifier = Modifier.size(40.dp),
                                )
                                {
                                    Box(contentAlignment = Alignment.Center)
                                    {
                                        Text(
                                            text = user?.fullName?.firstOrNull()?.toString() ?: "U",
                                            color = CampusOrange,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        }

                        if (role == UserRole.VENDOR)
                        {
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.large))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            {
                                Column()
                                {
                                    Text(
                                        text = user?.shopName ?: "Shop Name",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                    )
                                    Text(
                                        text = if (user?.shopStatus == ShopStatus.OPEN) "Status: Accepting Orders" else "Status: Offline",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (user?.shopStatus == ShopStatus.OPEN) Color(
                                            0xFF4CAF50,
                                        ) else MaterialTheme.colorScheme.error,
                                    )
                                }
                                Switch(
                                    checked = user?.shopStatus == ShopStatus.OPEN,
                                    onCheckedChange = { isOpen ->
                                        coroutineScope.launch()
                                        {
                                            authRepository.updateShopStatus(
                                                userId,
                                                if (isOpen) ShopStatus.OPEN else ShopStatus.CLOSED,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Vendor Dashboard Section
        if (role == UserRole.VENDOR)
        {
            item()
            {
                vendorStats?.let()
                { stats ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
                    )
                    {
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        StatCardFull(
                            label = "All-time Earnings",
                            value = "R${String.format(locale, "%.2f", stats.allTimeEarnings)}",
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
                        )
                        {
                            StatCardHalf(
                                label = "Menu Items",
                                value = stats.menuItemCount.toString(),
                                modifier = Modifier.weight(1f),
                            )
                            StatCardHalf(
                                label = "Active Orders",
                                value = stats.activeOrders.toString(),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        StatCardFull(
                            label = "Today's Revenue",
                            value = "R${String.format(locale, "%.2f", stats.todayRevenue)}",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        )
                    }
                }
            }
        }

        // Admin Dashboard remains similar or adjusted for HIG
        if (role == UserRole.ADMINISTRATOR)
        {
            item()
            {
                adminStats?.let()
                { stats ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
                    )
                    {
                        Text(
                            text = "Global Overview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        AdminGridStats(stats)
                    }
                }
            }
        }

        // Student/Standard: Featured Vendors or Quick Access
        if ((role == UserRole.STUDENT) || (role == UserRole.STANDARD))
        {
            item()
            {
                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
                {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    )
                    {
                        Text(
                            text = "Featured Vendors",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        TextButton(onClick = onExploreVendors)
                        {
                            Text(text = "See All")
                        }
                    }

                    if (vendors.isEmpty())
                    {
                        Text(
                            text = "No vendors available yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }

                    vendors.take(3).forEach { vendor ->
                        HIGCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToMenuBrowse(userId, vendor.userId) },
                        )
                        {
                            Row(verticalAlignment = Alignment.CenterVertically)
                            {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(60.dp),
                                )
                                {
                                    Box(contentAlignment = Alignment.Center)
                                    {
                                        Icon(
                                            imageVector = Icons.Rounded.Store,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(DesignSystem.Spacing.medium))
                                Column()
                                {
                                    Text(
                                        text = vendor.shopName ?: "Vendor",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "Open • Traditional Meals",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCardFull(
    label: String,
    value: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
)
{
    HIGCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = containerColor,
    )
    {
        Column()
        {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
fun StatCardHalf(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
)
{
    HIGCard(
        modifier = modifier,
    )
    {
        Column()
        {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun AdminGridStats(stats: AdminStats)
{
    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
    {
        Row(horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
        {
            StatCardHalf(label = "Total Users", value = stats.totalUsers.toString(), modifier = Modifier.weight(1f))
            StatCardHalf(label = "Vendors", value = stats.activeVendors.toString(), modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
        {
            StatCardHalf(label = "Orders", value = stats.orderCount.toString(), modifier = Modifier.weight(1f))
            StatCardHalf(label = "Menu Items", value = stats.menuItemCount.toString(), modifier = Modifier.weight(1f))
        }
        val locale = LocalConfiguration.current.locales[0]
        StatCardFull(
            label = "Platform Revenue",
            value = "R${String.format(locale, "%.2f", stats.allTimeEarnings)}",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        )
    }
}

@Composable
fun ServicesScreenTab(
    userId: String,
    role: UserRole,
    menuRepository: MenuRepository,
    adminViewModel: AdminViewModel,
    orderRepository: OrderRepository,
    onNavigateToVendorMenu: (String) -> Unit,
    onNavigateToMenuBrowse: (String, String) -> Unit,
    onNavigateToAddMenuItem: (String, Long?) -> Unit,
    onReturnHome: () -> Unit,
)
{
    var activeView by remember { mutableStateOf("Main") }
    var selectedOrder by remember { mutableStateOf<OrderEntity?>(null) }
    val responsivePadding = DesignSystem.Spacing.responsiveHorizontalPadding()

    if (activeView == "Main")
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = responsivePadding, vertical = DesignSystem.Spacing.large)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
        )
        {
            when (role)
            {
                UserRole.STUDENT, UserRole.STANDARD ->
                {
                    HIGServiceRow(
                        title = "Vendors",
                        description = "Browse available campus dining options.",
                        icon = Icons.Rounded.Store,
                        onClick = {
                            activeView = "VendorsList"
                        },
                    )
                    HIGServiceRow(
                        title = "Order Receipts",
                        description = "Audit your past transaction history.",
                        icon = Icons.Rounded.Receipt,
                        onClick = {
                            activeView = "Receipts"
                        },
                    )
                    HIGServiceRow(
                        title = "Cumulative Spending",
                        description = "Monitor your total platform expenditure.",
                        icon = Icons.Rounded.AccountBalance,
                        onClick = {
                            activeView = "Spending"
                        },
                    )
                }

                UserRole.VENDOR ->
                {
                    HIGServiceRow(
                        title = "New Menu Item",
                        description = "Add a fresh food offering to your shop.",
                        icon = Icons.Rounded.Add,
                        onClick = {
                            onNavigateToAddMenuItem(userId, null)
                        },
                    )
                    HIGServiceRow(
                        title = "Inventory Manager",
                        description = "Update and monitor your current stock.",
                        icon = Icons.Rounded.Inventory,
                        onClick = {
                            onNavigateToVendorMenu(userId)
                        },
                    )
                }

                UserRole.ADMINISTRATOR ->
                {
                    HIGServiceRow(
                        title = "User Directory",
                        description = "Manage system accounts and access.",
                        icon = Icons.Rounded.People,
                        onClick = {
                            activeView = "Users"
                        },
                    )
                    HIGServiceRow(
                        title = "Vendor Portal",
                        description = "Moderate campus shop configurations.",
                        icon = Icons.Rounded.Store,
                        onClick = {
                            activeView = "Vendors"
                        },
                    )
                    HIGServiceRow(
                        title = "Order Master",
                        description = "High-level overview of system transactions.",
                        icon = Icons.AutoMirrored.Rounded.List,
                        onClick = {
                            activeView = "Orders"
                        },
                    )
                }
            }
        }
    }
    else
    {
        Column(modifier = Modifier.fillMaxSize())
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignSystem.Spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            )
            {
                IconButton(
                    onClick = {
                        activeView = "Main"
                    },
                )
                {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Text(
                    text = activeView.replace(Regex("([a-z])([A-Z])"), "$1 $2"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onReturnHome)
                {
                    Text(text = "Home")
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = responsivePadding),
            )
            {
                when (activeView)
                {
                    "VendorsList" -> StudentVendorList(userId, menuRepository, onNavigateToMenuBrowse)
                    "Receipts" -> StudentReceipts(userId, orderRepository) {
                        selectedOrder = it
                        activeView = "OrderDetail"
                    }
                    "Spending" -> StudentTotalSpending(userId, orderRepository)
                    "Users" -> AdminUserManagement(adminViewModel)
                    "Vendors" -> AdminVendorManagement(adminViewModel)
                    "Orders" -> AdminOrderManagement(adminViewModel)
                    "OrderDetail" -> OrderDetailWindow(
                        order = selectedOrder,
                        role = role,
                        userId = userId,
                        orderRepository = orderRepository,
                    )
                    {
                        activeView = "Receipts"
                    }
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
    menuRepository: MenuRepository,
    onNavigateToCheckout: () -> Unit,
    onReturnHome: () -> Unit,
)
{
    var currentHubView by remember { mutableStateOf("Main") }
    val userRole = remember(role)
    {
        UserRole.entries.find { it.name == role } ?: UserRole.STANDARD
    }
    var selectedOrder by remember { mutableStateOf<OrderEntity?>(null) }
    val responsivePadding = DesignSystem.Spacing.responsiveHorizontalPadding()

    if (currentHubView == "Main")
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = responsivePadding, vertical = DesignSystem.Spacing.large)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
        )
        {
            when (userRole)
            {
                UserRole.STUDENT, UserRole.STANDARD ->
                {
                    HIGServiceRow(
                        title = "Live Status",
                        description = "Complete your purchase and track active orders.",
                        icon = Icons.Rounded.ShoppingCart,
                        onClick = {
                            currentHubView = "Current"
                        },
                    )
                    HIGServiceRow(
                        title = "Order History",
                        description = "Comprehensive list of all past meals.",
                        icon = Icons.Rounded.History,
                        onClick = {
                            currentHubView = "ReceiptsHub"
                        },
                    )
                    HIGServiceRow(
                        title = "Analytics",
                        description = "Personal consumption trends and reports.",
                        icon = Icons.Rounded.Analytics,
                        onClick = {
                            currentHubView = "ReportsHub"
                        },
                    )
                }

                UserRole.VENDOR ->
                {
                    HIGServiceRow(
                        title = "Live Orders",
                        description = "Fulfill pending and active customer tasks.",
                        icon = Icons.AutoMirrored.Rounded.ListAlt,
                        onClick = {
                            currentHubView = "VendorOrders"
                        },
                    )
                    HIGServiceRow(
                        title = "Financial Reports",
                        description = "Detailed revenue and growth analytics.",
                        icon = Icons.Rounded.BarChart,
                        onClick = {
                            currentHubView = "VendorReports"
                        },
                    )
                }

                UserRole.ADMINISTRATOR ->
                {
                    HIGServiceRow(
                        title = "Global Receipts",
                        description = "Audit every transaction on the platform.",
                        icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                        onClick = {
                            currentHubView = "AdminReceipts"
                        },
                    )
                    HIGServiceRow(
                        title = "System Summary",
                        description = "High-level financial aggregates.",
                        icon = Icons.Rounded.AccountBalance,
                        onClick = {
                            currentHubView = "AdminSummary"
                        },
                    )
                    HIGServiceRow(
                        title = "Insight Reports",
                        description = "Advanced user and revenue analytics.",
                        icon = Icons.Rounded.Assessment,
                        onClick = {
                            currentHubView = "AdminReports"
                        },
                    )
                }
            }
        }
    }
    else
    {
        Column(modifier = Modifier.fillMaxSize())
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignSystem.Spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            )
            {
                IconButton(
                    onClick = {
                        currentHubView = if (currentHubView == "OrderDetail")
                        {
                            if (userRole == UserRole.VENDOR) "VendorOrders" else "ReceiptsHub"
                        }
                        else
                        {
                            "Main"
                        }
                    },
                )
                {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Text(
                    text = if (currentHubView == "OrderDetail") "Detailed Receipt" else currentHubView.replace(Regex("([a-z])([A-Z])"), "$1 $2"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onReturnHome)
                {
                    Text(text = "Home")
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = responsivePadding),
            )
            {
                when (currentHubView)
                {
                    "Current" -> StudentCurrentOrderHub(
                        userId,
                        cartRepository,
                        orderRepository,
                        menuRepository,
                        onNavigateToCheckout,
                        onReturnHome,
                    )

                    "ReceiptsHub" -> StudentReceipts(
                        userId,
                        orderRepository,
                    )
                    {
                        selectedOrder = it
                        currentHubView = "OrderDetail"
                    }

                    "ReportsHub" -> StudentActivityReports(userId, orderRepository, menuRepository)
                    "VendorOrders" -> VendorOrderHub(
                        vendorId = userId,
                        orderRepository = orderRepository,
                    )
                    {
                        selectedOrder = it
                        currentHubView = "OrderDetail"
                    }

                    "VendorReports" -> VendorReportHub(userId, orderRepository)
                    "AdminReceipts" -> AdminReceiptsHub(orderRepository)
                    "AdminSummary" -> AdminGlobalSummary(orderRepository)
                    "AdminReports" -> AdminReportHub()
                    "OrderDetail" -> OrderDetailWindow(
                        order = selectedOrder,
                        role = userRole,
                        userId = userId,
                        orderRepository = orderRepository,
                    )
                    {
                        currentHubView = "Main"
                    }
                }
            }
        }
    }
}

@Composable
fun StudentVendorList(
    userId: String,
    menuRepository: MenuRepository,
    onVendorClick: (String, String) -> Unit,
)
{
    val vendors by menuRepository.getAllVendors().collectAsState(emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = DesignSystem.Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small),
    )
    {
        items(vendors)
        { vendor ->
            HIGCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onVendorClick(userId, vendor.userId) },
            )
            {
                Row(verticalAlignment = Alignment.CenterVertically)
                {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(60.dp),
                    )
                    {
                        Box(contentAlignment = Alignment.Center)
                        {
                            Icon(
                                imageVector = Icons.Rounded.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.medium))
                    Column()
                    {
                        Text(
                            text = vendor.shopName ?: "Vendor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Online • View Menu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserManagement(viewModel: AdminViewModel)
{
    val users by viewModel.users.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = DesignSystem.Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small),
    )
    {
        items(users)
        { user ->
            HIGCard(modifier = Modifier.fillMaxWidth())
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                )
                {
                    Column(modifier = Modifier.weight(1f))
                    {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${user.role} • ${user.status}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    Row()
                    {
                        IconButton(onClick = { viewModel.toggleUserStatus(user) })
                        {
                            Icon(
                                imageVector = if (user.status == UserStatus.ACTIVE) Icons.Rounded.People else Icons.Rounded.Add,
                                contentDescription = "Toggle Status",
                                tint = if (user.status == UserStatus.ACTIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminVendorManagement(viewModel: AdminViewModel)
{
    val users by viewModel.users.collectAsState()
    val vendors = users.filter { it.role == UserRole.VENDOR }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = DesignSystem.Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small),
    )
    {
        items(vendors)
        { vendor ->
            HIGCard(modifier = Modifier.fillMaxWidth())
            {
                Column()
                {
                    Text(
                        text = vendor.shopName ?: "Unknown Shop",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Owner: ${vendor.fullName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
    }
}

@Composable
fun AdminOrderManagement(viewModel: AdminViewModel)
{
    val orders by viewModel.orders.collectAsState()
    val locale = LocalConfiguration.current.locales[0]
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = DesignSystem.Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small),
    )
    {
        items(orders)
        { order ->
            HIGCard(modifier = Modifier.fillMaxWidth())
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                )
                {
                    Column()
                    {
                        Text(
                            text = "Order #${order.orderId}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Total: R${String.format(locale, "%.2f", order.totalAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Surface(
                        color = when (order.status)
                        {
                            OrderStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        shape = MaterialTheme.shapes.small,
                    )
                    {
                        Text(
                            text = order.status.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (order.status)
                            {
                                OrderStatus.COMPLETED -> Color(0xFF4CAF50)
                                OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentReceipts(
    userId: String,
    orderRepository: OrderRepository,
    onOrderClick: (OrderEntity) -> Unit,
)
{
    val orders by orderRepository.getOrdersForUser(userId).collectAsState(emptyList())
    val locale = LocalConfiguration.current.locales[0]

    var selectedMonth by remember { mutableIntStateOf(-1) }
    var selectedYear by remember { mutableIntStateOf(-1) }
    var sortBy by remember { mutableStateOf("Date (Newest)") }

    val months = listOf(
        "All Months", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )
    val currentYear = Calendar.getInstance()[Calendar.YEAR]
    val years = listOf("All Years") + (currentYear downTo (currentYear - 5)).map { it.toString() }
    val sortOptions = listOf(
        "Amount (High to Low)",
        "Amount (Low to High)",
        "Date (Newest)",
        "Date (Oldest)",
    )

    val filteredOrders = remember(orders, selectedMonth, selectedYear, sortBy)
    {
        orders.asSequence().filter()
        { order ->
            val cal = Calendar.getInstance().apply { timeInMillis = order.timestamp }
            val monthMatch = (selectedMonth == -1) || (cal[Calendar.MONTH] == selectedMonth)
            val yearMatch = (selectedYear == -1) || (cal[Calendar.YEAR] == selectedYear)
            val isFinalStatus = (order.status == OrderStatus.COMPLETED) || (order.status == OrderStatus.CANCELLED)
            monthMatch && yearMatch && isFinalStatus
        }.let()
        { seq ->
            when (sortBy)
            {
                "Amount (High to Low)" -> seq.sortedByDescending { it.totalAmount }
                "Amount (Low to High)" -> seq.sortedBy { it.totalAmount }
                "Date (Newest)" -> seq.sortedByDescending { it.timestamp }
                "Date (Oldest)" -> seq.sortedBy { it.timestamp }
                else -> seq
            }
        }.toList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
    {
        Row(horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small))
        {
            MinimalDropdown(
                label = "Month",
                selectedOption = if (selectedMonth == -1) "All" else months[selectedMonth + 1],
                options = months,
                onOptionSelected = {
                    selectedMonth = months.indexOf(it) - 1
                },
                modifier = Modifier.weight(1f),
            )
            MinimalDropdown(
                label = "Year",
                selectedOption = if (selectedYear == -1) "All" else selectedYear.toString(),
                options = years,
                onOptionSelected = {
                    selectedYear = if (it == "All Years") -1 else it.toInt()
                },
                modifier = Modifier.weight(1f),
            )
        }

        MinimalDropdown(
            label = "Sort",
            selectedOption = sortBy,
            options = sortOptions,
            onOptionSelected = {
                sortBy = it
            },
            modifier = Modifier.fillMaxWidth(),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = DesignSystem.Spacing.small),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small),
        )
        {
            items(filteredOrders)
            { order ->
                HIGCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOrderClick(order) },
                )
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    {
                        Column()
                        {
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy", locale).format(
                                    Date(order.timestamp),
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Text(
                                text = "Order #${order.orderId}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "R${String.format(locale, "%.2f", order.totalAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = if (order.status == OrderStatus.COMPLETED) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentTotalSpending(
    userId: String,
    orderRepository: OrderRepository,
)
{
    val orders by orderRepository.getOrdersForUser(userId).collectAsState(emptyList())
    val total = orders.asSequence().filter { it.status == OrderStatus.COMPLETED }.sumOf { it.totalAmount }
    val locale = LocalConfiguration.current.locales[0]

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    )
    {
        Text(
            text = "Lifetime Spending",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "R${String.format(locale, "%.2f", total)}",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = CampusOrange,
        )
    }
}

@Composable
fun SettingsScreenTab(
    userId: String,
    role: UserRole,
    authRepository: AuthRepository,
    feedbackRepository: FeedbackRepository,
    couponRepository: CouponRepository,
    adminViewModel: AdminViewModel,
    debitCardRepository: DebitCardRepository,
    onLogout: () -> Unit,
)
{
    var activeSettingView by remember { mutableStateOf("Main") }
    val user by authRepository.getUserFlow(userId).collectAsState(null)
    val coroutineScope = rememberCoroutineScope()
    val locale = LocalConfiguration.current.locales[0]
    val isAdmin by adminViewModel.isAdmin.collectAsState()
    val responsivePadding = DesignSystem.Spacing.responsiveHorizontalPadding()

    if (activeSettingView == "Main")
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = responsivePadding, vertical = DesignSystem.Spacing.large)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.large),
        )
        {
            // Language Selection Group
            HIGCard(modifier = Modifier.fillMaxWidth())
            {
                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
                {
                    Text(
                        text = LanguageManager.getString("Language Settings", "Taalinstellings"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
                    )
                    {
                        FilterChip(
                            selected = LanguageManager.currentLanguage.value == "English",
                            onClick = {
                                LanguageManager.currentLanguage.value = "English"
                            },
                            label = { Text("English") },
                        )
                        
                        FilterChip(
                            selected = LanguageManager.currentLanguage.value == "Afrikaans",
                            onClick = {
                                LanguageManager.currentLanguage.value = "Afrikaans"
                            },
                            label = { Text("Afrikaans") },
                        )
                    }
                }
            }

            // Account Group
            HIGCard(modifier = Modifier.fillMaxWidth())
            {
                var newFullName by remember { mutableStateOf(user?.fullName ?: "") }
                var newUsername by remember { mutableStateOf(user?.username ?: "") }
                var newPassword by remember { mutableStateOf("") }

                LaunchedEffect(user)
                {
                    if (newFullName.isEmpty())
                    {
                        newFullName = user?.fullName ?: ""
                    }
                    if (newUsername.isEmpty())
                    {
                        newUsername = user?.username ?: ""
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
                {
                    Text(
                        text = LanguageManager.getString("Profile Identity", "Profielidentiteit"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedTextField(
                        value = newFullName,
                        onValueChange = {
                            newFullName = it
                        },
                        label = {
                            Text(text = LanguageManager.getString("Full Name", "Volle Naam"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    )
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = {
                            newUsername = it
                        },
                        label = {
                            Text(text = LanguageManager.getString("Username", "Gebruikersnaam"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                        },
                        label = {
                            Text(text = LanguageManager.getString("New Security Key", "Nuwe Sekuriteitsleutel"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        shape = MaterialTheme.shapes.medium,
                    )
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            coroutineScope.launch()
                            {
                                val result = authRepository.updateProfile(
                                    userId,
                                    newFullName,
                                    newUsername,
                                    newPassword.takeIf { it.isNotBlank() },
                                )
                                result.onSuccess()
                                {
                                    Toast.makeText(context, "Credentials successfully updated.", Toast.LENGTH_SHORT).show()
                                    newPassword = ""
                                }.onFailure()
                                { e ->
                                    Toast.makeText(context, e.message ?: "Update failed.", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    )
                    {
                        Text(text = LanguageManager.getString("Update Credentials", "Werk Bewysbriewe Op"))
                    }
                }
            }

            // Financial Group
            HIGCard(modifier = Modifier.fillMaxWidth())
            {
                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small))
                {
                    Text(
                        text = LanguageManager.getString("Financial Controls", "Finansiële Kontroles"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    when (role)
                    {
                        UserRole.STUDENT, UserRole.STANDARD ->
                        {
                            Text(
                                text = "${LanguageManager.getString("Balance", "Balans")}: R${
                                    String.format(
                                        locale,
                                        "%.2f",
                                        user?.walletBalance ?: 0.0,
                                    )
                                }",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            TextButton(
                                onClick = {
                                    activeSettingView = "AddCard"
                                },
                            )
                            {
                                Text(text = LanguageManager.getString("Link Debit Card", "Koppel Debietkaart"))
                            }
                            TextButton(
                                onClick = {
                                    activeSettingView = "Redeem"
                                },
                            )
                            {
                                Text(text = LanguageManager.getString("Redeem Coupon", "Los Koepon In"))
                            }
                        }

                        UserRole.VENDOR ->
                        {
                            Text(
                                text = if (user?.bankAccountInfo != null)
                                {
                                    LanguageManager.getString("Payout Enabled", "Uitbetaling Geaktiveer")
                                }
                                else
                                {
                                    LanguageManager.getString("Payout Not Configured", "Uitbetaling Nie Opgestel Nie")
                                },
                            )
                            TextButton(
                                onClick = {
                                    activeSettingView = "Bank"
                                },
                            )
                            {
                                Text(text = LanguageManager.getString("Update Bank Details", "Werk Bankbesonderhede Op"))
                            }
                        }

                        UserRole.ADMINISTRATOR ->
                        {
                            TextButton(
                                onClick = {
                                    activeSettingView = "Credits"
                                },
                            )
                            {
                                Text(text = LanguageManager.getString("Issue System Credits", "Uitreik Stelselkrediete"))
                            }
                            TextButton(
                                onClick = {
                                    activeSettingView = "Coupons"
                                },
                            )
                            {
                                Text(text = LanguageManager.getString("Generate Coupons", "Genereer Koepons"))
                            }
                        }
                    }
                }
            }

            // Support Group
            HIGCard(modifier = Modifier.fillMaxWidth())
            {
                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small))
                {
                    Text(
                        text = LanguageManager.getString("System Support", "Stelselondersteuning"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (role == UserRole.ADMINISTRATOR)
                    {
                        TextButton(
                            onClick = {
                                activeSettingView = "Complaints"
                            },
                        )
                        {
                            Text(text = LanguageManager.getString("Review Complaints", "Hersien Klagtes"))
                        }
                        TextButton(
                            onClick = {
                                activeSettingView = "Compliments"
                            },
                        )
                        {
                            Text(text = LanguageManager.getString("Review Compliments", "Hersien Komplimente"))
                        }
                    }
                    else
                    {
                        Button(
                            onClick = {
                                activeSettingView = "Feedback"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                        )
                        {
                            Text(text = LanguageManager.getString("Submit Feedback", "Dien Terugvoer In"))
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
                shape = MaterialTheme.shapes.large,
            )
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(DesignSystem.Spacing.small))
                Text(
                    text = LanguageManager.getString("Logout Session", "Teken Uit Stelsel"),
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = "v3.0.0",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
    else
    {
        // Sub-view Controller
        Column(modifier = Modifier.fillMaxSize())
        {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(DesignSystem.Spacing.medium),
            )
            {
                IconButton(
                    onClick = {
                        activeSettingView = "Main"
                    },
                )
                {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Text(
                    text = activeSettingView.replace(Regex("([a-z])([A-Z])"), "$1 $2"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = responsivePadding),
            )
            {
                if (isAdmin)
                {
                    when (activeSettingView)
                    {
                        "Credits" -> AdminIssueCreditsWindow(viewModel = adminViewModel)
                        "Coupons" -> AdminGenerateCouponsWindow(viewModel = adminViewModel)
                        "Complaints" -> AdminFeedbackWindow(
                            viewModel = adminViewModel,
                            type = FeedbackType.COMPLAINT,
                        )
                        "Compliments" -> AdminFeedbackWindow(
                            viewModel = adminViewModel,
                            type = FeedbackType.COMPLIMENT,
                        )
                    }
                }

                when (activeSettingView)
                {
                    "Redeem" -> StudentRedeemCouponWindow(couponRepository = couponRepository)
                    "AddCard" -> StudentAddCardWindow(
                        debitCardRepository = debitCardRepository,
                        userId = userId,
                    )
                    "Bank" -> VendorBankDetailsWindow(
                        authRepository = authRepository,
                        userId = userId,
                        currentInfo = user?.bankAccountInfo,
                    )
                    "Feedback" -> UserFeedbackWindow(
                        feedbackRepository = feedbackRepository,
                        userId = userId,
                    )
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
    modifier: Modifier = Modifier,
)
{
    var expanded by remember { mutableStateOf(value = false) }

    Box(modifier = modifier)
    {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = true })
                {
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            ),
            shape = MaterialTheme.shapes.medium,
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f),
        )
        {
            options.forEach()
            { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun StudentCurrentOrderHub(
    userId: String,
    cartRepository: CartRepository,
    orderRepository: OrderRepository,
    menuRepository: MenuRepository,
    onNavigateToCheckout: () -> Unit,
    onReturnHome: () -> Unit,
)
{
    val cartItems by cartRepository.getCart(userId).collectAsState(emptyList())
    val orders by orderRepository.getOrdersForUser(userId).collectAsState(emptyList())
    val vendors by menuRepository.getAllVendors().collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()
    val locale = LocalConfiguration.current.locales[0]

    val activeOrders = remember(orders)
    {
        orders.filter { (it.status != OrderStatus.COMPLETED) && (it.status != OrderStatus.CANCELLED) }
    }

    if (cartItems.isEmpty() && activeOrders.isEmpty())
    {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        )
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally)
            {
                Icon(
                    imageVector = Icons.Rounded.RemoveShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline,
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.medium))
                Text(
                    text = "No active cart or orders.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline,
                )
                TextButton(onClick = onReturnHome)
                {
                    Text(text = "Start Shopping")
                }
            }
        }
    }
    else
    {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = DesignSystem.Spacing.large),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
        )
        {
            if (activeOrders.isNotEmpty())
            {
                item()
                {
                    Text(
                        text = "Active Orders",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                items(activeOrders)
                { order ->
                    HIGCard(modifier = Modifier.fillMaxWidth())
                    {
                        val vendorName = vendors.find { it.userId == order.vendorId }?.shopName ?: "Vendor #${order.vendorId}"
                        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small))
                        {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            )
                            {
                                Text(
                                    text = vendorName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small,
                                )
                                {
                                    Text(
                                        text = order.status.name,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                            Text(
                                text = "Order ID: #${order.orderId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Text(
                                text = "Total: R${String.format(locale, "%.2f", order.totalAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "Pickup: ${order.pickupTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
            }

            if (cartItems.isNotEmpty())
            {
                item()
                {
                    Text(
                        text = "Active Cart",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                items(cartItems)
                { item ->
                    HIGCard(modifier = Modifier.fillMaxWidth())
                    {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        {
                            Column(modifier = Modifier.weight(1f))
                            {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "R${String.format(locale, "%.2f", item.price)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically)
                            {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch()
                                        {
                                            if (item.quantity > 1)
                                            {
                                                cartRepository.removeFromCart(item)
                                            }
                                            else
                                            {
                                                cartRepository.deleteCartItem(item)
                                            }
                                        }
                                    },
                                )
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Decrease",
                                    )
                                }

                                Text(
                                    text = item.quantity.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                )

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch()
                                        {
                                            cartRepository.incrementCartItem(item)
                                        }
                                    },
                                )
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowUp,
                                        contentDescription = "Increase",
                                    )
                                }
                            }
                        }
                    }
                }
                item()
                {
                    val subtotal = cartItems.sumOf { it.price * it.quantity }
                    Button(
                        onClick = onNavigateToCheckout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    )
                    {
                        Text(
                            text = "Proceed to Checkout • R${String.format(locale, "%.2f", subtotal)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentActivityReports(
    userId: String,
    orderRepository: OrderRepository,
    menuRepository: MenuRepository,
)
{
    val orders by orderRepository.getOrdersForUser(userId).collectAsState(emptyList())
    val vendors by menuRepository.getAllVendors().collectAsState(emptyList())
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locale = LocalConfiguration.current.locales[0]

    val spendingByVendor = remember(orders, vendors)
    {
        orders.asSequence()
            .filter { it.status == OrderStatus.COMPLETED }
            .groupBy { it.vendorId }
            .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
            .asSequence()
            .map { entry ->
                val vendorName = vendors.find { it.userId == entry.key }?.shopName ?: "Unknown Vendor"
                vendorName to entry.value
            }.sortedByDescending { it.second }
            .toList()
    }

    val totalSpending = spendingByVendor.sumOf { it.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.large),
    )
    {
        Text(
            text = "Spending by Vendor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        if (spendingByVendor.isNotEmpty())
        {
            // Simplified Pie Chart using Canvas
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(DesignSystem.Spacing.medium),
                contentAlignment = Alignment.Center,
            )
            {
                Canvas(modifier = Modifier.fillMaxSize())
                {
                    var startAngle = -90f
                    val colors = listOf(CampusOrange, Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFF9C27B0))
                    
                    spendingByVendor.forEachIndexed { index, pair ->
                        val sweepAngle = if (totalSpending > 0) (pair.second / totalSpending).toFloat() * 360f else 0f
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 40.dp.toPx()),
                        )
                        
                        // Requirement: Display the corresponding numeric spending value on the chart.
                        if (sweepAngle > 30) {
                            val angleInRadians = Math.toRadians((startAngle + (sweepAngle / 2)).toDouble())
                            val textRadius = (size.minDimension / 2) - 10.dp.toPx()
                            val x = (size.width / 2) + (cos(angleInRadians) * textRadius).toFloat()
                            val y = (size.height / 2) + (sin(angleInRadians) * textRadius).toFloat()
                            
                            drawContext.canvas.nativeCanvas.drawText(
                                "R${pair.second.toInt()}",
                                x,
                                y,
                                Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 12.sp.toPx()
                                    textAlign = Paint.Align.CENTER
                                    isFakeBoldText = true
                                },
                            )
                        }

                        startAngle += sweepAngle
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally)
                {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = "R${String.format(locale, "%.0f", totalSpending)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small),
            )
            {
                spendingByVendor.forEachIndexed { index, (name, amount) ->
                    val percentage = if (totalSpending > 0) ((amount / totalSpending) * 100).toInt() else 0
                    val colors = listOf(CampusOrange, Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFF9C27B0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    )
                    {
                        Row(verticalAlignment = Alignment.CenterVertically)
                        {
                            Box(modifier = Modifier.size(12.dp).background(colors[index % colors.size], CircleShape))
                            Spacer(modifier = Modifier.width(DesignSystem.Spacing.small))
                            Text(text = name, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = "R${String.format(locale, "%.2f", amount)} ($percentage%)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        else
        {
            Text(
                text = "No completed orders found.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.large))

        Button(
            onClick = {
                coroutineScope.launch()
                {
                    try
                    {
                        val reportData = mapOf(
                            "userId" to userId,
                            "totalSpending" to totalSpending,
                            "vendorBreakdown" to spendingByVendor.map { mapOf("vendor" to it.first, "amount" to it.second) },
                            "orderCount" to orders.size,
                        )
                        val json = prettyJson.encodeToString(reportData)
                        val file = File(context.getExternalFilesDir(null), "spending_report.json")
                        file.writeText(json)
                        Toast.makeText(context, "Report exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    }
                    catch (_: Exception)
                    {
                        Toast.makeText(context, "Export failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        )
        {
            Icon(imageVector = Icons.Rounded.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.small))
            Text(text = "Export as JSON")
        }
    }
}

@Composable
fun VendorOrderHub(
    vendorId: String,
    orderRepository: OrderRepository,
    onOrderClick: (OrderEntity) -> Unit,
)
{
    val orders by orderRepository.getOrdersForVendor(vendorId).collectAsState(emptyList())
    val locale = LocalConfiguration.current.locales[0]
    val coroutineScope = rememberCoroutineScope()

    val activeOrders = remember(orders)
    {
        orders.filter { (it.status != OrderStatus.COMPLETED) && (it.status != OrderStatus.CANCELLED) }
    }

    if (activeOrders.isEmpty())
    {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center)
        {
            Text(text = "No pending orders.", color = MaterialTheme.colorScheme.outline)
        }
    }
    else
    {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = DesignSystem.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small),
        )
        {
            items(activeOrders)
            { order ->
                HIGCard(modifier = Modifier.fillMaxWidth(), onClick = { onOrderClick(order) })
                {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    )
                    {
                        Column()
                        {
                            Text(text = "Order #${order.orderId}", fontWeight = FontWeight.Bold)
                            Text(text = "R${String.format(locale, "%.2f", order.totalAmount)}")
                        }
                        IconButton(
                            onClick = {
                                coroutineScope.launch()
                                {
                                    val nextStatus = when (order.status)
                                    {
                                        OrderStatus.PENDING -> OrderStatus.ACCEPTED
                                        OrderStatus.ACCEPTED -> OrderStatus.PREPARING
                                        OrderStatus.PREPARING -> OrderStatus.READY
                                        OrderStatus.READY -> OrderStatus.COMPLETED
                                        else -> order.status
                                    }
                                    orderRepository.updateOrderStatus(order, nextStatus)
                                }
                            },
                        )
                        {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Progress",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VendorReportHub(
    vendorId: String,
    orderRepository: OrderRepository,
)
{
    val orders by orderRepository.getOrdersForVendor(vendorId).collectAsState(emptyList())
    val completedOrders = remember(orders) { orders.filter { it.status == OrderStatus.COMPLETED } }
    val totalRevenue = completedOrders.sumOf { it.totalAmount }
    val locale = LocalConfiguration.current.locales[0]

    val itemRevenue = remember(completedOrders)
    {
        val revenueMap = mutableMapOf<String, Double>()
        completedOrders.forEach { order ->
            try
            {
                val items = Json.decodeFromString<List<CartItemEntity>>(order.itemsJson)
                items.forEach { item ->
                    revenueMap[item.name] = (revenueMap[item.name] ?: 0.0) + (item.price * item.quantity)
                }
            }
            catch (_: Exception) { }
        }
        revenueMap.asSequence().map { it.key to it.value }.sortedByDescending { it.second }.toList()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.large),
    )
    {
        HIGCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        )
        {
            Column()
            {
                Text(
                    text = "Total Completed Revenue",
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = "R${String.format(locale, "%.2f", totalRevenue)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                )
            }
        }

        Text(
            text = "Revenue by Item",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small))
        {
            itemRevenue.forEach { (name, revenue) ->
                HIGCard(modifier = Modifier.fillMaxWidth())
                {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    )
                    {
                        Text(text = name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            text = "R${String.format(locale, "%.2f", revenue)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReceiptsHub(orderRepository: OrderRepository)
{
    val orders by orderRepository.getAllOrders().collectAsState(emptyList())
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = DesignSystem.Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small),
    )
    {
        items(orders)
        { order ->
            HIGCard(modifier = Modifier.fillMaxWidth())
            {
                Text(text = "Order #${order.orderId} - R${order.totalAmount}")
            }
        }
    }
}

@Composable
fun AdminGlobalSummary(orderRepository: OrderRepository)
{
    val orders by orderRepository.getAllOrders().collectAsState(emptyList())
    val total = orders.asSequence().filter { it.status == OrderStatus.COMPLETED }.sumOf { it.totalAmount }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center)
    {
        Text(text = "Global Revenue: R$total", style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
fun AdminReportHub()
{
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center)
    {
        Text(text = "System Admin Reports")
    }
}

@Composable
fun OrderDetailWindow(
    order: OrderEntity?,
    @Suppress("UNUSED_PARAMETER") role: UserRole,
    @Suppress("UNUSED_PARAMETER") userId: String,
    @Suppress("UNUSED_PARAMETER") orderRepository: OrderRepository,
    onBack: () -> Unit,
)
{
    if (order == null)
    {
        onBack()
        return
    }

    val locale = LocalConfiguration.current.locales[0]
    val items = remember(order)
    {
        try
        {
            Json.decodeFromString<List<CartItemEntity>>(order.itemsJson)
        }
        catch (_: Exception)
        {
            emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.large),
    )
    {
        HIGCard(modifier = Modifier.fillMaxWidth())
        {
            Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small))
            {
                Text(text = "Status: ${order.status}", fontWeight = FontWeight.Bold)
                Text(text = "Date: ${SimpleDateFormat("dd MMM yyyy, HH:mm", locale).format(Date(order.timestamp))}")
                Text(text = "Payment: ${order.paymentMethod}")
                Text(text = "Pickup Time: ${order.pickupTime}")
                order.specialRequests?.let { Text(text = "Notes: $it", color = MaterialTheme.colorScheme.outline) }
            }
        }

        Text(text = "Items", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        items.forEach()
        { item ->
            HIGCard(modifier = Modifier.fillMaxWidth())
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                )
                {
                    Text(text = "${item.quantity}x ${item.name}")
                    Text(text = "R${String.format(locale, "%.2f", item.price * item.quantity)}")
                }
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        )
        {
            Text(text = "Grand Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(
                text = "R${String.format(locale, "%.2f", order.totalAmount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminIssueCreditsWindow(viewModel: AdminViewModel)
{
    val users by viewModel.users.collectAsState()
    val eligibleUsers = remember(users)
    {
        users.filter { (it.role == UserRole.STUDENT) || (it.role == UserRole.STANDARD) }
    }

    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }
    var name by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var expiryDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val successMsg = remember { mutableStateOf("") }
    val errorMsg = remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = expiryDate,
        selectableDates = object : SelectableDates
        {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean
            {
                val now = System.currentTimeMillis()
                val fortyDays = now + (40L * 24 * 60 * 60 * 1000)
                // Allow today and up to 40 days in the future
                return (utcTimeMillis >= (now - (24 * 60 * 60 * 1000))) && (utcTimeMillis <= fortyDays)
            }
        },
    )

    if (showDatePicker)
    {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { expiryDate = it }
                        showDatePicker = false
                    },
                )
                {
                    Text("OK")
                }
            },
        )
        {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
    )
    {
        MinimalDropdown(
            label = "Select User",
            selectedOption = selectedUser?.fullName ?: "Choose a user...",
            options = eligibleUsers.map { it.fullName },
            onOptionSelected = { fullName ->
                selectedUser = eligibleUsers.find { it.fullName == fullName }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Coupon Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = name.isEmpty() && errorMsg.value.isNotEmpty(),
        )

        OutlinedTextField(
            value = discount,
            onValueChange = { discount = it },
            label = { Text("Coupon Discount (Max 20%)") },
            modifier = Modifier.fillMaxWidth(),
            isError = (discount.toDoubleOrNull() ?: 0.0) > 20.0,
        )

        val locale = LocalConfiguration.current.locales[0]
        val dateStr = SimpleDateFormat("dd MMM yyyy", locale).format(Date(expiryDate))

        OutlinedTextField(
            value = dateStr,
            onValueChange = {},
            label = { Text("Valid Date") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.DateRange,
                    contentDescription = null,
                    modifier = Modifier.clickable { showDatePicker = true },
                )
            },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )

        if (errorMsg.value.isNotEmpty())
        {
            Text(text = errorMsg.value, color = MaterialTheme.colorScheme.error)
        }
        if (successMsg.value.isNotEmpty())
        {
            Text(text = successMsg.value, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        }

        HIGButton(
            onClick = {
                val d = discount.toDoubleOrNull() ?: 0.0
                if (selectedUser == null)
                {
                    errorMsg.value = "Please select a user."
                }
                else if (name.isBlank())
                {
                    errorMsg.value = "Coupon name is required."
                }
                else if (d <= 0 || d > 20)
                {
                    errorMsg.value = "Discount must be between 0.1% and 20%."
                }
                else
                {
                    viewModel.generateCoupon(name, d, expiryDate, selectedUser?.userId)
                    successMsg.value = "Coupon '$name' issued to ${selectedUser?.fullName}."
                    errorMsg.value = ""
                }
            },
            text = "Done",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGenerateCouponsWindow(viewModel: AdminViewModel)
{
    var name by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var expiryDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val successMsg = remember { mutableStateOf("") }
    val errorMsg = remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = expiryDate,
        selectableDates = object : SelectableDates
        {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean
            {
                val now = System.currentTimeMillis()
                val fortyDays = now + (40L * 24 * 60 * 60 * 1000)
                return (utcTimeMillis >= (now - (24 * 60 * 60 * 1000))) && (utcTimeMillis <= fortyDays)
            }
        },
    )

    if (showDatePicker)
    {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { expiryDate = it }
                        showDatePicker = false
                    },
                )
                {
                    Text("OK")
                }
            },
        )
        {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
    )
    {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Coupon Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = name.isEmpty() && errorMsg.value.isNotEmpty(),
        )

        OutlinedTextField(
            value = discount,
            onValueChange = { discount = it },
            label = { Text("Coupon Discount (Max 20%)") },
            modifier = Modifier.fillMaxWidth(),
            isError = (discount.toDoubleOrNull() ?: 0.0) > 20.0,
        )

        val locale = LocalConfiguration.current.locales[0]
        val dateStr = SimpleDateFormat("dd MMM yyyy", locale).format(Date(expiryDate))

        OutlinedTextField(
            value = dateStr,
            onValueChange = {},
            label = { Text("Valid Date") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.DateRange,
                    contentDescription = null,
                    modifier = Modifier.clickable { showDatePicker = true },
                )
            },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )

        if (errorMsg.value.isNotEmpty())
        {
            Text(text = errorMsg.value, color = MaterialTheme.colorScheme.error)
        }
        if (successMsg.value.isNotEmpty())
        {
            Text(text = successMsg.value, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        }

        HIGButton(
            onClick = {
                val d = discount.toDoubleOrNull() ?: 0.0
                if (name.isBlank())
                {
                    errorMsg.value = "Coupon name is required."
                }
                else if (d <= 0 || d > 20)
                {
                    errorMsg.value = "Discount must be between 0.1% and 20%."
                }
                else
                {
                    viewModel.generateCoupon(name, d, expiryDate)
                    successMsg.value = "Coupon '$name' created successfully."
                    errorMsg.value = ""
                }
            },
            text = "Done",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun AdminFeedbackWindow(viewModel: AdminViewModel, type: FeedbackType)
{
    val feedbacks by viewModel.getFeedbackByType(type).collectAsState(emptyList())
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = DesignSystem.Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small),
    )
    {
        items(feedbacks)
        { fb ->
            HIGCard(modifier = Modifier.fillMaxWidth())
            {
                Column()
                {
                    Text(text = fb.subject, fontWeight = FontWeight.Bold)
                    Text(text = "From: ${fb.userName} (${fb.userEmail})", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = fb.message, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun StudentRedeemCouponWindow(@Suppress("UNUSED_PARAMETER") couponRepository: CouponRepository)
{
    var code by remember { mutableStateOf(value = "") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
    {
        Text(text = "Enter a promotional code to add credits to your wallet.")
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Code") },
            modifier = Modifier.fillMaxWidth(),
        )
        HIGButton(
            onClick = {
                coroutineScope.launch()
                {
                    // Mock logic for demo
                    Toast.makeText(context, "Coupon verification pending...", Toast.LENGTH_SHORT).show()
                }
            },
            text = "Redeem",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun StudentAddCardWindow(debitCardRepository: DebitCardRepository, userId: String)
{
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
    {
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { cardNumber = it },
            label = { Text("Card Number") },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
        {
            OutlinedTextField(
                value = expiry,
                onValueChange = { expiry = it },
                label = { Text("Expiry (MM/YY)") },
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = cvv,
                onValueChange = { cvv = it },
                label = { Text("CVV") },
                modifier = Modifier.weight(1f),
            )
        }
        HIGButton(
            onClick = {
                coroutineScope.launch()
                {
                    debitCardRepository.addCard(
                        userId = userId,
                        cardNumber = cardNumber,
                        expiryDate = expiry,
                        cvv = cvv,
                    )
                    Toast.makeText(context, "Card linked successfully.", Toast.LENGTH_SHORT).show()
                }
            },
            text = "Link Card",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun VendorBankDetailsWindow(authRepository: AuthRepository, userId: String, currentInfo: String?)
{
    var info by remember { mutableStateOf(currentInfo ?: "") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
    {
        Text(text = "Configure your bank account for automated daily payouts.")
        OutlinedTextField(
            value = info,
            onValueChange = { info = it },
            label = { Text("Account Details") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
        HIGButton(
            onClick = {
                coroutineScope.launch()
                {
                    authRepository.linkBankAccount(userId, info)
                    Toast.makeText(context, "Bank details updated.", Toast.LENGTH_SHORT).show()
                }
            },
            text = "Save Configuration",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun UserFeedbackWindow(feedbackRepository: FeedbackRepository, userId: String)
{
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(FeedbackType.COMPLIMENT) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
    {
        HIGSegmentedControl(
            options = FeedbackType.entries,
            selectedOption = type,
            onOptionSelected = { type = it },
            labelProvider = { it.name.lowercase().replaceFirstChar { char -> char.uppercase() } },
        )
        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Details") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
        )
        HIGButton(
            onClick = {
                coroutineScope.launch()
                {
                    feedbackRepository.submitFeedback(userId, subject, message, type)
                    Toast.makeText(context, "Feedback submitted. Thank you!", Toast.LENGTH_SHORT).show()
                    subject = ""
                    message = ""
                }
            },
            text = "Submit Feedback",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
