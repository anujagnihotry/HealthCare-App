package com.healthcare.app.data.repository

import com.healthcare.app.data.api.DoctorCareApi
import com.healthcare.app.data.api.UploadApi
import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.DoctorCareRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoctorCareRepositoryImpl @Inject constructor(
    private val doctorCareApi: DoctorCareApi,
    private val uploadApi: UploadApi,
) : DoctorCareRepository {

    override suspend fun searchPatients(q: String, mode: String?): Resource<List<PatientSearchResultDto>> {
        return try {
            val response = doctorCareApi.searchPatients(q, mode)
            if (response.isSuccessful) Resource.Success(response.body() ?: emptyList())
            else Resource.Error(response.errorBody()?.string() ?: "Search failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getPatientHistory(familyMemberId: String): Resource<PatientHistoryDto> {
        return try {
            val response = doctorCareApi.getPatientHistory(familyMemberId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to load history")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun assignCustomCode(
        familyMemberId: String,
        code: String,
    ): Resource<DoctorPatientMappingDto> {
        return try {
            val response = doctorCareApi.assignCustomCode(
                familyMemberId,
                AssignCustomCodeRequest(code),
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to save code")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun createConsultation(body: CreateConsultationRequest): Resource<ConsultationDto> {
        return try {
            val response = doctorCareApi.createConsultation(body)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to save consultation")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun uploadFileForConsultation(
        familyMemberId: String,
        appointmentId: String?,
        consultationId: String,
        type: String,
        date: String,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Resource<UploadDto> {
        return try {
            val media = (mimeType.ifBlank { "application/octet-stream" }).toMediaTypeOrNull()
            val filePart = MultipartBody.Part.createFormData(
                "file",
                fileName,
                bytes.toRequestBody(media),
            )
            val fmBody = familyMemberId.toRequestBody(null)
            val typeBody = type.toRequestBody(null)
            val dateBody = date.toRequestBody(null)
            val consultBody = consultationId.toRequestBody(null)
            val aptBody = appointmentId?.toRequestBody(null)
            val response = uploadApi.uploadFile(
                file = filePart,
                familyMemberId = fmBody,
                type = typeBody,
                date = dateBody,
                appointmentId = aptBody,
                consultationId = consultBody,
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Upload failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
