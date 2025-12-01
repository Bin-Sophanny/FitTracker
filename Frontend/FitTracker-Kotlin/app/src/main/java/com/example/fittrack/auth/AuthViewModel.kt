package com.example.fittrack.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Patterns
import com.example.fittrack.data.repository.FitTrackRepository
import android.util.Log

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

    fun signIn(email: String, password: String) {
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
            val result = authRepository.signIn(email, password)
            result.fold(
                onSuccess = { user ->
                    // Firebase sign-in successful, now register/login to backend
                    try {
                        val backendResponse = backendRepository.loginUser(user.email ?: email)
                        if (backendResponse.isSuccessful) {
                            Log.d("AuthViewModel", "Backend login successful")
                            _authState.value = AuthState.Success(user)
                            _isAuthenticated.value = true
                        } else {
                            Log.e("AuthViewModel", "Backend login failed: ${backendResponse.code()}")
                            // Still allow user to proceed if backend fails
                            _authState.value = AuthState.Success(user)
                            _isAuthenticated.value = true
                        }
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Backend login error: ${e.message}")
                        // Still allow user to proceed if backend fails
                        _authState.value = AuthState.Success(user)
                        _isAuthenticated.value = true
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

    fun signUp(name: String, email: String, password: String) {
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
            val result = authRepository.signUp(name, email, password)
            result.fold(
                onSuccess = { user ->
                    // Firebase sign-up successful, now register to backend
                    try {
                        val backendResponse = backendRepository.registerUser(
                            displayName = name,
                            email = user.email ?: email
                        )
                        if (backendResponse.isSuccessful) {
                            Log.d("AuthViewModel", "Backend registration successful")
                            _authState.value = AuthState.Success(user)
                            _isAuthenticated.value = true
                        } else {
                            Log.e("AuthViewModel", "Backend registration failed: ${backendResponse.code()}")
                            // Still allow user to proceed if backend fails
                            _authState.value = AuthState.Success(user)
                            _isAuthenticated.value = true
                        }
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Backend registration error: ${e.message}")
                        // Still allow user to proceed if backend fails
                        _authState.value = AuthState.Success(user)
                        _isAuthenticated.value = true
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

    fun sendPasswordResetEmail(email: String) {
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
                    _authState.value = AuthState.Success(authRepository.currentUser!!)
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(
                        exception.message ?: "Failed to send reset email"
                    )
                }
            )
        }
    }

    fun signOut() {
        authRepository.signOut()
        _isAuthenticated.value = false
        _authState.value = AuthState.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

