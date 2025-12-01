# STEP 4: Connect Kotlin Frontend to Backend

Complete guide to integrate your Kotlin app with FitTrack backend services.

---

## 📋 Files Already Created For You:

1. ✅ `ApiService.kt` - All API endpoints
2. ✅ `RetrofitClient.kt` - HTTP client setup

---

## 🔧 INTEGRATION STEPS:

### Step 1: Add Dependencies to build.gradle (Module: app)

Open: `app/build.gradle`

Add to `dependencies { }` section:

```gradle
dependencies {
    // Retrofit (HTTP Client)
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    
    // GSON (JSON parsing)
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Coroutines (async operations)
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1'
    
    // Lifecycle (for viewModelScope)
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.2'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2'
}
```

Then click **"Sync Now"** in Android Studio.

---

### Step 2: Add Internet Permission

Open: `AndroidManifest.xml`

Add before `<application>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

### Step 3: Configure Backend IP Address

**IMPORTANT!** Update the base URL in `RetrofitClient.kt`

#### For Android Emulator:
```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/"
```

#### For Real Device:
1. Find your computer's IP address:
   ```powershell
   ipconfig
   ```
   Look for "IPv4 Address" (e.g., `192.168.1.100`)

2. Update `RetrofitClient.kt`:
   ```kotlin
   private const val BASE_URL = "http://192.168.1.100:3000/"
   ```

3. Make sure phone is on **same WiFi network** as your computer

---

### Step 4: Update AuthViewModel

Replace your login function in `AuthViewModel.kt`:

**BEFORE (Mock):**
```kotlin
fun loginWithFirebase(email: String, password: String) {
    // Mock login
}
```

**AFTER (Real API):**
```kotlin
fun loginWithFirebase(email: String, password: String) {
    viewModelScope.launch {
        _authState.value = AuthState.Loading
        try {
            // 1. Firebase authenticates
            val firebaseUser = auth.signInWithEmailAndPassword(email, password).await().user
            
            if (firebaseUser != null) {
                // 2. Login to backend
                val response = RetrofitClient.apiService.loginUser(
                    LoginRequest(
                        firebaseUid = firebaseUser.uid,
                        email = firebaseUser.email ?: email
                    )
                )
                
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null && authResponse.success) {
                        // Save token and user info
                        saveTokenToPreferences(authResponse.token)
                        saveUserToPreferences(authResponse.user)
                        
                        _authState.value = AuthState.Success(authResponse.user)
                    } else {
                        _authState.value = AuthState.Error("Backend login failed")
                    }
                } else {
                    _authState.value = AuthState.Error("${response.code()}: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Login failed")
        }
    }
}
```

---

### Step 5: Update HomeScreen

Replace mock data with real API calls:

```kotlin
var fitnessData by remember { mutableStateOf<FitnessData?>(null) }
var isLoading by remember { mutableStateOf(true) }

LaunchedEffect(Unit) {
    try {
        val response = RetrofitClient.apiService.getTodayFitness(
            userId = userId,
            token = "Bearer $authToken"
        )
        fitnessData = response.body()
    } catch (e: Exception) {
        println("Error: ${e.message}")
    } finally {
        isLoading = false
    }
}
```

---

### Step 6: Update StatsScreen

```kotlin
var statsData by remember { mutableStateOf<StatsResponse?>(null) }

LaunchedEffect(selectedRange) {
    try {
        val response = RetrofitClient.apiService.getStats(
            userId = userId,
            range = selectedRange,
            token = "Bearer $authToken"
        )
        statsData = response.body()
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
```

---

### Step 7: Update ProfileScreen

```kotlin
var userProfile by remember { mutableStateOf<UserProfile?>(null) }

LaunchedEffect(Unit) {
    try {
        val response = RetrofitClient.apiService.getProfile(
            userId = userId,
            token = "Bearer $authToken"
        )
        userProfile = response.body()
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}

Button(onClick = {
    viewModelScope.launch {
        val response = RetrofitClient.apiService.updateProfile(
            userId = userId,
            token = "Bearer $authToken",
            request = UpdateProfileRequest(displayName = newDisplayName)
        )
        if (response.isSuccessful) {
            userProfile = response.body()
        }
    }
}) {
    Text("Save")
}
```

---

## 🧪 Testing

### Start Backend Services:

```powershell
# Terminal 1 - API Gateway
npm start

# Terminal 2 - User Service
npm run services:user

# Terminal 3 - Fitness Service
npm run services:fitness
```

### Verify Services:
```powershell
curl http://localhost:3000/health
curl http://localhost:3001/health
curl http://localhost:3002/health
```

### Run Kotlin App:
- Update base URL in `RetrofitClient.kt`
- Run on emulator or device
- Try login

---

## ✅ Checklist

- [ ] Added Retrofit dependencies
- [ ] Added Internet permission
- [ ] Created `ApiService.kt`
- [ ] Created `RetrofitClient.kt`
- [ ] Updated AuthViewModel
- [ ] Updated HomeScreen
- [ ] Updated StatsScreen
- [ ] Updated ProfileScreen
- [ ] Backend services running
- [ ] Base URL configured correctly

---

## 🐛 Common Issues

**"Unable to connect"**
- Check backend is running: `curl http://localhost:3000/health`
- Verify base URL in `RetrofitClient.kt`
- If real device: phone must be on same WiFi as computer

**"Invalid JSON"**
- Verify backend service is returning JSON
- Test: `curl http://localhost:3001/health`

**"Authentication failed"**
- Check Firebase credentials in `.env`
- Verify User Service is running

---

## 🎉 Done!

Your full-stack FitTrack app is now fully integrated! 

Your app can now:
✅ Register & login with Firebase
✅ Get fitness data from backend
✅ View statistics
✅ Update profile
✅ Track blockchain rewards

**Ready to deploy!** 🚀
