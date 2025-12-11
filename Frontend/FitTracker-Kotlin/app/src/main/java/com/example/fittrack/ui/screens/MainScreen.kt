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
import com.example.fittrack.ui.theme.LocalThemeManager
import com.example.fittrack.ui.theme.getAppColors
import com.example.fittrack.data.model.DailyStats
import com.example.fittrack.ui.theme.ResponsiveDimens

@Composable
fun MainScreen(
    userName: String = "User",
    fitnessData: List<DailyStats>,
    onManualSync: () -> Unit = {},
    isSyncing: Boolean = false
) {
    val themeManager = LocalThemeManager.current
    val colors = getAppColors(themeManager.isDarkMode)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = ResponsiveDimens.horizontalPadding())
            .padding(bottom = 70.dp),
        verticalArrangement = Arrangement.spacedBy(ResponsiveDimens.spacingMedium())
    ) {
        // Header with user greeting, profile avatar
        item {
            Column(modifier = Modifier.padding(vertical = ResponsiveDimens.spacingSmall())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hello, $userName! 👋",
                            fontSize = ResponsiveDimens.textSizeTitle(),
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Today's Activity",
                            fontSize = ResponsiveDimens.textSizeBody(),
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(ResponsiveDimens.avatarSizeSmall())
                            .background(colors.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            fontSize = ResponsiveDimens.textSizeSubtitle(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
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
                subtitle = "Total steps today",
                progress = fitnessData[0].steps / 10000f,
                color = colors.primary,
                showProgress = false,
                appColors = colors
            )
        }

        item {
            // Calories and Distance Cards (side by side, full width combined)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ResponsiveDimens.spacingMedium())
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
                    value = String.format(java.util.Locale.US, "%.2f km", fitnessData[0].distance),
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
                .padding(
                    if (showProgress) ResponsiveDimens.cardPaddingLarge()
                    else ResponsiveDimens.cardPadding()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(
                        if (showProgress) ResponsiveDimens.iconBoxSizeLarge()
                        else ResponsiveDimens.iconBoxSize()
                    )
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(
                        if (showProgress) ResponsiveDimens.iconSizeMedium()
                        else ResponsiveDimens.iconSizeSmall()
                    )
                )
            }

            Spacer(modifier = Modifier.height(
                if (showProgress) ResponsiveDimens.spacingMedium()
                else ResponsiveDimens.spacingSmall()
            ))

            Text(
                text = value,
                fontSize = if (showProgress) ResponsiveDimens.textSizeTitle() else ResponsiveDimens.textSizeSubtitle(),
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )

            Spacer(modifier = Modifier.height(ResponsiveDimens.spacingSmall()))

            Text(
                text = title,
                fontSize = ResponsiveDimens.textSizeBody(),
                color = appColors.textSecondary,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = subtitle,
                fontSize = ResponsiveDimens.textSizeCaption(),
                color = appColors.textSecondary
            )

            // Only show progress bar if showProgress is true
            if (showProgress) {
                Spacer(modifier = Modifier.height(ResponsiveDimens.spacingMedium()))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(color.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(6.dp)
                            .background(color, RoundedCornerShape(3.dp))
                    )
                }

                Spacer(modifier = Modifier.height(ResponsiveDimens.spacingSmall()))

                // Progress percentage text
                Text(
                    text = "${(progress * 100).toInt()}% of goal",
                    fontSize = ResponsiveDimens.textSizeCaption(),
                    color = appColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
