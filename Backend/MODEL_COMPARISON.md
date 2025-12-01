# 📋 Backend vs Kotlin Model Comparison

## 🔴 BACKEND MODELS (Node.js)

### FitnessData Model
```javascript
{
  userId: String (required),
  date: Date (required),
  steps: Number (default: 0),
  calories: Number (default: 0),
  distance: Number (default: 0),
  activeMinutes: Number (default: 0),
  heartRate: Number (optional),
  createdAt: Date (auto: now),
  updatedAt: Date (auto: now)
}
```

**Example JSON:**
```json
{
  "_id": "692d50de9f9f560f767e8e8a",
  "userId": "test-user-1764577502785",
  "date": "2025-11-30T17:00:00.000Z",
  "steps": 2240,
  "calories": 183,
  "distance": 2.16,
  "activeMinutes": 131,
  "heartRate": 67,
  "createdAt": "2025-12-01T08:25:02.851Z",
  "updatedAt": "2025-12-01T08:25:02.851Z"
}
```

### User Model
```javascript
{
  firebaseUid: String,
  email: String (unique, required),
  displayName: String,
  photoUrl: String,
  createdAt: Date (auto: now),
  updatedAt: Date (auto: now)
}
```

---

## 🟢 KOTLIN MODELS (What You Need)

Based on the backend, here's what your Kotlin models **MUST** have:

### FitnessData.kt
```kotlin
data class FitnessData(
    val _id: String? = null,           // MongoDB ID
    val userId: String,                 // Required
    val date: String,                   // ISO format: "2025-12-01T08:25:02.851Z"
    val steps: Int,                     // Required
    val calories: Int,                  // Required
    val distance: Float,                // Required
    val activeMinutes: Int,             // Required
    val heartRate: Int? = null,         // Optional
    val createdAt: String? = null,      // Auto from backend
    val updatedAt: String? = null       // Auto from backend
)
```

### FitnessLogRequest.kt (What you SEND to backend)
```kotlin
data class FitnessLogRequest(
    val userId: String,                 // Your Firebase UID
    val date: String,                   // Current ISO date
    val steps: Int,                     // Steps today
    val calories: Int,                  // Calories burned
    val distance: Float,                // Distance in km
    val activeMinutes: Int,             // Active minutes today
    val heartRate: Int? = null          // Optional heart rate
)
```

### User.kt
```kotlin
data class User(
    val id: String? = null,             // MongoDB _id
    val firebaseUid: String,            // Firebase UID
    val email: String,                  // Email
    val displayName: String? = null,    // Display name
    val photoUrl: String? = null,       // Profile photo URL
    val createdAt: String? = null,      // Auto from backend
    val updatedAt: String? = null       // Auto from backend
)
```

### DailyStats.kt (For UI display)
```kotlin
data class DailyStats(
    val date: String,                   // "2025-12-01"
    val steps: Int,                     // 8450
    val calories: Int,                  // 420
    val distance: Float,                // 6.2
    val activeMinutes: Int              // 45
)
```

---

## ✅ API Endpoints (What Your Kotlin App Calls)

### Login Endpoint
```
POST /api/auth/login
Body: {
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "123",
    "email": "user@example.com",
    "displayName": "John Doe"
  }
}
```

### Log Fitness Data Endpoint
```
POST /api/fitness/log
Headers: Authorization: Bearer <token>
Body: {
  "userId": "firebase-uid",
  "date": "2025-12-01T08:25:02.851Z",
  "steps": 2240,
  "calories": 183,
  "distance": 2.16,
  "activeMinutes": 131,
  "heartRate": 67
}

Response:
{
  "success": true,
  "data": {
    "_id": "692d50de9f9f560f767e8e8a",
    "userId": "firebase-uid",
    "date": "2025-11-30T17:00:00.000Z",
    "steps": 2240,
    "calories": 183,
    "distance": 2.16,
    "activeMinutes": 131,
    "heartRate": 67,
    "createdAt": "2025-12-01T08:25:02.851Z",
    "updatedAt": "2025-12-01T08:25:02.851Z"
  }
}
```

### Get Today's Fitness Endpoint
```
GET /api/fitness/today/{userId}
Headers: Authorization: Bearer <token>

Response:
{
  "_id": "692d50de9f9f560f767e8e8a",
  "userId": "firebase-uid",
  "date": "2025-11-30T17:00:00.000Z",
  "steps": 2240,
  "calories": 183,
  "distance": 2.16,
  "activeMinutes": 131,
  "heartRate": 67,
  "createdAt": "2025-12-01T08:25:02.851Z",
  "updatedAt": "2025-12-01T08:25:02.851Z"
}
```

---

## 🔍 Key Differences to Watch For

| Field | Backend | Kotlin | Notes |
|-------|---------|--------|-------|
| `userId` | String | String | Use Firebase UID |
| `date` | Date (stored as ISO) | String (ISO format) | Send: "2025-12-01T08:25:02.851Z" |
| `steps` | Number | Int | Integer only |
| `calories` | Number | Int | Integer only |
| `distance` | Number | Float | Can have decimals (2.16 km) |
| `activeMinutes` | Number | Int | Integer only |
| `heartRate` | Number (optional) | Int? (nullable) | Optional field |
| `_id` | ObjectId | String? (nullable) | MongoDB ID, don't send from client |
| `createdAt` | Date (auto) | String? (nullable) | Don't send from client |
| `updatedAt` | Date (auto) | String? (nullable) | Don't send from client |

---

## 📤 Example: Upload Steps from Kotlin

```kotlin
// Step 1: Get current user
val currentUser = FirebaseAuth.getInstance().currentUser ?: return
val token = currentUser.getIdToken(false).await().token ?: ""

// Step 2: Create request with backend model
val request = FitnessLogRequest(
    userId = currentUser.uid,
    date = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        Locale.getDefault()
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date()),
    steps = 2240,
    calories = 183,
    distance = 2.16f,
    activeMinutes = 131,
    heartRate = 67
)

// Step 3: Send to backend
val response = apiService.logFitness(
    authorization = "Bearer $token",
    request = request
)

// Step 4: Get response
if (response.success) {
    Log.d("Fitness", "✅ Data saved: ${response.data}")
} else {
    Log.e("Fitness", "❌ Failed")
}
```

---

## 🚀 Quick Checklist

- [ ] All Kotlin data classes match backend schema
- [ ] Date fields use ISO 8601 format (String)
- [ ] Float fields for distance (not Int)
- [ ] Token included in Authorization header
- [ ] userId is Firebase UID, not email
- [ ] Don't send _id, createdAt, updatedAt from client
- [ ] Optional fields use nullable types (Int?, String?)

---

## 📞 Questions?

If fields don't match between your Kotlin app and backend:
1. Check this guide first
2. Update Kotlin models to match backend schema
3. Test with: `node test-fitness-upload.js`

