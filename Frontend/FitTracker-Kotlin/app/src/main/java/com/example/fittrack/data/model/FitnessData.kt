package com.example.fittrack.data.model

/**
 * Fitness data model matching the backend schema
 */
data class FitnessData(
    val _id: String? = null,
    val userId: String,
    val date: String,
    val steps: Int = 0,
    val calories: Int = 0,
    val distance: Float = 0f,
    val activeMinutes: Int = 0,
    val heartRate: Int? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Request body for logging fitness data
 */
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

/**
 * Response from logging fitness data
 */
data class FitnessLogResponse(
    val success: Boolean,
    val data: FitnessData
)

/**
 * Response from getting fitness stats
 */
data class FitnessStatsResponse(
    val period: String,
    val totalSteps: Int,
    val totalCalories: Int,
    val totalDistance: Float,
    val totalActiveMinutes: Int,
    val averageSteps: Int,
    val averageCalories: Int,
    val data: List<FitnessData>
)

