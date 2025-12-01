package com.example.fittrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.fittrack.data.model.DailyStats
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(
    fitnessData: List<DailyStats>,
    selectedDate: Int,
    onDateSelected: (Int) -> Unit
) {
    val themeManager = LocalThemeManager.current
    val colors = getAppColors(themeManager.isDarkMode)
    val currentStats = fitnessData[selectedDate]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
            .padding(bottom = 70.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Stats Header
        item {
            Text(
                text = "Statistics",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Date Selection Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Select Date",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(fitnessData.size) { index ->
                            DateCard(
                                date = fitnessData[index].date,
                                isSelected = selectedDate == index,
                                isToday = index == 0,
                                onClick = { onDateSelected(index) },
                                appColors = colors
                            )
                        }
                    }
                }
            }
        }

        // Main Stats Cards
        item {
            // Steps Card (full width)
            StatCard(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                title = "Steps",
                value = "${currentStats.steps}",
                subtitle = "Total steps",
                progress = currentStats.steps / 10000f,
                color = colors.primary,
                showProgress = false,
                appColors = colors
            )
        }

        item {
            // Calories and Distance Cards (side by side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calories Card
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalFireDepartment,
                    title = "Calories",
                    value = "${currentStats.calories}",
                    subtitle = "kcal burned",
                    progress = currentStats.calories / 600f,
                    color = Color(0xFFE53E3E),
                    showProgress = false,
                    appColors = colors
                )

                // Distance Card
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Route,
                    title = "Distance",
                    value = "${currentStats.distance} km",
                    subtitle = "Total walked",
                    progress = currentStats.distance / 12f,
                    color = Color(0xFF38A169),
                    showProgress = false,
                    appColors = colors
                )
            }
        }
    }
}

@Composable
fun DateCard(
    date: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    appColors: com.example.fittrack.ui.theme.AppColors
) {
    val displayText = when {
        isToday -> "Today"
        date == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
        ) -> "Yesterday"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date) ?: Date()
        )
    }

    Card(
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) appColors.primary else appColors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = displayText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else appColors.textPrimary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Composable
fun StatCard(
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                color = appColors.textSecondary,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = appColors.textSecondary
            )

            // Only show progress bar if showProgress is true
            if (showProgress) {
                Spacer(modifier = Modifier.height(10.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(color.copy(alpha = 0.2f), RoundedCornerShape(2.5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(5.dp)
                            .background(color, RoundedCornerShape(2.5.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyStatItem(
    title: String,
    value: String,
    colors: com.example.fittrack.ui.theme.AppColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Text(
            text = title,
            fontSize = 11.sp,
            color = colors.textSecondary
        )
    }
}
