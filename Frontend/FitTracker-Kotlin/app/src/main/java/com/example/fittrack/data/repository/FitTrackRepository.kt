package com.example.fittrack.data.repository

import com.example.fittrack.api.*
import com.example.fittrack.api.RetrofitClient
import com.example.fittrack.data.model.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository class to handle all API calls with automatic Firebase token injection
 * Updated to work with 4 backend services: API Gateway, User Service, Fitness Service, Blockchain Service
 */
class FitTrackRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /**
     * Get Firebase auth token with "Bearer " prefix
     */
    private suspend fun getAuthToken(): String? {
        return try {
            android.util.Log.d("FitTrackRepo", "🔑 Getting Firebase auth token...")
            android.util.Log.d("FitTrackRepo", "Current user: ${auth.currentUser?.email ?: "NULL"}")

            if (auth.currentUser == null) {
                android.util.Log.e("FitTrackRepo", "❌ CRITICAL: No Firebase user logged in!")
                return null
            }

            val tokenResult = auth.currentUser?.getIdToken(true)?.await()
            val token = tokenResult?.token?.let { "Bearer $it" }

            if (token != null) {
                android.util.Log.d("FitTrackRepo", "✅ Token obtained: ${token.take(30)}...")
            } else {
                android.util.Log.e("FitTrackRepo", "❌ Failed to get token from Firebase")
            }

            token
        } catch (e: Exception) {
            android.util.Log.e("FitTrackRepo", "❌ Exception getting token: ${e.message}", e)
            null
        }
    }

    /**
     * Get current user ID from Firebase
     */
    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    // ==================== User Service Methods (via /api/auth) ====================

    suspend fun registerUser(displayName: String, email: String): Response<AuthResponse> {
        val userId = getUserId() ?: throw Exception("User ID not found")
        return apiService.registerUser(RegisterRequest(userId, email, displayName))
    }

    suspend fun loginUser(email: String): Response<AuthResponse> {
        val userId = getUserId() ?: throw Exception("User ID not found")
        return apiService.loginUser(LoginRequest(userId, email))
    }

    suspend fun getUserProfile(): Response<com.example.fittrack.api.UserProfile> {
        val token = getAuthToken() ?: throw Exception("User not authenticated")
        val userId = getUserId() ?: throw Exception("User ID not found")
        return apiService.getProfile(userId, token)
    }

    suspend fun updateProfile(profile: com.example.fittrack.data.model.UpdateProfileRequest): Response<com.example.fittrack.api.UserProfile> {
        val token = getAuthToken() ?: throw Exception("User not authenticated")
        val userId = getUserId() ?: throw Exception("User ID not found")

        val apiProfile = com.example.fittrack.api.UpdateProfileRequest(
            displayName = profile.displayName,
            photoUrl = profile.photoUrl,
            walletAddress = profile.walletAddress
        )
        return apiService.updateProfile(userId, token, apiProfile)
    }

    suspend fun linkWallet(walletAddress: String): Response<LinkWalletResponse> {
        val token = getAuthToken() ?: throw Exception("User not authenticated")
        val userId = getUserId() ?: throw Exception("User ID not found")
        return apiService.linkWallet(token, LinkWalletRequest(userId, walletAddress))
    }

    // ==================== Fitness Service Methods (via /api/fitness) ====================

    suspend fun getDailyStats(limit: Int = 5): Response<List<DailyStats>> {
        android.util.Log.d("FitTrackRepo", "")
        android.util.Log.d("FitTrackRepo", "========================================")
        android.util.Log.d("FitTrackRepo", "📥 getDailyStats() CALLED")
        android.util.Log.d("FitTrackRepo", "========================================")

        val token = getAuthToken()
        if (token == null) {
            android.util.Log.e("FitTrackRepo", "❌ ABORTING: No auth token available")
            throw Exception("User not authenticated")
        }

        val userId = getUserId()
        if (userId == null) {
            android.util.Log.e("FitTrackRepo", "❌ ABORTING: No user ID available")
            throw Exception("User ID not found")
        }

        android.util.Log.d("FitTrackRepo", "✅ Auth OK - UserID: $userId")
        android.util.Log.d("FitTrackRepo", "📤 Making API call to: /api/fitness/stats/$userId/week")

        // Get stats for the week and convert to DailyStats format
        val response = apiService.getStats(userId, "week", token)

        android.util.Log.d("FitTrackRepo", "📥 Response code: ${response.code()}")
        android.util.Log.d("FitTrackRepo", "Response message: ${response.message()}")

        if (response.isSuccessful) {
            val body = response.body()
            android.util.Log.d("FitTrackRepo", "Success! Response body: $body")
            android.util.Log.d("FitTrackRepo", "Data count: ${body?.data?.size ?: 0}")
            val statsResponse = response.body()
            val dailyStatsList = statsResponse?.data?.map { fitness ->
                DailyStats(
                    date = fitness.date ?: getCurrentDate(),
                    steps = fitness.steps,
                    calories = fitness.calories,
                    distance = fitness.distance,
                    activeMinutes = fitness.activeMinutes
                )
            } ?: emptyList()
            return Response.success(dailyStatsList.take(limit))
        } else {
            // Log error details
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("FitTrackRepo", "getDailyStats ERROR ${response.code()}: $errorBody")

            // Return empty list instead of crashing on 404
            if (response.code() == 404) {
                android.util.Log.w("FitTrackRepo", "Backend route not found - returning empty stats")
                return Response.success(emptyList())
            }
            return Response.error(response.code(), response.errorBody()!!)
        }
    }

    suspend fun getTodayStats(): Response<DailyStats> {
        val token = getAuthToken() ?: throw Exception("User not authenticated")
        val userId = getUserId() ?: throw Exception("User ID not found")

        val response = apiService.getTodayFitness(userId, token)

        android.util.Log.d("FitTrackRepo", "getTodayStats - URL: /api/fitness/today/$userId")
        android.util.Log.d("FitTrackRepo", "getTodayStats - Response code: ${response.code()}")

        if (response.isSuccessful) {
            val fitness = response.body()!!
            val dailyStats = DailyStats(
                date = fitness.date ?: getCurrentDate(),
                steps = fitness.steps,
                calories = fitness.calories,
                distance = fitness.distance,
                activeMinutes = fitness.activeMinutes
            )
            return Response.success(dailyStats)
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("FitTrackRepo", "getTodayStats ERROR ${response.code()}: $errorBody")

            // Return empty stats on 404
            if (response.code() == 404) {
                val emptyStats = DailyStats(
                    date = getCurrentDate(),
                    steps = 0,
                    calories = 0,
                    distance = 0f,
                    activeMinutes = 0
                )
                return Response.success(emptyStats)
            }
            return Response.error(response.code(), response.errorBody()!!)
        }
    }

    suspend fun logDailyStats(stats: DailyStats): Response<DailyStats> {
        android.util.Log.d("FitTrackRepo", "=== logDailyStats START ===")

        val token = getAuthToken()
        if (token == null) {
            android.util.Log.e("FitTrackRepo", "❌ CRITICAL: User not authenticated - no Firebase token available")
            android.util.Log.e("FitTrackRepo", "Current user: ${auth.currentUser?.email ?: "NULL"}")
            android.util.Log.e("FitTrackRepo", "UID: ${auth.currentUser?.uid ?: "NULL"}")
            throw Exception("User not authenticated")
        }

        val userId = getUserId()
        if (userId == null) {
            android.util.Log.e("FitTrackRepo", "❌ CRITICAL: User ID not found")
            throw Exception("User ID not found")
        }

        android.util.Log.d("FitTrackRepo", "✅ Auth OK - User: ${auth.currentUser?.email}")
        android.util.Log.d("FitTrackRepo", "✅ Token: ${token.take(20)}...")
        android.util.Log.d("FitTrackRepo", "✅ UserID: $userId")

        val logRequest = LogFitnessRequest(
            userId = userId,
            date = stats.date,
            steps = stats.steps,
            calories = stats.calories,
            distance = stats.distance,
            activeMinutes = stats.activeMinutes
        )

        android.util.Log.d("FitTrackRepo", "📤 Sending request to /api/fitness/log")
        android.util.Log.d("FitTrackRepo", "📊 Data: steps=${stats.steps}, calories=${stats.calories}, distance=${stats.distance}km, date=${stats.date}")

        val response = apiService.logFitness(token, logRequest)

        android.util.Log.d("FitTrackRepo", "📥 Response code: ${response.code()}")

        if (response.isSuccessful) {
            android.util.Log.d("FitTrackRepo", "✅ SUCCESS! Data synced to backend")
            val fitness = response.body()!!
            val dailyStats = DailyStats(
                date = fitness.date ?: getCurrentDate(),
                steps = fitness.steps,
                calories = fitness.calories,
                distance = fitness.distance,
                activeMinutes = fitness.activeMinutes
            )
            return Response.success(dailyStats)
        } else {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("FitTrackRepo", "❌ FAILED: ${response.code()} - ${response.message()}")
            android.util.Log.e("FitTrackRepo", "❌ Error body: $errorBody")
        }
        return Response.error(response.code(), response.errorBody()!!)
    }

    suspend fun getFitnessSummary(): Response<SummaryResponse> {
        val token = getAuthToken() ?: throw Exception("User not authenticated")
        val userId = getUserId() ?: throw Exception("User ID not found")
        return apiService.getFitnessSummary(userId, token)
    }

    // ==================== Blockchain Service Methods (via /api/blockchain) ====================

    suspend fun getRewards(userAddress: String): Response<RewardsResponse> {
        val token = getAuthToken() ?: throw Exception("User not authenticated")
        return apiService.getRewards(userAddress, token)
    }

    suspend fun getTokenBalance(): Response<TokenBalance> {
        val token = getAuthToken() ?: throw Exception("User not authenticated")

        // Get user profile to get wallet address
        val profileResponse = getUserProfile()
        if (profileResponse.isSuccessful) {
            val profile = profileResponse.body()
            // Check if wallet address exists in the profile
            if (profile != null) {
                val walletAddress = profile.walletAddress
                if (walletAddress != null && walletAddress.isNotEmpty()) {
                    val rewardsResponse = apiService.getRewards(walletAddress, token)
                    if (rewardsResponse.isSuccessful) {
                        val rewards = rewardsResponse.body()!!
                        // Convert RewardsResponse to TokenBalance
                        val tokenBalance = TokenBalance(
                            balance = rewards.rewardBalance.toIntOrNull() ?: 0,
                            totalEarned = rewards.rewardBalance.toIntOrNull() ?: 0,
                            transactions = emptyList() // Transaction history not implemented yet
                        )
                        return Response.success(tokenBalance)
                    }
                    return Response.error(rewardsResponse.code(), rewardsResponse.errorBody()!!)
                }
            }
        }
        throw Exception("Unable to fetch wallet address")
    }

    // ==================== Stub Methods for Features Not Yet Implemented ====================
    // These methods return empty data since Workout and Goal services are not running

    fun getWorkouts(): Response<List<Workout>> {
        // Return empty list since workout service is not running
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
        // Return empty list since goal service is not running
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
        // Return empty list since transaction history is not implemented
        return Response.success(emptyList())
    }


    // ==================== Helper Methods ====================

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}

