package com.example.campus_eats_app_kt.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class StudentDashboardViewModel(
    private val orderRepository: OrderRepository,
    val userId: String
) : ViewModel() {
    val orders: StateFlow<List<OrderEntity>> = orderRepository.getOrdersForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun exportHistory(context: Context) {
        viewModelScope.launch {
            val historyJson = Json.encodeToString(orders.value)
            val file = File(context.getExternalFilesDir(null), "order_history_${userId}.json")
            file.writeText(historyJson)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun StudentDashboardScreen(
    onBrowseVendors: () -> Unit,
    onLogout: () -> Unit,
    viewModel: StudentDashboardViewModel
) {
    val orders by viewModel.orders.collectAsState()
    val context = LocalContext.current
    val navigator = rememberListDetailPaneScaffoldNavigator<OrderEntity>()
    val coroutineScope = rememberCoroutineScope()

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("My Orders") },
                        actions = {
                            IconButton(onClick = { viewModel.exportHistory(context) }) {
                                Icon(Icons.Rounded.Download, contentDescription = "Export JSON")
                            }
                            IconButton(onClick = onLogout) {
                                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Logout")
                            }
                        }
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = onBrowseVendors,
                        icon = { Icon(Icons.Rounded.ShoppingCart, contentDescription = null) },
                        text = { Text("Browse Food") }
                    )
                }
            ) { innerPadding ->
                if (orders.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                            Text("No orders yet.", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders) { order ->
                            OrderCard(
                                order = order,
                                onClick = { 
                                    coroutineScope.launch {
                                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, order) 
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        detailPane = {
            val selectedOrder = navigator.currentDestination?.contentKey
            if (selectedOrder != null) {
                OrderDetailPane(order = selectedOrder)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select an order to see details")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCard(order: OrderEntity, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order #${order.orderId}", fontWeight = FontWeight.Bold)
                StatusBadge(order.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total: R${String.format(Locale.getDefault(), "%.2f", order.totalAmount)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(dateFormat.format(Date(order.timestamp)), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun OrderDetailPane(order: OrderEntity) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Order Details", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow("Order ID", "#${order.orderId}")
                DetailRow("Status", order.status.name)
                DetailRow("Date", dateFormat.format(Date(order.timestamp)))
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow("Total Paid", "R${String.format(Locale.getDefault(), "%.2f", order.totalAmount)}", isPrimary = true)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Items", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(order.itemsJson, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun DetailRow(label: String, value: String, isPrimary: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value, 
            style = if (isPrimary) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal,
            color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StatusBadge(status: com.example.campus_eats_app_kt.data.entity.OrderStatus) {
    val color = when (status) {
        com.example.campus_eats_app_kt.data.entity.OrderStatus.PENDING -> MaterialTheme.colorScheme.secondary
        com.example.campus_eats_app_kt.data.entity.OrderStatus.ACTIVE -> MaterialTheme.colorScheme.primary
        com.example.campus_eats_app_kt.data.entity.OrderStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
        com.example.campus_eats_app_kt.data.entity.OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
