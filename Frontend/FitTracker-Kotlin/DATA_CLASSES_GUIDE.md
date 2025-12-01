# FitTracker Data Classes & API Integration Guide

## 📁 Project Structure

```
com.example.fittrack/
├── data/
│   ├── model/              # All data classes
│   │   ├── DailyStats.kt
│   │   ├── UserProfile.kt
│   │   ├── Workout.kt
│   │   ├── Goal.kt
│   │   ├── Blockchain.kt
│   │   └── AI.kt
│   ├── api/                # API configuration
│   │   ├── ApiService.kt
│   │   └── RetrofitClient.kt
│   └── repository/         # Repository pattern
│       └── FitTrackRepository.kt
```

---

## 📦 Data Classes Overview

### 1. **DailyStats** - Daily Fitness Statistics
```kotlin
data class DailyStats(
    val date: String,           // "2025-11-24"
    val steps: Int,             // 8450
    val calories: Int,          // 420
    val distance: Float,        // 6.2
    val activeMinutes: Int      // 45
)
```

### 2. **UserProfile** - User Information
```kotlin
data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val age: Int? = null,
    val weight: Float? = null,
    val height: Float? = null,
    val profileImage: String? = null
)
```

### 3. **Workout** - Exercise Activity
```kotlin
data class Workout(
    val id: String,
    val name: String,
    val category: String,
    val duration: Int,
    val calories: Int,
    val date: String
)
```

### 4. **Goal** - Fitness Goals
```kotlin
data class Goal(
    val id: String,
    val title: String,
    val type: GoalType,
    val targetValue: Int,
    val currentValue: Int,
    val deadline: String,
    val isCompleted: Boolean
)
```

### 5. **TokenBalance** - Blockchain Rewards
```kotlin
data class TokenBalance(
    val balance: Int,
    val totalEarned: Int,
    val transactions: List<TokenTransaction>
)
```

---

## 🔧 How to Use in Your Screens

### Example 1: Update HomeScreen to Use API

**Before (Mock Data):**
```kotlin
// HomeScreen.kt
val fitnessData = getMockFitnessData()
```

**After (Real API):**
```kotlin
// HomeScreen.kt
import com.example.fittrack.data.model.DailyStats
import com.example.fittrack.data.repository.FitTrackRepository
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    val repository = remember { FitTrackRepository() }
    var fitnessData by remember { mutableStateOf<List<DailyStats>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = repository.getDailyStats(limit = 5)
                if (response.isSuccessful) {
                    fitnessData = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else {
        MainScreen(userName, fitnessData)
    }
}
```

---

### Example 2: Log a Workout

```kotlin
import com.example.fittrack.data.model.CreateWorkoutRequest
import com.example.fittrack.data.repository.FitTrackRepository

val repository = FitTrackRepository()
val scope = rememberCoroutineScope()

Button(onClick = {
    scope.launch {
        val workout = CreateWorkoutRequest(
            name = "Running",
            category = "Cardio",
            duration = 30,
            calories = 300,
            date = "2025-11-24"
        )
        
        try {
            val response = repository.logWorkout(workout)
            if (response.isSuccessful) {
                // Success! Show message
                println("Workout logged: ${response.body()}")
            }
        } catch (e: Exception) {
            // Handle error
            println("Error: ${e.message}")
        }
    }
}) {
    Text("Log Workout")
}
```

---

### Example 3: Get User Profile

```kotlin
import com.example.fittrack.data.repository.FitTrackRepository

val repository = FitTrackRepository()
var userProfile by remember { mutableStateOf<UserProfile?>(null) }

LaunchedEffect(Unit) {
    try {
        val response = repository.getUserProfile()
        if (response.isSuccessful) {
            userProfile = response.body()
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}

userProfile?.let { profile ->
    Text("Name: ${profile.displayName}")
    Text("Email: ${profile.email}")
    Text("Age: ${profile.age ?: "Not set"}")
    Text("Weight: ${profile.weight ?: "Not set"} kg")
}
```

---

### Example 4: Create a Goal

```kotlin
import com.example.fittrack.data.model.CreateGoalRequest

val repository = FitTrackRepository()

Button(onClick = {
    scope.launch {
        val goal = CreateGoalRequest(
            title = "Walk 10000 steps daily",
            type = "STEPS",
            targetValue = 10000,
            deadline = "2025-12-31"
        )
        
        val response = repository.createGoal(goal)
        if (response.isSuccessful) {
            println("Goal created!")
        }
    }
}) {
    Text("Create Goal")
}
```

---

### Example 5: Check Token Balance

```kotlin
import com.example.fittrack.data.repository.FitTrackRepository

val repository = FitTrackRepository()
var tokenBalance by remember { mutableStateOf<TokenBalance?>(null) }

LaunchedEffect(Unit) {
    val response = repository.getTokenBalance()
    if (response.isSuccessful) {
        tokenBalance = response.body()
    }
}

tokenBalance?.let { balance ->
    Text("FitTokens: ${balance.balance}")
    Text("Total Earned: ${balance.totalEarned}")
}
```

---

### Example 6: AI Calorie Prediction

```kotlin
import com.example.fittrack.data.model.PredictCaloriesRequest

val repository = FitTrackRepository()

Button(onClick = {
    scope.launch {
        val request = PredictCaloriesRequest(
            steps = 5000,
            activeMinutes = 30,
            weight = 70.0f,
            exerciseType = "walking"
        )
        
        val response = repository.predictCalories(request)
        if (response.isSuccessful) {
            val prediction = response.body()
            println("Predicted Calories: ${prediction?.predictedCalories}")
            println("Recommendation: ${prediction?.recommendation}")
        }
    }
}) {
    Text("Predict Calories")
}
```

---

## 🔐 Authentication

All API calls automatically include Firebase authentication token. The `FitTrackRepository` handles this for you:

```kotlin
private suspend fun getAuthToken(): String? {
    val tokenResult = auth.currentUser?.getIdToken(true)?.await()
    return tokenResult?.token?.let { "Bearer $it" }
}
```

**Make sure user is logged in via Firebase before making API calls!**

---

## ⚙️ Setup Required

### 1. Add Dependencies to `build.gradle.kts` (app level):

```kotlin
dependencies {
    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Existing Firebase dependencies...
}
```

### 2. Update Backend URL in `RetrofitClient.kt`:

```kotlin
// For Android Emulator (connects to localhost on your computer)
private const val BASE_URL = "http://10.0.2.2:8000/"

// For Physical Device (replace with your computer's IP address)
private const val BASE_URL = "http://192.168.1.100:8000/"
```

---

## 📝 Next Steps

1. ✅ **Data classes created** - All models ready
2. ✅ **API Service defined** - All endpoints configured
3. ✅ **Repository created** - Easy-to-use methods
4. ⏳ **Build backend** - Create Express.js services
5. ⏳ **Update screens** - Replace mock data with API calls

---

## 🎯 Quick Integration Checklist

- [ ] Add Retrofit dependencies to `build.gradle.kts`
- [ ] Update `BASE_URL` in `RetrofitClient.kt`
- [ ] Remove `DailyStats` from `HomeScreen.kt` (now imported from `data.model`)
- [ ] Replace `getMockFitnessData()` with `repository.getDailyStats()`
- [ ] Test API connection with backend
- [ ] Update all screens to use real API data

---

## 🐛 Troubleshooting

**Network Error:**
- Check if backend is running on correct port
- Verify BASE_URL is correct
- Check Android manifest has internet permission

**Authentication Error:**
- Make sure user is logged in via Firebase
- Check Firebase token is valid
- Verify backend receives correct token format

---

Need help integrating? Just ask! 🚀

