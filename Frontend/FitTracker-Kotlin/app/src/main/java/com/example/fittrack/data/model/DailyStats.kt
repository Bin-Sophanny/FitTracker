package com.example.fittrack.data.model

/**
 * Data class representing daily fitness statistics
 * Matches backend fitness-service model
 */
data class DailyStats(
    val userId: String,         // User ID - ADDED to match backend
    val date: String,           // Format: "yyyy-MM-dd"
    val steps: Int,             // Number of steps taken
    val calories: Int,          // Calories burned
    val distance: Float,        // Distance in kilometers
    val activeMinutes: Int,     // Active minutes
    val heartRate: Int? = null  // Heart rate - ADDED to match backend
)

