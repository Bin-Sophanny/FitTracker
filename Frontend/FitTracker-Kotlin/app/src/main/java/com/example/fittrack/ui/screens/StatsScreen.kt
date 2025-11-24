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
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(
    fitnessData: List<DailyStats>,
    selectedDate: Int,
    onDateSelected: (Int) -> Unit
) {
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
                color = Color(0xFF2D3748),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Date Navigation Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select Date",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D3748),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(fitnessData.size) { index ->
                            DateCard(
                                date = fitnessData[index].date,
                                isSelected = selectedDate == index,
                                isToday = index == 0,
                                onClick = { onDateSelected(index) }
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
                value = DecimalFormat("#,###").format(currentStats.steps),
                subtitle = "Goal: 10,000",
                progress = currentStats.steps / 10000f,
                color = Color(0xFF667eea),
                showProgress = true
            )
        }

        item {
            // Calories and Distance Cards (side by side, full width combined)
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
                    showProgress = false
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
                    showProgress = false
                )
            }
        }

        // Weekly Progress Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Weekly Overview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mini weekly stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeeklyStatItem("Steps", "68.2K")
                        WeeklyStatItem("Calories", "3.1K")
                        WeeklyStatItem("Distance", "47.8 km")
                    }
                }
            }
        }
    }
}

@Composable
fun DateCard(
    date: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
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
            containerColor = if (isSelected) Color(0xFF667eea) else Color(0xFFF7FAFC)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Text(
            text = displayText,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color(0xFF4A5568),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
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
    showProgress: Boolean = true // Add parameter to control progress bar visibility
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // Increased padding for better spacing
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp) // Increased size for better visibility
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(28.dp) // Increased icon size
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = value,
                fontSize = 24.sp, // Increased font size
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                fontSize = 14.sp, // Increased font size
                color = Color(0xFF718096),
                fontWeight = FontWeight.Medium
            )

            Text(
                text = subtitle,
                fontSize = 12.sp, // Increased font size
                color = Color(0xFF718096)
            )

            // Only show progress bar if showProgress is true
            if (showProgress) {
                Spacer(modifier = Modifier.height(12.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp) // Increased height for better visibility
                        .background(color.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(6.dp)
                            .background(color, RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyStatItem(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color(0xFF718096)
        )
    }
}
