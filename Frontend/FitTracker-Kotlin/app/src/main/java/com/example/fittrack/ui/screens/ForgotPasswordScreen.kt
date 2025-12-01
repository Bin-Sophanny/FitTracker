package com.example.fittrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.auth.AuthState
import com.example.fittrack.auth.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    authState: AuthState,
    onBackToSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isEmailSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Handle auth state changes - success means email was sent
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Idle -> {
                if (errorMessage == null && email.isNotEmpty() && !isEmailSent) {
                    isEmailSent = true
                }
            }
            is AuthState.Error -> {
                errorMessage = authState.message
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF667eea))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 8.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Fitness Center Icon
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = "FitTracker App Icon",
                tint = Color.White,
                modifier = Modifier
                    .size(60.dp)
                    .padding(bottom = 8.dp)
            )

            // App Title
            Text(
                text = "FitTracker",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Reset Your Password",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Reset Password Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isEmailSent) {
                        // Password Reset Request
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Email Reset",
                            tint = Color(0xFF667eea),
                            modifier = Modifier
                                .size(48.dp)
                                .padding(bottom = 16.dp)
                        )

                        Text(
                            text = "Forgot Password?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "Don't worry! Enter your email address below and we'll send you a link to reset your password.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF718096),
                            modifier = Modifier.padding(bottom = 12.dp),
                            lineHeight = 19.sp
                        )

                        // Error message
                        if (errorMessage != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFEE2E2)
                                )
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = Color(0xFFDC2626),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = Color(0xFF667eea)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF667eea),
                                focusedLabelColor = Color(0xFF667eea)
                            )
                        )

                        // Send Reset Button
                        Button(
                            onClick = {
                                errorMessage = null
                                authViewModel.sendPasswordResetEmail(email)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = email.isNotEmpty() && authState !is AuthState.Loading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF667eea)
                            )
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    "Send Reset Link",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Email Sent Confirmation
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Email Sent",
                            tint = Color(0xFF48BB78),
                            modifier = Modifier
                                .size(80.dp)
                                .padding(bottom = 24.dp)
                        )

                        Text(
                            text = "Email Sent!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF48BB78),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "We've sent a password reset link to",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF718096),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = email,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF2D3748),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "Check your inbox and follow the instructions to reset your password.",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF718096),
                            modifier = Modifier.padding(bottom = 32.dp),
                            lineHeight = 20.sp
                        )

                        // Send Another Email Button
                        OutlinedButton(
                            onClick = {
                                isEmailSent = false
                                email = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Send Another Email",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF667eea)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Back to Sign In Link
                    TextButton(onClick = onBackToSignInClick) {
                        Text(
                            "← Back to Sign In",
                            color = Color(0xFF667eea),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

