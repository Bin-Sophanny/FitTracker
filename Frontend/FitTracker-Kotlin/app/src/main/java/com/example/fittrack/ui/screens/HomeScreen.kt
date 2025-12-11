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
import com.example.fittrack.util.StepCounterHelper
import com.example.fittrack.util.DateUtils
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

    // Get userId from Firebase - NOT cached, updates when auth changes
    val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var selectedTab by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(0) }
    var profileScreenState by remember { mutableStateOf(ProfileScreen.MAIN) }

    // Real-time step counter state - will reset when userId changes
    var realTimeSteps by remember(userId) { mutableStateOf(0) }
    var realTimeStats by remember(userId) { mutableStateOf<DailyStats?>(null) }
    var isSyncing by remember { mutableStateOf(false) }

    // Fetch data from backend API
    val dailyStatsState by fitnessViewModel.dailyStatsState.collectAsState()

    // Start step counter service and update real-time steps
    // Restart when userId changes (account switch)
    LaunchedEffect(userId) {
        android.util.Log.d("HomeScreen", "")
        android.util.Log.d("HomeScreen", "========================================")
        android.util.Log.d("HomeScreen", "🔄 USER LOGIN/SWITCH DETECTED")
        android.util.Log.d("HomeScreen", "========================================")
        android.util.Log.d("HomeScreen", "👤 User ID (Firebase UID): $userId")
        android.util.Log.d("HomeScreen", "📱 Device: New device or session")
        android.util.Log.d("HomeScreen", "📊 Local steps on this device: 0 (new device)")
        android.util.Log.d("HomeScreen", "🔍 Will fetch backend data for this user...")
        android.util.Log.d("HomeScreen", "========================================")

        // Reset steps immediately for new user
        realTimeSteps = 0
        realTimeStats = null

        // Restart step counter service for new user
        StepCounterService.stop(context)
        StepCounterService.start(context)

        // Fetch backend data for this user - THIS IS CRUCIAL FOR CROSS-DEVICE DATA
        android.util.Log.d("HomeScreen", "📡 Fetching data from backend for user: $userId")
        fitnessViewModel.getDailyStats(limit = 5)

        // Update real-time steps every second
        while (true) {
            delay(1000)
            realTimeSteps = StepCounterHelper.getCurrentSteps(context)
            realTimeStats = if (userId.isNotEmpty()) {
                StepCounterHelper.getCurrentDailyStats(context, userId)
            } else null
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
                        date = DateUtils.getCurrentDate(),
                        steps = realTimeSteps,
                        calories = 0,
                        distance = 0f,
                        activeMinutes = 0
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
                    date = DateUtils.getCurrentDate(),
                    steps = realTimeSteps,
                    calories = 0,
                    distance = 0f,
                    activeMinutes = 0
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
        android.util.Log.d("HomeScreen", "========================================")
        android.util.Log.d("HomeScreen", "🖥️ UI STATE CHANGED - Backend Response")
        android.util.Log.d("HomeScreen", "========================================")
        android.util.Log.d("HomeScreen", "API State: ${dailyStatsState::class.simpleName}")
        when (dailyStatsState) {
            is ApiResult.Success -> {
                val data = (dailyStatsState as ApiResult.Success<List<DailyStats>>).data
                android.util.Log.d("HomeScreen", "✅ SUCCESS - Data received from backend")
                android.util.Log.d("HomeScreen", "📊 Total days of data: ${data.size}")
                if (data.isNotEmpty()) {
                    android.util.Log.d("HomeScreen", "")
                    android.util.Log.d("HomeScreen", "📋 BACKEND DATA DETAILS:")
                    data.forEachIndexed { index, stats ->
                        android.util.Log.d("HomeScreen", "  Day $index: ${stats.date} - ${stats.steps} steps, ${stats.calories} cal")
                    }
                    android.util.Log.d("HomeScreen", "")
                    android.util.Log.d("HomeScreen", "✅ This data will be displayed in the app")
                } else {
                    android.util.Log.w("HomeScreen", "⚠️ Backend returned empty data array")
                    android.util.Log.w("HomeScreen", "   This could mean:")
                    android.util.Log.w("HomeScreen", "   - New user with no data yet")
                    android.util.Log.w("HomeScreen", "   - Data was pushed from another device but not synced")
                    android.util.Log.w("HomeScreen", "   - Backend issue")
                }
            }
            is ApiResult.Error -> {
                android.util.Log.e("HomeScreen", "❌ ERROR from API: ${(dailyStatsState as ApiResult.Error).message}")
                android.util.Log.e("HomeScreen", "   Data from other devices will NOT be visible")
            }
            is ApiResult.Loading -> {
                android.util.Log.d("HomeScreen", "⏳ Loading backend data...")
            }
        }
        android.util.Log.d("HomeScreen", "========================================")
        android.util.Log.d("HomeScreen", "")
    }

    // Extract fitness data from API response and merge with real-time data
    val fitnessData = when (dailyStatsState) {
        is ApiResult.Success -> {
            android.util.Log.d("HomeScreen", "🎨 Building UI with SUCCESS data")
            val data = (dailyStatsState as ApiResult.Success<List<DailyStats>>).data.toMutableList()

            if (data.isEmpty()) {
                android.util.Log.w("HomeScreen", "⚠️ Backend data empty, using real-time only")
                // Backend returned empty - this is a new user or no data yet
                listOf(realTimeStats ?: DailyStats(
                    userId = userId,
                    date = DateUtils.getCurrentDate(),
                    steps = realTimeSteps,
                    calories = 0,
                    distance = 0f,
                    activeMinutes = 0
                ))
            } else {
                // Backend has data - show it!
                android.util.Log.d("HomeScreen", "✅ Backend has ${data.size} days of data")

                // Sort data by date descending (newest first, today at index 0)
                data.sortByDescending { DateUtils.extractDateFromIso(it.date) }
                android.util.Log.d("HomeScreen", "📅 Sorted data by date (newest first)")

                // Find today's data in the array (should be at index 0 after sorting)
                val todayIndex = data.indexOfFirst { DateUtils.isToday(it.date) }
                val hasTodayInBackend = todayIndex >= 0

                if (hasTodayInBackend) {
                    // Backend has today's data - merge with real-time steps (use maximum)
                    val backendToday = data[todayIndex]
                    android.util.Log.d("HomeScreen", "🔄 Found today at index $todayIndex: ${backendToday.steps} steps, Local: ${realTimeSteps} steps")

                    // Merge local and backend data for today
                    data[todayIndex] = backendToday.copy(
                        steps = maxOf(realTimeSteps, backendToday.steps),
                        calories = maxOf(realTimeStats?.calories ?: 0, backendToday.calories),
                        distance = maxOf(realTimeStats?.distance ?: 0f, backendToday.distance),
                        activeMinutes = maxOf(realTimeStats?.activeMinutes ?: 0, backendToday.activeMinutes)
                    )
                    android.util.Log.d("HomeScreen", "✅ Merged today's data: ${data[todayIndex].steps} steps")
                } else if (realTimeStats != null && realTimeSteps > 0) {
                    // Backend doesn't have today yet, but we have local steps
                    android.util.Log.d("HomeScreen", "➕ Adding today's local data (${realTimeSteps} steps) to backend data")
                    data.add(0, realTimeStats!!)
                }

                android.util.Log.d("HomeScreen", "✅ Displaying ${data.size} days of data")
                android.util.Log.d("HomeScreen", "")
                android.util.Log.d("HomeScreen", "📋 FINAL DATA ARRAY (passing to StatsScreen):")
                data.forEachIndexed { index, stats ->
                    val dateDisplay = DateUtils.extractDateFromIso(stats.date)
                    val isToday = DateUtils.isToday(stats.date)
                    android.util.Log.d("HomeScreen", "  [$index] Date: $dateDisplay ${if (isToday) "(TODAY)" else "(PAST)"} - Steps: ${stats.steps}")
                }
                android.util.Log.d("HomeScreen", "")
                data
            }
        }
        is ApiResult.Error -> {
            android.util.Log.w("HomeScreen", "⚠️ Backend ERROR - showing real-time data only")
            // Backend not connected - show real-time data only
            listOf(realTimeStats ?: DailyStats(
                userId = userId,
                date = DateUtils.getCurrentDate(),
                steps = realTimeSteps,
                calories = 0,
                distance = 0f,
                activeMinutes = 0
            ))
        }
        else -> {
            android.util.Log.d("HomeScreen", "⏳ Loading state - showing real-time data")
            // Loading state - show real-time data or default
            listOf(realTimeStats ?: DailyStats(
                userId = userId,
                date = DateUtils.getCurrentDate(),
                steps = realTimeSteps,
                calories = 0,
                distance = 0f,
                activeMinutes = 0
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
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .statusBarsPadding(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = Color(0xFFE53E3E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Backend not connected - Steps counting locally",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53E3E)
                        )
                    }
                }
            }

            // Floating/Overlay Bottom Navigation - Responsive design
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.95f) // 95% of screen width for better fit
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
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

