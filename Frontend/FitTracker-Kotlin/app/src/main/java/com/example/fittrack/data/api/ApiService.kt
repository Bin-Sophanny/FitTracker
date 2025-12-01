package com.example.fittrack.data.api

import com.example.fittrack.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service interface for FitTracker Backend
 * All endpoints require Firebase authentication token in Authorization header
 */
interface ApiService {

    // ==================== User Service Endpoints ====================

    /**
     * Get user profile information
     */
    @GET("api/user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<UserProfile>

    /**
     * Update user profile
     */
    @PUT("api/user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body profile: UpdateProfileRequest
    ): Response<UserProfile>

    /**
     * Delete user account
     */
    @DELETE("api/user/account")
    suspend fun deleteAccount(
        @Header("Authorization") token: String
    ): Response<Unit>


    // ==================== Stats Service Endpoints ====================

    /**
     * Get daily stats history
     * @param limit Number of days to retrieve (default: 5)
     */
    @GET("api/stats")
    suspend fun getDailyStats(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 5
    ): Response<List<DailyStats>>

    /**
     * Get today's stats
     */
    @GET("api/stats/today")
    suspend fun getTodayStats(
        @Header("Authorization") token: String
    ): Response<DailyStats>

    /**
     * Log daily stats
     */
    @POST("api/stats")
    suspend fun logDailyStats(
        @Header("Authorization") token: String,
        @Body stats: DailyStats
    ): Response<DailyStats>


    // ==================== Workout Service Endpoints ====================

    /**
     * Get all workouts for the user
     */
    @GET("api/workouts")
    suspend fun getWorkouts(
        @Header("Authorization") token: String
    ): Response<List<Workout>>

    /**
     * Get a specific workout by ID
     */
    @GET("api/workouts/{id}")
    suspend fun getWorkoutById(
        @Header("Authorization") token: String,
        @Path("id") workoutId: String
    ): Response<Workout>

    /**
     * Log a new workout
     */
    @POST("api/workouts")
    suspend fun logWorkout(
        @Header("Authorization") token: String,
        @Body workout: CreateWorkoutRequest
    ): Response<Workout>

    /**
     * Update a workout
     */
    @PUT("api/workouts/{id}")
    suspend fun updateWorkout(
        @Header("Authorization") token: String,
        @Path("id") workoutId: String,
        @Body workout: CreateWorkoutRequest
    ): Response<Workout>

    /**
     * Delete a workout
     */
    @DELETE("api/workouts/{id}")
    suspend fun deleteWorkout(
        @Header("Authorization") token: String,
        @Path("id") workoutId: String
    ): Response<Unit>


    // ==================== Goal Service Endpoints ====================

    /**
     * Get all goals for the user
     */
    @GET("api/goals")
    suspend fun getGoals(
        @Header("Authorization") token: String
    ): Response<List<Goal>>

    /**
     * Get a specific goal by ID
     */
    @GET("api/goals/{id}")
    suspend fun getGoalById(
        @Header("Authorization") token: String,
        @Path("id") goalId: String
    ): Response<Goal>

    /**
     * Create a new goal
     */
    @POST("api/goals")
    suspend fun createGoal(
        @Header("Authorization") token: String,
        @Body goal: CreateGoalRequest
    ): Response<Goal>

    /**
     * Update goal progress
     */
    @PUT("api/goals/{id}")
    suspend fun updateGoal(
        @Header("Authorization") token: String,
        @Path("id") goalId: String,
        @Body goal: UpdateGoalRequest
    ): Response<Goal>

    /**
     * Delete a goal
     */
    @DELETE("api/goals/{id}")
    suspend fun deleteGoal(
        @Header("Authorization") token: String,
        @Path("id") goalId: String
    ): Response<Unit>


    // ==================== Blockchain Service Endpoints ====================

    /**
     * Get user's token balance
     */
    @GET("api/blockchain/balance")
    suspend fun getTokenBalance(
        @Header("Authorization") token: String
    ): Response<TokenBalance>

    /**
     * Get transaction history
     */
    @GET("api/blockchain/transactions")
    suspend fun getTransactions(
        @Header("Authorization") token: String
    ): Response<List<TokenTransaction>>

    /**
     * Award tokens for completing activities (usually called by backend)
     */
    @POST("api/blockchain/reward")
    suspend fun awardTokens(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Response<RewardResponse>
}

