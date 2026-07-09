package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import com.example.campus_eats_app_kt.ui.components.HIGTopAppBar
import com.example.campus_eats_app_kt.ui.theme.DesignSystem

/**
 * VendorMenuManagementScreen provides a control panel for vendors to manage their food items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorMenuManagementScreen(
    onBackClick: () -> Unit,
    onAddItemClick: () -> Unit,
    onEditItemClick: (Long) -> Unit,
    viewModel: VendorMenuViewModel
)
{
    val menuItems by viewModel.menuItems.collectAsState()

    Scaffold(
        topBar = {
            HIGTopAppBar(
                title = "Manage Menu",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddItemClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        if (menuItems.isEmpty())
        {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No items in your menu yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        else
        {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(DesignSystem.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)
            ) {
                items(menuItems) { item ->
                    MenuManagementCard(
                        item = item,
                        onEdit = { onEditItemClick(item.itemId) },
                        onDelete = { viewModel.deleteItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun MenuManagementCard(item: MenuItemEntity, onEdit: () -> Unit, onDelete: () -> Unit)
{
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(DesignSystem.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "R${String.format("%.2f", item.price)} | Stock: ${item.stock}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit")
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
