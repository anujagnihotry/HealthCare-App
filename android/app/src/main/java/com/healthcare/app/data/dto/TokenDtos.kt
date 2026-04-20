package com.healthcare.app.data.dto

data class TokenDto(
    val id: String,
    val appointmentId: String?,
    val doctorId: String,
    val locationId: String,
    val date: String,
    val tokenNumber: Int,
    val status: String,
    val position: Int,
    val hasArrived: Boolean,
    val isOffline: Boolean,
    val patientName: String?,
    val appointment: AppointmentDto?,
)

data class CurrentTokenResponse(
    val currentToken: Int?,
    val totalWaiting: Int,
)

data class AdvanceTokenResponse(
    val currentToken: Int?,
    val tokenId: String?,
    val message: String?,
)

data class AssignOfflineTokenRequest(
    val locationId: String,
    val date: String,
    val patientName: String? = null,
)

data class SwapTokensRequest(
    val tokenId: String,
    val afterTokenId: String,
)

data class QueueRequest(
    val locationId: String,
    val date: String,
)
