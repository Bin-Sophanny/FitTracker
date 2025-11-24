package com.example.fittrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.ui.theme.LocalThemeManager
import com.example.fittrack.ui.theme.getAppColors

@Composable
fun MainScreen(
    userName: String = "User",
    fitnessData: List<DailyStats>
) {
    val themeManager = LocalThemeManager.current
    val colors = getAppColors(themeManager.isDarkMode)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
            .padding(bottom = 70.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header with user greeting and profile avatar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, $userName! 👋",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Today's Activity",
                        fontSize = 16.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(colors.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Today's Main Stats - Same layout as StatsScreen
        item {
            // Steps Card (full width)
            MainStatCard(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                title = "Steps",
                value = "${fitnessData[0].steps}",
                subtitle = "Goal: 10,000",
                progress = fitnessData[0].steps / 10000f,
                color = colors.primary,
                showProgress = true,
                appColors = colors
            )
        }

        item {
            // Calories and Distance Cards (side by side, full width combined)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calories Card
                MainStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalFireDepartment,
                    title = "Calories",
                    value = "${fitnessData[0].calories}",
                    subtitle = "kcal burned",
                    progress = fitnessData[0].calories / 600f,
                    color = Color(0xFFE53E3E),
                    showProgress = false,
                    appColors = colors
                )

                // Distance Card
                MainStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Route,
                    title = "Distance",
                    value = "${fitnessData[0].distance} km",
                    subtitle = "Total walked",
                    progress = fitnessData[0].distance / 12f,
                    color = Color(0xFF38A169),
                    showProgress = false,
                    appColors = colors
                )
            }
        }
    }
}

@Composable
fun MainStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    progress: Float,
    color: Color,
    showProgress: Boolean = true,
    appColors: com.example.fittrack.ui.theme.AppColors
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = appColors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (showProgress) 6.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (showProgress) 32.dp else 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (showProgress) 80.dp else 56.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(if (showProgress) 40.dp else 28.dp)
                )
            }

            Spacer(modifier = Modifier.height(if (showProgress) 24.dp else 16.dp))

            Text(
                text = value,
                fontSize = if (showProgress) 36.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )

            Spacer(modifier = Modifier.height(if (showProgress) 8.dp else 4.dp))

            Text(
                text = title,
                fontSize = if (showProgress) 18.sp else 14.sp,
                color = appColors.textSecondary,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = subtitle,
                fontSize = if (showProgress) 14.sp else 12.sp,
                color = appColors.textSecondary
            )

            // Only show progress bar if showProgress is true
            if (showProgress) {
                Spacer(modifier = Modifier.height(20.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(8.dp)
                            .background(color, RoundedCornerShape(4.dp))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress percentage text
                Text(
                    text = "${(progress * 100).toInt()}% of goal completed",
                    fontSize = 14.sp,
                    color = appColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
