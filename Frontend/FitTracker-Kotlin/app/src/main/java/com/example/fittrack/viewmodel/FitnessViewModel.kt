package com.example.fittrack.viewmodel

import android.content.Context
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
 */
class FitnessViewModel(
    private val context: Context,
    private val repository: FitTrackRepository = FitTrackRepository()
) : ViewModel() {

    // Daily Stats State
    private val _dailyStatsState = MutableStateFlow<ApiResult<List<DailyStats>>>(ApiResult.Loading)
    val dailyStatsState: StateFlow<ApiResult<List<DailyStats>>> = _dailyStatsState.asStateFlow()

    // User Profile State
    private val _userProfileState = MutableStateFlow<ApiResult<UserProfile>>(ApiResult.Loading)
    val userProfileState: StateFlow<ApiResult<UserProfile>> = _userProfileState.asStateFlow()

    // Workouts State
    private val _workoutsState = MutableStateFlow<ApiResult<List<Workout>>>(ApiResult.Loading)
    val workoutsState: StateFlow<ApiResult<List<Workout>>> = _workoutsState.asStateFlow()

    // Goals State
    private val _goalsState = MutableStateFlow<ApiResult<List<Goal>>>(ApiResult.Loading)
    val goalsState: StateFlow<ApiResult<List<Goal>>> = _goalsState.asStateFlow()

    /**
     * Fetch daily stats from backend
     */
    fun getDailyStats(limit: Int = 5) {
        viewModelScope.launch {
            _dailyStatsState.value = ApiResult.Loading
            val result = safeApiCall {
                repository.getDailyStats(context, limit)
            }
            _dailyStatsState.value = result
        }
    }

    /**
     * Log new daily stats
     */
    fun logDailyStats(stats: DailyStats, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = safeApiCall {
                repository.logDailyStats(context, stats)
            }
            if (result is ApiResult.Success) {
                onSuccess()
                getDailyStats()
            }
        }
    }

    /**
     * Sync steps to backend
     */
    fun syncStepsToBackend(stats: DailyStats) {
        viewModelScope.launch {
            val result = safeApiCall {
                repository.logDailyStats(context, stats)
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
                repository.getUserProfile(context)
            }
        }
    }

    /**
     * Update user profile
     */
    fun updateProfile(profile: UpdateProfileRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = safeApiCall {
                repository.updateProfile(context, profile)
            }
            if (result is ApiResult.Success) {
                onSuccess()
                getUserProfile()
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
                getWorkouts()
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
                getGoals()
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
                getGoals()
            }
        }
    }
}

