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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import com.example.campus_eats_app_kt.ui.components.HIGTopAppBar
import com.example.campus_eats_app_kt.ui.theme.DesignSystem

/**
 * CustomerMenuBrowseScreen allows customers to view food items from a specific vendor.
 * It employs a grid-based layout for optimal item visibility.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMenuBrowseScreen(
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: MenuBrowseViewModel
)
{
    val menuItems by viewModel.menuItems.collectAsState()

    Scaffold(
        topBar = {
            HIGTopAppBar(
                title = "Menu",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Rounded.ShoppingCart, contentDescription = "Cart")
                    }
                }
            )
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
                    text = "No items available in this menu.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        else
        {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(DesignSystem.Spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
            ) {
                items(menuItems) { item ->
                    MenuItemCard(item = item, onAddToCart = { viewModel.addToCart(item) })
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(item: MenuItemEntity, onAddToCart: () -> Unit)
{
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Visual item representation
            AsyncImage(
                model = item.imageUrl ?: "https://via.placeholder.com/300?text=${item.name}",
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(DesignSystem.Spacing.medium)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "R${String.format("%.2f", item.price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))

                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.extraSmall))
                    Text(
                        text = "Add to Cart",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
