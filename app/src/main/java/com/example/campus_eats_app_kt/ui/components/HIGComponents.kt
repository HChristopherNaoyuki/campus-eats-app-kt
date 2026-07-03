package com.example.campus_eats_app_kt.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campus_eats_app_kt.ui.theme.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HIGTopAppBar(
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
)
{
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = DesignSystem.Typography.titleSize
                )
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun HIGButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true
)
{
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
fun HIGAlert(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null
)
{
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismissRequest()
            }) {
                Text(confirmButtonText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = dismissButtonText?.let {
            {
                TextButton(onClick = {
                    onDismiss?.invoke()
                    onDismissRequest()
                }) {
                    Text(it)
                }
            }
        },
        shape = RoundedCornerShape(DesignSystem.CornerRadius.large)
    )
}
