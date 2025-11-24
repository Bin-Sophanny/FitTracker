package com.example.fittrack.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrack.auth.AuthState
import com.example.fittrack.auth.AuthViewModel
import com.example.fittrack.ui.screens.*

enum class Screen {
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD,
    HOME
}

@Composable
fun FitTrackNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    var currentScreen by remember { mutableStateOf(Screen.SIGN_IN) }
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }

    // Navigate to home when authenticated and update user info
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated && authState is AuthState.Success) {
            val user = (authState as AuthState.Success).user
            userName = user.displayName ?: user.email?.substringBefore("@") ?: "User"
            userEmail = user.email ?: ""
            currentScreen = Screen.HOME
        }
    }

    when (currentScreen) {
        Screen.SIGN_IN -> {
            SignInScreen(
                authViewModel = authViewModel,
                authState = authState,
                onSignInSuccess = {
                    currentScreen = Screen.HOME
                },
                onSignUpClick = {
                    authViewModel.resetAuthState()
                    currentScreen = Screen.SIGN_UP
                },
                onForgotPasswordClick = {
                    authViewModel.resetAuthState()
                    currentScreen = Screen.FORGOT_PASSWORD
                }
            )
        }

        Screen.SIGN_UP -> {
            SignUpScreen(
                authViewModel = authViewModel,
                authState = authState,
                onSignUpSuccess = {
                    currentScreen = Screen.HOME
                },
                onBackToSignInClick = {
                    authViewModel.resetAuthState()
                    currentScreen = Screen.SIGN_IN
                }
            )
        }

        Screen.FORGOT_PASSWORD -> {
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                authState = authState,
                onBackToSignInClick = {
                    authViewModel.resetAuthState()
                    currentScreen = Screen.SIGN_IN
                }
            )
        }

        Screen.HOME -> {
            HomeScreen(
                userName = userName,
                userEmail = userEmail,
                onLogoutClick = {
                    authViewModel.signOut()
                    currentScreen = Screen.SIGN_IN
                }
            )
        }
    }
}

