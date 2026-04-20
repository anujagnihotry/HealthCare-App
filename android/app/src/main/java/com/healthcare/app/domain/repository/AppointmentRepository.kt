package com.healthcare.app.domain.repository

import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource

interface AppointmentRepository {
    suspend fun getBookingWindow(doctorId: String, locationId: String): Resource<BookingWindowResponse>
    suspend fun bookAppointment(request: BookAppointmentRequest): Resource<BookingResponse>
    suspend fun cancelAppointment(appointmentId: String): Resource<Unit>
    suspend fun getDoctorAppointments(date: String, locationId: String? = null): Resource<List<AppointmentDto>>
}
