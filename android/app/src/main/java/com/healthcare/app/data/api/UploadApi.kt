package com.healthcare.app.data.api

import com.healthcare.app.data.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface UploadApi {
    @GET("uploads/family-member/{familyMemberId}")
    suspend fun getUploadsByFamilyMember(
        @Path("familyMemberId") familyMemberId: String,
    ): Response<List<UploadDto>>

    @GET("uploads/medical-records/{familyMemberId}")
    suspend fun getMedicalRecords(
        @Path("familyMemberId") familyMemberId: String,
    ): Response<List<MedicalRecordDto>>

    @GET("uploads/appointment/{appointmentId}")
    suspend fun getUploadsByAppointment(
        @Path("appointmentId") appointmentId: String,
    ): Response<List<UploadDto>>

    @Multipart
    @POST("uploads/file")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("familyMemberId") familyMemberId: RequestBody,
        @Part("type") type: RequestBody,
        @Part("date") date: RequestBody,
        @Part("appointmentId") appointmentId: RequestBody?,
        @Part("consultationId") consultationId: RequestBody?,
    ): Response<UploadDto>
}
