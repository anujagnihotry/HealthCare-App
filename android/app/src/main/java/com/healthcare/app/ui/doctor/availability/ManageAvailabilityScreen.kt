package com.healthcare.app.ui.doctor.availability

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.material3.ExperimentalMaterial3Api
import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.DoctorRepository
import com.healthcare.app.ui.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AvailabilityState(
    val isLoading: Boolean = true,
    val availabilities: List<AvailabilityDto> = emptyList(),
    val locations: List<LocationDto> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class ManageAvailabilityViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AvailabilityState())
    val state: StateFlow<AvailabilityState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val avResult = doctorRepository.getMyAvailabilities()
            val locResult = doctorRepository.getMyLocations()
            _state.value = _state.value.copy(
                isLoading = false,
                availabilities = (avResult as? Resource.Success)?.data ?: emptyList(),
                locations = (locResult as? Resource.Success)?.data ?: emptyList(),
            )
        }
    }

    fun addAvailability(
        locationId: String, dayOfWeek: Int, sessionName: String,
        startTime: String, endTime: String, slotDuration: Int,
    ) {
        viewModelScope.launch {
            doctorRepository.createAvailability(
                CreateAvailabilityRequest(
                    locationId = locationId,
                    dayOfWeek = dayOfWeek,
                    sessionName = sessionName,
                    startTime = startTime,
                    endTime = endTime,
                    slotDurationMinutes = slotDuration,
                )
            )
            load()
        }
    }
}

private val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

@Composable
fun ManageAvailabilityScreen(
    onBack: () -> Unit,
    viewModel: ManageAvailabilityViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        if (state.locations.isEmpty()) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("No Location Added") },
                text = { Text("Please add a clinic or practice location first before adding availability sessions.") },
                confirmButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("OK") }
                },
            )
        } else {
            AddAvailabilityDialog(
                locations = state.locations,
                onDismiss = { showAddDialog = false },
                onAdd = { locId, day, session, start, end, dur ->
                    viewModel.addAvailability(locId, day, session, start, end, dur)
                    showAddDialog = false
                },
            )
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Availability Schedule", onBack = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Session") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingScreen()
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.availabilities.isEmpty()) {
                item {
                    Text(
                        "No availability sessions configured yet.\nAdd your schedule to start receiving appointments.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            // Group by day
            val grouped = state.availabilities.groupBy { it.dayOfWeek }
            grouped.toSortedMap().forEach { (day, sessions) ->
                item {
                    Text(
                        dayNames.getOrElse(day) { "Day $day" },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(sessions) { av ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${av.sessionName} (${av.startTime} - ${av.endTime})",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    "${av.location?.name ?: "Unknown"} | ${av.slotDurationMinutes} min slots",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (av.breakStart != null && av.breakEnd != null) {
                                    Text(
                                        "Break: ${av.breakStart} - ${av.breakEnd}",
                                        style = MaterialTheme.typography.bodyMedium,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAvailabilityDialog(
    locations: List<LocationDto>,
    onDismiss: () -> Unit,
    onAdd: (locId: String, day: Int, session: String, start: String, end: String, dur: Int) -> Unit,
) {
    var selectedLoc by remember { mutableStateOf(locations.first()) }
    var selectedDay by remember { mutableIntStateOf(1) }
    var sessionName by remember { mutableStateOf("Morning") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("12:00") }
    var slotDuration by remember { mutableStateOf("15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Availability Session") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Location picker
                Text("Location", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    locations.take(3).forEach { loc ->
                        FilterChip(
                            selected = selectedLoc.id == loc.id,
                            onClick = { selectedLoc = loc },
                            label = { Text(loc.name, style = MaterialTheme.typography.bodyMedium) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Day picker
                Text("Day of Week", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    dayNames.forEachIndexed { idx, name ->
                        FilterChip(
                            selected = selectedDay == idx,
                            onClick = { selectedDay = idx },
                            label = { Text(name, style = MaterialTheme.typography.labelMedium) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = sessionName, onValueChange = { sessionName = it },
                    label = { Text("Session Name") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime, onValueChange = { startTime = it },
                        label = { Text("Start (HH:mm)") },
                        modifier = Modifier.weight(1f), singleLine = true,
                    )
                    OutlinedTextField(
                        value = endTime, onValueChange = { endTime = it },
                        label = { Text("End (HH:mm)") },
                        modifier = Modifier.weight(1f), singleLine = true,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = slotDuration, onValueChange = { slotDuration = it.filter { c -> c.isDigit() } },
                    label = { Text("Slot Duration (minutes)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                )
            }
        },
        confirmButton = {
            LargeButton(
                text = "Add",
                onClick = {
                    onAdd(
                        selectedLoc.id, selectedDay, sessionName,
                        startTime, endTime, slotDuration.toIntOrNull() ?: 15,
                    )
                },
                enabled = sessionName.isNotBlank(),
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
