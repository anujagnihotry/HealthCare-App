package com.healthcare.app.data.dto

data class PatientSearchResultDto(
    val familyMemberId: String,
    val name: String,
    val patientId: String,
    val phone: String?,
    val customCode: String?,
)

data class HistoryFamilyMemberDto(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val bloodGroup: String?,
    val allergies: List<String>,
    val isSelf: Boolean,
)

data class HistoryPatientDto(
    val id: String,
    val name: String,
    val phone: String?,
    val email: String?,
)

data class PatientVitalDto(
    val id: String,
    val doctorId: String,
    val patientId: String,
    val familyMemberId: String,
    val bp: String?,
    val sugar: String?,
    val height: String?,
    val weight: String?,
    val recordedAt: String?,
)

data class ConsultationDto(
    val id: String,
    val doctorId: String,
    val patientId: String,
    val familyMemberId: String,
    val appointmentId: String?,
    val symptoms: String,
    val diagnosis: String,
    val illness: String,
    val medications: String,
    val notes: String?,
    val createdAt: String?,
    val appointment: AppointmentDto?,
)

data class PatientHistoryDto(
    val familyMember: HistoryFamilyMemberDto,
    val patient: HistoryPatientDto,
    val customCode: String?,
    val vitals: List<PatientVitalDto>,
    val consultations: List<ConsultationDto>,
    val legacyMedicalRecords: List<MedicalRecordDto>,
    val uploads: List<UploadDto>,
)

data class AssignCustomCodeRequest(
    val customCode: String,
)

data class DoctorPatientMappingDto(
    val id: String,
    val doctorId: String,
    val patientId: String,
    val familyMemberId: String,
    val customCode: String?,
    val firstVisitDate: String?,
)

data class CreateConsultationRequest(
    val patientId: String,
    val familyMemberId: String,
    val appointmentId: String? = null,
    val symptoms: String,
    val diagnosis: String,
    val illness: String,
    val medications: String,
    val notes: String? = null,
    val bp: String? = null,
    val sugar: String? = null,
    val height: String? = null,
    val weight: String? = null,
    val uploadIds: List<String>? = null,
)
