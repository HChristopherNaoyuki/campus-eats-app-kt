package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.util.CheckoutEngine
import com.example.campus_eats_app_kt.util.CheckoutSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class CheckoutViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    val userId: String
) : ViewModel() {
    val cartItems: StateFlow<List<CartItemEntity>> = cartRepository.getCart(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<CheckoutSummary?> = cartItems.map { items ->
        if (items.isEmpty()) return@map null
        val subtotal = items.sumOf { it.price * it.quantity }
        CheckoutEngine.calculateSummary(subtotal, UserRole.STUDENT)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun placeOrder(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val items = cartItems.value
            val sum = summary.value
            if (items.isNotEmpty() && sum != null) {
                val vendorId = items.first().vendorId
                orderRepository.placeOrder(userId, vendorId, items, sum.total)
                onSuccess()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderPlaced: () -> Unit,
    viewModel: CheckoutViewModel
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val summary by viewModel.summary.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            summary?.let { sum ->
                Surface(tonalElevation = 8.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { viewModel.placeOrder(onOrderPlaced) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text("Place Order - R${String.format(Locale.getDefault(), "%.2f", sum.total)}")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Order Items", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            items(cartItems) { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.quantity}x ${item.name}")
                    Text("R${String.format(Locale.getDefault(), "%.2f", item.price * item.quantity)}")
                }
            }
            item { HorizontalDivider() }
            summary?.let { sum ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryRow("Subtotal", sum.subtotal)
                        SummaryRow("Tax (20%)", sum.tax)
                        SummaryRow("Service Fee", sum.serviceFee)
                        if (sum.studentDiscount > 0) {
                            SummaryRow("Student Discount (2.5%)", -sum.studentDiscount, color = MaterialTheme.colorScheme.primary)
                        }
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("R${String.format(Locale.getDefault(), "%.2f", sum.total)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, amount: Double, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = color)
        Text("R${String.format(Locale.getDefault(), "%.2f", amount)}", color = color)
    }
}
