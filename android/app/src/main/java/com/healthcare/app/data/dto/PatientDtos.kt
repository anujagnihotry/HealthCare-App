package com.healthcare.app.data.dto

data class PatientDto(
    val id: String,
    val name: String,
    val phone: String?,
    val email: String?,
    val familyMembers: List<FamilyMemberDto>?,
)

data class FamilyMemberDto(
    val id: String,
    val patientId: String,
    val name: String,
    val age: Int,
    val gender: String,
    val bloodGroup: String?,
    val allergies: List<String>,
    val isSelf: Boolean,
)

data class CreateFamilyMemberRequest(
    val name: String,
    val age: Int,
    val gender: String,
    val bloodGroup: String? = null,
    val allergies: List<String> = emptyList(),
)

data class DashboardResponse(
    val upcoming: List<AppointmentDto>,
    val past: List<AppointmentDto>,
)
