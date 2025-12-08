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
import com.example.fittrack.data.model.DailyStats
import com.example.fittrack.viewmodel.FitnessViewModel
import com.example.fittrack.data.api.ApiResult
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.fittrack.util.StepCounterHelper
import com.example.fittrack.service.StepCounterService
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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
    val context = LocalContext.current
    val themeManager = remember { ThemeManager() }
    val colors = getAppColors(themeManager.isDarkMode)
    val fitnessViewModel: FitnessViewModel = remember { FitnessViewModel(context) }
    val scope = rememberCoroutineScope()

    // Get userId from Firebase
    val userId = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    var selectedTab by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(0) }
    var profileScreenState by remember { mutableStateOf(ProfileScreen.MAIN) }

    // Real-time step counter state
    var realTimeSteps by remember { mutableStateOf(0) }
    var realTimeStats by remember { mutableStateOf<DailyStats?>(null) }
    var isSyncing by remember { mutableStateOf(false) }

    // Fetch data from backend API
    val dailyStatsState by fitnessViewModel.dailyStatsState.collectAsState()

    // Start step counter service and update real-time steps
    LaunchedEffect(Unit) {
        StepCounterService.start(context)
        fitnessViewModel.getDailyStats(limit = 5)

        while (true) {
            realTimeSteps = StepCounterHelper.getCurrentSteps(context)
            realTimeStats = if (userId.isNotEmpty()) {
                StepCounterHelper.getCurrentDailyStats(context, userId)
            } else null
            delay(1000)
        }
    }

    // Auto-sync when backend has no data but we have local steps
    LaunchedEffect(dailyStatsState, realTimeSteps) {
        if (dailyStatsState is ApiResult.Success &&
            (dailyStatsState as ApiResult.Success<List<DailyStats>>).data.isEmpty() &&
            realTimeSteps > 0 && !isSyncing && userId.isNotEmpty()) {

            android.util.Log.d("HomeScreen", "🔄 Auto-sync: Backend empty but we have $realTimeSteps steps locally")
            delay(2000) // Wait 2 seconds before auto-sync

            if (!isSyncing) { // Double check we're not already syncing
                isSyncing = true
                try {
                    val stats = realTimeStats ?: DailyStats(
                        userId = userId,
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        steps = realTimeSteps,
                        calories = 0,
                        distance = 0f,
                        activeMinutes = 0,
                        heartRate = null
                    )
                    android.util.Log.d("HomeScreen", "📤 Auto-syncing: ${stats.steps} steps")
                    fitnessViewModel.syncStepsToBackend(stats)
                    delay(2000) // Wait for sync to complete
                    fitnessViewModel.getDailyStats(limit = 5) // Refresh data
                    android.util.Log.d("HomeScreen", "✅ Auto-sync completed")
                } catch (e: Exception) {
                    android.util.Log.e("HomeScreen", "❌ Auto-sync failed: ${e.message}")
                } finally {
                    isSyncing = false
                }
            }
        }
    }

    // Manual sync function
    fun manualSync() {
        scope.launch {
            isSyncing = true
            android.util.Log.d("HomeScreen", "🔄 Manual sync triggered by user")
            try {
                val stats = realTimeStats ?: DailyStats(
                    userId = userId,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    steps = realTimeSteps,
                    calories = 0,
                    distance = 0f,
                    activeMinutes = 0,
                    heartRate = null
                )
                android.util.Log.d("HomeScreen", "📤 Syncing: ${stats.steps} steps")
                fitnessViewModel.syncStepsToBackend(stats)
                delay(2000) // Wait for sync to complete
                fitnessViewModel.getDailyStats(limit = 5) // Refresh data
                android.util.Log.d("HomeScreen", "✅ Manual sync completed")
            } catch (e: Exception) {
                android.util.Log.e("HomeScreen", "❌ Manual sync failed: ${e.message}")
            } finally {
                isSyncing = false
            }
        }
    }

    // Log the API state for debugging
    LaunchedEffect(dailyStatsState) {
        android.util.Log.d("HomeScreen", "")
        android.util.Log.d("HomeScreen", "🖥️🖥️🖥️ UI STATE CHANGED 🖥️🖥️🖥️")
        android.util.Log.d("HomeScreen", "API State: ${dailyStatsState::class.simpleName}")
        when (dailyStatsState) {
            is ApiResult.Success -> {
                val data = (dailyStatsState as ApiResult.Success<List<DailyStats>>).data
                android.util.Log.d("HomeScreen", "✅ SUCCESS - Data received from backend")
                android.util.Log.d("HomeScreen", "📊 Data size: ${data.size}")
                if (data.isNotEmpty()) {
                    android.util.Log.d("HomeScreen", "📊 Today's data: ${data[0].date} - ${data[0].steps} steps")
                } else {
                    android.util.Log.w("HomeScreen", "⚠️ Backend returned empty data array")
                }
            }
            is ApiResult.Error -> {
                android.util.Log.e("HomeScreen", "❌ ERROR from API: ${(dailyStatsState as ApiResult.Error).message}")
            }
            is ApiResult.Loading -> {
                android.util.Log.d("HomeScreen", "⏳ Loading state...")
            }
        }
        android.util.Log.d("HomeScreen", "")
    }

    // Extract fitness data from API response and merge with real-time data
    val fitnessData = when (dailyStatsState) {
        is ApiResult.Success -> {
            android.util.Log.d("HomeScreen", "🎨 Building UI with SUCCESS data")
            val data = (dailyStatsState as ApiResult.Success<List<DailyStats>>).data.toMutableList()

            // If we have real-time step data for today, replace the first entry
            if (realTimeStats != null && data.isNotEmpty() &&
                data[0].date == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) {
                android.util.Log.d("HomeScreen", "🔄 Merging real-time steps (${realTimeSteps}) with backend data (${data[0].steps})")
                // Update today's data with real-time steps
                data[0] = data[0].copy(
                    steps = maxOf(realTimeSteps, data[0].steps),
                    calories = realTimeStats!!.calories,
                    distance = realTimeStats!!.distance,
                    activeMinutes = realTimeStats!!.activeMinutes
                )
            } else if (realTimeStats != null) {
                android.util.Log.d("HomeScreen", "➕ Adding today's real-time data to beginning")
                // Add today's real-time data at the beginning
                data.add(0, realTimeStats!!)
            }

            if (data.isEmpty()) {
                android.util.Log.w("HomeScreen", "⚠️ Backend data empty, using real-time only")
                listOf(realTimeStats ?: DailyStats(
                    userId = userId,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    steps = realTimeSteps,
                    calories = 0,
                    distance = 0f,
                    activeMinutes = 0,
                    heartRate = null
                ))
            } else {
                android.util.Log.d("HomeScreen", "✅ Displaying ${data.size} days of data")
                data
            }
        }
        is ApiResult.Error -> {
            android.util.Log.w("HomeScreen", "⚠️ Backend ERROR - showing real-time data only")
            // Backend not connected - show real-time data only
            listOf(realTimeStats ?: DailyStats(
                userId = userId,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                steps = realTimeSteps,
                calories = 0,
                distance = 0f,
                activeMinutes = 0,
                heartRate = null
            ))
        }
        else -> {
            android.util.Log.d("HomeScreen", "⏳ Loading state - showing real-time data")
            // Loading state - show real-time data or default
            listOf(realTimeStats ?: DailyStats(
                userId = userId,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                steps = realTimeSteps,
                calories = 0,
                distance = 0f,
                activeMinutes = 0,
                heartRate = null
            ))
        }
    }

    // Only show error banner if there's an actual connection error
    // BUT - steps should still count locally even if backend is down!
    val isActualError = dailyStatsState is ApiResult.Error
    val errorMessage = if (dailyStatsState is ApiResult.Error) {
        (dailyStatsState as ApiResult.Error).message
    } else ""

    // Only show banner if it's a network/connection error (not 404 or empty data)
    val showBanner = isActualError &&
        (errorMessage.contains("Failed to connect", ignoreCase = true) ||
         errorMessage.contains("Unable to resolve host", ignoreCase = true) ||
         errorMessage.contains("timeout", ignoreCase = true) ||
         errorMessage.contains("Connection refused", ignoreCase = true))

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
                // Tab content based on selected tab
                when (selectedTab) {
                    0 -> {
                        // Main Screen - Today's activity
                        MainScreen(
                            userName = userName,
                            fitnessData = fitnessData,
                            onManualSync = { manualSync() },
                            isSyncing = isSyncing
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
                                    authViewModel = viewModel(),
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

            // Show backend connection status banner as overlay at the top
            if (showBanner) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .statusBarsPadding(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = Color(0xFFE53E3E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backend not connected - Steps counting locally",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53E3E)
                        )
                    }
                }
            }

            // Floating/Overlay Bottom Navigation
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .wrapContentSize()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationButton(
                        icon = Icons.Default.Home,
                        label = "Home",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationButton(
                        icon = Icons.Default.Assessment,
                        label = "Stats",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    NavigationButton(
                        icon = Icons.Default.Person,
                        label = "Profile",
                        isSelected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = getAppColors(LocalThemeManager.current.isDarkMode)

    Box(
        modifier = Modifier
            .height(56.dp)
            .clickable(onClick = onClick)
            .background(
                color = if (isSelected) colors.primary.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) colors.primary else colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) colors.primary else colors.textSecondary
            )
        }
    }
}

