package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.PaymentMethod
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.util.CheckoutEngine
import com.example.campus_eats_app_kt.util.CheckoutSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class CheckoutViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    val userId: String
) : ViewModel() {
    val cartItems: StateFlow<List<CartItemEntity>> = cartRepository.getCart(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userRole: StateFlow<UserRole> = authRepository.getUserFlow(userId)
        .map { it?.role ?: UserRole.STANDARD }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserRole.STANDARD)

    val summary: StateFlow<CheckoutSummary?> = combine(cartItems, userRole) { items, role ->
        if (items.isEmpty()) return@combine null
        val subtotal = items.sumOf { it.price * it.quantity }
        CheckoutEngine.calculateSummary(subtotal, role)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun placeOrder(
        paymentMethod: PaymentMethod,
        pickupTime: String,
        specialRequests: String?,
        onSuccess: (Long) -> Unit
    )
    {
        viewModelScope.launch {
            val items = cartItems.value
            val sum = summary.value
            if (items.isNotEmpty() && sum != null) {
                val vendorId = items.first().vendorId
                val orderId = orderRepository.placeOrder(
                    userId = userId,
                    vendorId = vendorId,
                    cartItems = items,
                    totalAmount = sum.total,
                    paymentMethod = paymentMethod,
                    pickupTime = pickupTime,
                    specialRequests = specialRequests
                )
                onSuccess(orderId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderPlaced: (Long) -> Unit,
    viewModel: CheckoutViewModel
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val role by viewModel.userRole.collectAsState()

    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.DEBIT_CARD) }
    var selectedPickupTime by remember { mutableStateOf("12:00") }
    var specialRequests by remember { mutableStateOf("") }

    val pickupTimes = remember {
        (10..15).flatMap { hour ->
            listOf("00", "15", "30", "45").map { min -> "${hour.toString().padStart(2, '0')}:$min" }
        } + "16:00"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout Summary", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            summary?.let { sum ->
                Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Button(
                            onClick = {
                                viewModel.placeOrder(
                                    selectedPaymentMethod,
                                    selectedPickupTime,
                                    specialRequests.takeIf { it.isNotBlank() },
                                    onOrderPlaced
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(
                                "Place Order - R${
                                    String.format(
                                        Locale.getDefault(),
                                        "%.2f",
                                        sum.total
                                    )
                                }",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SectionHeader("Items")
                Spacer(modifier = Modifier.height(8.dp))
                cartItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${item.quantity}x ${item.name}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "R${
                                String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    item.price * item.quantity
                                )
                            }",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                SectionHeader("Pickup Time")
                Spacer(modifier = Modifier.height(8.dp))
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedPickupTime,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Rounded.AccessTime, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        pickupTimes.forEach { time ->
                            DropdownMenuItem(
                                text = { Text(time) },
                                onClick = {
                                    selectedPickupTime = time
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader("Payment Method")
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentOption(
                        method = PaymentMethod.DEBIT_CARD,
                        label = "Debit Card",
                        selected = selectedPaymentMethod == PaymentMethod.DEBIT_CARD,
                        onClick = { selectedPaymentMethod = PaymentMethod.DEBIT_CARD }
                    )
                    PaymentOption(
                        method = PaymentMethod.CAMPUS_WALLET,
                        label = "Campus Wallet",
                        selected = selectedPaymentMethod == PaymentMethod.CAMPUS_WALLET,
                        onClick = { selectedPaymentMethod = PaymentMethod.CAMPUS_WALLET }
                    )
                    PaymentOption(
                        method = PaymentMethod.COUPON,
                        label = "Apply Coupon",
                        selected = selectedPaymentMethod == PaymentMethod.COUPON,
                        onClick = { selectedPaymentMethod = PaymentMethod.COUPON }
                    )
                }
            }

            item {
                SectionHeader("Special Requests")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = specialRequests,
                    onValueChange = { specialRequests = it },
                    placeholder = { Text("Allergies, extra sauce, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = MaterialTheme.shapes.medium
                )
            }

            summary?.let { sum ->
                item {
                    SectionHeader("Totals")
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryRow("Subtotal", sum.subtotal)
                        SummaryRow("Tax (20%)", sum.tax)
                        SummaryRow("Service Fee", sum.serviceFee)
                        if (sum.studentDiscount > 0) {
                            SummaryRow("Student Discount (2.5%)", -sum.studentDiscount, color = MaterialTheme.colorScheme.primary)
                        }

                        val rounding =
                            sum.total - (sum.subtotal + sum.tax + sum.serviceFee - sum.studentDiscount)
                        if (rounding > 0.01)
                        {
                            SummaryRow("Rounding Adjustment", rounding)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "Total",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "R${String.format(Locale.getDefault(), "%.2f", sum.total)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SectionHeader(title: String)
{
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.outline
    )
}

@Composable
fun SummaryRow(label: String, amount: Double, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = color)
        Text(
            if (amount >= 0) "R${String.format(Locale.getDefault(), "%.2f", amount)}"
            else "-R${String.format(Locale.getDefault(), "%.2f", -amount)}",
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PaymentOption(
    method: PaymentMethod,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
)
{
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) androidx.compose.foundation.BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.primary
        )
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.Payment,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
