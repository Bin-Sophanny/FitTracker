package com.example.fittrack.data.model

/**
 * Data class representing a fitness goal
 * Matches backend goal-service model
 */
data class Goal(
    val id: String,                 // Unique goal ID
    val title: String,              // Goal title (e.g., "Walk 10000 steps daily")
    val type: GoalType,             // Type of goal
    val targetValue: Int,           // Target value to achieve
    val currentValue: Int,          // Current progress
    val deadline: String,           // Deadline date in format "yyyy-MM-dd"
    val isCompleted: Boolean        // Whether goal is completed
)

/**
 * Request body for creating a new goal
 */
data class CreateGoalRequest(
    val title: String,
    val type: String,
    val targetValue: Int,
    val deadline: String
)

/**
 * Request body for updating goal progress
 */
data class UpdateGoalRequest(
    val currentValue: Int,
    val isCompleted: Boolean
)

/**
 * Enum class for goal types
 */
enum class GoalType(val displayName: String) {
    STEPS("Steps"),
    CALORIES("Calories"),
    DISTANCE("Distance"),
    WEIGHT("Weight Loss"),
    WORKOUT_COUNT("Workout Count"),
    ACTIVE_MINUTES("Active Minutes")
}

