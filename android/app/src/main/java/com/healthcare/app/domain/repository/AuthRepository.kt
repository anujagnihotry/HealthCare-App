package com.healthcare.app.domain.repository

import com.healthcare.app.data.dto.AuthResponse
import com.healthcare.app.domain.model.Resource

interface AuthRepository {
    suspend fun register(
        email: String?, phone: String?, password: String,
        name: String, role: String, specialization: String?,
    ): Resource<AuthResponse>

    suspend fun login(email: String?, phone: String?, password: String): Resource<AuthResponse>
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
    suspend fun getUserRole(): String?
}
