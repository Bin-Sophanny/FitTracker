# 🛣️ BACKEND API ROUTES

## Base URL (Emulator)
```
http://10.0.2.2:3000
```

---

## 🔐 AUTHENTICATION ROUTES

### Register User
```
POST /api/auth/register
Body: {
  "firebaseUid": "string",
  "email": "user@example.com",
  "displayName": "John Doe"
}
Response: {
  "success": true,
  "user": { id, firebaseUid, email, displayName },
  "token": "jwt-token"
}
```

### Login User
```
POST /api/auth/login
Body: {
  "firebaseUid": "string",
  "email": "user@example.com"
}
Response: {
  "success": true,
  "user": { id, firebaseUid, email, displayName, walletAddress },
  "token": "jwt-token"
}
```

### Get User Profile
```
GET /api/auth/profile
Headers: Authorization: Bearer <token>
Response: {
  "id": "string",
  "firebaseUid": "string",
  "email": "user@example.com",
  "displayName": "string",
  "photoUrl": "string",
  "walletAddress": "string",
  "createdAt": "date",
  "updatedAt": "date"
}
```

### Update User Profile
```
PUT /api/auth/profile
Headers: Authorization: Bearer <token>
Body: {
  "displayName": "string",
  "photoUrl": "string"
}
```

---

## 📊 FITNESS ROUTES

### Log Fitness Data
```
POST /api/fitness/log
Headers: Authorization: Bearer <token>
Body: {
  "userId": "firebase-uid",
  "date": "2025-12-08T12:00:00.000Z",
  "steps": 8450,
  "calories": 420,
  "distance": 6.2,
  "activeMinutes": 45,
  "heartRate": 72
}
Response: {
  "success": true,
  "data": { _id, userId, date, steps, calories, distance, activeMinutes, heartRate, createdAt, updatedAt }
}
```

### Get Today's Fitness Data
```
GET /api/fitness/today/{userId}
Headers: Authorization: Bearer <token>
Response: {
  "_id": "string",
  "userId": "string",
  "date": "date",
  "steps": 0,
  "calories": 0,
  "distance": 0,
  "activeMinutes": 0
}
```

### Get Fitness Stats (Week/Month/Year)
```
GET /api/fitness/stats/{userId}/{range}
Headers: Authorization: Bearer <token>
Range options: week, month, year
Response: {
  "period": "week",
  "totalSteps": 65930,
  "totalCalories": 3310,
  "totalDistance": 48.4,
  "totalActiveMinutes": 365,
  "averageSteps": 9418,
  "averageCalories": 473,
  "data": [{ fitness records }]
}
```

### Get Fitness Summary
```
GET /api/fitness/summary/{userId}
Headers: Authorization: Bearer <token>
Response: {
  "totalEntries": 7,
  "totalSteps": 65930,
  "totalCalories": 3310,
  "totalDistance": 48.4,
  "totalActiveMinutes": 365,
  "lastUpdate": "2025-12-08T..."
}
```

---

## ⛓️ BLOCKCHAIN ROUTES

### Get User Rewards
```
GET /api/blockchain/rewards/{userAddress}
Response: {
  "userAddress": "0x...",
  "rewardBalance": "1000",
  "nftCount": 5,
  "network": "ganache"
}
```

### Award Rewards
```
POST /api/blockchain/rewards
Body: {
  "userAddress": "0x...",
  "amount": 100
}
Response: {
  "message": "Rewards awarded",
  "tokensAwarded": 100,
  "newBalance": "1100",
  "transactionHash": "0x..."
}
```

---

## ✅ HEALTH CHECK

### API Gateway Health
```
GET /health
Response: {
  "status": "API Gateway OK",
  "timestamp": "2025-12-08T03:57:14.644Z"
}
```

---

## 📋 COMPLETE URL EXAMPLES FOR KOTLIN

```kotlin
// Base
const val BASE_URL = "http://10.0.2.2:3000/"

// Auth
POST http://10.0.2.2:3000/api/auth/register
POST http://10.0.2.2:3000/api/auth/login
GET http://10.0.2.2:3000/api/auth/profile
PUT http://10.0.2.2:3000/api/auth/profile

// Fitness
POST http://10.0.2.2:3000/api/fitness/log
GET http://10.0.2.2:3000/api/fitness/today/{userId}
GET http://10.0.2.2:3000/api/fitness/stats/{userId}/week
GET http://10.0.2.2:3000/api/fitness/stats/{userId}/month
GET http://10.0.2.2:3000/api/fitness/summary/{userId}

// Blockchain
GET http://10.0.2.2:3000/api/blockchain/rewards/{address}
POST http://10.0.2.2:3000/api/blockchain/rewards

// Health
GET http://10.0.2.2:3000/health
```

---

## 🔑 REQUIRED HEADERS

All protected routes need:
```
Authorization: Bearer <firebase-jwt-token>
Content-Type: application/json
```

---

## 📝 COPY TO ApiService.kt

```kotlin
interface ApiService {
    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @GET("api/auth/profile")
    suspend fun getProfile(@Header("Authorization") authorization: String): UserProfile
    
    @PUT("api/auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: UpdateProfileRequest
    ): UserProfile
    
    // Fitness
    @POST("api/fitness/log")
    suspend fun logFitness(
        @Header("Authorization") authorization: String,
        @Body request: LogFitnessRequest
    ): FitnessData
    
    @GET("api/fitness/today/{userId}")
    suspend fun getTodayFitness(
        @Header("Authorization") authorization: String,
        @Path("userId") userId: String
    ): FitnessData
    
    @GET("api/fitness/stats/{userId}/{range}")
    suspend fun getStats(
        @Header("Authorization") authorization: String,
        @Path("userId") userId: String,
        @Path("range") range: String
    ): StatsResponse
    
    @GET("api/fitness/summary/{userId}")
    suspend fun getSummary(
        @Header("Authorization") authorization: String,
        @Path("userId") userId: String
    ): SummaryResponse
    
    // Blockchain
    @GET("api/blockchain/rewards/{address}")
    suspend fun getRewards(@Path("address") address: String): RewardsResponse
    
    @POST("api/blockchain/rewards")
    suspend fun awardRewards(@Body request: AwardRequest): RewardResponse
}
```

---

## 🧪 TEST IN BROWSER/POSTMAN

```
Health Check:
GET http://localhost:3000/health

Login (with Firebase):
POST http://localhost:3000/api/auth/login
Body: {
  "firebaseUid": "ghnrk8yLcGOn3rmaZFlCPGliJUI3",
  "email": "sophannykind@gmail.com"
}

Get Stats:
GET http://localhost:3000/api/fitness/stats/ghnrk8yLcGOn3rmaZFlCPGliJUI3/week
Headers: Authorization: Bearer <your-token>
```
