package com.example.fittrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.ui.theme.LocalThemeManager
import com.example.fittrack.ui.theme.ThemeManager
import com.example.fittrack.ui.theme.getAppColors
import java.text.SimpleDateFormat
import java.util.*

// Mock data class for daily fitness stats
data class DailyStats(
    val date: String,
    val steps: Int,
    val calories: Int,
    val distance: Float,
    val activeMinutes: Int
)

// Mock data for demonstration
fun getMockFitnessData(): List<DailyStats> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    return listOf(
        DailyStats(
            date = dateFormat.format(calendar.time),
            steps = 8450,
            calories = 420,
            distance = 6.2f,
            activeMinutes = 45
        ),
        DailyStats(
            date = dateFormat.format(calendar.apply { add(Calendar.DAY_OF_YEAR, -1) }.time),
            steps = 12250,
            calories = 580,
            distance = 9.1f,
            activeMinutes = 68
        ),
        DailyStats(
            date = dateFormat.format(calendar.apply { add(Calendar.DAY_OF_YEAR, -1) }.time),
            steps = 6780,
            calories = 310,
            distance = 5.0f,
            activeMinutes = 32
        ),
        DailyStats(
            date = dateFormat.format(calendar.apply { add(Calendar.DAY_OF_YEAR, -1) }.time),
            steps = 15420,
            calories = 720,
            distance = 11.3f,
            activeMinutes = 85
        ),
        DailyStats(
            date = dateFormat.format(calendar.apply { add(Calendar.DAY_OF_YEAR, -1) }.time),
            steps = 9890,
            calories = 495,
            distance = 7.3f,
            activeMinutes = 52
        )
    )
}

enum class ProfileScreen {
    MAIN,
    EDIT_PROFILE,
    APP_SETTINGS,
    ABOUT
}

@Composable
fun HomeScreen(
    userName: String = "User",
    userEmail: String = "user@example.com",
    onLogoutClick: () -> Unit
) {
    val themeManager = remember { ThemeManager() }
    val colors = getAppColors(themeManager.isDarkMode)

    var selectedTab by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(0) }
    var profileScreenState by remember { mutableStateOf(ProfileScreen.MAIN) }
    val fitnessData = remember { getMockFitnessData() }

    CompositionLocalProvider(LocalThemeManager provides themeManager) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main content with dynamic background color
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Main Screen - Today's activity
                        MainScreen(
                            userName = userName,
                            fitnessData = fitnessData
                        )
                    }
                    1 -> {
                        // Statistics Screen - Detailed analytics
                        StatsScreen(
                            fitnessData = fitnessData,
                            selectedDate = selectedDate,
                            onDateSelected = { selectedDate = it }
                        )
                    }
                    2 -> {
                        // Profile Section with nested screens
                    when (profileScreenState) {
                        ProfileScreen.MAIN -> {
                            ProfileScreen(
                                userName = userName,
                                userEmail = userEmail,
                                onLogoutClick = onLogoutClick,
                                onEditProfileClick = { profileScreenState = ProfileScreen.EDIT_PROFILE },
                                onAppSettingsClick = { profileScreenState = ProfileScreen.APP_SETTINGS },
                                onAboutClick = { profileScreenState = ProfileScreen.ABOUT }
                            )
                        }
                        ProfileScreen.EDIT_PROFILE -> {
                            EditProfileScreen(
                                authViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                                onBackClick = { profileScreenState = ProfileScreen.MAIN }
                            )
                        }
                        ProfileScreen.APP_SETTINGS -> {
                            AppSettingsScreen(
                                onBackClick = { profileScreenState = ProfileScreen.MAIN }
                            )
                        }
                        ProfileScreen.ABOUT -> {
                            AboutScreen(
                                onBackClick = { profileScreenState = ProfileScreen.MAIN }
                            )
                        }
                    }
                }
            }
        }

        // Floating/Overlay Bottom Navigation
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .wrapContentSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Main Tab
                FloatingNavItem(
                    icon = Icons.Default.Home,
                    title = "Main",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = colors
                )

                // Stats Tab
                FloatingNavItem(
                    icon = Icons.Default.Analytics,
                    title = "Stats",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = colors
                )

                // Profile Tab
                FloatingNavItem(
                    icon = Icons.Default.Person,
                    title = "Profile",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = colors
                )
            }
        }
    }
    }
}

@Composable
fun FloatingNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.example.fittrack.ui.theme.AppColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (isSelected) colors.primary else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) colors.primary else colors.textSecondary
        )
    }
}
