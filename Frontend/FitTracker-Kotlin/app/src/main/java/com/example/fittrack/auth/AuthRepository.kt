package com.example.fittrack.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: FirebaseAuthException) {
            val errorMessage = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email address format"
                "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                "ERROR_USER_DISABLED" -> "This account has been disabled"
                "ERROR_TOO_MANY_REQUESTS" -> "Too many failed attempts. Please try again later"
                "ERROR_OPERATION_NOT_ALLOWED" -> "Email/password sign-in is disabled"
                else -> "Login failed: ${e.message ?: "Unknown error"}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(Exception("Login failed: ${e.message ?: "Unknown error"}"))
        }
    }

    suspend fun signUp(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!

            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()

            Result.success(user)
        } catch (e: FirebaseAuthException) {
            val errorMessage = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email address format"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered"
                "ERROR_WEAK_PASSWORD" -> "Password is too weak. Use at least 6 characters"
                "ERROR_OPERATION_NOT_ALLOWED" -> "Email/password sign-up is disabled"
                else -> "Sign up failed: ${e.message ?: "Unknown error"}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(Exception("Sign up failed: ${e.message ?: "Unknown error"}"))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            val errorMessage = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email address format"
                "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                else -> "Failed to send reset email: ${e.message ?: "Unknown error"}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to send reset email: ${e.message ?: "Unknown error"}"))
        }
    }

    fun signOut() {
        auth.signOut()
    }
}

