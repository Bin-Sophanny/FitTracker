package com.example.fittrack.data.model

/**
 * Data class representing user profile information
 * Matches backend user-service model
 */
data class UserProfile(
    val id: String? = null,                // MongoDB _id
    val firebaseUid: String,               // Firebase UID
    val email: String,                     // User email
    val displayName: String? = null,       // User full name
    val photoUrl: String? = null,          // Profile image URL
    val createdAt: String? = null,
    val updatedAt: String? = null
    // REMOVED: age, weight, height, profileImage (not in backend)
)

/**
 * Request body for updating user profile
 */
data class UpdateProfileRequest(
    val displayName: String? = null,
    val photoUrl: String? = null
    // REMOVED: age, weight, height, profileImage, walletAddress (not in backend)
)

