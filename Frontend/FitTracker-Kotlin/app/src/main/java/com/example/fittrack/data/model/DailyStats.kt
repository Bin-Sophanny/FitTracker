package com.example.fittrack.data.model

/**
 * Data class representing daily fitness statistics
 * Matches backend fitness-service fitnessDataSchema exactly
 */
data class DailyStats(
    val userId: String,             // User ID (indexed in backend)
    val date: String,               // ISO string format to preserve GMT+7 timezone (indexed in backend)
    val steps: Int = 0,             // Number of steps taken
    val calories: Int = 0,          // Calories burned
    val distance: Float = 0f,       // Distance in kilometers (2 decimal places in backend)
    val activeMinutes: Int = 0,     // Active minutes
    val createdAt: String? = null,  // ISO timestamp from backend
    val updatedAt: String? = null   // ISO timestamp from backend
)
