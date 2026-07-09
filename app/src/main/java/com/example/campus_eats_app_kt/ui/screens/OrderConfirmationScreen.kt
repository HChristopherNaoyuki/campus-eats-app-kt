package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.campus_eats_app_kt.ui.components.HIGButton
import com.example.campus_eats_app_kt.ui.theme.DesignSystem

/**
 * OrderConfirmationScreen provides positive reinforcement after a successful transaction.
 * It uses a centered layout with clear next steps for the user.
 */
@Composable
fun OrderConfirmationScreen(
    orderId: Long,
    onTrackOrder: () -> Unit,
    onReturnHome: () -> Unit
)
{
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(DesignSystem.Spacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Visual success indicator
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.large))
            
            Text(
                text = "Order Placed!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))
            
            Text(
                text = "Your order #$orderId has been received and is being processed by the vendor.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = DesignSystem.Spacing.large),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Primary path: Monitoring the order
            HIGButton(
                onClick = onTrackOrder,
                text = "Track Order Status",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.medium))

            // Secondary path: Returning to the home dashboard
            OutlinedButton(
                onClick = onReturnHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "Return to Home",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
