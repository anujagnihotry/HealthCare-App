package com.healthcare.app.data.repository

import com.healthcare.app.data.api.AppointmentApi
import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.AppointmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepositoryImpl @Inject constructor(
    private val api: AppointmentApi,
) : AppointmentRepository {

    override suspend fun getBookingWindow(
        doctorId: String,
        locationId: String,
    ): Resource<BookingWindowResponse> {
        return try {
            val response = api.getBookingWindow(doctorId, locationId)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load booking window")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun bookAppointment(request: BookAppointmentRequest): Resource<BookingResponse> {
        return try {
            val response = api.bookAppointment(request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Booking failed. Slot may already be taken.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun cancelAppointment(appointmentId: String): Resource<Unit> {
        return try {
            val response = api.cancelAppointment(appointmentId)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Failed to cancel appointment")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getDoctorAppointments(
        date: String,
        locationId: String?,
    ): Resource<List<AppointmentDto>> {
        return try {
            val response = api.getDoctorAppointments(date, locationId)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load appointments")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
