package com.healthcare.app.data.repository

import com.healthcare.app.data.TokenManager
import com.healthcare.app.data.api.AuthApi
import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
) : AuthRepository {

    override suspend fun register(
        email: String?,
        phone: String?,
        password: String,
        name: String,
        role: String,
        specialization: String?,
    ): Resource<AuthResponse> {
        return try {
            val response = authApi.register(
                RegisterRequest(email, phone, password, name, role, specialization)
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveTokens(
                    body.accessToken, body.refreshToken,
                    body.user.id, body.user.role,
                    body.user.doctorId, body.user.patientId,
                )
                Resource.Success(body)
            } else {
                Resource.Error(response.message() ?: "Registration failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun login(email: String?, phone: String?, password: String): Resource<AuthResponse> {
        return try {
            val response = authApi.login(LoginRequest(email, phone, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveTokens(
                    body.accessToken, body.refreshToken,
                    body.user.id, body.user.role,
                    body.user.doctorId, body.user.patientId,
                )
                Resource.Success(body)
            } else {
                Resource.Error("Invalid credentials")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun logout() {
        tokenManager.clearTokens()
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.getAccessToken() != null
    }

    override suspend fun getUserRole(): String? {
        return tokenManager.getUserRole()
    }
}
