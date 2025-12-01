package com.example.fittrack.data.model

/**
 * Data class representing daily fitness statistics
 * Matches backend stats-service model
 */
data class DailyStats(
    val date: String,           // Format: "yyyy-MM-dd"
    val steps: Int,             // Number of steps taken
    val calories: Int,          // Calories burned
    val distance: Float,        // Distance in kilometers
    val activeMinutes: Int      // Active minutes
)

