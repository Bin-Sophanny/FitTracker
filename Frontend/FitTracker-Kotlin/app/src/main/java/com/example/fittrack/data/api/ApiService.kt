package com.example.fittrack.data.api

import com.example.fittrack.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service interface for FitTracker Backend
 * All endpoints match the actual backend routes
 */
interface ApiService {

    // ==================== AUTH ROUTES ====================

    /**
     * Register new user
     * POST /api/auth/register
     */
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    /**
     * Login user
     * POST /api/auth/login
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    /**
     * Get user profile
     * GET /api/auth/profile
     */
    @GET("api/auth/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<UserProfile>

    /**
     * Update user profile
     * PUT /api/auth/profile
     */
    @PUT("api/auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<UserProfile>


    // ==================== FITNESS ROUTES ====================

    /**
     * Log fitness data
     * POST /api/fitness/log
     */
    @POST("api/fitness/log")
    suspend fun logFitness(
        @Header("Authorization") token: String,
        @Body request: LogFitnessRequest
    ): Response<FitnessLogResponse>

    /**
     * Get today's fitness data
     * GET /api/fitness/today/{userId}
     */
    @GET("api/fitness/today/{userId}")
    suspend fun getTodayFitness(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<FitnessData>

    /**
     * Get fitness stats (week/month/year)
     * GET /api/fitness/stats/{userId}/{range}
     */
    @GET("api/fitness/stats/{userId}/{range}")
    suspend fun getStats(
        @Path("userId") userId: String,
        @Path("range") range: String,
        @Header("Authorization") token: String
    ): Response<FitnessStatsResponse>

    /**
     * Get fitness summary
     * GET /api/fitness/summary/{userId}
     */
    @GET("api/fitness/summary/{userId}")
    suspend fun getSummary(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<SummaryResponse>


    // ==================== BLOCKCHAIN ROUTES ====================

    /**
     * Get user rewards
     * GET /api/blockchain/rewards/{address}
     */
    @GET("api/blockchain/rewards/{address}")
    suspend fun getRewards(
        @Path("address") address: String
    ): Response<RewardsResponse>

    /**
     * Award rewards
     * POST /api/blockchain/rewards
     */
    @POST("api/blockchain/rewards")
    suspend fun awardRewards(
        @Body request: AwardRewardsRequest
    ): Response<RewardResponse>


    // ==================== HEALTH CHECK ====================

    /**
     * Health check
     * GET /health
     */
    @GET("health")
    suspend fun healthCheck(): Response<Map<String, Any>>
}

