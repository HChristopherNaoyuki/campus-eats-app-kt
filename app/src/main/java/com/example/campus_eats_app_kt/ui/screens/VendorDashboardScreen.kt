package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.entity.OrderEntity
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class VendorDashboardViewModel(
    private val orderRepository: OrderRepository,
    val vendorId: String
) : ViewModel() {
    val orders: StateFlow<List<OrderEntity>> = orderRepository.getOrdersForVendor(vendorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analytics: StateFlow<VendorAnalytics> = orders.map { list ->
        VendorAnalytics(
            totalRevenue = list.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.totalAmount },
            orderCount = list.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VendorAnalytics(0.0, 0))

    fun updateStatus(order: OrderEntity, status: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(order, status)
        }
    }
}

data class VendorAnalytics(val totalRevenue: Double, val orderCount: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    onManageMenu: () -> Unit,
    onLogout: () -> Unit,
    viewModel: VendorDashboardViewModel
) {
    val orders by viewModel.orders.collectAsState()
    val analytics by viewModel.analytics.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendor Dashboard") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onManageMenu,
                icon = { Icon(Icons.Rounded.RestaurantMenu, contentDescription = null) },
                text = { Text("Manage Menu") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AnalyticsCard(analytics)
            }
            item {
                Text("Incoming Orders", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            if (orders.isEmpty()) {
                item {
                    Text("No orders yet.", modifier = Modifier.padding(vertical = 32.dp))
                }
            } else {
                items(orders) { order ->
                    VendorOrderCard(order, onUpdateStatus = { viewModel.updateStatus(order, it) })
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(analytics: VendorAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Revenue", style = MaterialTheme.typography.labelMedium)
                Text("R${String.format(Locale.getDefault(), "%.2f", analytics.totalRevenue)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Orders", style = MaterialTheme.typography.labelMedium)
                Text("${analytics.orderCount}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VendorOrderCard(order: OrderEntity, onUpdateStatus: (OrderStatus) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order #${order.orderId}", fontWeight = FontWeight.Bold)
                StatusBadge(order.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Amount: R${String.format(Locale.getDefault(), "%.2f", order.totalAmount)}")
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (order.status == OrderStatus.PENDING) {
                    Button(onClick = { onUpdateStatus(OrderStatus.ACTIVE) }) {
                        Text("Accept")
                    }
                }
                if (order.status == OrderStatus.ACTIVE) {
                    Button(onClick = { onUpdateStatus(OrderStatus.COMPLETED) }) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ready")
                    }
                }
            }
        }
    }
}
