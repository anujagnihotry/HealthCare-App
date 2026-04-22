package com.healthcare.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import com.healthcare.app.ui.auth.branding.SplashBrandedBackground
import com.healthcare.app.ui.common.AppTopBar
import com.healthcare.app.ui.common.LargeButton

private val SPECIALIZATIONS = listOf(
    "General Physician",
    "Cardiologist",
    "Dermatologist",
    "Orthopedic Surgeon",
    "Neurologist",
    "Pediatrician",
    "Gynecologist",
    "Ophthalmologist",
    "ENT Specialist",
    "Psychiatrist",
    "Dentist",
    "Radiologist",
    "Oncologist",
    "Urologist",
    "Endocrinologist",
    "Pulmonologist",
    "Gastroenterologist",
    "Nephrologist",
    "Rheumatologist",
    "Other",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (role: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isDoctor by remember { mutableStateOf(false) }
    var specialization by remember { mutableStateOf("") }
    var specializationExpanded by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color.White.copy(alpha = 0.8f),
        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
        focusedBorderColor = Color.White,
        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
        focusedLeadingIconColor = Color.White,
        unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
        focusedTrailingIconColor = Color.White,
        unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
        cursorColor = Color.White,
    )

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess && state.userRole != null) {
            onRegisterSuccess(state.userRole!!)
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Create Account", onBack = onNavigateBack) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            SplashBrandedBackground(Modifier.fillMaxSize())
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "I am a:",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    FilterChip(
                        selected = !isDoctor,
                        onClick = { isDoctor = false },
                        label = { Text("Patient", style = MaterialTheme.typography.bodyLarge) },
                        modifier = Modifier.weight(1f).height(50.dp),
                    )
                    FilterChip(
                        selected = isDoctor,
                        onClick = { isDoctor = true },
                        label = { Text("Doctor", style = MaterialTheme.typography.bodyLarge) },
                        modifier = Modifier.weight(1f).height(50.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    colors = textFieldColors,
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    colors = textFieldColors,
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    colors = textFieldColors,
                )

                if (isDoctor) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ExposedDropdownMenuBox(
                        expanded = specializationExpanded,
                        onExpandedChange = { specializationExpanded = !specializationExpanded },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = specialization,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Specialization") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            colors = textFieldColors,
                        )
                        ExposedDropdownMenu(
                            expanded = specializationExpanded,
                            onDismissRequest = { specializationExpanded = false },
                        ) {
                            SPECIALIZATIONS.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        specialization = option
                                        specializationExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                LargeButton(
                    text = if (state.isLoading) "Creating account..." else "Register",
                    onClick = {
                        viewModel.register(
                            email = email,
                            password = password,
                            name = name,
                            role = if (isDoctor) "doctor" else "patient",
                            specialization = if (isDoctor) specialization else null,
                        )
                    },
                    enabled = !state.isLoading && name.isNotBlank() &&
                        email.isNotBlank() && password.length >= 6 &&
                        (!isDoctor || specialization.isNotBlank()),
                )
            }
        }
    }
}
