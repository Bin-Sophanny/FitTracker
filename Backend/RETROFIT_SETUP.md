# Retrofit Setup Instructions

## Your Computer IP: 192.168.50.249

### Step 1: Update RetrofitClient.kt

Replace the `BASE_URL` in your `RetrofitClient.kt` with:

```kotlin
package com.example.fittrack.network

import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.50.249:3000/"
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
```

---

## Step 2: Add Dependencies to build.gradle (Module: app)

Open your `app/build.gradle` file and add these dependencies in the `dependencies` block:

```gradle
dependencies {
    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    
    // GSON
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1'
    
    // ViewModel and LiveData
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.1'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.1'
    
    // Existing dependencies (keep these)
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.compose.ui:ui:1.5.4'
    implementation 'androidx.compose.ui:ui-graphics:1.5.4'
    implementation 'androidx.compose.material3:material3:1.1.1'
}
```

After adding, click **"Sync Now"** in Android Studio.

---

## Step 3: Add Internet Permission to AndroidManifest.xml

Open `app/src/main/AndroidManifest.xml` and add this line before the `<application>` tag:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Add this line -->
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        ...
    </application>

</manifest>
```

---

## Step 4: Copy ApiService.kt to Your Project

Copy the file `KOTLIN_INTEGRATION.md` which contains `ApiService.kt` and add it to:
`app/src/main/java/com/example/fittrack/network/ApiService.kt`

---

## Testing the Connection

After syncing gradle, run your app on an emulator or device. Check **Logcat** for any errors.

To test manually, add this to a button in your app:

```kotlin
lifecycleScope.launch {
    try {
        val response = RetrofitClient.apiService.getHealth()
        Log.d("API Test", "Success: $response")
    } catch (e: Exception) {
        Log.e("API Test", "Error: ${e.message}")
    }
}
```

---

## Your Backend IP Configuration

```
API Gateway: http://192.168.50.249:3000
User Service: http://192.168.50.249:3001
Fitness Service: http://192.168.50.249:3002
Blockchain Service: http://192.168.50.249:3003
```

All requests will go through the API Gateway on port 3000.

---

## Next Steps in Android Studio

1. ✅ Update RetrofitClient.kt with your IP
2. ✅ Add dependencies to build.gradle
3. ✅ Add internet permission to AndroidManifest.xml
4. ⏳ Update AuthViewModel to use real API calls
5. ⏳ Update HomeScreen to use real API calls
6. ⏳ Update ProfileScreen to use real API calls
7. ⏳ Test the app

Ready to proceed with updating the screens?
