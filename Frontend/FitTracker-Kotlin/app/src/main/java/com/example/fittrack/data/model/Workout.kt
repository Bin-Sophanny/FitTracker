package com.example.fittrack.data.model

/**
 * Data class representing a workout/exercise
 * Matches backend workout-service model
 */
data class Workout(
    val id: String,                 // Unique workout ID
    val name: String,               // Exercise name (e.g., "Running", "Push-ups")
    val category: String,           // Category (e.g., "Cardio", "Strength", "Flexibility")
    val duration: Int,              // Duration in minutes
    val calories: Int,              // Calories burned
    val date: String                // Date in format "yyyy-MM-dd"
)

/**
 * Request body for creating a new workout
 */
data class CreateWorkoutRequest(
    val name: String,
    val category: String,
    val duration: Int,
    val calories: Int,
    val date: String
)

/**
 * Enum class for workout categories
 */
enum class WorkoutCategory(val displayName: String) {
    CARDIO("Cardio"),
    STRENGTH("Strength"),
    FLEXIBILITY("Flexibility"),
    SPORTS("Sports"),
    YOGA("Yoga"),
    OTHER("Other")
}

