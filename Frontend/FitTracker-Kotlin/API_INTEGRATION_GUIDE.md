# 🚀 Complete API Integration Guide

## ✅ What I've Set Up For You

### 1. **Dependencies Added** (`build.gradle.kts`)
- ✅ Retrofit 2.9.0 (for API calls)
- ✅ Gson Converter (for JSON parsing)
- ✅ OkHttp Logging Interceptor (for debugging)
- ✅ Coroutines (for async operations)

### 2. **Data Models Created** (`data/model/`)
- ✅ **DailyStats.kt** - Fitness statistics
- ✅ **UserProfile.kt** - User information
- ✅ **Workout.kt** - Exercise activities
- ✅ **Goal.kt** - Fitness goals
- ✅ **Blockchain.kt** - Token rewards
- ✅ **AI.kt** - AI predictions

### 3. **API Infrastructure** (`data/api/`)
- ✅ **ApiService.kt** - All API endpoints
- ✅ **RetrofitClient.kt** - Retrofit configuration
- ✅ **ApiResult.kt** - Response wrapper for error handling

### 4. **Repository Layer** (`data/repository/`)
- ✅ **FitTrackRepository.kt** - Handles all API calls with Firebase auth

### 5. **ViewModel** (`viewmodel/`)
- ✅ **FitnessViewModel.kt** - Manages UI state and business logic

### 6. **Permissions** (`AndroidManifest.xml`)
- ✅ INTERNET permission added
- ✅ ACCESS_NETWORK_STATE permission added

### 7. **Updated Imports**
- ✅ HomeScreen.kt - Now imports DailyStats from data.model
- ✅ StatsScreen.kt - Now imports DailyStats from data.model
- ✅ MainScreen.kt - Now imports DailyStats from data.model

---

## 🎯 How to Use in Your Screens (Examples)

### Example 1: Update HomeScreen to Fetch Real Data

Replace the mock data with real API calls:

```kotlin
// In HomeScreen.kt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrack.viewmodel.FitnessViewModel
import com.example.fittrack.data.api.ApiResult

@Composable
fun HomeScreen(
    userName: String = "User",
    userEmail: String = "user@example.com",
    onLogoutClick: () -> Unit
) {
    // Add ViewModel
    val fitnessViewModel: FitnessViewModel = viewModel()
    val dailyStatsState by fitnessViewModel.dailyStatsState.collectAsState()
    
    // Fetch data when screen loads
    LaunchedEffect(Unit) {
        fitnessViewModel.getDailyStats(limit = 5)
    }
    
    // Handle different states
    val fitnessData = when (dailyStatsState) {
        is ApiResult.Success -> (dailyStatsState as ApiResult.Success).data
        is ApiResult.Error -> {
            // Show error message
            println("Error: ${(dailyStatsState as ApiResult.Error).message}")
            getMockFitnessData() // Fallback to mock data
        }
        is ApiResult.Loading -> {
            getMockFitnessData() // Show mock data while loading
        }
    }
    
    // Rest of your existing code...
    var selectedTab by remember { mutableStateOf(0) }
    // ... etc
}
```

### Example 2: Create a Workout Screen with API Integration

```kotlin
@Composable
fun CreateWorkoutScreen(
    onBackClick: () -> Unit,
    fitnessViewModel: FitnessViewModel = viewModel()
) {
    var workoutName by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    
    Button(onClick = {
        val workout = CreateWorkoutRequest(
            name = workoutName,
            category = "Cardio",
            duration = duration.toIntOrNull() ?: 0,
            calories = calories.toIntOrNull() ?: 0,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date())
        )
        
        fitnessViewModel.logWorkout(workout) {
            // Success callback
            onBackClick()
        }
    }) {
        Text("Log Workout")
    }
}
```

### Example 3: Display User Profile

```kotlin
@Composable
fun ProfileScreen(
    fitnessViewModel: FitnessViewModel = viewModel()
) {
    val profileState by fitnessViewModel.userProfileState.collectAsState()
    
    LaunchedEffect(Unit) {
        fitnessViewModel.getUserProfile()
    }
    
    when (val state = profileState) {
        is ApiResult.Success -> {
            val profile = state.data
            Column {
                Text("Name: ${profile.displayName}")
                Text("Email: ${profile.email}")
                Text("Age: ${profile.age ?: "Not set"}")
                Text("Weight: ${profile.weight ?: "Not set"} kg")
            }
        }
        is ApiResult.Error -> {
            Text("Error: ${state.message}")
        }
        is ApiResult.Loading -> {
            CircularProgressIndicator()
        }
    }
}
```

### Example 4: Show Token Balance (Blockchain)

