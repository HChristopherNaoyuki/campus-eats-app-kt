package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.campus_eats_app_kt.ui.components.HIGButton
import com.example.campus_eats_app_kt.ui.components.HIGTopAppBar
import com.example.campus_eats_app_kt.ui.theme.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onResetSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel
) {
    var userId by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    
    val resetState by viewModel.resetState.collectAsState()

    LaunchedEffect(resetState) {
        if (resetState is ResetState.Success) {
            onResetSuccess()
        }
    }

    Scaffold(
        topBar = {
            HIGTopAppBar(
                title = "Reset Password",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(DesignSystem.Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
        ) {
            Text(
                "Enter your unique 16-character User ID and a new password to reset your account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.medium)
            )

            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Key, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
                placeholder = { Text("XXXX-XXXX-XXXX-XXXX") }
            )
            
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium)
            )

            Spacer(modifier = Modifier.weight(1f))
            
            if (resetState is ResetState.Error) {
                Text(
                    text = (resetState as ResetState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.small),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            HIGButton(
                onClick = { viewModel.resetPassword(userId, newPassword) },
                text = "Reset Password",
                modifier = Modifier.fillMaxWidth(),
                enabled = resetState !is ResetState.Loading
            )

            if (resetState is ResetState.Loading)
            {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.large))
        }
    }
}
