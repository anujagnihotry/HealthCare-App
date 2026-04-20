package com.healthcare.app.data.api

import com.healthcare.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface DoctorApi {
    // Public
    @GET("doctors")
    suspend fun getAllDoctors(): Response<List<DoctorDto>>

    @GET("doctors/{id}")
    suspend fun getDoctorById(@Path("id") id: String): Response<DoctorDto>

    @GET("doctors/{doctorId}/locations/{locationId}/slots")
    suspend fun getAvailableSlots(
        @Path("doctorId") doctorId: String,
        @Path("locationId") locationId: String,
        @Query("date") date: String,
    ): Response<List<SlotDto>>

    // Doctor auth
    @GET("doctors/me/profile")
    suspend fun getMyProfile(): Response<DoctorDto>

    @PATCH("doctors/me/profile")
    suspend fun updateMyProfile(@Body request: UpdateDoctorProfileRequest): Response<DoctorDto>

    @POST("doctors/me/locations")
    suspend fun createLocation(@Body request: CreateLocationRequest): Response<LocationDto>

    @GET("doctors/me/locations")
    suspend fun getMyLocations(): Response<List<LocationDto>>

    @PATCH("doctors/me/locations/{locationId}")
    suspend fun updateLocation(
        @Path("locationId") locationId: String,
        @Body request: CreateLocationRequest,
    ): Response<LocationDto>

    @DELETE("doctors/me/locations/{locationId}")
    suspend fun deleteLocation(@Path("locationId") locationId: String): Response<Any>

    @POST("doctors/me/availability")
    suspend fun createAvailability(@Body request: CreateAvailabilityRequest): Response<AvailabilityDto>

    @GET("doctors/me/availability")
    suspend fun getMyAvailabilities(
        @Query("locationId") locationId: String? = null,
    ): Response<List<AvailabilityDto>>

    @DELETE("doctors/me/availability/{availabilityId}")
    suspend fun deleteAvailability(@Path("availabilityId") availabilityId: String): Response<Any>

    @POST("doctors/me/unavailability")
    suspend fun createUnavailability(@Body request: CreateUnavailabilityRequest): Response<Any>

    @GET("doctors/me/unavailability")
    suspend fun getMyUnavailabilities(): Response<List<Any>>

    @DELETE("doctors/me/unavailability/{id}")
    suspend fun deleteUnavailability(@Path("id") id: String): Response<Any>
}
