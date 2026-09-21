package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import com.example.campus_eats_app_kt.ui.components.HIGCard
import com.example.campus_eats_app_kt.ui.components.HIGTopAppBar
import com.example.campus_eats_app_kt.ui.theme.DesignSystem

/**
 * CustomerMenuBrowseScreen facilitates vendor-specific discovery. It uses a grid layout
 * to maximize visual density while maintaining generous whitespace and clear touch targets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMenuBrowseScreen(
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: MenuBrowseViewModel,
)
{
    val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var pendingItem by remember { mutableStateOf<MenuItemEntity?>(null) }

    if (error != null)
    {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Conflict in Cart") },
            text = { Text(error!!) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingItem?.let { viewModel.clearCartAndAdd(it) }
                        viewModel.clearError()
                    },
                )
                {
                    Text("Clear and Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearError() })
                {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            HIGTopAppBar(
                title = "Menu Options",
                navigationIcon = {
                    IconButton(onClick = onBackClick)
                    {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick)
                    {
                        Icon(
                            Icons.Rounded.ShoppingCart,
                            contentDescription = "View Cart",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    )
    { innerPadding ->
        if (menuItems.isEmpty())
        {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            )
            {
                Text(
                    text = "This vendor has no active listings.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
        else
        {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 165.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(DesignSystem.Spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
            )
            {
                items(menuItems)
                { item ->
                    MenuItemGridCard(item = item)
                    {
                        pendingItem = item
                        viewModel.addToCart(item)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemGridCard(item: MenuItemEntity, onAddToCart: () -> Unit)
{
    HIGCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
    )
    {
        Column()
        {
            // Visual identification with bleed-to-edge layout
            AsyncImage(
                model = item.imageUrl ?: "https://via.placeholder.com/300?text=${item.name}",
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentScale = ContentScale.Crop,
            )

            Column(modifier = Modifier.padding(DesignSystem.Spacing.medium))
            {
                val locale = LocalConfiguration.current.locales[0]
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = "R${String.format(locale, "%.2f", item.price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                )

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))

                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(0.dp),
                )
                {
                    Icon(
                        imageVector = Icons.Rounded.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.extraSmall))
                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
