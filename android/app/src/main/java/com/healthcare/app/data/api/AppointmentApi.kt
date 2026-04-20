package com.healthcare.app.data.api

import com.healthcare.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AppointmentApi {
    @GET("appointments/booking-window")
    suspend fun getBookingWindow(
        @Query("doctorId") doctorId: String,
        @Query("locationId") locationId: String,
    ): Response<BookingWindowResponse>

    @POST("appointments/book")
    suspend fun bookAppointment(@Body request: BookAppointmentRequest): Response<BookingResponse>

    @PATCH("appointments/{appointmentId}/cancel")
    suspend fun cancelAppointment(@Path("appointmentId") appointmentId: String): Response<Map<String, String>>

    // Doctor
    @GET("appointments/doctor/by-date")
    suspend fun getDoctorAppointments(
        @Query("date") date: String,
        @Query("locationId") locationId: String? = null,
    ): Response<List<AppointmentDto>>

    @POST("appointments/doctor/reschedule-all")
    suspend fun rescheduleAll(@Body request: RescheduleRequest): Response<Map<String, Any>>
}
