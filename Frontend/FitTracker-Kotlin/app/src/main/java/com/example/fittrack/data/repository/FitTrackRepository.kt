package com.example.fittrack.data.repository

import android.content.Context
import com.example.fittrack.data.api.*
import com.example.fittrack.data.model.*
import com.example.fittrack.auth.TokenManager
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository class to handle all API calls with JWT token authentication
 * Updated to use JWT tokens from backend instead of Firebase tokens
 */
class FitTrackRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /**
     * Get JWT auth token from backend (stored after login)
     * Uses the JWT token from backend login stored in TokenManager
     */
    private fun getAuthToken(context: Context): String? {
        return try {
            android.util.Log.d("FitTrackRepo", "🔑 Getting JWT auth token from TokenManager...")
            android.util.Log.d("FitTrackRepo", "Current user: ${auth.currentUser?.email ?: "NULL"}")

            if (auth.currentUser == null) {
                android.util.Log.e("FitTrackRepo", "❌ CRITICAL: No Firebase user logged in!")
                return null
            }

            // Get JWT token from TokenManager (stored during login/register)
            val jwtToken = TokenManager.getToken(context)
            val token = jwtToken?.let { "Bearer $it" }

            if (token != null) {
                android.util.Log.d("FitTrackRepo", "✅ JWT Token obtained: ${token.take(30)}...")
            } else {
                android.util.Log.e("FitTrackRepo", "❌ Failed to get JWT token - user needs to login")
            }

            token
        } catch (e: Exception) {
            android.util.Log.e("FitTrackRepo", "❌ Exception getting token: ${e.message}", e)
            null
        }
    }

    /**
     * Get current user ID from Firebase UID
     */
    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    // ==================== Auth Service Methods ====================

    /**
     * Register user with backend and get JWT token
     */
    suspend fun registerUser(request: RegisterRequest): Response<AuthResponse> {
        android.util.Log.d("FitTrackRepo", "📝 Registering user with backend: ${request.email}")
        return apiService.register(request)
    }

    /**
     * Login user with backend and get JWT token
     */
    suspend fun loginUser(request: LoginRequest): Response<AuthResponse> {
        android.util.Log.d("FitTrackRepo", "🔐 Logging in user with backend: ${request.email}")
        return apiService.login(request)
    }

    // ==================== User Service Methods ====================

    suspend fun getUserProfile(context: Context): Response<UserProfile> {
        val token = getAuthToken(context) ?: throw Exception("User not authenticated")
        return apiService.getProfile(token)
    }

    suspend fun updateProfile(context: Context, profile: UpdateProfileRequest): Response<UserProfile> {
        val token = getAuthToken(context) ?: throw Exception("User not authenticated")
        return apiService.updateProfile(token, profile)
    }

    // ==================== Fitness Service Methods ====================

    suspend fun getDailyStats(context: Context, limit: Int = 5): Response<List<DailyStats>> {
        android.util.Log.d("FitTrackRepo", "")
        android.util.Log.d("FitTrackRepo", "========================================")
        android.util.Log.d("FitTrackRepo", "📥 getDailyStats() CALLED")
        android.util.Log.d("FitTrackRepo", "========================================")

        val token = getAuthToken(context)
        if (token == null) {
            android.util.Log.e("FitTrackRepo", "❌ ABORTING: No auth token available")
            return Response.success(emptyList())
        }

        val userId = getUserId()
        if (userId == null) {
            android.util.Log.e("FitTrackRepo", "❌ ABORTING: No user ID available")
            return Response.success(emptyList())
        }

        android.util.Log.d("FitTrackRepo", "✅ Auth OK - UserID (Firebase UID): $userId")
        android.util.Log.d("FitTrackRepo", "📤 Making API call to: /api/fitness/stats/$userId/week")

        return try {
            val response = apiService.getStats(userId, "week", token)

            android.util.Log.d("FitTrackRepo", "📥 Response code: ${response.code()}")
            android.util.Log.d("FitTrackRepo", "Response message: ${response.message()}")

            if (response.isSuccessful) {
                val statsResponse = response.body()
                android.util.Log.d("FitTrackRepo", "Success! Response body: $statsResponse")

                // Convert FitnessData to DailyStats
                val dailyStatsList = statsResponse?.data?.map { fitness ->
                    DailyStats(
                        userId = fitness.userId,
                        date = fitness.date,
                        steps = fitness.steps,
                        calories = fitness.calories,
                        distance = fitness.distance,
                        activeMinutes = fitness.activeMinutes,
                        heartRate = fitness.heartRate
                    )
                } ?: emptyList()

                android.util.Log.d("FitTrackRepo", "Data count: ${dailyStatsList.size}")
                Response.success(dailyStatsList.take(limit))
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("FitTrackRepo", "getDailyStats ERROR ${response.code()}: $errorBody")
                android.util.Log.w("FitTrackRepo", "Backend error - returning empty stats for offline mode")
                Response.success(emptyList())
            }
        } catch (e: Exception) {
            android.util.Log.e("FitTrackRepo", "❌ Exception in getDailyStats: ${e.message}", e)
            Response.success(emptyList())
        }
    }

    suspend fun getTodayStats(context: Context): Response<DailyStats> {
        val token = getAuthToken(context)
        val userId = getUserId()

        if (token == null || userId == null) {
            android.util.Log.e("FitTrackRepo", "getTodayStats - No auth, returning empty stats for offline mode")
            val emptyStats = DailyStats(
                userId = userId ?: "",
                date = getCurrentDate(),
                steps = 0,
                calories = 0,
                distance = 0f,
                activeMinutes = 0,
                heartRate = null
            )
            return Response.success(emptyStats)
        }

        return try {
            val response = apiService.getTodayFitness(userId, token)

            android.util.Log.d("FitTrackRepo", "getTodayStats - URL: /api/fitness/today/$userId")
            android.util.Log.d("FitTrackRepo", "getTodayStats - Response code: ${response.code()}")

            if (response.isSuccessful) {
                val fitness = response.body()!!
                val dailyStats = DailyStats(
                    userId = fitness.userId,
                    date = fitness.date,
                    steps = fitness.steps,
                    calories = fitness.calories,
                    distance = fitness.distance,
                    activeMinutes = fitness.activeMinutes,
                    heartRate = fitness.heartRate
                )
                Response.success(dailyStats)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("FitTrackRepo", "getTodayStats ERROR ${response.code()}: $errorBody")

                val emptyStats = DailyStats(
                    userId = userId,
                    date = getCurrentDate(),
                    steps = 0,
                    calories = 0,
                    distance = 0f,
                    activeMinutes = 0,
                    heartRate = null
                )
                Response.success(emptyStats)
            }
        } catch (e: Exception) {
            android.util.Log.e("FitTrackRepo", "getTodayStats - Exception: ${e.message}", e)
            val emptyStats = DailyStats(
                userId = userId,
                date = getCurrentDate(),
                steps = 0,
                calories = 0,
                distance = 0f,
                activeMinutes = 0,
                heartRate = null
            )
            Response.success(emptyStats)
        }
    }

    suspend fun logDailyStats(context: Context, stats: DailyStats): Response<DailyStats> {
        android.util.Log.d("FitTrackRepo", "=== logDailyStats START ===")

        val token = getAuthToken(context)
        if (token == null) {
            android.util.Log.e("FitTrackRepo", "❌ User not authenticated - will count steps locally only")
            android.util.Log.e("FitTrackRepo", "Current user: ${auth.currentUser?.email ?: "NULL"}")
            android.util.Log.e("FitTrackRepo", "UID: ${auth.currentUser?.uid ?: "NULL"}")
            return Response.success(stats)
        }

        val userId = getUserId()
        if (userId == null) {
            android.util.Log.e("FitTrackRepo", "❌ User ID not found - will count steps locally only")
            return Response.success(stats)
        }

        android.util.Log.d("FitTrackRepo", "✅ Auth OK - User: ${auth.currentUser?.email}")
        android.util.Log.d("FitTrackRepo", "✅ JWT Token: ${token.take(20)}...")
        android.util.Log.d("FitTrackRepo", "✅ UserID (Firebase UID): $userId")

        return try {
            // Convert DailyStats to LogFitnessRequest
            // IMPORTANT: Using Firebase UID as userId for backend
            val logRequest = LogFitnessRequest(
                userId = userId,  // Firebase UID used as userId
                date = stats.date,
                steps = stats.steps,
                calories = stats.calories,
                distance = stats.distance,
                activeMinutes = stats.activeMinutes,
                heartRate = stats.heartRate
            )

            android.util.Log.d("FitTrackRepo", "📤 Sending request to /api/fitness/log")
            android.util.Log.d("FitTrackRepo", "📊 Data: steps=${stats.steps}, calories=${stats.calories}, distance=${stats.distance}km, date=${stats.date}")

            val response = apiService.logFitness(token, logRequest)

            android.util.Log.d("FitTrackRepo", "📥 Response code: ${response.code()}")

            if (response.isSuccessful) {
                android.util.Log.d("FitTrackRepo", "✅ SUCCESS! Data synced to backend")
                val logResponse = response.body()!!
                // Convert FitnessData back to DailyStats
                val dailyStats = DailyStats(
                    userId = logResponse.data.userId,
                    date = logResponse.data.date,
                    steps = logResponse.data.steps,
                    calories = logResponse.data.calories,
                    distance = logResponse.data.distance,
                    activeMinutes = logResponse.data.activeMinutes,
                    heartRate = logResponse.data.heartRate
                )
                Response.success(dailyStats)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("FitTrackRepo", "❌ FAILED: ${response.code()} - ${response.message()}")
                android.util.Log.e("FitTrackRepo", "❌ Error body: $errorBody")
                android.util.Log.w("FitTrackRepo", "⚠️ Backend sync failed but steps counted locally")
                Response.success(stats)
            }
        } catch (e: Exception) {
            android.util.Log.e("FitTrackRepo", "❌ Exception during sync: ${e.message}", e)
            android.util.Log.w("FitTrackRepo", "⚠️ Network error but steps counted locally")
            Response.success(stats)
        }
    }

    // ==================== Stub Methods for Features Not Yet Implemented ====================

    fun getWorkouts(): Response<List<Workout>> {
        return Response.success(emptyList())
    }

    @Suppress("UNUSED_PARAMETER")
    fun getWorkoutById(workoutId: String): Response<Workout> {
        throw Exception("Workout service not available")
    }

    @Suppress("UNUSED_PARAMETER")
    fun logWorkout(workout: CreateWorkoutRequest): Response<Workout> {
        throw Exception("Workout service not available")
    }

    @Suppress("UNUSED_PARAMETER")
    fun updateWorkout(workoutId: String, workout: CreateWorkoutRequest): Response<Workout> {
        throw Exception("Workout service not available")
    }

    @Suppress("UNUSED_PARAMETER")
    fun deleteWorkout(workoutId: String): Response<Unit> {
        throw Exception("Workout service not available")
    }

    fun getGoals(): Response<List<Goal>> {
        return Response.success(emptyList())
    }

    @Suppress("UNUSED_PARAMETER")
    fun getGoalById(goalId: String): Response<Goal> {
        throw Exception("Goal service not available")
    }

    @Suppress("UNUSED_PARAMETER")
    fun createGoal(goal: CreateGoalRequest): Response<Goal> {
        throw Exception("Goal service not available")
    }

    @Suppress("UNUSED_PARAMETER")
    fun updateGoal(goalId: String, goal: UpdateGoalRequest): Response<Goal> {
        throw Exception("Goal service not available")
    }

    @Suppress("UNUSED_PARAMETER")
    fun deleteGoal(goalId: String): Response<Unit> {
        throw Exception("Goal service not available")
    }

    @Suppress("unused")
    fun getTransactions(): Response<List<TokenTransaction>> {
        return Response.success(emptyList())
    }

    fun getTokenBalance(): Response<TokenBalance> {
        val tokenBalance = TokenBalance(
            balance = 0,
            totalEarned = 0,
            transactions = emptyList()
        )
        return Response.success(tokenBalance)
    }

    // ==================== Helper Methods ====================

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}

