package com.example.fittrack.api

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // ========== HEALTH CHECK ==========
    @GET("/health")
    suspend fun healthCheck(): Response<Map<String, Any>>

    @GET("/api/health")
    suspend fun apiHealthCheck(): Response<Map<String, Any>>

    // ========== AUTH/USER SERVICE ==========
    @POST("/api/auth/register")
    suspend fun registerUser(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun loginUser(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @GET("/api/auth/profile/{userId}")
    suspend fun getProfile(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<UserProfile>

    @PUT("/api/auth/profile/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<UserProfile>

    @POST("/api/auth/link-wallet")
    suspend fun linkWallet(
        @Header("Authorization") token: String,
        @Body request: LinkWalletRequest
    ): Response<LinkWalletResponse>

    // ========== FITNESS DATA ==========
    @GET("/api/fitness/today/{userId}")
    suspend fun getTodayFitness(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<FitnessData>

    @GET("/api/fitness/stats/{userId}/{range}")
    suspend fun getStats(
        @Path("userId") userId: String,
        @Path("range") range: String, // "week", "month", "year"
        @Header("Authorization") token: String
    ): Response<StatsResponse>

    @POST("/api/fitness/log")
    suspend fun logFitness(
        @Header("Authorization") token: String,
        @Body request: LogFitnessRequest
    ): Response<FitnessData>

    @GET("/api/fitness/summary/{userId}")
    suspend fun getFitnessSummary(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<SummaryResponse>

    // ========== BLOCKCHAIN ==========
    @GET("/api/blockchain/rewards/{userAddress}")
    suspend fun getRewards(
        @Path("userAddress") userAddress: String,
        @Header("Authorization") token: String
    ): Response<RewardsResponse>
}

// ========== REQUEST/RESPONSE MODELS ==========

data class RegisterRequest(
    val firebaseUid: String,
    val email: String,
    val displayName: String
)

data class LoginRequest(
    val firebaseUid: String,
    val email: String
)

data class AuthResponse(
    val success: Boolean,
    val user: UserProfile,
    val token: String
)

data class UserProfile(
    val id: String,
    val firebaseUid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val walletAddress: String? = null,
    val createdAt: String? = null
)

data class UpdateProfileRequest(
    val displayName: String? = null,
    val photoUrl: String? = null,
    val walletAddress: String? = null
)

data class LinkWalletRequest(
    val userId: String,
    val walletAddress: String
)

data class LinkWalletResponse(
    val success: Boolean,
    val message: String,
    val walletAddress: String
)

data class FitnessData(
    val userId: String? = null,
    val date: String? = null,
    val steps: Int = 0,
    val calories: Int = 0,
    val distance: Float = 0f,
    val activeMinutes: Int = 0,
    val heartRate: Int? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class LogFitnessRequest(
    val userId: String,
    val date: String,
    val steps: Int,
    val calories: Int,
    val distance: Float,
    val activeMinutes: Int,
    val heartRate: Int? = null,
    val notes: String? = null
)

data class StatsResponse(
    val period: String,
    val totalSteps: Int,
    val totalCalories: Int,
    val totalDistance: Float,
    val totalActiveMinutes: Int,
    val averageSteps: Int,
    val averageCalories: Int,
    val data: List<FitnessData>
)

data class SummaryResponse(
    val totalEntries: Int,
    val totalSteps: Int,
    val totalCalories: Int,
    val totalDistance: Float,
    val totalActiveMinutes: Int,
    val lastUpdate: String? = null
)

data class RewardsResponse(
    val userAddress: String,
    val rewardBalance: String,
    val nftCount: Int,
    val network: String
)
