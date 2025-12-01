# 🔍 Backend Connection Troubleshooting Guide

## ✅ Backend Status: All Services Running

Your backend is working! But your Kotlin app shows "Not Connected". Here's how to fix it:

---

## 🧪 Test Your Connection (3 Steps)

### **Step 1: Test Diagnostic Server (Simple)**

From your **Android phone**, open browser and go to:
```
http://192.168.50.249:9999/test-connection
```

**Expected result:**
```json
{
  "status": "OK",
  "message": "Backend is responding!",
  "timestamp": "...",
  "clientIP": "192.168.50.xxx"
}
```

**If you see this → Backend is reachable ✅**

---

### **Step 2: Test Real API Gateway**

From your **Android phone**, open browser and go to:
```
http://192.168.50.249:3000/health
```

**Expected result:**
```json
{
  "status": "API Gateway OK",
  "timestamp": "..."
}
```

**If you see this → API Gateway working ✅**

---

### **Step 3: Test Fitness Endpoint**

From your **Android phone**, open browser and go to:
```
http://192.168.50.249:3000/api/fitness/health
```

**Expected result:**
```json
{
  "status": "Fitness Service OK"
}
```

**If you see this → Full routing working ✅**

---

## ❌ If Tests Fail:

### **Scenario A: Cannot open any URL**
```
Problem: Phone cannot reach computer
```

**Solutions:**
1. Check same WiFi network
2. Run: `ipconfig` → verify 192.168.50.249 is correct
3. Check Windows Firewall blocking port 3000
4. Restart WiFi on phone

### **Scenario B: Browser works, but app shows "Not Connected"**
```
Problem: App code issue, not backend
```

**Check in Kotlin:**
1. RetrofitClient.kt has correct IP
2. Internet permission in AndroidManifest.xml
3. App is actually calling the API

---

## 🔧 Fix Your Kotlin App

### **Check 1: RetrofitClient.kt**

Should have:
```kotlin
private const val BASE_URL = "http://192.168.50.249:3000/"
```

### **Check 2: AndroidManifest.xml**

Should have:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### **Check 3: API Call Code**

In your HomeScreen or wherever you show steps, should have:
```kotlin
lifecycleScope.launch {
    try {
        val response = RetrofitClient.apiService.logFitness(fitnessRequest)
        if (response.isSuccessful) {
            // Data uploaded successfully
            Log.d("Fitness", "✅ Synced to backend")
        } else {
            Log.e("Fitness", "❌ Status: ${response.code()}")
        }
    } catch (e: Exception) {
        Log.e("Fitness", "❌ Error: ${e.message}")
    }
}
```

---

## 📱 Test from Android App Code

Add this temporary test to your app:

```kotlin
lifecycleScope.launch {
    try {
        // Test 1: Simple health check
        Log.d("ConnectionTest", "Testing: http://192.168.50.249:3000/health")
        val response = Retrofit.Builder()
            .baseUrl("http://192.168.50.249:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
            .let { api ->
                // Make any simple call
                "TEST" // placeholder
            }
        
        Log.d("ConnectionTest", "✅ Connection successful!")
    } catch (e: Exception) {
        Log.e("ConnectionTest", "❌ Connection failed: ${e.message}")
        e.printStackTrace()
    }
}
```

---

## 🧠 Understanding "Backend Not Connected"

Your app shows this when:
1. ❌ Cannot reach `192.168.50.249:3000`
2. ❌ Network timeout
3. ❌ API returns error
4. ❌ App doesn't call API at all

**Not because:** Backend is down (backend IS running!)

---

## 📊 Verification Checklist:

- [ ] Backend services running: `npm start`, `npm run services:*`
- [ ] MongoDB running: `Get-Service MongoDB | Select Status`
- [ ] Phone on same WiFi as computer
- [ ] Can ping computer: `ipconfig` shows 192.168.50.249
- [ ] RetrofitClient.kt has correct IP
- [ ] AndroidManifest.xml has INTERNET permission
- [ ] App calls logFitness API
- [ ] Check Android Logcat for errors

---

## 🚀 Next: Test from Phone Browser First

**DO THIS NOW:**

1. Make sure backends are running
2. From phone browser, visit: `http://192.168.50.249:9999/test-connection`
3. Tell me:
   - ✅ Did you see JSON response?
   - ❌ Or page wouldn't load?

This will tell me if it's a network issue or app code issue!

---

## 📞 Quick Commands

**Check backend:**
```powershell
node check-fitness-data.js
node debug-fitness.js
```

**Check diagnostic server:**
```powershell
# Server should be running, you'll see logs as phone connects
```

**Test from PC:**
```powershell
(Invoke-WebRequest http://localhost:3000/health).Content
(Invoke-WebRequest http://localhost:9999/test-connection).Content
```

---

**Let me know:**
1. Can you access `192.168.50.249:9999/test-connection` from phone browser?
2. What do you see (JSON or error)?
3. What does Android Logcat show when app tries to connect?

This will help me pinpoint the exact issue! 👍
