package com.healthcare.app.ui.doctor.patients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.app.data.dto.ConsultationDto
import com.healthcare.app.data.dto.PatientHistoryDto
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.DoctorCareRepository
import com.healthcare.app.ui.common.AppTopBar
import com.healthcare.app.ui.common.LargeButton
import com.healthcare.app.ui.common.LoadingScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DoctorPatientProfileState(
    val isLoading: Boolean = true,
    val history: PatientHistoryDto? = null,
    val error: String? = null,
    val codeDialog: Boolean = false,
    val codeInput: String = "",
    val codeSaving: Boolean = false,
    val codeMessage: String? = null,
)

@HiltViewModel
class DoctorPatientProfileViewModel @Inject constructor(
    private val doctorCareRepository: DoctorCareRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val familyMemberId: String = savedStateHandle["familyMemberId"] ?: ""

    private val _state = MutableStateFlow(DoctorPatientProfileState())
    val state: StateFlow<DoctorPatientProfileState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val res = doctorCareRepository.getPatientHistory(familyMemberId)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isLoading = false, history = res.data)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun openCodeDialog() {
        val current = _state.value.history?.customCode.orEmpty()
        _state.value = _state.value.copy(codeDialog = true, codeInput = current, codeMessage = null)
    }

    fun dismissCodeDialog() {
        _state.value = _state.value.copy(codeDialog = false)
    }

    fun setCodeInput(s: String) {
        _state.value = _state.value.copy(codeInput = s)
    }

    fun saveCustomCode() {
        val code = _state.value.codeInput.trim()
        if (code.length < 2) {
            _state.value = _state.value.copy(codeMessage = "Code must be at least 2 characters")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(codeSaving = true, codeMessage = null)
            when (val res = doctorCareRepository.assignCustomCode(familyMemberId, code)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        codeSaving = false,
                        codeDialog = false,
                        history = _state.value.history?.copy(customCode = res.data.customCode),
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(codeSaving = false, codeMessage = res.message)
                }
                is Resource.Loading -> {}
            }
        }
    }
}

@Composable
fun DoctorPatientProfileScreen(
    memberName: String,
    onBack: () -> Unit,
    onAddConsultation: (familyMemberId: String, patientId: String, appointmentId: String?) -> Unit,
    viewModel: DoctorPatientProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    if (state.codeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCodeDialog() },
            title = { Text("Patient code") },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.codeInput,
                        onValueChange = { viewModel.setCodeInput(it) },
                        label = { Text("Custom code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    state.codeMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveCustomCode() },
                    enabled = !state.codeSaving,
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCodeDialog() }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = memberName,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { viewModel.openCodeDialog() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit code")
                    }
                },
            )
        },
        floatingActionButton = {
            val h = state.history
            if (h != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onAddConsultation(h.familyMember.id, h.patient.id, null)
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add visit") },
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingScreen()
            state.error != null -> {
                Column(Modifier.padding(padding).padding(24.dp)) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    LargeButton(text = "Retry", onClick = { viewModel.load() })
                }
            }
            state.history != null -> {
                ProfileContent(
                    modifier = Modifier.padding(padding),
                    history = state.history!!,
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    modifier: Modifier = Modifier,
    history: PatientHistoryDto,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionTitle("Basic info")
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(history.familyMember.name, style = MaterialTheme.typography.titleMedium)
                    Text("Age ${history.familyMember.age} · ${history.familyMember.gender}")
                    history.familyMember.bloodGroup?.let {
                        Text("Blood group: $it")
                    }
                    Text("Mobile: ${history.patient.phone ?: "—"}")
                    history.customCode?.let { Text("Your code: $it", style = MaterialTheme.typography.labelLarge) }
                    if (history.familyMember.allergies.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Allergies: ${history.familyMember.allergies.joinToString()}",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        item {
            SectionTitle("Vitals (recent)")
            if (history.vitals.isEmpty()) {
                Text("No vitals recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val v = history.vitals.first()
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("BP: ${v.bp ?: "—"}")
                        Text("Sugar: ${v.sugar ?: "—"}")
                        Text("Height: ${v.height ?: "—"}")
                        Text("Weight: ${v.weight ?: "—"}")
                        v.recordedAt?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }

        item { SectionTitle("Consultation history") }

        if (history.consultations.isEmpty() && history.legacyMedicalRecords.isEmpty()) {
            item {
                Text(
                    "No visits recorded yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        lazyItems(history.consultations) { c ->
            ConsultationCard(c)
        }

        if (history.legacyMedicalRecords.isNotEmpty()) {
            item { SectionTitle("Earlier notes (legacy)") }
            lazyItems(history.legacyMedicalRecords) { rec ->
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(rec.diagnosis ?: "Record", style = MaterialTheme.typography.titleSmall)
                        rec.notes?.let { Text(it) }
                        rec.createdAt?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }

        if (history.uploads.isNotEmpty()) {
            item { SectionTitle("Files") }
            lazyItems(history.uploads) { u ->
                Card(shape = RoundedCornerShape(8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(u.fileName, style = MaterialTheme.typography.bodyMedium)
                        Text("${u.type} · ${u.date}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun ConsultationCard(c: ConsultationDto) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            c.createdAt?.let { Text(it, style = MaterialTheme.typography.labelMedium) }
            Text("Symptoms", style = MaterialTheme.typography.labelSmall)
            Text(c.symptoms, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Diagnosis", style = MaterialTheme.typography.labelSmall)
            Text(c.diagnosis, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Illness", style = MaterialTheme.typography.labelSmall)
            Text(c.illness, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Medications", style = MaterialTheme.typography.labelSmall)
            Text(c.medications, style = MaterialTheme.typography.bodyMedium)
            c.notes?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Notes", style = MaterialTheme.typography.labelSmall)
                Text(it)
            }
        }
    }
}
