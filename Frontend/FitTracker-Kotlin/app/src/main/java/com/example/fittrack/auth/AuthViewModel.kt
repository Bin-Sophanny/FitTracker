package com.example.fittrack.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: com.google.firebase.auth.FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

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

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signIn(email, password)
            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.Success(user)
                    _isAuthenticated.value = true
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(
                        exception.message ?: "Authentication failed"
                    )
                }
            )
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signUp(name, email, password)
            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.Success(user)
                    _isAuthenticated.value = true
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
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = {
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

    fun signOut() {
        authRepository.signOut()
        _isAuthenticated.value = false
        _authState.value = AuthState.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

