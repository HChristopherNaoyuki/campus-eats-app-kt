package com.example.campus_eats_app_kt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * FoodItemImage renders a food photograph using Coil AsyncImage with a fallback icon.
 * Ensures high contrast presentation and graceful handling when image URLs are missing.
 */
@Composable
fun FoodItemImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
)
{
    if (!imageUrl.isNullOrBlank())
    {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
            contentScale = ContentScale.Crop,
        )
    }
    else
    {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        )
        {
            Icon(
                imageVector = Icons.Rounded.Restaurant,
                contentDescription = contentDescription ?: "Food Item",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * FoodItemThumbnail renders a compact food item photograph for list rows and cart items.
 */
@Composable
fun FoodItemThumbnail(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
)
{
    FoodItemImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        cornerRadius = 8.dp,
    )
}
