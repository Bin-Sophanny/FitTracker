package com.example.fittrack.data.repository

import android.content.Context
import com.example.fittrack.data.api.*
import com.example.fittrack.data.model.*
import com.example.fittrack.auth.TokenManager
import com.example.fittrack.util.DateUtils
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Response

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
                android.util.Log.d("FitTrackRepo", "")
                android.util.Log.d("FitTrackRepo", "🔍 BACKEND RESPONSE DETAILS:")
                android.util.Log.d("FitTrackRepo", "   Data array size: ${statsResponse?.data?.size ?: 0}")
                android.util.Log.d("FitTrackRepo", "   Total steps: ${statsResponse?.totalSteps ?: 0}")
                android.util.Log.d("FitTrackRepo", "   Period: ${statsResponse?.period ?: "unknown"}")
                android.util.Log.d("FitTrackRepo", "")

                // Convert FitnessData to DailyStats
                // Keep raw UTC date from MongoDB to display actual UTC date in StatsScreen
                val dailyStatsList = statsResponse?.data?.map { fitness ->
                    // MongoDB returns: "2025-12-10T00:00:00.000+07:00" or "2025-12-09T00:00:00.000+00:00"
                    // Keep as-is to show UTC date in StatsScreen for verification

                    android.util.Log.d("FitTrackRepo", "📅 Retrieved from MongoDB: date=${fitness.date}, steps=${fitness.steps}, cal=${fitness.calories}, dist=${fitness.distance}")

                    DailyStats(
                        userId = fitness.userId,
                        date = fitness.date,  // Keep raw date from MongoDB (UTC format)
                        steps = fitness.steps,
                        calories = fitness.calories,
                        distance = fitness.distance,
                        activeMinutes = fitness.activeMinutes,
                    )
                } ?: emptyList()

                android.util.Log.d("FitTrackRepo", "")
                android.util.Log.d("FitTrackRepo", "========== RETRIEVED DATES (from MongoDB) ==========")
                dailyStatsList.forEachIndexed { index, stats ->
                    android.util.Log.d("FitTrackRepo", "[$index] Date: ${stats.date}, Steps: ${stats.steps}, Cal: ${stats.calories}, Dist: ${stats.distance} km")
                }
                android.util.Log.d("FitTrackRepo", "=========================================================")
                android.util.Log.d("FitTrackRepo", "✅ Total records from backend: ${dailyStatsList.size}")
                android.util.Log.d("FitTrackRepo", "✅ Returning (with limit=$limit): ${dailyStatsList.take(limit).size} records")
                android.util.Log.d("FitTrackRepo", "")

                if (dailyStatsList.size < 2) {
                    android.util.Log.w("FitTrackRepo", "⚠️ WARNING: Backend only returned ${dailyStatsList.size} day(s) of data!")
                    android.util.Log.w("FitTrackRepo", "   Expected: Multiple days for 'week' range")
                    android.util.Log.w("FitTrackRepo", "   This is a BACKEND ISSUE - the MongoDB query is not returning historical data")
                    android.util.Log.w("FitTrackRepo", "   WORKAROUND: Try using getSummary endpoint or fix backend query")
                }

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
                activeMinutes = 0
            )
            return Response.success(emptyStats)
        }

        return try {
            val response = apiService.getTodayFitness(userId, token)

            android.util.Log.d("FitTrackRepo", "getTodayStats - URL: /api/fitness/today/$userId")
            android.util.Log.d("FitTrackRepo", "getTodayStats - Response code: ${response.code()}")

            if (response.isSuccessful) {
                val fitness = response.body()!!

                // Keep raw UTC date from MongoDB for verification
                android.util.Log.d("FitTrackRepo", "📅 Today Stats (UTC): ${fitness.date}")

                val dailyStats = DailyStats(
                    userId = fitness.userId,
                    date = fitness.date,  // Keep raw UTC date from MongoDB
                    steps = fitness.steps,
                    calories = fitness.calories,
                    distance = fitness.distance,
                    activeMinutes = fitness.activeMinutes,
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
                    activeMinutes = 0
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
                activeMinutes = 0
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
            // Send date with GMT+7 timezone information to prevent MongoDB from converting to UTC
            // Format: "2025-12-10T00:00:00.000+07:00" instead of just "2025-12-10"
            // This ensures MongoDB stores December 10th in GMT+7, not December 9th in UTC
            val dateWithTimezone = DateUtils.getCurrentDateWithTimezone()

            android.util.Log.d("FitTrackRepo", "📅 Date Formatting for MongoDB:")
            android.util.Log.d("FitTrackRepo", "   Original: ${stats.date}")
            android.util.Log.d("FitTrackRepo", "   With TZ:  $dateWithTimezone")
            android.util.Log.d("FitTrackRepo", "   Purpose: Preserve GMT+7 timezone in MongoDB")

            val logRequest = LogFitnessRequest(
                userId = userId,
                date = dateWithTimezone,  // Send with timezone: "2025-12-10T00:00:00.000+07:00"
                steps = stats.steps,
                calories = stats.calories,
                distance = stats.distance,
                activeMinutes = stats.activeMinutes
            )

            android.util.Log.d("FitTrackRepo", "📤 Sending request to /api/fitness/log")
            android.util.Log.d("FitTrackRepo", "📊 REQUEST BODY:")
            android.util.Log.d("FitTrackRepo", "   userId: $userId")
            android.util.Log.d("FitTrackRepo", "   date: ${stats.date} (Asia/Singapore GMT+8)")
            android.util.Log.d("FitTrackRepo", "   AWS Region: ap-southeast-1")
            android.util.Log.d("FitTrackRepo", "   steps: ${stats.steps}")
            android.util.Log.d("FitTrackRepo", "   calories: ${stats.calories}")
            android.util.Log.d("FitTrackRepo", "   distance: ${stats.distance}km")
            android.util.Log.d("FitTrackRepo", "   activeMinutes: ${stats.activeMinutes}")

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


    // ==================== Helper Methods ====================

    private fun getCurrentDate(): String {
        return DateUtils.getCurrentDate()
    }
}

