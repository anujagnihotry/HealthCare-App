package com.healthcare.app.ui.doctor.patients

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.app.data.dto.CreateConsultationRequest
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.DoctorCareRepository
import com.healthcare.app.ui.common.AppTopBar
import com.healthcare.app.ui.common.LargeButton
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AttachmentDraft(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val type: String,
)

data class DoctorAddConsultationState(
    val symptoms: String = "",
    val diagnosis: String = "",
    val illness: String = "",
    val medications: String = "",
    val notes: String = "",
    val bp: String = "",
    val sugar: String = "",
    val height: String = "",
    val weight: String = "",
    val attachments: List<AttachmentDraft> = emptyList(),
    val nextFileType: String = "prescription",
    val isSaving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

@HiltViewModel
class DoctorAddConsultationViewModel @Inject constructor(
    @ApplicationContext private val app: Context,
    private val doctorCareRepository: DoctorCareRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val familyMemberId: String = savedStateHandle["familyMemberId"] ?: ""
    val patientId: String = savedStateHandle["patientId"] ?: ""
    val appointmentId: String? =
        savedStateHandle.get<String>("appointmentId")?.let { raw ->
            if (raw == "none" || raw.isBlank()) null else raw
        }

    private val _state = MutableStateFlow(DoctorAddConsultationState())
    val state: StateFlow<DoctorAddConsultationState> = _state

    fun updateSymptoms(s: String) {
        _state.value = _state.value.copy(symptoms = s)
    }

    fun updateDiagnosis(s: String) {
        _state.value = _state.value.copy(diagnosis = s)
    }

    fun updateIllness(s: String) {
        _state.value = _state.value.copy(illness = s)
    }

    fun updateMedications(s: String) {
        _state.value = _state.value.copy(medications = s)
    }

    fun updateNotes(s: String) {
        _state.value = _state.value.copy(notes = s)
    }

    fun updateBp(s: String) {
        _state.value = _state.value.copy(bp = s)
    }

    fun updateSugar(s: String) {
        _state.value = _state.value.copy(sugar = s)
    }

    fun updateHeight(s: String) {
        _state.value = _state.value.copy(height = s)
    }

    fun updateWeight(s: String) {
        _state.value = _state.value.copy(weight = s)
    }

    fun setNextFileType(t: String) {
        _state.value = _state.value.copy(nextFileType = t)
    }

    fun addAttachment(uri: Uri) {
        val cr = app.contentResolver
        val mime = cr.getType(uri) ?: "application/octet-stream"
        val name = displayName(cr, uri)
        val bytes = cr.openInputStream(uri)?.use { it.readBytes() } ?: return
        val type = _state.value.nextFileType
        _state.value = _state.value.copy(
            attachments = _state.value.attachments + AttachmentDraft(name, mime, bytes, type),
        )
    }

    fun removeAttachment(index: Int) {
        _state.value = _state.value.copy(
            attachments = _state.value.attachments.filterIndexed { i, _ -> i != index },
        )
    }

    fun submit(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.symptoms.isBlank() || s.diagnosis.isBlank() || s.illness.isBlank() || s.medications.isBlank()) {
            _state.value = s.copy(error = "Symptoms, diagnosis, illness, and medications are required")
            return
        }
        viewModelScope.launch {
            val snapshot = s
            _state.value = snapshot.copy(isSaving = true, error = null)
            val body = CreateConsultationRequest(
                patientId = patientId,
                familyMemberId = familyMemberId,
                appointmentId = appointmentId,
                symptoms = snapshot.symptoms.trim(),
                diagnosis = snapshot.diagnosis.trim(),
                illness = snapshot.illness.trim(),
                medications = snapshot.medications.trim(),
                notes = snapshot.notes.trim().ifBlank { null },
                bp = snapshot.bp.trim().ifBlank { null },
                sugar = snapshot.sugar.trim().ifBlank { null },
                height = snapshot.height.trim().ifBlank { null },
                weight = snapshot.weight.trim().ifBlank { null },
                uploadIds = null,
            )
            when (val created = doctorCareRepository.createConsultation(body)) {
                is Resource.Success -> {
                    val consultationId = created.data.id
                    val today = LocalDate.now().toString()
                    var uploadError: String? = null
                    for (a in snapshot.attachments) {
                        when (
                            val up = doctorCareRepository.uploadFileForConsultation(
                                familyMemberId = familyMemberId,
                                appointmentId = appointmentId,
                                consultationId = consultationId,
                                type = a.type,
                                date = today,
                                fileName = a.fileName,
                                mimeType = a.mimeType,
                                bytes = a.bytes,
                            )
                        ) {
                            is Resource.Success -> Unit
                            is Resource.Error -> {
                                uploadError = up.message
                                break
                            }
                            is Resource.Loading -> Unit
                        }
                    }
                    _state.value = _state.value.copy(isSaving = false, done = true, error = uploadError)
                    if (uploadError == null) onSuccess()
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isSaving = false, error = created.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    @SuppressLint("Range")
    private fun displayName(cr: android.content.ContentResolver, uri: Uri): String {
        cr.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) return c.getString(idx) ?: "file"
            }
        }
        return uri.lastPathSegment ?: "file"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorAddConsultationScreen(
    onBack: () -> Unit,
    viewModel: DoctorAddConsultationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val pickFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { viewModel.addAttachment(it) }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AppTopBar(title = "Add consultation", onBack = onBack)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
        ) {
            OutlinedTextField(
                value = state.symptoms,
                onValueChange = { viewModel.updateSymptoms(it) },
                label = { Text("Symptoms") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.diagnosis,
                onValueChange = { viewModel.updateDiagnosis(it) },
                label = { Text("Diagnosis") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.illness,
                onValueChange = { viewModel.updateIllness(it) },
                label = { Text("Illness description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.medications,
                onValueChange = { viewModel.updateMedications(it) },
                label = { Text("Medications prescribed") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Vitals (optional)", style = MaterialTheme.typography.titleSmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.bp,
                    onValueChange = { viewModel.updateBp(it) },
                    label = { Text("BP") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.sugar,
                    onValueChange = { viewModel.updateSugar(it) },
                    label = { Text("Sugar") },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.height,
                    onValueChange = { viewModel.updateHeight(it) },
                    label = { Text("Height") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.weight,
                    onValueChange = { viewModel.updateWeight(it) },
                    label = { Text("Weight") },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Attachments", style = MaterialTheme.typography.titleSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                FilterChip(
                    selected = state.nextFileType == "prescription",
                    onClick = { viewModel.setNextFileType("prescription") },
                    label = { Text("Next: Prescription") },
                )
                FilterChip(
                    selected = state.nextFileType == "report",
                    onClick = { viewModel.setNextFileType("report") },
                    label = { Text("Next: Report") },
                )
            }
            OutlinedButton(onClick = { pickFile.launch("*/*") }) {
                Icon(Icons.Filled.AttachFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add file")
            }

            state.attachments.forEachIndexed { index, a ->
                ListItem(
                    headlineContent = { Text(a.fileName) },
                    supportingContent = { Text(a.type) },
                    trailingContent = {
                        TextButton(onClick = { viewModel.removeAttachment(index) }) {
                            Text("Remove")
                        }
                    },
                )
            }

            state.error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))
            LargeButton(
                text = if (state.isSaving) "Saving…" else "Save consultation",
                onClick = {
                    viewModel.submit(onSuccess = onBack)
                },
                enabled = !state.isSaving,
            )
        }
    }
}
