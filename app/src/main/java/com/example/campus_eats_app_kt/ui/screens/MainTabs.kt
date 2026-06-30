package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campus_eats_app_kt.data.AdminRepository
import com.example.campus_eats_app_kt.data.AuthRepository
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.OrderRepository
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserRole
import kotlinx.coroutines.flow.firstOrNull
import java.util.Locale

@Composable
fun HomeScreenTab(userId: String, authRepository: AuthRepository) {
    var user by remember { mutableStateOf<UserEntity?>(null) }
    
    LaunchedEffect(userId) {
        // In a real app, this would be a Flow from the DB
        user = authRepository.login("", "").getOrNull() // Hack for now
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hello, ${user?.fullName ?: "User"}",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Welcome back to Campus Eats!",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Your User ID", style = MaterialTheme.typography.labelMedium)
                Text(userId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Keep this ID safe for account recovery.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ServicesScreenTab(
    userId: String,
    role: UserRole,
    menuRepository: MenuRepository,
    adminRepository: AdminRepository,
    onNavigateToVendorMenu: (String) -> Unit,
    onNavigateToMenuBrowse: (String, String) -> Unit,
    onNavigateToCart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Services", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        when (role) {
            UserRole.STUDENT, UserRole.STANDARD -> {
                ServiceCard(
                    title = "Browse Vendors",
                    description = "Find the best food on campus.",
                    icon = Icons.Rounded.Restaurant,
                    onClick = { /* This would trigger VendorBrowseScreen logic */ }
                )
                ServiceCard(
                    title = "My Cart",
                    description = "Check your selected items.",
                    icon = Icons.Rounded.ShoppingCart,
                    onClick = onNavigateToCart
                )
            }
            UserRole.VENDOR -> {
                ServiceCard(
                    title = "Manage Menu",
                    description = "Update your items and prices.",
                    icon = Icons.Rounded.MenuBook,
                    onClick = { onNavigateToVendorMenu(userId) }
                )
            }
            UserRole.ADMIN -> {
                ServiceCard(
                    title = "User Management",
                    description = "Moderate users and vendors.",
                    icon = Icons.Rounded.People,
                    onClick = { /* Admin dashboard logic */ }
                )
                ServiceCard(
                    title = "Wallet Control",
                    description = "Issue credits to users.",
                    icon = Icons.Rounded.AccountBalanceWallet,
                    onClick = { /* Admin wallet logic */ }
                )
            }
        }
    }
}

@Composable
fun ActivityScreenTab(userId: String, role: String, orderRepository: OrderRepository) {
    val orders by orderRepository.getOrdersForUser(userId).collectAsState(initial = emptyList())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Activity", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        TabRow(selectedTabIndex = 0) {
            Tab(selected = true, onClick = {}, text = { Text("Receipts") })
            Tab(selected = false, onClick = {}, text = { Text("Reports") })
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (orders.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No recent activity.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orders) { order ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Order #${order.orderId}", fontWeight = FontWeight.Bold)
                                Text("Status: ${order.status.name}")
                            }
                            Text("R${String.format(Locale.getDefault(), "%.2f", order.totalAmount)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreenTab(userId: String, authRepository: AuthRepository, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Update Profile", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("New Email") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {}) { Text("Update Email") }
            }
        }
        
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Rounded.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
        
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Text("App Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun ServiceCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
