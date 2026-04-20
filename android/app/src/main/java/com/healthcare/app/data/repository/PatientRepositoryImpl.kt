package com.healthcare.app.data.repository

import com.healthcare.app.data.api.PatientApi
import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.PatientRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepositoryImpl @Inject constructor(
    private val api: PatientApi,
) : PatientRepository {

    override suspend fun getProfile(): Resource<PatientDto> {
        return try {
            val response = api.getMyProfile()
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load profile")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getDashboard(): Resource<DashboardResponse> {
        return try {
            val response = api.getDashboard()
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load dashboard")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getFamilyMembers(): Resource<List<FamilyMemberDto>> {
        return try {
            val response = api.getFamilyMembers()
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load family members")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun createFamilyMember(request: CreateFamilyMemberRequest): Resource<FamilyMemberDto> {
        return try {
            val response = api.createFamilyMember(request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to add family member")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun updateFamilyMember(
        memberId: String,
        request: CreateFamilyMemberRequest,
    ): Resource<FamilyMemberDto> {
        return try {
            val response = api.updateFamilyMember(memberId, request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to update family member")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun deleteFamilyMember(memberId: String): Resource<Unit> {
        return try {
            val response = api.deleteFamilyMember(memberId)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Failed to delete family member")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
