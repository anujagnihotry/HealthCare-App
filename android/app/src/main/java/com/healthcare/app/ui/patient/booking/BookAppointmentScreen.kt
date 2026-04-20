package com.healthcare.app.ui.patient.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.AppointmentRepository
import com.healthcare.app.domain.repository.PatientRepository
import com.healthcare.app.ui.common.*
import com.healthcare.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingUiState(
    val isLoading: Boolean = true,
    val bookingWindow: BookingWindowResponse? = null,
    val familyMembers: List<FamilyMemberDto> = emptyList(),
    val selectedDate: String? = null,
    val selectedSlot: String? = null,
    val selectedMember: FamilyMemberDto? = null,
    val isBooking: Boolean = false,
    val bookingResult: BookingResponse? = null,
    val error: String? = null,
)

@HiltViewModel
class BookAppointmentViewModel @Inject constructor(
    private val appointmentRepo: AppointmentRepository,
    private val patientRepo: PatientRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val doctorId: String = savedStateHandle["doctorId"] ?: ""
    val locationId: String = savedStateHandle["locationId"] ?: ""

    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val windowResult = appointmentRepo.getBookingWindow(doctorId, locationId)
            val membersResult = patientRepo.getFamilyMembers()

            val members = (membersResult as? Resource.Success)?.data ?: emptyList()
            val window = (windowResult as? Resource.Success)?.data

            _state.value = _state.value.copy(
                isLoading = false,
                bookingWindow = window,
                familyMembers = members,
                selectedMember = members.firstOrNull { it.isSelf } ?: members.firstOrNull(),
                error = if (window == null) "No available slots found" else null,
            )
        }
    }

    fun selectDate(date: String) {
        _state.value = _state.value.copy(selectedDate = date, selectedSlot = null)
    }

    fun selectSlot(slot: String) {
        _state.value = _state.value.copy(selectedSlot = slot)
    }

    fun selectMember(member: FamilyMemberDto) {
        _state.value = _state.value.copy(selectedMember = member)
    }

    fun book() {
        val s = _state.value
        if (s.selectedDate == null || s.selectedSlot == null || s.selectedMember == null) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isBooking = true, error = null)
            val result = appointmentRepo.bookAppointment(
                BookAppointmentRequest(
                    doctorId = doctorId,
                    locationId = locationId,
                    familyMemberId = s.selectedMember.id,
                    date = s.selectedDate,
                    timeSlot = s.selectedSlot,
                )
            )
            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isBooking = false, bookingResult = result.data,
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isBooking = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    doctorId: String,
    locationId: String,
    onBookingComplete: () -> Unit,
    onBack: () -> Unit,
    viewModel: BookAppointmentViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showConfirmation by remember { mutableStateOf(false) }

    // Show booking confirmation dialog
    if (state.bookingResult != null) {
        AlertDialog(
            onDismissRequest = onBookingComplete,
            title = { Text("Booking Confirmed!", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Text(
                        "Token Number: ${state.bookingResult!!.tokenNumber}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        state.bookingResult!!.message,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            },
            confirmButton = {
                LargeButton(text = "OK", onClick = onBookingComplete)
            },
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "Book Appointment", onBack = onBack) },
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
        ) {
            // Step 1: Select family member
            Text("Step 1: Who is the appointment for?", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.familyMembers) { member ->
                    FilterChip(
                        selected = state.selectedMember?.id == member.id,
                        onClick = { viewModel.selectMember(member) },
                        label = {
                            Text(
                                if (member.isSelf) "Self (${member.name})" else member.name,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        modifier = Modifier.height(48.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 2: Select date
            val window = state.bookingWindow
            if (window != null && window.availableDates.isNotEmpty()) {
                Text(
                    "Step 2: Select Date",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "Available from ${window.startDate} to ${window.endDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(window.availableDates) { dateObj ->
                        FilterChip(
                            selected = state.selectedDate == dateObj.date,
                            onClick = { viewModel.selectDate(dateObj.date) },
                            label = {
                                Text(dateObj.date, style = MaterialTheme.typography.bodyLarge)
                            },
                            modifier = Modifier.height(48.dp),
                        )
                    }
                }

                // Step 3: Select time slot
                val selectedDateObj = window.availableDates.find { it.date == state.selectedDate }
                if (selectedDateObj != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Step 3: Select Time", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Group by session
                    val grouped = selectedDateObj.slots.groupBy { it.sessionName }
                    grouped.forEach { (session, slots) ->
                        Text(session, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            slots.forEach { slot ->
                                SlotChip(
                                    time = slot.time,
                                    isBooked = slot.isBooked,
                                    isSelected = state.selectedSlot == slot.time,
                                    onClick = { viewModel.selectSlot(slot.time) },
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Text(
                    "No available slots found in the booking window.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Confirm button
            LargeButton(
                text = if (state.isBooking) "Booking..." else "Confirm Booking",
                onClick = { viewModel.book() },
                enabled = !state.isBooking &&
                    state.selectedDate != null &&
                    state.selectedSlot != null &&
                    state.selectedMember != null,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit,
) {
    // Simple flow-row approximation using a layout
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() },
    )
}
