package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.AdminRepository
import com.example.campus_eats_app_kt.data.entity.UserEntity
import com.example.campus_eats_app_kt.data.entity.UserStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class AdminDashboardViewModel(private val adminRepository: AdminRepository) : ViewModel() {
    val users: StateFlow<List<UserEntity>> = adminRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleStatus(user: UserEntity) {
        viewModelScope.launch {
            if (user.status == UserStatus.ACTIVE) {
                adminRepository.suspendUser(user.userId)
            } else {
                adminRepository.activateUser(user.userId)
            }
        }
    }

    fun addCredits(userId: String, amount: Double) {
        viewModelScope.launch {
            adminRepository.issueCredits(userId, amount)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    viewModel: AdminDashboardViewModel
) {
    val users by viewModel.users.collectAsState()
    var showCreditDialog by remember { mutableStateOf<UserEntity?>(null) }

    if (showCreditDialog != null) {
        var amount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreditDialog = null },
            title = { Text("Issue Credits") },
            text = {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (R)") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addCredits(showCreditDialog!!.userId, amount.toDoubleOrNull() ?: 0.0)
                    showCreditDialog = null
                }) { Text("Add") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Console") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("System Users", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            items(users) { user ->
                UserControlCard(
                    user = user,
                    onToggleStatus = { viewModel.toggleStatus(user) },
                    onIssueCredits = { showCreditDialog = user }
                )
            }
        }
    }
}

@Composable
fun UserControlCard(
    user: UserEntity,
    onToggleStatus: () -> Unit,
    onIssueCredits: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(user.fullName, fontWeight = FontWeight.Bold)
                    Text(user.role.name, style = MaterialTheme.typography.labelSmall)
                }
                Text("Balance: R${String.format(Locale.getDefault(), "%.2f", user.walletBalance)}", color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onToggleStatus,
                    colors = if (user.status == UserStatus.ACTIVE) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(if (user.status == UserStatus.ACTIVE) Icons.Rounded.Block else Icons.Rounded.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (user.status == UserStatus.ACTIVE) "Suspend" else "Activate")
                }
                OutlinedButton(onClick = onIssueCredits) {
                    Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Credits")
                }
            }
        }
    }
}
