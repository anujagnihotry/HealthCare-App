package com.healthcare.app.ui.doctor.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.app.data.dto.DoctorDto
import com.healthcare.app.data.dto.LocationDto
import com.healthcare.app.data.dto.UpdateDoctorProfileRequest
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.AuthRepository
import com.healthcare.app.domain.repository.DoctorRepository
import com.healthcare.app.ui.common.AppTopBar
import com.healthcare.app.ui.common.LargeButton
import com.healthcare.app.ui.common.LoadingScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private data class MedicalSystemOption(val apiValue: String, val label: String)

private val medicalSystemOptions = listOf(
    MedicalSystemOption("homeopathy", "Homeopathy"),
    MedicalSystemOption("allopathy", "Allopathy"),
    MedicalSystemOption("ayush", "Ayush (Ayurvedic)"),
)

private fun formatMedicalSystem(api: String?): String? =
    medicalSystemOptions.find { it.apiValue == api }?.label

private fun buildProfileSummaryLine(doctor: DoctorDto): String? {
    val parts = mutableListOf<String>()
    doctor.yearsOfExperience?.let { if (it >= 0) parts += "$it yrs experience" }
    doctor.degree?.trim()?.takeIf { it.isNotEmpty() }?.let { parts += it }
    formatMedicalSystem(doctor.medicalSystem)?.let { parts += it }
    return if (parts.isEmpty()) null else parts.joinToString(" · ")
}

data class DoctorDashboardState(
    val isLoading: Boolean = true,
    val doctor: DoctorDto? = null,
    val locations: List<LocationDto> = emptyList(),
    val error: String? = null,
    val profileMessage: String? = null,
)

@HiltViewModel
class DoctorDashboardViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DoctorDashboardState())
    val state: StateFlow<DoctorDashboardState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, profileMessage = null)
            val profileResult = doctorRepository.getMyProfile()
            val locationsResult = doctorRepository.getMyLocations()

            _state.value = _state.value.copy(
                isLoading = false,
                doctor = (profileResult as? Resource.Success)?.data,
                locations = (locationsResult as? Resource.Success)?.data ?: emptyList(),
                error = (profileResult as? Resource.Error)?.message,
            )
        }
    }

    fun saveProfile(request: UpdateDoctorProfileRequest) {
        viewModelScope.launch {
            when (val result = doctorRepository.updateProfile(request)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(profileMessage = "Profile updated")
                    load()
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(profileMessage = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearProfileMessage() {
        _state.value = _state.value.copy(profileMessage = null)
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(
    onManageLocations: () -> Unit,
    onManageAvailability: () -> Unit,
    onTokenQueue: (locationId: String, date: String) -> Unit,
    onPatientSearch: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DoctorDashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val today = remember { LocalDate.now().toString() }
    var showProfileDialog by remember { mutableStateOf(false) }

    if (showProfileDialog && state.doctor != null) {
        key(state.doctor!!.id) {
            DoctorProfileEditDialog(
                doctor = state.doctor!!,
                onDismiss = {
                    showProfileDialog = false
                    viewModel.clearProfileMessage()
                },
                onSave = { req ->
                    viewModel.saveProfile(req)
                    showProfileDialog = false
                },
            )
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Doctor Panel",
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingScreen()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.doctor?.let { doctor ->
                Text(
                    "Welcome, ${doctor.name}",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    doctor.specialization,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                buildProfileSummaryLine(doctor)?.let { line ->
                    Text(
                        line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(
                    onClick = { showProfileDialog = true },
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit profile")
                }
            }

            state.profileMessage?.let { msg ->
                Text(
                    msg,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Management", style = MaterialTheme.typography.titleLarge)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionCard(
                    title = "Locations",
                    icon = Icons.Default.LocationOn,
                    onClick = onManageLocations,
                    modifier = Modifier.weight(1f),
                )
                ActionCard(
                    title = "Schedule",
                    icon = Icons.Default.Schedule,
                    onClick = onManageAvailability,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionCard(
                    title = "My patients",
                    icon = Icons.Default.Search,
                    onClick = onPatientSearch,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Today's Queues", style = MaterialTheme.typography.titleLarge)

            if (state.locations.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Text(
                        "No locations added yet.\nAdd a clinic location first.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            } else {
                state.locations.filter { it.isActive }.forEach { location ->
                    Card(
                        onClick = { onTokenQueue(location.id, today) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    location.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    location.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Open queue",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoctorProfileEditDialog(
    doctor: DoctorDto,
    onDismiss: () -> Unit,
    onSave: (UpdateDoctorProfileRequest) -> Unit,
) {
    var name by remember(doctor.id) { mutableStateOf(doctor.name) }
    var specialization by remember(doctor.id) { mutableStateOf(doctor.specialization) }
    var yearsText by remember(doctor.id) {
        mutableStateOf(doctor.yearsOfExperience?.toString() ?: "")
    }
    var degree by remember(doctor.id) { mutableStateOf(doctor.degree ?: "") }
    var medicalApi by remember(doctor.id) {
        mutableStateOf(
            doctor.medicalSystem?.takeIf { v -> medicalSystemOptions.any { it.apiValue == v } }
                ?: medicalSystemOptions.first().apiValue,
        )
    }

    val scrollState = rememberScrollState()
    var fieldError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text("Edit profile", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = specialization,
                    onValueChange = { specialization = it },
                    label = { Text("Specialization") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = yearsText,
                    onValueChange = { v -> yearsText = v.filter { c -> c.isDigit() } },
                    label = { Text("Total years of experience") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("Whole years only (0–80)") },
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("Degree") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g. MBBS, MD (Cardiology)") },
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("System of medicine", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    medicalSystemOptions.forEach { opt ->
                        FilterChip(
                            selected = medicalApi == opt.apiValue,
                            onClick = { medicalApi = opt.apiValue },
                            label = { Text(opt.label) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                fieldError?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))
                LargeButton(
                    text = "Save",
                    onClick = {
                        fieldError = null
                        if (name.isBlank() || specialization.isBlank()) {
                            fieldError = "Name and specialization are required."
                            return@LargeButton
                        }
                        val yearsParsed = if (yearsText.isBlank()) null else yearsText.toIntOrNull()
                        if (yearsText.isNotBlank() && (yearsParsed == null || yearsParsed !in 0..80)) {
                            fieldError = "Enter a valid number of years (0–80), or leave blank."
                            return@LargeButton
                        }
                        onSave(
                            UpdateDoctorProfileRequest(
                                name = name.trim(),
                                specialization = specialization.trim(),
                                yearsOfExperience = yearsParsed,
                                degree = degree.trim().ifBlank { null },
                                medicalSystem = medicalApi,
                            ),
                        )
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge)
        }
    }
}
