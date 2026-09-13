package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed interface CheckoutState
{
    data object Idle : CheckoutState
    data object Processing : CheckoutState
    data class Success(val orderId: Long) : CheckoutState
    data class Error(val message: String) : CheckoutState
}

/**
 * CheckoutViewModel manages the final transaction flow.
 * Handles payment method selection, order construction, and repository persistence.
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
        .map()
        { 
            it?.role ?: UserRole.STANDARD 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserRole.STANDARD)

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState

    fun placeOrder(
        paymentMethod: PaymentMethod,
        pickupTime: String,
        specialRequests: String?,
        onSuccess: (Long) -> Unit,
    )
    {
        viewModelScope.launch()
        {
            _checkoutState.value = CheckoutState.Processing
            try
            {
                val items = cartItems.value
                val subtotal = items.sumOf { it.price * it.quantity }
                val summary = CheckoutEngine.calculateSummary(subtotal, userRole.value)

                val orderId = orderRepository.placeOrder(
                    userId = userId,
                    vendorId = items.first().vendorId,
                    cartItems = items,
                    totalAmount = summary.total.toDouble(),
                    paymentMethod = paymentMethod,
                    pickupTime = pickupTime,
                    specialRequests = specialRequests,
                )
                _checkoutState.value = CheckoutState.Success(orderId)
                onSuccess(orderId)
            }
            catch (e: Exception)
            {
                _checkoutState.value = CheckoutState.Error(e.message ?: "Failed to place order")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderPlaced: (Long) -> Unit,
    viewModel: CheckoutViewModel,
)
{
    val cartItems by viewModel.cartItems.collectAsState()
    val role by viewModel.userRole.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()
    val locale = LocalConfiguration.current.locales[0]

    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CAMPUS_WALLET) }
    var selectedPickupTime by remember { mutableStateOf("As soon as possible") }
    var specialRequests by remember { mutableStateOf("") }

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val sum = CheckoutEngine.calculateSummary(subtotal, role)

    Scaffold(
        topBar = {
            HIGTopAppBar(
                title = "Checkout",
                navigationIcon = {
                    IconButton(onClick = onBackClick)
                    {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
            )
            {
                Column(modifier = Modifier.padding(DesignSystem.Spacing.screenPadding))
                {
                    if (checkoutState is CheckoutState.Error)
                    {
                        Text(
                            text = (checkoutState as CheckoutState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }

                    if (checkoutState is CheckoutState.Processing)
                    {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    else
                    {
                        Button(
                            onClick = {
                                viewModel.placeOrder(
                                    selectedPaymentMethod,
                                    selectedPickupTime,
                                    specialRequests.takeIf { it.isNotBlank() },
                                    onOrderPlaced,
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CampusOrange,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                        {
                            Text(
                                text = "Place order, R${
                                    String.format(
                                        locale,
                                        "%.2f",
                                        sum.total.toDouble(),
                                    )
                                }",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        },
    )
    { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(DesignSystem.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.large),
        )
        {
            // Payment Method Section
            Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
            {
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                PaymentMethodCard(
                    method = PaymentMethod.CAMPUS_WALLET,
                    icon = Icons.Rounded.AccountBalanceWallet,
                    selected = selectedPaymentMethod == PaymentMethod.CAMPUS_WALLET,
                )
                { 
                    selectedPaymentMethod = PaymentMethod.CAMPUS_WALLET 
                }
                PaymentMethodCard(
                    method = PaymentMethod.DEBIT_CARD,
                    icon = Icons.Rounded.CreditCard,
                    selected = selectedPaymentMethod == PaymentMethod.DEBIT_CARD,
                )
                { 
                    selectedPaymentMethod = PaymentMethod.DEBIT_CARD 
                }
                PaymentMethodCard(
                    method = PaymentMethod.COUPON,
                    icon = Icons.Rounded.ConfirmationNumber,
                    selected = selectedPaymentMethod == PaymentMethod.COUPON,
                )
                { 
                    selectedPaymentMethod = PaymentMethod.COUPON 
                }
            }

            // Pickup Details
            Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium))
            {
                Text(
                    text = "Pickup Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                OutlinedTextField(
                    value = selectedPickupTime,
                    onValueChange = { selectedPickupTime = it },
                    label = { Text("Pickup Time") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )
                OutlinedTextField(
                    value = specialRequests,
                    onValueChange = { specialRequests = it },
                    label = { Text("Special Requests (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = MaterialTheme.shapes.medium,
                )
            }

            // Order Summary
            Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small))
            {
                Text(
                    text = "Order Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                CalculationRow("Subtotal", sum.subtotal, locale)
                CalculationRow("Fees & Taxes", sum.tax + sum.serviceFee, locale)
                if (sum.studentDiscount > BigDecimal.ZERO)
                {
                    CalculationRow("Student Discount", sum.studentDiscount.negate(), locale)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                )
                {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "R${String.format(locale, "%.2f", sum.total.toDouble())}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.large))
        }
    }
}

@Composable
fun PaymentMethodCard(
    method: PaymentMethod,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
)
{
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable()
            { 
                onClick() 
            },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.primary,
        ) else null,
    )
    {
        Row(
            modifier = Modifier.padding(DesignSystem.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        )
        {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.medium))
            val locale = LocalConfiguration.current.locales[0]
            Text(
                text = method.name.replace("_", " ").lowercase()
                    .replaceFirstChar() 
                    { 
                        if (it.isLowerCase()) it.titlecase(locale) else it.toString() 
                    },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (selected)
            {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
