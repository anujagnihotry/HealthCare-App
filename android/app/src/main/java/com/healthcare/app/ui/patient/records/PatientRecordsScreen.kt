@file:OptIn(ExperimentalMaterial3Api::class)

package com.healthcare.app.ui.patient.records

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.app.data.api.UploadApi
import com.healthcare.app.data.dto.MedicalRecordDto
import com.healthcare.app.data.dto.UploadDto
import com.healthcare.app.ui.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordsState(
    val isLoading: Boolean = true,
    val records: List<MedicalRecordDto> = emptyList(),
    val uploads: List<UploadDto> = emptyList(),
    val error: String? = null,
    val selectedTab: Int = 0, // 0=Visits, 1=Prescriptions, 2=Reports
)

@HiltViewModel
class PatientRecordsViewModel @Inject constructor(
    private val uploadApi: UploadApi,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val familyMemberId: String = savedStateHandle["familyMemberId"] ?: ""
    val memberName: String = Uri.decode(savedStateHandle["memberName"] ?: "")

    private val _state = MutableStateFlow(RecordsState())
    val state: StateFlow<RecordsState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val recordsResp = uploadApi.getMedicalRecords(familyMemberId)
                val uploadsResp = uploadApi.getUploadsByFamilyMember(familyMemberId)

                _state.value = _state.value.copy(
                    isLoading = false,
                    records = if (recordsResp.isSuccessful) recordsResp.body() ?: emptyList() else emptyList(),
                    uploads = if (uploadsResp.isSuccessful) uploadsResp.body() ?: emptyList() else emptyList(),
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun selectTab(tab: Int) {
        _state.value = _state.value.copy(selectedTab = tab)
    }
}

@Composable
fun PatientRecordsScreen(
    familyMemberId: String,
    memberName: String,
    onBack: () -> Unit,
    viewModel: PatientRecordsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val tabs = listOf("Visits", "Prescriptions", "Reports")

    // TabRow uses an internal SubcomposeLayout; Scaffold's *body* slot does too.
    // Nesting them in the body triggers SlotTable crashes (ArrayIndexOutOfBoundsException).
    // Keep tabs in topBar; body is a plain Box only.
    Scaffold(
        topBar = {
            Column(Modifier.fillMaxWidth()) {
                AppTopBar(title = "$memberName's Records", onBack = onBack)
                TabRow(
                    selectedTabIndex = state.selectedTab,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = state.selectedTab == index,
                            onClick = { viewModel.selectTab(index) },
                            text = {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            icon = {
                                Icon(
                                    when (index) {
                                        0 -> Icons.Default.LocalHospital
                                        1 -> Icons.Default.Description
                                        else -> Icons.Default.Assessment
                                    },
                                    contentDescription = title,
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading -> LoadingScreen()
                state.error != null -> ErrorMessage(state.error!!, onRetry = { viewModel.load() })
                else -> when (state.selectedTab) {
                    0 -> VisitsTab(state.records)
                    1 -> UploadsTab(state.uploads.filter { it.type == "prescription" }, "prescription")
                    2 -> UploadsTab(state.uploads.filter { it.type == "report" }, "report")
                }
            }
        }
    }
}

@Composable
private fun VisitsTab(records: List<MedicalRecordDto>) {
    if (records.isEmpty()) {
        EmptyState("No visit records yet", Icons.Default.LocalHospital)
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(records) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocalHospital,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    record.doctor?.name ?: "Doctor",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            Text(
                                record.createdAt?.take(10) ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (!record.diagnosis.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Diagnosis",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(record.diagnosis, style = MaterialTheme.typography.bodyLarge)
                        }

                        if (!record.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Notes",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(record.notes, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadsTab(uploads: List<UploadDto>, type: String) {
    val label = if (type == "prescription") "prescriptions" else "reports"

    if (uploads.isEmpty()) {
        EmptyState(
            "No $label uploaded yet",
            if (type == "prescription") Icons.Default.Description else Icons.Default.Assessment,
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uploads) { upload ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (type == "prescription") Icons.Default.Description else Icons.Default.Assessment,
                            contentDescription = null,
                            tint = if (type == "prescription") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                upload.fileName,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                "Date: ${upload.date}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (upload.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    upload.tags.take(3).forEach { tag ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(8.dp),
                                        ) {
                                            Text(
                                                tag,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
