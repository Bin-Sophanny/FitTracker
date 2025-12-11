package com.example.fittrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fittrack.auth.AuthViewModel
import com.example.fittrack.ui.theme.LocalThemeManager
import com.example.fittrack.ui.theme.getAppColors
import com.example.fittrack.ui.theme.ResponsiveDimens
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun EditProfileScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit
) {
    val themeManager = LocalThemeManager.current
    val colors = getAppColors(themeManager.isDarkMode)

    val authState by authViewModel.authState.collectAsState()
    val currentUser = if (authState is com.example.fittrack.auth.AuthState.Success) {
        (authState as com.example.fittrack.auth.AuthState.Success).user
    } else null

    var name by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ResponsiveDimens.horizontalPadding()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(ResponsiveDimens.iconSizeMedium())
                )
            }
            Text(
                text = "Edit Profile",
                fontSize = ResponsiveDimens.textSizeTitle(),
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.padding(start = ResponsiveDimens.spacingSmall())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ResponsiveDimens.horizontalPadding())
                .padding(bottom = ResponsiveDimens.spacingMedium()),
            verticalArrangement = Arrangement.spacedBy(ResponsiveDimens.spacingMedium())
        ) {
            // Profile Picture
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveDimens.cardElevation())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ResponsiveDimens.cardPadding()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(ResponsiveDimens.avatarSizeMedium())
                            .background(colors.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.take(1).uppercase().ifEmpty { "U" },
                            fontSize = ResponsiveDimens.textSizeHeading(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(ResponsiveDimens.spacingSmall()))
                    Text(
                        text = "Profile Picture",
                        fontSize = ResponsiveDimens.textSizeSmall(),
                        color = colors.textSecondary
                    )
                }
            }

            // Edit Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveDimens.cardElevation())
            ) {
                Column(
                    modifier = Modifier.padding(ResponsiveDimens.cardPadding())
                ) {
                    Text(
                        text = "Profile Information",
                        fontSize = ResponsiveDimens.textSizeSubtitle(),
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(bottom = ResponsiveDimens.spacingMedium())
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(ResponsiveDimens.iconSizeMedium())
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ResponsiveDimens.cornerRadius()),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            focusedLabelColor = colors.primary,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            unfocusedBorderColor = colors.divider,
                            unfocusedLabelColor = colors.textSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(ResponsiveDimens.spacingMedium()))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { },
                        label = { Text("Email (Read Only)") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(ResponsiveDimens.iconSizeMedium())
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ResponsiveDimens.cornerRadius()),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = colors.divider,
                            disabledTextColor = colors.textSecondary,
                            disabledLabelColor = colors.textSecondary,
                            disabledLeadingIconColor = colors.textSecondary
                        ),
                        enabled = false
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(ResponsiveDimens.spacingMedium()))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.error.copy(alpha = 0.1f))
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = colors.error,
                                fontSize = ResponsiveDimens.textSizeBody(),
                                modifier = Modifier.padding(ResponsiveDimens.spacingMedium())
                            )
                        }
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        errorMessage = null
                        try {
                            currentUser?.let { user ->
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build()
                                user.updateProfile(profileUpdates).await()
                                showSuccessDialog = true
                            }
                        } catch (e: Exception) {
                            errorMessage = "Failed to update profile: ${e.message}"
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ResponsiveDimens.buttonHeight()),
                shape = RoundedCornerShape(ResponsiveDimens.cornerRadius()),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                ),
                enabled = !isSaving && name.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(ResponsiveDimens.iconSizeMedium()),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(ResponsiveDimens.iconSizeMedium())
                    )
                    Spacer(modifier = Modifier.width(ResponsiveDimens.spacingSmall()))
                    Text(
                        "Save Changes",
                        fontSize = ResponsiveDimens.textSizeSubtitle(),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBackClick()
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colors.success,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Profile Updated",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Your name has been updated successfully!")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onBackClick()
                    }
                ) {
                    Text("OK", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colors.cardBackground,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary
        )
    }
}

