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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.campus_eats_app_kt.data.CartRepository
import com.example.campus_eats_app_kt.data.entity.CartItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(
    private val repository: CartRepository,
    val userId: String
) : ViewModel() {
    val cartItems: StateFlow<List<CartItemEntity>> = repository.getCart(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addItem(item: CartItemEntity) {
        viewModelScope.launch {
            repository.incrementCartItem(item)
        }
    }

    fun removeItem(item: CartItemEntity) {
        viewModelScope.launch {
            repository.removeFromCart(item)
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
    val totalAmount = cartItems.sumOf { it.price * it.quantity }

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
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", style = MaterialTheme.typography.titleLarge)
                            Text("R${String.format("%.2f", totalAmount)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCheckoutClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text("Proceed to Checkout")
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
                Text("Your cart is empty.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
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
                                IconButton(onClick = { viewModel.removeItem(item) }) {
                                    Icon(Icons.Rounded.Remove, contentDescription = "Decrease")
                                }
                                Text("${item.quantity}", style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { viewModel.addItem(item) }) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Increase")
                                }
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { viewModel.deleteItem(item) }) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error
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
