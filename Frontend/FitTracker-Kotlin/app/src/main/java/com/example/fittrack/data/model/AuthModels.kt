package com.example.fittrack.data.model

/**
 * Auth-related request and response models
 * These match the backend API routes exactly
 */

// ==================== Auth Requests ====================

data class RegisterRequest(
    val firebaseUid: String,
    val email: String,
    val displayName: String
)

data class LoginRequest(
    val firebaseUid: String,
    val email: String
)

// ==================== Auth Response ====================

data class AuthResponse(
    val success: Boolean,
    val user: UserProfile,
    val token: String
)

// ==================== Response Models ====================

data class RewardsResponse(
    val userAddress: String,
    val rewardBalance: String,
    val nftCount: Int,
    val network: String
)

data class SummaryResponse(
    val totalEntries: Int,
    val totalSteps: Int,
    val totalCalories: Int,
    val totalDistance: Float,
    val totalActiveMinutes: Int,
    val lastUpdate: String?
)

