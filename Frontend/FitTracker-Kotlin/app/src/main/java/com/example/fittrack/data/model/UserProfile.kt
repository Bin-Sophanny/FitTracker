package com.example.fittrack.data.model

/**
 * Data class representing user profile information
 * Matches backend user-service model
 */
data class UserProfile(
    val uid: String,                // Firebase UID
    val email: String,              // User email
    val displayName: String,        // User full name
    val age: Int? = null,           // User age (optional)
    val weight: Float? = null,      // Weight in kg (optional)
    val height: Float? = null,      // Height in cm (optional)
    val profileImage: String? = null // Profile image URL (optional)
)

/**
 * Request body for updating user profile
 */
data class UpdateProfileRequest(
    val displayName: String? = null,
    val age: Int? = null,
    val weight: Float? = null,
    val height: Float? = null,
    val profileImage: String? = null,
    val photoUrl: String? = null,        // For API compatibility
    val walletAddress: String? = null    // For blockchain wallet linking
)

