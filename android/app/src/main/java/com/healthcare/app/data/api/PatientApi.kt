package com.healthcare.app.data.api

import com.healthcare.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface PatientApi {
    @GET("patients/me/profile")
    suspend fun getMyProfile(): Response<PatientDto>

    @PATCH("patients/me/profile")
    suspend fun updateMyProfile(@Body request: Map<String, String>): Response<PatientDto>

    @GET("patients/me/dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    // Family Members
    @POST("patients/me/family-members")
    suspend fun createFamilyMember(@Body request: CreateFamilyMemberRequest): Response<FamilyMemberDto>

    @GET("patients/me/family-members")
    suspend fun getFamilyMembers(): Response<List<FamilyMemberDto>>

    @GET("patients/me/family-members/{memberId}")
    suspend fun getFamilyMember(@Path("memberId") memberId: String): Response<FamilyMemberDto>

    @PATCH("patients/me/family-members/{memberId}")
    suspend fun updateFamilyMember(
        @Path("memberId") memberId: String,
        @Body request: CreateFamilyMemberRequest,
    ): Response<FamilyMemberDto>

    @DELETE("patients/me/family-members/{memberId}")
    suspend fun deleteFamilyMember(@Path("memberId") memberId: String): Response<Any>
}
