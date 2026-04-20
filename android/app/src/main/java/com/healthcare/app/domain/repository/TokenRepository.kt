package com.healthcare.app.domain.repository

import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource

interface TokenRepository {
    suspend fun getCurrentToken(doctorId: String, locationId: String, date: String): Resource<CurrentTokenResponse>
    suspend fun getTokenQueue(locationId: String, date: String): Resource<List<TokenDto>>
    suspend fun startServing(locationId: String, date: String): Resource<AdvanceTokenResponse>
    suspend fun advanceToken(locationId: String, date: String): Resource<AdvanceTokenResponse>
    suspend fun assignOfflineToken(request: AssignOfflineTokenRequest): Resource<TokenDto>
    suspend fun swapTokens(request: SwapTokensRequest): Resource<Unit>
    suspend fun markArrival(tokenId: String, hasArrived: Boolean): Resource<TokenDto>
    suspend fun skipToken(tokenId: String): Resource<TokenDto>
}
