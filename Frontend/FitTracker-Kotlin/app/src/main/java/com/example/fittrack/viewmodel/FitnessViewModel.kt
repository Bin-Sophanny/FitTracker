package com.example.fittrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.api.ApiResult
import com.example.fittrack.data.api.safeApiCall
import com.example.fittrack.data.model.*
import com.example.fittrack.data.repository.FitTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing fitness data from the backend API
 * Use this in your screens to fetch and manage data
 */
class FitnessViewModel(
    private val repository: FitTrackRepository = FitTrackRepository()
) : ViewModel() {

    // Daily Stats State
    private val _dailyStatsState = MutableStateFlow<ApiResult<List<DailyStats>>>(ApiResult.Loading)
    val dailyStatsState: StateFlow<ApiResult<List<DailyStats>>> = _dailyStatsState.asStateFlow()

    // User Profile State (using API UserProfile type)
    private val _userProfileState = MutableStateFlow<ApiResult<com.example.fittrack.api.UserProfile>>(ApiResult.Loading)
    val userProfileState: StateFlow<ApiResult<com.example.fittrack.api.UserProfile>> = _userProfileState.asStateFlow()

    // Workouts State
    private val _workoutsState = MutableStateFlow<ApiResult<List<Workout>>>(ApiResult.Loading)
    val workoutsState: StateFlow<ApiResult<List<Workout>>> = _workoutsState.asStateFlow()

    // Goals State
    private val _goalsState = MutableStateFlow<ApiResult<List<Goal>>>(ApiResult.Loading)
    val goalsState: StateFlow<ApiResult<List<Goal>>> = _goalsState.asStateFlow()

    // Token Balance State
    private val _tokenBalanceState = MutableStateFlow<ApiResult<TokenBalance>>(ApiResult.Loading)
    val tokenBalanceState: StateFlow<ApiResult<TokenBalance>> = _tokenBalanceState.asStateFlow()

    /**
     * Fetch daily stats from backend
     */
    fun getDailyStats(limit: Int = 5) {
        viewModelScope.launch {
            android.util.Log.d("FitnessViewModel", "")
            android.util.Log.d("FitnessViewModel", "🔄🔄🔄 getDailyStats() STARTING 🔄🔄🔄")
            android.util.Log.d("FitnessViewModel", "Limit: $limit")

            _dailyStatsState.value = ApiResult.Loading
            android.util.Log.d("FitnessViewModel", "State set to: Loading")

            val result = safeApiCall {
                repository.getDailyStats(limit)
            }

            _dailyStatsState.value = result

            android.util.Log.d("FitnessViewModel", "")
            android.util.Log.d("FitnessViewModel", "📊 Final Result:")
            when (result) {
                is ApiResult.Success -> {
                    android.util.Log.d("FitnessViewModel", "✅ SUCCESS!")
                    android.util.Log.d("FitnessViewModel", "Data size: ${result.data.size}")
                    result.data.forEachIndexed { index, stats ->
                        android.util.Log.d("FitnessViewModel", "[$index] ${stats.date}: ${stats.steps} steps")
                    }
                }
                is ApiResult.Error -> {
                    android.util.Log.e("FitnessViewModel", "❌ ERROR: ${result.message}")
                }
                is ApiResult.Loading -> {
                    android.util.Log.d("FitnessViewModel", "⏳ Still loading...")
                }
            }
            android.util.Log.d("FitnessViewModel", "🔄🔄🔄 getDailyStats() COMPLETED 🔄🔄🔄")
            android.util.Log.d("FitnessViewModel", "")
        }
    }

    /**
     * Log new daily stats
     */
    fun logDailyStats(stats: DailyStats, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = safeApiCall {
                repository.logDailyStats(stats)
            }
            if (result is ApiResult.Success) {
                onSuccess()
                getDailyStats() // Refresh data
            }
        }
    }

    /**
     * Sync steps to backend - for manual sync
     */
    fun syncStepsToBackend(stats: DailyStats) {
        viewModelScope.launch {
            android.util.Log.d("FitnessViewModel", "🔄 Syncing stats to backend...")
            val result = safeApiCall {
                repository.logDailyStats(stats)
            }
            when (result) {
                is ApiResult.Success -> {
                    android.util.Log.d("FitnessViewModel", "✅ Sync successful!")
                }
                is ApiResult.Error -> {
                    android.util.Log.e("FitnessViewModel", "❌ Sync failed: ${result.message}")
                }
                else -> {}
            }
        }
    }

    /**
     * Fetch user profile
     */
    fun getUserProfile() {
        viewModelScope.launch {
            _userProfileState.value = ApiResult.Loading
            _userProfileState.value = safeApiCall {
                repository.getUserProfile()
            }
        }
    }

    /**
     * Update user profile
     */
    fun updateProfile(profile: UpdateProfileRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = safeApiCall {
                repository.updateProfile(profile)
            }
            if (result is ApiResult.Success) {
                onSuccess()
                getUserProfile() // Refresh profile
            }
        }
    }

    /**
     * Fetch all workouts
     */
    fun getWorkouts() {
        viewModelScope.launch {
            _workoutsState.value = ApiResult.Loading
            _workoutsState.value = safeApiCall {
                repository.getWorkouts()
            }
        }
    }

    /**
     * Log a new workout
     */
    fun logWorkout(workout: CreateWorkoutRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = safeApiCall {
                repository.logWorkout(workout)
            }
            if (result is ApiResult.Success) {
                onSuccess()
                getWorkouts() // Refresh workouts
            }
        }
    }

    /**
     * Fetch all goals
     */
    fun getGoals() {
        viewModelScope.launch {
            _goalsState.value = ApiResult.Loading
            _goalsState.value = safeApiCall {
                repository.getGoals()
            }
        }
    }

    /**
     * Create a new goal
     */
    fun createGoal(goal: CreateGoalRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = safeApiCall {
                repository.createGoal(goal)
            }
            if (result is ApiResult.Success) {
                onSuccess()
                getGoals() // Refresh goals
            }
        }
    }

    /**
     * Update goal progress
     */
    fun updateGoal(goalId: String, goal: UpdateGoalRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = safeApiCall {
                repository.updateGoal(goalId, goal)
            }
            if (result is ApiResult.Success) {
                onSuccess()
                getGoals() // Refresh goals
            }
        }
    }

    /**
     * Get token balance
     */
    fun getTokenBalance() {
        viewModelScope.launch {
            _tokenBalanceState.value = ApiResult.Loading
            _tokenBalanceState.value = safeApiCall {
                repository.getTokenBalance()
            }
        }
    }
}

