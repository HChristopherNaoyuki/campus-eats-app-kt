package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.ui.components.HIGButton
import com.example.campus_eats_app_kt.ui.components.HIGTopAppBar
import com.example.campus_eats_app_kt.ui.theme.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onRegistrationSuccess: (String, String) -> Unit, // userId, role
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegistrationViewModel
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var expanded by remember { mutableStateOf(false) }
    
    val registrationState by viewModel.registrationState.collectAsState()
    var showIdDialog by remember { mutableStateOf(false) }
    var registeredUserId by remember { mutableStateOf("") }

    LaunchedEffect(registrationState) {
        if (registrationState is RegistrationState.Success) {
            registeredUserId = (registrationState as RegistrationState.Success).user.userId
            showIdDialog = true
        }
    }

    if (showIdDialog) {
        AlertDialog(
            onDismissRequest = { /* Don't dismiss without action */ },
            shape = RoundedCornerShape(DesignSystem.CornerRadius.extraLarge),
            title = {
                Text(
                    "Registration Successful!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Your unique User ID is:",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.medium))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = registeredUserId,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 2.sp
                            ),
                            modifier = Modifier
                                .padding(DesignSystem.Spacing.medium)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.large))
                    val context = androidx.compose.ui.platform.LocalContext.current
                    OutlinedButton(
                        onClick = {
                            val clipboard =
                                context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip =
                                android.content.ClipData.newPlainText("User ID", registeredUserId)
                            clipboard.setPrimaryClip(clip)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(DesignSystem.CornerRadius.medium)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.small))
                        Text("Copy User ID")
                    }
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.medium))
                    Text(
                        "Please save this ID safely. You will need it for account recovery.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                HIGButton(
                    onClick = {
                        showIdDialog = false
                        onRegistrationSuccess(registeredUserId, selectedRole.name)
                    },
                    text = "Return to Login",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    Scaffold(
        topBar = {
            HIGTopAppBar(
                title = "Create Account",
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
                .padding(DesignSystem.Spacing.large)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium)
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium)
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedRole.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("I am a...") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) },
                    shape = RoundedCornerShape(DesignSystem.CornerRadius.medium)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    UserRole.values().forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                selectedRole = role
                                expanded = false
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedRole == UserRole.VENDOR,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Rounded.Store, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(DesignSystem.CornerRadius.medium)
                )
            }
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium)
            )
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.LockReset, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium)
            )

            Spacer(modifier = Modifier.weight(1f))
            
            if (registrationState is RegistrationState.Error) {
                Text(
                    text = (registrationState as RegistrationState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = DesignSystem.Spacing.small),
                    textAlign = TextAlign.Center
                )
            }

            HIGButton(
                onClick = { 
                    if (password == confirmPassword) {
                        viewModel.register(
                            fullName,
                            email,
                            password,
                            selectedRole,
                            if (selectedRole == UserRole.VENDOR) shopName else null
                        )
                    }
                },
                text = "Register",
                modifier = Modifier.fillMaxWidth(),
                enabled = registrationState !is RegistrationState.Loading
            )

            if (registrationState is RegistrationState.Loading)
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
