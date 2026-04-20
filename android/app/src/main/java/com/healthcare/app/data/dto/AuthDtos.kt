package com.healthcare.app.data.dto

import com.google.gson.annotations.SerializedName

// ── Request DTOs ───────────────────────────────────────

data class RegisterRequest(
    val email: String? = null,
    val phone: String? = null,
    val password: String,
    val name: String,
    val role: String,
    val specialization: String? = null,
)

data class LoginRequest(
    val email: String? = null,
    val phone: String? = null,
    val password: String,
)

data class RefreshTokenRequest(
    val refreshToken: String,
)

data class FcmTokenRequest(
    val fcmToken: String,
)

// ── Response DTOs ──────────────────────────────────────

data class AuthResponse(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
)

data class UserDto(
    val id: String,
    val email: String?,
    val phone: String?,
    val role: String,
    val doctorId: String?,
    val patientId: String?,
)
