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
import com.example.campus_eats_app_kt.ui.components.HIGTopAppBar
import com.example.campus_eats_app_kt.ui.theme.DesignSystem
import com.example.campus_eats_app_kt.util.CheckoutEngine
import com.example.campus_eats_app_kt.util.CheckoutSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * CheckoutViewModel coordinates the finalization of an order.
 * It combines cart items and user profile data to generate a definitive financial summary.
 */
class CheckoutViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    val userId: String
) : ViewModel()
{
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

    /**
     * Executes the order placement process.
     */
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
            if (items.isNotEmpty() && sum != null)
            {
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

/**
 * CheckoutScreen provides the final interface for users to select payment methods,
 * specify pickup times, and confirm their purchase.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderPlaced: (Long) -> Unit,
    viewModel: CheckoutViewModel
)
{
    val cartItems by viewModel.cartItems.collectAsState()
    val summary by viewModel.summary.collectAsState()

    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.DEBIT_CARD) }
    var selectedPickupTime by remember { mutableStateOf("12:00") }
    var specialRequests by remember { mutableStateOf("") }

    // Hardcoded logic for valid pickup intervals (10:00 AM to 16:00 PM)
    val pickupTimes = remember {
        (10..15).flatMap { hour ->
            listOf("00", "15", "30", "45").map { min -> "${hour.toString().padStart(2, '0')}:$min" }
        } + "16:00"
    }

    Scaffold(
        topBar = {
            HIGTopAppBar(
                title = "Checkout Summary",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            summary?.let { sum ->
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(DesignSystem.Spacing.screenPadding)) {
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
                                text = "Place Order - R${
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
            contentPadding = PaddingValues(DesignSystem.Spacing.large),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.large)
        ) {
            // Summary of items to be purchased
            item {
                SectionHeader(title = "Items")
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))
                cartItems.forEach { item ->
                    ItemSummaryRow(item = item)
                }
            }

            // Pickup Time Selection module
            item {
                SectionHeader(title = "Pickup Time")
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))
                PickupTimePicker(
                    selectedTime = selectedPickupTime,
                    times = pickupTimes,
                    onTimeSelected = { selectedPickupTime = it }
                )
            }

            // Financial transaction method module
            item {
                SectionHeader(title = "Payment Method")
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))
                PaymentMethodSelector(
                    selectedMethod = selectedPaymentMethod,
                    onMethodSelected = { selectedPaymentMethod = it }
                )
            }

            // User-provided fulfillment instructions
            item {
                SectionHeader(title = "Special Requests")
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))
                OutlinedTextField(
                    value = specialRequests,
                    onValueChange = { specialRequests = it },
                    placeholder = { Text("Allergies, extra sauce, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = MaterialTheme.shapes.medium
                )
            }

            // Final financial breakdown
            summary?.let { sum ->
                item {
                    SectionHeader(title = "Totals")
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))
                    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)) {
                        SummaryRow("Subtotal", sum.subtotal)
                        SummaryRow("Tax (20%)", sum.tax)
                        SummaryRow("Service Fee", sum.serviceFee)

                        if (sum.studentDiscount > 0)
                        {
                            SummaryRow(
                                label = "Student Discount (2.5%)",
                                amount = -sum.studentDiscount,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        val rounding =
                            sum.total - (sum.subtotal + sum.tax + sum.serviceFee - sum.studentDiscount)
                        if (kotlin.math.abs(rounding) > 0.001)
                        {
                            SummaryRow("Rounding Adjustment", rounding)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = DesignSystem.Spacing.small))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "R${String.format(Locale.getDefault(), "%.2f", sum.total)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(DesignSystem.Spacing.extraLarge)) }
        }
    }
}

@Composable
fun ItemSummaryRow(item: CartItemEntity)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${item.quantity}x ${item.name}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "R${String.format(Locale.getDefault(), "%.2f", item.price * item.quantity)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickupTimePicker(
    selectedTime: String,
    times: List<String>,
    onTimeSelected: (String) -> Unit
)
{
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedTime,
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
            times.forEach { time ->
                DropdownMenuItem(
                    text = { Text(time) },
                    onClick = {
                        onTimeSelected(time)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentMethodSelector(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit
)
{
    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)) {
        PaymentOption(
            method = PaymentMethod.DEBIT_CARD,
            label = "Debit Card",
            selected = selectedMethod == PaymentMethod.DEBIT_CARD,
            onClick = { onMethodSelected(PaymentMethod.DEBIT_CARD) }
        )
        PaymentOption(
            method = PaymentMethod.CAMPUS_WALLET,
            label = "Campus Wallet",
            selected = selectedMethod == PaymentMethod.CAMPUS_WALLET,
            onClick = { onMethodSelected(PaymentMethod.CAMPUS_WALLET) }
        )
        PaymentOption(
            method = PaymentMethod.COUPON,
            label = "Apply Coupon",
            selected = selectedMethod == PaymentMethod.COUPON,
            onClick = { onMethodSelected(PaymentMethod.COUPON) }
        )
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
fun SummaryRow(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
)
{
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = color)
        Text(
            text = if (amount >= 0) "R${String.format(Locale.getDefault(), "%.2f", amount)}"
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
                .padding(DesignSystem.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.Payment,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.medium))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
