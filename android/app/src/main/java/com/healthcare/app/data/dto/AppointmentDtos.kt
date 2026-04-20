package com.healthcare.app.data.dto

data class AppointmentDto(
    val id: String,
    val patientId: String,
    val familyMemberId: String,
    val doctorId: String,
    val locationId: String,
    val date: String,
    val timeSlot: String,
    val status: String,
    val doctor: DoctorDto?,
    val location: LocationDto?,
    val familyMember: FamilyMemberDto?,
    val patient: PatientDto?,
    val token: TokenDto?,
)

data class BookAppointmentRequest(
    val doctorId: String,
    val locationId: String,
    val familyMemberId: String,
    val date: String,
    val timeSlot: String,
)

data class BookingResponse(
    val appointment: AppointmentDto,
    val tokenNumber: Int,
    val message: String,
)

data class SlotDto(
    val time: String,
    val sessionName: String,
    val isBooked: Boolean,
)

data class BookingWindowResponse(
    val startDate: String?,
    val endDate: String?,
    val availableDates: List<AvailableDateDto>,
)

data class AvailableDateDto(
    val date: String,
    val slots: List<SlotDto>,
)

data class RescheduleRequest(
    val fromDate: String,
    val toDate: String,
    val locationId: String? = null,
)
