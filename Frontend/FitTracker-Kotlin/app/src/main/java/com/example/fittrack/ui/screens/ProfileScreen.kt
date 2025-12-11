package com.example.fittrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.fittrack.ui.theme.LocalThemeManager
import com.example.fittrack.ui.theme.getAppColors
import com.example.fittrack.ui.theme.ResponsiveDimens

@Composable
fun ProfileScreen(
    userName: String = "User",
    userEmail: String = "user@example.com",
    onLogoutClick: () -> Unit,
    onEditProfileClick: () -> Unit = {},
    onAppSettingsClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    val themeManager = LocalThemeManager.current
    val colors = getAppColors(themeManager.isDarkMode)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(ResponsiveDimens.horizontalPadding()),
        verticalArrangement = Arrangement.spacedBy(ResponsiveDimens.spacingMedium())
    ) {
        // Header without back button - just title
        Text(
            text = "Profile",
            fontSize = ResponsiveDimens.textSizeTitle(),
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.padding(vertical = ResponsiveDimens.spacingSmall())
        )

        // Profile Avatar and Info
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
                // Large Profile Avatar
                Box(
                    modifier = Modifier
                        .size(ResponsiveDimens.avatarSizeMedium())
                        .background(colors.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        fontSize = ResponsiveDimens.textSizeHeading(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(ResponsiveDimens.spacingMedium()))

                Text(
                    text = userName,
                    fontSize = ResponsiveDimens.textSizeTitle(),
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Text(
                    text = userEmail,
                    fontSize = ResponsiveDimens.textSizeBody(),
                    color = colors.textSecondary
                )
            }
        }

        // Profile Options
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveDimens.cardElevation())
        ) {
            Column(
                modifier = Modifier.padding(ResponsiveDimens.spacingMedium())
            ) {
                Text(
                    text = "Settings",
                    fontSize = ResponsiveDimens.textSizeSubtitle(),
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = ResponsiveDimens.spacingMedium())
                )

                ProfileMenuItem(
                    icon = Icons.Default.Person,
                    title = "Edit Profile",
                    subtitle = "Update your personal information",
                    onClick = onEditProfileClick,
                    colors = colors
                )

                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "App Settings",
                    subtitle = "Customize your app experience",
                    onClick = onAppSettingsClick,
                    colors = colors
                )


                ProfileMenuItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "App version and information",
                    onClick = onAboutClick,
                    colors = colors
                )

                Spacer(modifier = Modifier.height(ResponsiveDimens.spacingMedium()))

                // Logout Button inside settings card
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ResponsiveDimens.buttonHeight()),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error
                    ),
                    shape = RoundedCornerShape(ResponsiveDimens.cornerRadius())
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        modifier = Modifier.size(ResponsiveDimens.iconSizeMedium())
                    )
                    Spacer(modifier = Modifier.width(ResponsiveDimens.spacingSmall()))
                    Text(
                        "Logout",
                        fontSize = ResponsiveDimens.textSizeSubtitle(),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    colors: com.example.fittrack.ui.theme.AppColors
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = ResponsiveDimens.spacingSmall()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(ResponsiveDimens.iconBoxSize())
                    .background(colors.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = colors.primary,
                    modifier = Modifier.size(ResponsiveDimens.iconSizeSmall())
                )
            }

            Spacer(modifier = Modifier.width(ResponsiveDimens.spacingMedium()))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = ResponsiveDimens.textSizeBody(),
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = ResponsiveDimens.textSizeSmall(),
                    color = colors.textSecondary
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = colors.textSecondary,
                modifier = Modifier.size(ResponsiveDimens.iconSizeSmall())
            )
        }
    }
}

