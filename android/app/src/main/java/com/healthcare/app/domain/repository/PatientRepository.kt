package com.healthcare.app.domain.repository

import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource

interface PatientRepository {
    suspend fun getProfile(): Resource<PatientDto>
    suspend fun getDashboard(): Resource<DashboardResponse>
    suspend fun getFamilyMembers(): Resource<List<FamilyMemberDto>>
    suspend fun createFamilyMember(request: CreateFamilyMemberRequest): Resource<FamilyMemberDto>
    suspend fun updateFamilyMember(memberId: String, request: CreateFamilyMemberRequest): Resource<FamilyMemberDto>
    suspend fun deleteFamilyMember(memberId: String): Resource<Unit>
}
