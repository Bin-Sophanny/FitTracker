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
import com.example.fittrack.ui.theme.LocalThemeManager
import com.example.fittrack.ui.theme.getAppColors
import com.example.fittrack.data.model.DailyStats
import com.example.fittrack.util.DateUtils
import com.example.fittrack.ui.theme.ResponsiveDimens
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

    // Debug logging to see what data StatsScreen receives
    LaunchedEffect(fitnessData) {
        android.util.Log.d("StatsScreen", "")
        android.util.Log.d("StatsScreen", "========================================")
        android.util.Log.d("StatsScreen", "📊 STATSSCREEN DATA RECEIVED")
        android.util.Log.d("StatsScreen", "========================================")
        android.util.Log.d("StatsScreen", "Total days of data: ${fitnessData.size}")
        fitnessData.forEachIndexed { index, stats ->
            val isToday = DateUtils.extractDateFromIso(stats.date) == DateUtils.getCurrentDate()
            val displayDate = DateUtils.extractDateFromIso(stats.date)
            android.util.Log.d("StatsScreen", "[$index] Date: $displayDate (${if (isToday) "TODAY" else "PAST"}) - Steps: ${stats.steps}, Cal: ${stats.calories}, Dist: %.2f km".format(stats.distance))
        }
        android.util.Log.d("StatsScreen", "Currently selected index: $selectedDate")
        if (fitnessData.isNotEmpty() && selectedDate < fitnessData.size) {
            android.util.Log.d("StatsScreen", "Displaying: ${DateUtils.extractDateFromIso(fitnessData[selectedDate].date)} with ${fitnessData[selectedDate].steps} steps")
        }
        android.util.Log.d("StatsScreen", "========================================")
        android.util.Log.d("StatsScreen", "")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = ResponsiveDimens.horizontalPadding())
            .padding(bottom = 75.dp),
        verticalArrangement = Arrangement.spacedBy(ResponsiveDimens.spacingMedium())
    ) {
        // Stats Header
        item {
            Text(
                text = "Statistics",
                fontSize = ResponsiveDimens.textSizeHeading(),
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.padding(vertical = ResponsiveDimens.spacingSmall())
            )
        }

        // Date Selection Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveDimens.cardElevation())
            ) {
                Column(
                    modifier = Modifier.padding(ResponsiveDimens.cardPadding())
                ) {
                    Text(
                        text = "Select Date",
                        fontSize = ResponsiveDimens.textSizeSubtitle(),
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(ResponsiveDimens.spacingMedium()))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(ResponsiveDimens.spacingSmall())
                    ) {
                        items(fitnessData.size) { index ->
                            val stats = fitnessData[index]
                            DateCard(
                                date = stats.date,
                                isSelected = index == selectedDate,
                                isToday = DateUtils.extractDateFromIso(stats.date) == DateUtils.getCurrentDate(),
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
            Text(
                text = "Daily Activity",
                fontSize = ResponsiveDimens.textSizeSubtitle(),
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.padding(vertical = ResponsiveDimens.spacingSmall())
            )
        }

        // Steps Card
        item {
            StatCard(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                title = "Steps",
                value = "${currentStats.steps}",
                subtitle = "Total steps",
                progress = currentStats.steps / 10000f,
                color = Color(0xFF3B82F6),
                appColors = colors
            )
        }

        // Calories and Distance Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ResponsiveDimens.spacingMedium())
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
                    value = String.format(Locale.US, "%.2f km", currentStats.distance),
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
    val displayDate = DateUtils.extractDateFromIso(date)
    val todayDate = DateUtils.getCurrentDate()

    val bangkokTimezone = TimeZone.getTimeZone("Asia/Bangkok")
    val yesterday = Calendar.getInstance(bangkokTimezone).apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = bangkokTimezone
    }
    val yesterdayDate = dateFormat.format(yesterday.time)

    val displayText = when {
        displayDate == todayDate -> "Today"
        displayDate == yesterdayDate -> "Yesterday"
        else -> displayDate
    }

    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(ResponsiveDimens.spacingSmall() * 0.5f),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) appColors.primary else appColors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else ResponsiveDimens.cardElevation()
        )
    ) {
        Column(
            modifier = Modifier
                .padding(ResponsiveDimens.cardPadding())
                .widthIn(min = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = displayText,
                fontSize = ResponsiveDimens.textSizeBody(),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else appColors.textPrimary
            )

            if (isToday && !isSelected) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(6.dp)
                        .background(appColors.primary, CircleShape)
                )
            }
        }
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
        elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveDimens.cardElevation())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ResponsiveDimens.cardPadding()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(ResponsiveDimens.iconBoxSize())
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(ResponsiveDimens.iconSizeMedium())
                )
            }

            Spacer(modifier = Modifier.height(ResponsiveDimens.spacingMedium()))

            Text(
                text = value,
                fontSize = ResponsiveDimens.textSizeTitle(),
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
                fontSize = ResponsiveDimens.textSizeSmall(),
                color = appColors.textSecondary
            )
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
            fontSize = ResponsiveDimens.textSizeBody(),
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Text(
            text = title,
            fontSize = ResponsiveDimens.textSizeSmall(),
            color = colors.textSecondary
        )
    }
}

