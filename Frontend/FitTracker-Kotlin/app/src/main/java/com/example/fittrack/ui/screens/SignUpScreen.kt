package com.example.fittrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.auth.AuthState
import com.example.fittrack.auth.AuthViewModel

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    authState: AuthState,
    onSignUpSuccess: () -> Unit,
    onBackToSignInClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Responsive sizing based on screen width
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val isSmallScreen = screenWidth < 360.dp
    val isMediumScreen = screenWidth in 360.dp..599.dp

    val horizontalPadding: Dp = when {
        isSmallScreen -> 12.dp
        isMediumScreen -> 16.dp
        else -> 24.dp
    }

    val iconSize: Dp = when {
        isSmallScreen -> 48.dp
        isMediumScreen -> 60.dp
        else -> 72.dp
    }

    val titleSize: TextUnit = when {
        isSmallScreen -> 24.sp
        isMediumScreen -> 32.sp
        else -> 36.sp
    }

    val subtitleSize: TextUnit = when {
        isSmallScreen -> 12.sp
        isMediumScreen -> 14.sp
        else -> 16.sp
    }

    val headingSize: TextUnit = when {
        isSmallScreen -> 18.sp
        isMediumScreen -> 22.sp
        else -> 24.sp
    }

    val bodySize: TextUnit = when {
        isSmallScreen -> 13.sp
        isMediumScreen -> 14.sp
        else -> 16.sp
    }

    val cardPadding: Dp = when {
        isSmallScreen -> 16.dp
        isMediumScreen -> 20.dp
        else -> 24.dp
    }

    val spacing: Dp = when {
        isSmallScreen -> 8.dp
        isMediumScreen -> 12.dp
        else -> 16.dp
    }

    val buttonHeight: Dp = when {
        isSmallScreen -> 48.dp
        isMediumScreen -> 50.dp
        else -> 56.dp
    }

    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onSignUpSuccess()
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding)
                .padding(top = spacing, bottom = spacing)
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
                    .size(iconSize)
                    .padding(bottom = spacing)
            )

            // App Title
            Text(
                text = "FitTracker",
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = spacing / 2)
            )

            Text(
                text = "Start Your Fitness Journey",
                fontSize = subtitleSize,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = spacing * 2)
            )

            // Sign Up Card
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
                    modifier = Modifier.padding(cardPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        fontSize = headingSize,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748),
                        modifier = Modifier.padding(bottom = spacing)
                    )

                    // Error message
                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = spacing),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEE2E2)
                            )
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFDC2626),
                                fontSize = bodySize,
                                modifier = Modifier.padding(spacing)
                            )
                        }
                    }

                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name", fontSize = bodySize) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Name",
                                tint = Color(0xFF667eea)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedLabelColor = Color(0xFF667eea)
                        )
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address", fontSize = bodySize) },
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
                            .padding(bottom = spacing),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedLabelColor = Color(0xFF667eea)
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", fontSize = bodySize) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = Color(0xFF667eea)
                            )
                        },
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(
                                    if (passwordVisible) "Hide" else "Show",
                                    color = Color(0xFF667eea),
                                    fontSize = subtitleSize
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            focusedLabelColor = Color(0xFF667eea)
                        )
                    )

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password", fontSize = bodySize) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Confirm Password",
                                tint = Color(0xFF667eea)
                            )
                        },
                        trailingIcon = {
                            TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Text(
                                    if (confirmPasswordVisible) "Hide" else "Show",
                                    color = Color(0xFF667eea),
                                    fontSize = subtitleSize
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing / 2),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword)
                                Color.Red else Color(0xFF667eea),
                            focusedLabelColor = if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword)
                                Color.Red else Color(0xFF667eea)
                        ),
                        isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword
                    )

                    // Error message for password mismatch
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(spacing * 2)
                    ) {
                        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                            Text(
                                text = "Passwords don't match",
                                color = Color.Red,
                                fontSize = subtitleSize,
                                modifier = Modifier.padding(start = spacing)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing))

                    // Sign Up Button
                    Button(
                        onClick = {
                            errorMessage = null
                            authViewModel.signUp(name, email, password, context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonHeight),
                        enabled = name.isNotEmpty() && email.isNotEmpty() &&
                                 password.isNotEmpty() && password == confirmPassword && authState !is AuthState.Loading,
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
                                "Create Account",
                                fontSize = headingSize * 0.8f,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing * 1.5f))

                    // Sign In Link
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Already have an account? ",
                            color = Color(0xFF718096),
                            fontSize = bodySize
                        )
                        TextButton(onClick = onBackToSignInClick) {
                            Text(
                                "Sign In",
                                color = Color(0xFF667eea),
                                fontWeight = FontWeight.Bold,
                                fontSize = bodySize
                            )
                        }
                    }
                }
            }
        }
    }
}

