package com.healthcare.app.data.api

import com.healthcare.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface DoctorCareApi {
    @GET("doctor/patients/search")
    suspend fun searchPatients(
        @Query("q") q: String,
        @Query("mode") mode: String?,
    ): Response<List<PatientSearchResultDto>>

    @GET("doctor/patients/{familyMemberId}/history")
    suspend fun getPatientHistory(
        @Path("familyMemberId") familyMemberId: String,
    ): Response<PatientHistoryDto>

    @POST("doctor/patients/{familyMemberId}/custom-code")
    suspend fun assignCustomCode(
        @Path("familyMemberId") familyMemberId: String,
        @Body body: AssignCustomCodeRequest,
    ): Response<DoctorPatientMappingDto>

    @POST("consultations")
    suspend fun createConsultation(
        @Body body: CreateConsultationRequest,
    ): Response<ConsultationDto>
}
