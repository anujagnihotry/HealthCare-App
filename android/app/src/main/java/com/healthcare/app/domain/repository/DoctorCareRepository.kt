package com.healthcare.app.domain.repository

import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource

interface DoctorCareRepository {
    suspend fun searchPatients(q: String, mode: String?): Resource<List<PatientSearchResultDto>>

    suspend fun getPatientHistory(familyMemberId: String): Resource<PatientHistoryDto>

    suspend fun assignCustomCode(familyMemberId: String, code: String): Resource<DoctorPatientMappingDto>

    suspend fun createConsultation(body: CreateConsultationRequest): Resource<ConsultationDto>

    suspend fun uploadFileForConsultation(
        familyMemberId: String,
        appointmentId: String?,
        consultationId: String,
        type: String,
        date: String,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Resource<UploadDto>
}
