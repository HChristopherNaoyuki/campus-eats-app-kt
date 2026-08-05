package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campus_eats_app_kt.ui.components.HIGButton
import com.example.campus_eats_app_kt.ui.theme.CampusEatsAppTheme
import com.example.campus_eats_app_kt.ui.theme.CampusOrange
import com.example.campus_eats_app_kt.ui.theme.DesignSystem

/**
 * LandingScreen serves as the welcome page for the application.
 * It features a full-bleed brand orange background and clear primary actions.
 */
@Composable
fun LandingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier
)
{
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CampusOrange)
            .padding(DesignSystem.Spacing.screenPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // App Icon in white rounded square
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(DesignSystem.CornerRadius.large),
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Fastfood,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = CampusOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.large))
            
            Text(
                text = "Campus Eats",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1.5).sp
                )
            )
            
            Text(
                text = "Order. Track. Pickup.",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1.5f))

            // Stacked Buttons
            HIGButton(
                onClick = onRegisterClick,
                text = "Register",
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                contentColor = CampusOrange
            )

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.medium))

            OutlinedButton(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = Color.White
                )
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Forgot Password?",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.large))
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun LandingScreenPreview()
{
    CampusEatsAppTheme {
        LandingScreen(onLoginClick = {}, onRegisterClick = {}, onForgotPasswordClick = {})
    }
}
