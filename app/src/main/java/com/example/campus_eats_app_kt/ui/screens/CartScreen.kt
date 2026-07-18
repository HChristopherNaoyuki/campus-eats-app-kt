package com.example.campus_eats_app_kt.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RemoveShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.ui.components.HIGTopAppBar
import com.example.campus_eats_app_kt.ui.theme.DesignSystem
import com.example.campus_eats_app_kt.util.CheckoutEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * CartViewModel manages the state of the user's shopping cart.
 * It provides reactive streams of cart items and user role for dynamic price calculations.
 */
class CartViewModel(
    private val repository: CartRepository,
    private val authRepository: AuthRepository,
    val userId: String
) : ViewModel()
{
    val cartItems: StateFlow<List<CartItemEntity>> = repository.getCart(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userRole: StateFlow<UserRole> = authRepository.getUserFlow(userId)
        .map { it?.role ?: UserRole.STANDARD }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserRole.STANDARD)

    fun addItem(item: CartItemEntity)
    {
        viewModelScope.launch {
            repository.incrementCartItem(item)
        }
    }

    fun removeItem(item: CartItemEntity)
    {
        viewModelScope.launch {
            if (item.quantity > 1)
            {
                repository.removeFromCart(item)
            }
            else
            {
                repository.deleteCartItem(item)
            }
        }
    }

    fun deleteItem(item: CartItemEntity)
    {
        viewModelScope.launch {
            repository.deleteCartItem(item)
        }
    }

    fun clearCart()
    {
        viewModelScope.launch {
            repository.clearCart(userId)
        }
    }
}

/**
 * CartScreen displays the user's selected items and a summary of the financial calculations
 * including taxes, fees, and discounts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    viewModel: CartViewModel
)
{
    val cartItems by viewModel.cartItems.collectAsState()
    val role by viewModel.userRole.collectAsState()
    val locale = LocalConfiguration.current.locales[0]

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val summary = CheckoutEngine.calculateSummary(subtotal, role)

    Scaffold(
        topBar = {
            HIGTopAppBar(
                title = "Your Cart",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty())
                    {
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("Clear Cart", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty())
            {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(DesignSystem.Spacing.screenPadding)) {
                        CalculationRow("Subtotal", summary.subtotal, locale)
                        CalculationRow("Tax (20%)", summary.tax, locale)
                        CalculationRow("Service Fee", summary.serviceFee, locale)

                        if (summary.studentDiscount > 0)
                        {
                            CalculationRow(
                                label = "Student Discount (2.5%)",
                                amount = -summary.studentDiscount,
                                locale = locale,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Rounding logic for cash transactions compliance
                        val rounding =
                            summary.total - (summary.subtotal + summary.tax + summary.serviceFee - summary.studentDiscount)
                        if (kotlin.math.abs(rounding) > 0.001)
                        {
                            CalculationRow("Rounding Adjustment", rounding, locale)
                        }

                        HorizontalDivider(Modifier.padding(vertical = DesignSystem.Spacing.medium))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "R${String.format(locale, "%.2f", summary.total)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.large))
                        
                        Button(
                            onClick = onCheckoutClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(
                                text = "Proceed to Checkout",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (cartItems.isEmpty())
        {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.RemoveShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(DesignSystem.Spacing.medium))
                    Text("Your cart is empty.", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(DesignSystem.Spacing.large))
                    Button(onClick = onBackClick) { Text("Browse Items") }
                }
            }
        }
        else
        {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(DesignSystem.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.itemSpacing)
            ) {
                items(cartItems) { item ->
                    CartItemCard(item = item, viewModel = viewModel, locale = locale)
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: CartItemEntity, viewModel: CartViewModel, locale: Locale)
{
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(DesignSystem.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "R${String.format(locale, "%.2f", item.price)} each",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Subtotal: R${
                        String.format(
                            locale,
                            "%.2f",
                            item.price * item.quantity
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.removeItem(item) }) {
                            Icon(Icons.Rounded.Remove, "Decrease")
                        }
                        Text(
                            text = "${item.quantity}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.small)
                        )
                        IconButton(onClick = { viewModel.addItem(item) }) {
                            Icon(Icons.Rounded.Add, "Increase")
                        }
                    }
                }

                Spacer(Modifier.width(DesignSystem.Spacing.itemSpacing))

                IconButton(
                    onClick = { viewModel.deleteItem(item) },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Rounded.Delete, "Remove")
                }
            }
        }
    }
}

@Composable
fun CalculationRow(
    label: String,
    amount: Double,
    locale: Locale,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = if (amount >= 0) "R${String.format(locale, "%.2f", amount)}"
            else "-R${String.format(locale, "%.2f", -amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
