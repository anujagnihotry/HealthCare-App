package com.healthcare.app.data.dto

data class DoctorDto(
    val id: String,
    val name: String,
    val specialization: String,
    val bio: String?,
    val profileImageUrl: String?,
    val contactPhone: String?,
    val contactEmail: String?,
    val locations: List<LocationDto>?,
    val availabilities: List<AvailabilityDto>?,
    /** Total years of clinical experience (optional; omitted on older API responses). */
    val yearsOfExperience: Int? = null,
    val degree: String? = null,
    /** `homeopathy` | `allopathy` | `ayush` */
    val medicalSystem: String? = null,
)

data class LocationDto(
    val id: String,
    val doctorId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isActive: Boolean,
)

data class AvailabilityDto(
    val id: String,
    val doctorId: String,
    val locationId: String,
    val dayOfWeek: Int,
    val sessionName: String,
    val startTime: String,
    val endTime: String,
    val slotDurationMinutes: Int,
    val breakStart: String?,
    val breakEnd: String?,
    val isActive: Boolean,
    val location: LocationDto?,
)

data class CreateLocationRequest(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)

data class CreateAvailabilityRequest(
    val locationId: String,
    val dayOfWeek: Int,
    val sessionName: String,
    val startTime: String,
    val endTime: String,
    val slotDurationMinutes: Int,
    val breakStart: String? = null,
    val breakEnd: String? = null,
)

data class CreateUnavailabilityRequest(
    val date: String,
    val locationId: String? = null,
    val sessionName: String? = null,
    val reason: String? = null,
)

data class UpdateDoctorProfileRequest(
    val name: String? = null,
    val specialization: String? = null,
    val bio: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val yearsOfExperience: Int? = null,
    val degree: String? = null,
    val medicalSystem: String? = null,
)
