package com.healthcare.app.data.api

import com.healthcare.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface TokenApi {
    // Public
    @GET("tokens/current")
    suspend fun getCurrentToken(
        @Query("doctorId") doctorId: String,
        @Query("locationId") locationId: String,
        @Query("date") date: String,
    ): Response<CurrentTokenResponse>

    // Doctor
    @GET("tokens/queue")
    suspend fun getTokenQueue(
        @Query("locationId") locationId: String,
        @Query("date") date: String,
    ): Response<List<TokenDto>>

    @POST("tokens/start-serving")
    suspend fun startServing(@Body request: QueueRequest): Response<AdvanceTokenResponse>

    @POST("tokens/advance")
    suspend fun advanceToken(@Body request: QueueRequest): Response<AdvanceTokenResponse>

    @POST("tokens/swap")
    suspend fun swapTokens(@Body request: SwapTokensRequest): Response<Map<String, String>>

    @POST("tokens/offline")
    suspend fun assignOfflineToken(@Body request: AssignOfflineTokenRequest): Response<TokenDto>

    @PATCH("tokens/{tokenId}/arrival")
    suspend fun markArrival(
        @Path("tokenId") tokenId: String,
        @Body request: Map<String, Boolean>,
    ): Response<TokenDto>

    @PATCH("tokens/{tokenId}/skip")
    suspend fun skipToken(@Path("tokenId") tokenId: String): Response<TokenDto>
}
