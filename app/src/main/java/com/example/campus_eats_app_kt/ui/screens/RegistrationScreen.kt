package com.example.campus_eats_app_kt.ui.screens

import com.example.campus_eats_app_kt.util.LanguageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.ui.components.HIGButton
import com.example.campus_eats_app_kt.ui.components.HIGSegmentedControl
import com.example.campus_eats_app_kt.ui.components.HIGTopAppBar
import com.example.campus_eats_app_kt.ui.theme.DesignSystem
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * RegistrationScreen facilitates the creation of new platform accounts.
 * Supports all four mandatory roles (Student, Standard, Vendor, Administrator)
 * and integrates with Google SSO for streamlined onboarding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onRegistrationSuccess: (String, String) -> Unit, // userId, role
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegistrationViewModel,
)
{
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var shopName by remember { mutableStateOf("") }

    val registrationState by viewModel.registrationState.collectAsState()

    // Process-driven navigation: triggered only on successful persistence
    LaunchedEffect(registrationState)
    {
        if (registrationState is RegistrationState.Success)
        {
            val user = (registrationState as RegistrationState.Success).user
            onRegistrationSuccess(user.userId, user.role.name)
        }
    }

    Scaffold(
        topBar = {
            HIGTopAppBar(
                title = "Create Account",
                navigationIcon = {
                    IconButton(onClick = onBackClick)
                    {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    )
    { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(DesignSystem.Spacing.screenPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
        )
        {
            Text(
                text = "Join Campus Eats",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "Select your account type to get started.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
            )

            // Requirement: Account types must include Student, Standard, Vendor, Administrator.
            HIGSegmentedControl(
                options = UserRole.entries.toList(),
                selectedOption = selectedRole,
                onOptionSelected = { selectedRole = it },
                labelProvider = { role ->
                    role.name.lowercase().replaceFirstChar() 
                    { char -> 
                        if (char.isLowerCase()) char.titlecase() else char.toString() 
                    }
                },
            )

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))

            // Principle: Direct Manipulation - Clear fields for identity registration
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Legal Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Display Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("University Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
            )

            if (selectedRole == UserRole.VENDOR)
            {
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop or Merchant Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Rounded.Store, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Secure Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
            )

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.large))

            if (registrationState is RegistrationState.Error)
            {
                Text(
                    text = (registrationState as RegistrationState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.small),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }

            // Standard registration trigger
            HIGButton(
                onClick = {
                    if (password == confirmPassword)
                    {
                        viewModel.register(
                            fullName,
                            username,
                            email,
                            password,
                            selectedRole,
                            shopName.takeIf { selectedRole == UserRole.VENDOR },
                        )
                    }
                    else
                    {
                        viewModel.setError("Passwords do not match.")
                    }
                },
                text = "Create account",
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                enabled = registrationState !is RegistrationState.Loading,
            )

            // Requirement: Continue with Google button.
            // Requirement: Handle Google SSO integration for new account creation.
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            
            OutlinedButton(
                onClick = {
                    coroutineScope.launch()
                    {
                        try 
                        {
                            val credentialManager = CredentialManager.create(context)
                            
                            // Implementation fix: Ensure valid project identity is used for the request.
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts = false)
                                .setServerClientId("project-google-sso.apps.googleusercontent.com") 
                                .setAutoSelectEnabled(false)
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val result = credentialManager.getCredential(context, request)
                            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)

                            viewModel.registerWithGoogle(
                                credential.idToken, 
                                selectedRole, 
                                shopName.takeIf { selectedRole == UserRole.VENDOR },
                            ) 
                        } 
                        catch (e: NoCredentialException)
                        {
                            viewModel.setError(LanguageManager.getString("No accounts found.", "Geen rekeninge gevind nie."))
                        }
                        catch (e: GetCredentialException) 
                        {
                            // Output detailed failure message to assist in diagnosis
                            viewModel.setError("Google SSO failed: ${e.message}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                enabled = registrationState !is RegistrationState.Loading,
            ) 
            {
                Row(verticalAlignment = Alignment.CenterVertically) 
                {
                    if (registrationState is RegistrationState.Loading) 
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    } 
                    else 
                    {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.small))
                    Text(
                        text = LanguageManager.getString("Continue with Google", "Gaan voort met Google"),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            if (registrationState is RegistrationState.Loading)
            {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
            }

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.large))
        }
    }
}
