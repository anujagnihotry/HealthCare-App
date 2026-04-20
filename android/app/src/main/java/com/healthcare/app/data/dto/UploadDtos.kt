package com.healthcare.app.data.dto

data class UploadDto(
    val id: String,
    val familyMemberId: String,
    val appointmentId: String?,
    val uploadedByUserId: String,
    val type: String, // "report" or "prescription"
    val fileUrl: String,
    val fileName: String,
    val tags: List<String>,
    val date: String,
    val createdAt: String?,
)

data class MedicalRecordDto(
    val id: String,
    val familyMemberId: String,
    val doctorId: String,
    val appointmentId: String?,
    val diagnosis: String?,
    val notes: String?,
    val doctor: DoctorDto?,
    val appointment: AppointmentDto?,
    val createdAt: String?,
)

data class PatientRecordsResponse(
    val records: List<MedicalRecordDto>,
    val uploads: List<UploadDto>,
)