```kotlin
@Composable
fun TokenBalanceCard(
    fitnessViewModel: FitnessViewModel = viewModel()
) {
    val tokenBalanceState by fitnessViewModel.tokenBalanceState.collectAsState()
    
    LaunchedEffect(Unit) {
        fitnessViewModel.getTokenBalance()
    }
    
    when (val state = tokenBalanceState) {
        is ApiResult.Success -> {
            val balance = state.data
            Card {
                Column {
                    Text("FitTokens: ${balance.balance}")
                    Text("Total Earned: ${balance.totalEarned}")
                }
            }
        }
        is ApiResult.Loading -> CircularProgressIndicator()
        is ApiResult.Error -> Text("Error loading balance")
    }
}
```

### Example 5: AI Calorie Prediction

```kotlin
@Composable
fun CaloriePredictionButton(
    fitnessViewModel: FitnessViewModel = viewModel()
) {
    var prediction by remember { mutableStateOf<CaloriePrediction?>(null) }
    val scope = rememberCoroutineScope()
    
    Button(onClick = {
        scope.launch {
            val request = PredictCaloriesRequest(
                steps = 5000,
                activeMinutes = 30,
                weight = 70.0f
            )
            
            when (val result = fitnessViewModel.predictCalories(request)) {
                is ApiResult.Success -> {
                    prediction = result.data
                }
                is ApiResult.Error -> {
                    println("Error: ${result.message}")
                }
                else -> {}
            }
        }
    }) {
        Text("Predict Calories")
    }
    
    prediction?.let {
        Text("Predicted: ${it.predictedCalories} calories")
        Text("Recommendation: ${it.recommendation}")
    }
}
```

---

## ⚙️ Configuration Steps

### Step 1: Update Backend URL

When you have your backend running, update the URL in `RetrofitClient.kt`:

```kotlin
// Current default (for Android Emulator)
private const val BASE_URL = "http://10.0.2.2:8000/"

// Change to your backend URL:
// For localhost on emulator: "http://10.0.2.2:8000/"
// For physical device: "http://YOUR_COMPUTER_IP:8000/"
// For production: "https://your-backend-domain.com/"
```

### Step 2: Test Connection

Create a simple test to verify backend connection:

```kotlin
@Composable
fun TestApiConnection() {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("Not tested") }
    
    Button(onClick = {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getDailyStats("Bearer test", 1)
                result = if (response.isSuccessful) {
                    "✅ Connected!"
                } else {
                    "❌ Error: ${response.code()}"
                }
            } catch (e: Exception) {
                result = "❌ Error: ${e.message}"
            }
        }
    }) {
        Text("Test API")
    }
    
    Text(result)
}
```

---

## 📝 Quick Start Checklist

When you finish building your backend:

1. ✅ **Sync Gradle** - Let Android Studio download dependencies
2. ⏳ **Start your backend services** (Express.js on port 8000)
3. ⏳ **Update BASE_URL** in `RetrofitClient.kt` with your backend URL
4. ⏳ **Test API connection** using the test function above
5. ⏳ **Replace mock data** in your screens with ViewModel calls
6. ⏳ **Build and run** your app

---

## 🐛 Common Issues & Solutions

### Issue: "Unable to resolve host"
**Solution:** Check BASE_URL is correct. For emulator use `10.0.2.2`, for device use your computer's IP address.

### Issue: "Authentication failed"
**Solution:** Make sure user is logged in via Firebase before making API calls.

### Issue: "Connection refused"
**Solution:** Ensure backend is running and accessible. Check firewall settings.

### Issue: "Cleartext HTTP not permitted"
**Solution:** For development, add to AndroidManifest.xml:
```xml
<application
    android:usesCleartextTraffic="true"
    ...>
```

---

## 🔄 State Management Pattern

All API calls follow this pattern:

```
Loading → API Call → Success/Error
   ↓           ↓           ↓
 Show      Execute    Update UI
Loading    Request   with Result
```

Using StateFlow ensures your UI automatically updates when data changes!

---

## 📊 Available API Endpoints

| Service | Method | Endpoint | Description |
|---------|--------|----------|-------------|
| **Stats** | GET | `/api/stats` | Get daily stats |
| | POST | `/api/stats` | Log new stats |
| **User** | GET | `/api/user/profile` | Get profile |
| | PUT | `/api/user/profile` | Update profile |
| **Workout** | GET | `/api/workouts` | Get workouts |
| | POST | `/api/workouts` | Log workout |
| **Goal** | GET | `/api/goals` | Get goals |
| | POST | `/api/goals` | Create goal |
| **Blockchain** | GET | `/api/blockchain/balance` | Get tokens |
| **AI** | POST | `/api/ai/predict-calories` | Predict calories |
| | POST | `/api/ai/recommend-workout` | Get recommendations |

---

## 🎉 You're Ready!

Everything is set up and ready to connect to your backend. Just:

1. Build your Express.js backend (microservices)
2. Give me the URL
3. I'll update `RetrofitClient.kt` for you
4. Your app will be fully functional!

---

**Next:** Build the backend with Express.js, MongoDB, Blockchain, and AI services! 🚀

