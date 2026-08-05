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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.campus_eats_app_kt.ui.theme.CampusOrange
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
    cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    authRepository: AuthRepository,
    val userId: String,
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
        onSuccess: (Long) -> Unit,
    )
    {
        viewModelScope.launch {
            val items = cartItems.value
            val sum = summary.value
            if (items.isNotEmpty() && (sum != null))
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

    val locale = LocalConfiguration.current.locales[0]

    // Hardcoded logic for valid pickup intervals (10:00 AM to 16:00 PM)
    val pickupTimes = remember {
        (10..15).flatMap { hour ->
            listOf("00", "15", "30", "45").map { min -> "${hour.toString().padStart(2, '0')}:$min" }
        } + "16:00"
    }

    Scaffold(
        topBar = {
            HIGTopAppBar(
                title = "Order summary",
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
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CampusOrange,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Place order, R${
                                    String.format(
                                        locale,
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
            // Items Section
            item {
                SectionHeader(title = "Items")
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))
                cartItems.forEach { item ->
                    ItemSummaryRow(item = item, locale = locale)
                }
            }

            // Pickup Time Section
            item {
                SectionHeader(title = "Pickup time")
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)
                ) {
                    items(pickupTimes) { time ->
                        val isSelected = selectedPickupTime == time
                        Surface(
                            onClick = { selectedPickupTime = time },
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Text(
                                text = time,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Payment Section
            item {
                SectionHeader(title = "Payment")
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
                        SummaryRow("Subtotal", sum.subtotal, locale = locale)
                        SummaryRow("Tax (20%)", sum.tax, locale = locale)
                        SummaryRow("Service Fee", sum.serviceFee, locale = locale)

                        if (sum.studentDiscount > 0)
                        {
                            SummaryRow(
                                label = "Student Discount (2.5%)",
                                amount = -sum.studentDiscount,
                                color = MaterialTheme.colorScheme.primary,
                                locale = locale
                            )
                        }

                        val rounding =
                            sum.total - ((sum.subtotal + sum.tax + sum.serviceFee) - sum.studentDiscount)
                        if (kotlin.math.abs(rounding) > 0.001)
                        {
                            SummaryRow("Rounding Adjustment", rounding, locale = locale)
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
                                text = "R${String.format(locale, "%.2f", sum.total)}",
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
fun ItemSummaryRow(item: CartItemEntity, locale: Locale)
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
            text = "R${String.format(locale, "%.2f", item.price * item.quantity)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
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
            label = "Debit card",
            selected = selectedMethod == PaymentMethod.DEBIT_CARD,
            onClick = { onMethodSelected(PaymentMethod.DEBIT_CARD) }
        )
        PaymentOption(
            label = "Campus Wallet",
            selected = selectedMethod == PaymentMethod.CAMPUS_WALLET,
            onClick = { onMethodSelected(PaymentMethod.CAMPUS_WALLET) }
        )
        PaymentOption(
            label = "Coupons",
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
    locale: Locale,
    color: Color = MaterialTheme.colorScheme.onSurface
)
{
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = color)
        Text(
            text = if (amount >= 0) "R${String.format(locale, "%.2f", amount)}"
            else "-R${String.format(locale, "%.2f", -amount)}",
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PaymentOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
)
{
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DesignSystem.Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.small))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
