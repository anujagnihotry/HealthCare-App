package com.healthcare.app.data.repository

import com.healthcare.app.data.api.TokenApi
import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.TokenRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRepositoryImpl @Inject constructor(
    private val api: TokenApi,
) : TokenRepository {

    override suspend fun getCurrentToken(
        doctorId: String,
        locationId: String,
        date: String,
    ): Resource<CurrentTokenResponse> {
        return try {
            val response = api.getCurrentToken(doctorId, locationId, date)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to get current token")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getTokenQueue(
        locationId: String,
        date: String,
    ): Resource<List<TokenDto>> {
        return try {
            val response = api.getTokenQueue(locationId, date)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to load token queue")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun startServing(locationId: String, date: String): Resource<AdvanceTokenResponse> {
        return try {
            val response = api.startServing(QueueRequest(locationId, date))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to start serving")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun advanceToken(locationId: String, date: String): Resource<AdvanceTokenResponse> {
        return try {
            val response = api.advanceToken(QueueRequest(locationId, date))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to advance token")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun assignOfflineToken(request: AssignOfflineTokenRequest): Resource<TokenDto> {
        return try {
            val response = api.assignOfflineToken(request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to assign offline token")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun swapTokens(request: SwapTokensRequest): Resource<Unit> {
        return try {
            val response = api.swapTokens(request)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Failed to swap tokens")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun markArrival(tokenId: String, hasArrived: Boolean): Resource<TokenDto> {
        return try {
            val response = api.markArrival(tokenId, mapOf("hasArrived" to hasArrived))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to update arrival")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun skipToken(tokenId: String): Resource<TokenDto> {
        return try {
            val response = api.skipToken(tokenId)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Failed to skip token")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
