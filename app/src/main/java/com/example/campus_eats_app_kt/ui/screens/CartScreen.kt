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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.util.CheckoutEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(
    private val repository: CartRepository,
    private val authRepository: AuthRepository,
    val userId: String
) : ViewModel() {
    val cartItems: StateFlow<List<CartItemEntity>> = repository.getCart(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userRole: StateFlow<UserRole> = authRepository.getUserFlow(userId)
        .map { it?.role ?: UserRole.STANDARD }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserRole.STANDARD)

    fun addItem(item: CartItemEntity) {
        viewModelScope.launch {
            repository.incrementCartItem(item)
        }
    }

    fun removeItem(item: CartItemEntity) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    viewModel: CartViewModel
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val role by viewModel.userRole.collectAsState()

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val summary = CheckoutEngine.calculateSummary(subtotal, role)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Cart") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        CalculationRow("Subtotal", summary.subtotal)
                        CalculationRow("Tax (20%)", summary.tax)
                        CalculationRow("Service Fee", summary.serviceFee)
                        if (summary.studentDiscount > 0)
                        {
                            CalculationRow(
                                "Student Discount (2.5%)",
                                -summary.studentDiscount,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        val rounding =
                            summary.total - (summary.subtotal + summary.tax + summary.serviceFee - summary.studentDiscount)
                        if (rounding > 0.01)
                        {
                            CalculationRow("Rounding Adjustment", rounding)
                        }

                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "Total",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "R${String.format("%.2f", summary.total)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onCheckoutClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(
                                "Proceed to Checkout",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(Modifier
                .fillMaxSize()
                .padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.RemoveShoppingCart,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Your cart is empty.", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onBackClick) { Text("Browse Items") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "R${String.format("%.2f", item.price)} each",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Subtotal: R${
                                        String.format(
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
                                            "${item.quantity}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        IconButton(onClick = { viewModel.addItem(item) }) {
                                            Icon(Icons.Rounded.Add, "Increase")
                                        }
                                    }
                                }

                                Spacer(Modifier.width(12.dp))
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
            }
        }
    }
}

@Composable
fun CalculationRow(
    label: String,
    amount: Double,
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
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            if (amount >= 0) "R${String.format("%.2f", amount)}"
            else "-R${
                String.format(
                    "%.2f",
                    -amount
                )
            }",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
