package com.healthcare.app.domain.repository

import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource

interface DoctorRepository {
    suspend fun getAllDoctors(): Resource<List<DoctorDto>>
    suspend fun getDoctorById(id: String): Resource<DoctorDto>
    suspend fun getMyProfile(): Resource<DoctorDto>
    suspend fun updateProfile(request: UpdateDoctorProfileRequest): Resource<DoctorDto>
    suspend fun getMyLocations(): Resource<List<LocationDto>>
    suspend fun createLocation(request: CreateLocationRequest): Resource<LocationDto>
    suspend fun updateLocation(locationId: String, request: CreateLocationRequest): Resource<LocationDto>
    suspend fun deleteLocation(locationId: String): Resource<Unit>
    suspend fun createAvailability(request: CreateAvailabilityRequest): Resource<AvailabilityDto>
    suspend fun getMyAvailabilities(locationId: String? = null): Resource<List<AvailabilityDto>>
}
