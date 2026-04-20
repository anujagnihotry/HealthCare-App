@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)

package com.healthcare.app.ui.patient.family

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.app.data.dto.CreateFamilyMemberRequest
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.PatientRepository
import com.healthcare.app.ui.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFamilyMemberViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun addMember(name: String, age: Int, gender: String, bloodGroup: String?, allergies: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val allergyList = if (allergies.isBlank()) emptyList()
            else allergies.split(",").map { it.trim() }

            val result = patientRepository.createFamilyMember(
                CreateFamilyMemberRequest(
                    name = name,
                    age = age,
                    gender = gender,
                    bloodGroup = bloodGroup?.takeIf { it.isNotBlank() },
                    allergies = allergyList,
                )
            )
            _isLoading.value = false
            when (result) {
                is Resource.Success -> _success.value = true
                is Resource.Error -> _error.value = result.message
                is Resource.Loading -> {}
            }
        }
    }
}

@Composable
fun AddFamilyMemberScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddFamilyMemberViewModel = hiltViewModel(),
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("male") }
    var bloodGroup by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }

    LaunchedEffect(success) { if (success) onSuccess() }

    val genders = listOf("male", "female", "other")
    val bloodGroups = listOf("", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Scaffold(
        topBar = { AppTopBar(title = "Add Family Member", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { c -> c.isDigit() } },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Gender", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                genders.forEach { g ->
                    FilterChip(
                        selected = selectedGender == g,
                        onClick = { selectedGender = g },
                        label = {
                            Text(
                                g.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Blood Group (optional)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                bloodGroups.drop(1).take(4).forEach { bg ->
                    FilterChip(
                        selected = bloodGroup == bg,
                        onClick = { bloodGroup = if (bloodGroup == bg) "" else bg },
                        label = { Text(bg) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                bloodGroups.drop(5).forEach { bg ->
                    FilterChip(
                        selected = bloodGroup == bg,
                        onClick = { bloodGroup = if (bloodGroup == bg) "" else bg },
                        label = { Text(bg) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = allergies,
                onValueChange = { allergies = it },
                label = { Text("Allergies (comma separated, optional)") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            LargeButton(
                text = if (isLoading) "Adding..." else "Add Family Member",
                onClick = {
                    viewModel.addMember(
                        name, age.toIntOrNull() ?: 0, selectedGender, bloodGroup, allergies,
                    )
                },
                enabled = !isLoading && name.isNotBlank() && age.isNotBlank(),
            )
        }
    }
}
