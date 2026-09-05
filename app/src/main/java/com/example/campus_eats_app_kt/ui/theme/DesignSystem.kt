package com.example.campus_eats_app_kt.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object DesignSystem
{
    object Spacing
    {
        val extraSmall = 4.dp
        val small = 8.dp
        val medium = 16.dp
        val large = 24.dp
        val extraLarge = 32.dp

        val screenPadding = 20.dp
        val itemSpacing = 12.dp

        // Requirement: 3.7 mm horizontal spacing from left/right edges.
        // Formula: mm * (160 / 25.4) = dp.
        // 3.7 * 6.2992 = 23.307 dp (rounded to 23.3 dp).
        val tabBarHorizontalMargin = 23.3.dp
    }

    object Typography
    {
        val titleSize = 20.sp
    }

    object CornerRadius
    {
        val small = 10.dp
        val medium = 14.dp
        val large = 20.dp
        val extraLarge = 28.dp
    }
}
