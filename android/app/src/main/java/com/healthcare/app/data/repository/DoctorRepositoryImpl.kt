package com.healthcare.app.data.repository

import com.healthcare.app.data.api.DoctorApi
import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.DoctorRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoctorRepositoryImpl @Inject constructor(
    private val api: DoctorApi,
) : DoctorRepository {

    override suspend fun getAllDoctors(): Resource<List<DoctorDto>> {
        return try {
            val response = api.getAllDoctors()
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load doctors")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getDoctorById(id: String): Resource<DoctorDto> {
        return try {
            val response = api.getDoctorById(id)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Doctor not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getMyProfile(): Resource<DoctorDto> {
        return try {
            val response = api.getMyProfile()
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load profile")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun updateProfile(request: UpdateDoctorProfileRequest): Resource<DoctorDto> {
        return try {
            val response = api.updateMyProfile(request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Update failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getMyLocations(): Resource<List<LocationDto>> {
        return try {
            val response = api.getMyLocations()
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load locations")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun createLocation(request: CreateLocationRequest): Resource<LocationDto> {
        return try {
            val response = api.createLocation(request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to create location")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun updateLocation(
        locationId: String,
        request: CreateLocationRequest,
    ): Resource<LocationDto> {
        return try {
            val response = api.updateLocation(locationId, request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to update location")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun deleteLocation(locationId: String): Resource<Unit> {
        return try {
            val response = api.deleteLocation(locationId)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Failed to remove location")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun createAvailability(request: CreateAvailabilityRequest): Resource<AvailabilityDto> {
        return try {
            val response = api.createAvailability(request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to create availability")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getMyAvailabilities(locationId: String?): Resource<List<AvailabilityDto>> {
        return try {
            val response = api.getMyAvailabilities(locationId)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load availabilities")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
