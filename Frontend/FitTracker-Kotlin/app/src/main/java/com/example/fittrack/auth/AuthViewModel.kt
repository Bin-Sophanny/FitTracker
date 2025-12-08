package com.example.fittrack.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Patterns
import android.util.Log
import com.example.fittrack.data.repository.FitTrackRepository
import com.example.fittrack.data.model.LoginRequest
import com.example.fittrack.data.model.RegisterRequest

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: com.google.firebase.auth.FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val backendRepository = FitTrackRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _jwtToken = MutableStateFlow<String?>(null)
    val jwtToken: StateFlow<String?> = _jwtToken.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val currentUser = authRepository.currentUser
        _isAuthenticated.value = currentUser != null
        if (currentUser != null) {
            _authState.value = AuthState.Success(currentUser)
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email address format"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            name.length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
    }

    fun signIn(email: String, password: String, context: Context) {
        // Validate email
        val emailError = validateEmail(email)
        if (emailError != null) {
            _authState.value = AuthState.Error("Email: $emailError")
            return
        }

        // Validate password
        val passwordError = validatePassword(password)
        if (passwordError != null) {
            _authState.value = AuthState.Error("Password: $passwordError")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // Step 1: Firebase Authentication
            val result = authRepository.signIn(email, password)
            result.fold(
                onSuccess = { user ->
                    Log.d("AuthViewModel", "✅ Firebase sign-in successful: ${user.email}")

                    // Step 2: Call Backend Login API to get JWT token
                    try {
                        val loginRequest = LoginRequest(
                            firebaseUid = user.uid,
                            email = user.email ?: email
                        )

                        val backendResponse = backendRepository.loginUser(loginRequest)

                        if (backendResponse.isSuccessful) {
                            val authResponse = backendResponse.body()
                            if (authResponse?.success == true) {
                                // Save JWT token
                                val jwtToken = authResponse.token
                                TokenManager.saveToken(context, jwtToken, user.uid)
                                _jwtToken.value = jwtToken

                                Log.d("AuthViewModel", "✅ Backend login successful")
                                Log.d("AuthViewModel", "🔑 JWT Token: ${jwtToken.take(20)}...")
                                Log.d("AuthViewModel", "👤 User ID (Firebase UID): ${user.uid}")

                                _authState.value = AuthState.Success(user)
                                _isAuthenticated.value = true
                            } else {
                                _authState.value = AuthState.Error("Backend login failed")
                            }
                        } else {
                            _authState.value = AuthState.Error("Backend login failed: ${backendResponse.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "❌ Backend login error: ${e.message}")
                        _authState.value = AuthState.Error("Backend connection failed: ${e.message}")
                    }
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(
                        exception.message ?: "Sign in failed"
                    )
                }
            )
        }
    }

    fun signUp(name: String, email: String, password: String, context: Context) {
        // Validate name
        val nameError = validateName(name)
        if (nameError != null) {
            _authState.value = AuthState.Error("Name: $nameError")
            return
        }

        // Validate email
        val emailError = validateEmail(email)
        if (emailError != null) {
            _authState.value = AuthState.Error("Email: $emailError")
            return
        }

        // Validate password
        val passwordError = validatePassword(password)
        if (passwordError != null) {
            _authState.value = AuthState.Error("Password: $passwordError")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // Step 1: Firebase Registration
            val result = authRepository.signUp(name, email, password)
            result.fold(
                onSuccess = { user ->
                    Log.d("AuthViewModel", "✅ Firebase sign-up successful: ${user.email}")

                    // Step 2: Call Backend Register API to get JWT token
                    try {
                        val registerRequest = RegisterRequest(
                            firebaseUid = user.uid,
                            email = user.email ?: email,
                            displayName = name
                        )

                        val backendResponse = backendRepository.registerUser(registerRequest)

                        if (backendResponse.isSuccessful) {
                            val authResponse = backendResponse.body()
                            if (authResponse?.success == true) {
                                // Save JWT token
                                val jwtToken = authResponse.token
                                TokenManager.saveToken(context, jwtToken, user.uid)
                                _jwtToken.value = jwtToken

                                Log.d("AuthViewModel", "✅ Backend registration successful")
                                Log.d("AuthViewModel", "🔑 JWT Token: ${jwtToken.take(20)}...")
                                Log.d("AuthViewModel", "👤 User ID (Firebase UID): ${user.uid}")

                                _authState.value = AuthState.Success(user)
                                _isAuthenticated.value = true
                            } else {
                                _authState.value = AuthState.Error("Backend registration failed")
                            }
                        } else {
                            _authState.value = AuthState.Error("Backend registration failed: ${backendResponse.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "❌ Backend registration error: ${e.message}")
                        _authState.value = AuthState.Error("Backend connection failed: ${e.message}")
                    }
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(
                        exception.message ?: "Sign up failed"
                    )
                }
            )
        }
    }

    fun resetPassword(email: String) {
        // Validate email
        val emailError = validateEmail(email)
        if (emailError != null) {
            _authState.value = AuthState.Error("Email: $emailError")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = {
                    // Password reset email sent successfully
                    // Return to idle state so user can go back to login
                    _authState.value = AuthState.Idle
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(
                        exception.message ?: "Failed to send reset email"
                    )
                }
            )
        }
    }

    fun signOut(context: Context) {
        authRepository.signOut()
        TokenManager.clearToken(context)
        _jwtToken.value = null
        _isAuthenticated.value = false
        _authState.value = AuthState.Idle
        Log.d("AuthViewModel", "✅ Signed out and cleared JWT token")
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

