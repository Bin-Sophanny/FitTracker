# FitTrack - Complete Setup Guide for Beginners

This guide walks you through setting up MongoDB, Firebase, Blockchain, and connecting your Kotlin frontend.

---

## 🗄️ STEP 1: Install MongoDB Locally

### What is MongoDB?
MongoDB is a database where your backend services store data (users, fitness records).

### Installation Steps:

#### **Windows:**

1. **Download MongoDB Community Edition**
   - Go to: https://www.mongodb.com/try/download/community
   - Select **Windows** and download the `.msi` file

2. **Run the Installer**
   - Double-click the downloaded `.msi` file
   - Click **Next** on all screens
   - At "Service Configuration" screen:
     - Check ✓ "Install MongoDB as a Service"
     - Check ✓ "Run MongoDB as a Service"
   - Click **Install**

3. **Verify Installation**
   ```powershell
   mongod --version
   ```
   You should see a version number output.

4. **Start MongoDB Service**
   ```powershell
   # MongoDB starts automatically as a Windows Service
   # To check if it's running:
   Get-Service MongoDB | Select-Object Status
   ```

5. **Verify MongoDB is Running**
   ```powershell
   mongo
   ```
   You should see:
   ```
   MongoDB shell version v4.x.x
   connecting to: mongodb://127.0.0.1:27017/?compressors=disabled&gssapiServiceName=mongodb
   ```
   Type `exit` to quit.

### MongoDB Data Folders:
- **Data location:** `C:\Program Files\MongoDB\Server\4.4\data`
- **Logs location:** `C:\Program Files\MongoDB\Server\4.4\log\`

### Verify Both Databases Exist:
MongoDB creates databases automatically when services first connect.

```powershell
mongo
# In the mongo shell:
show dbs
# You'll see default databases
exit
```

---

## 🔐 STEP 2: Configure Firebase

### What is Firebase?
Firebase handles user authentication (login/signup). Your Kotlin app uses it, backend verifies tokens.

### Setup Steps:

#### **1. Create a Firebase Project**

1. Go to: https://console.firebase.google.com/
2. Click **"Create a project"**
3. Enter project name: `FitTrack`
4. Click **Continue**
5. Choose location (default is fine)
6. Click **Create project** (wait 1-2 minutes)

#### **2. Enable Email/Password Authentication**

1. In Firebase Console, go to **Authentication** (left menu)
2. Click **Get started**
3. Click on **Email/Password** provider
4. Toggle **Enable** ✓
5. Click **Save**

#### **3. Get Firebase Admin SDK Credentials**

1. Go to **Project Settings** (gear icon, top right)
2. Click **Service Accounts** tab
3. Click **Generate New Private Key** button
4. A JSON file downloads - **save it somewhere safe**

#### **4. Update Your .env File**

Open `c:/Users/Phanny/Desktop/FitTrack/Backend/.env`

Add these lines (from the JSON file you downloaded):

```
FIREBASE_PROJECT_ID=your-project-id-from-json
FIREBASE_PRIVATE_KEY=your_private_key_from_json
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@your-project.iam.gserviceaccount.com
```

**How to find these in your JSON file:**
```json
{
  "type": "service_account",
  "project_id": "fittrack-xxxxx",           ← FIREBASE_PROJECT_ID
  "private_key": "-----BEGIN PRIVATE KEY-----\n...",  ← FIREBASE_PRIVATE_KEY
  "client_email": "firebase-adminsdk-xxxxx@..."  ← FIREBASE_CLIENT_EMAIL
}
```

#### **5. Test Firebase Connection**

Run User Service:
```powershell
npm run services:user
```

If no errors appear and it says `🚀 User Service running on http://localhost:3001`, Firebase is configured correctly!

---

## ⛓️ STEP 3: Create Smart Contracts for Blockchain

### What is a Smart Contract?
A smart contract is code running on Ethereum that handles rewards and NFTs.

### Prerequisites:
- Ethereum wallet with Sepolia testnet funds
- Solidity knowledge (basic)

### Quick Setup (Using Pre-built Contracts):

#### **Option A: Use OpenZeppelin (Easiest)**

1. **Create a new folder:**
   ```powershell
   mkdir c:/Users/Phanny/Desktop/FitTrack/Backend/contracts
   cd c:/Users/Phanny/Desktop/FitTrack/Backend/contracts
   ```

2. **Initialize Hardhat (Ethereum development framework):**
   ```powershell
   npm init -y
   npm install --save-dev hardhat @nomicfoundation/hardhat-toolbox
   npx hardhat
   ```
   - Select: **Create a JavaScript project**
   - Choose default locations
   - Install dependencies when asked

3. **Create Reward Token Contract**

   Create file: `contracts/FitnessReward.sol`

   ```solidity
   // SPDX-License-Identifier: MIT
   pragma solidity ^0.8.0;

   import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
   import "@openzeppelin/contracts/access/Ownable.sol";

   contract FitnessReward is ERC20, Ownable {
       constructor() ERC20("FitnessReward", "FIT") {
           _mint(msg.sender, 1000000 * 10 ** decimals());
       }

       function mint(address to, uint256 amount) public onlyOwner {
           _mint(to, amount);
       }
   }
   ```

4. **Create NFT Achievement Contract**

   Create file: `contracts/AchievementNFT.sol`

   ```solidity
   // SPDX-License-Identifier: MIT
   pragma solidity ^0.8.0;

   import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
   import "@openzeppelin/contracts/access/Ownable.sol";
   import "@openzeppelin/contracts/utils/Counters.sol";

   contract AchievementNFT is ERC721, Ownable {
       using Counters for Counters.Counter;
       Counters.Counter private tokenIdCounter;

       constructor() ERC721("FitTrack Achievement", "FTA") {}

       function mint(address to, string memory uri) public onlyOwner returns (uint256) {
           tokenIdCounter.increment();
           uint256 tokenId = tokenIdCounter.current();
           _safeMint(to, tokenId);
           return tokenId;
       }
   }
   ```

5. **Compile and Deploy**

   ```powershell
   npx hardhat compile
   ```

   Create deployment script: `scripts/deploy.js`

   ```javascript
   async function main() {
       const RewardToken = await ethers.getContractFactory("FitnessReward");
       const reward = await RewardToken.deploy();
       console.log("Reward Token deployed to:", reward.address);

       const NFT = await ethers.getContractFactory("AchievementNFT");
       const nft = await NFT.deploy();
       console.log("NFT Contract deployed to:", nft.address);
   }

   main().catch((error) => {
       console.error(error);
       process.exitCode = 1;
   });
   ```

   Deploy to Sepolia:
   ```powershell
   npx hardhat run scripts/deploy.js --network sepolia
   ```

6. **Save Contract Addresses**

   Add to `.env`:
   ```
   BLOCKCHAIN_CONTRACT_ADDRESS=0x...    (Reward Token address)
   NFT_CONTRACT_ADDRESS=0x...          (NFT address)
   ```

#### **Option B: Skip Smart Contracts for Now**

If this is too complex, you can:
1. Skip smart contract deployment
2. Just update `.env` with placeholder addresses:
   ```
   BLOCKCHAIN_CONTRACT_ADDRESS=0x0000000000000000000000000000000000000000
   NFT_CONTRACT_ADDRESS=0x0000000000000000000000000000000000000000
   ```
3. Continue with frontend integration
4. Deploy contracts later when you're ready

---

## 📱 STEP 4: Connect Kotlin Frontend to Backend

### What We're Doing:
Replacing mock data in your Kotlin app with real API calls to your backend.

### Step-by-Step:

#### **1. Create Retrofit API Service**

In your Kotlin project, create: `app/src/main/java/com/example/fittrack/api/ApiService.kt`

```kotlin
package com.example.fittrack.api

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // ========== AUTHENTICATION ==========
    @POST("/api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>

    @GET("/api/auth/profile/{userId}")
    suspend fun getProfile(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<UserProfile>

    @PUT("/api/auth/profile/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<UserProfile>

    // ========== FITNESS DATA ==========
    @GET("/api/fitness/today/{userId}")
    suspend fun getTodayFitness(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<FitnessData>

    @GET("/api/fitness/stats/{userId}/{range}")
    suspend fun getStats(
        @Path("userId") userId: String,
        @Path("range") range: String, // "week", "month", "year"
        @Header("Authorization") token: String
    ): Response<StatsResponse>

    @POST("/api/fitness/log")
    suspend fun logFitness(
        @Header("Authorization") token: String,
        @Body request: LogFitnessRequest
    ): Response<FitnessData>

    @GET("/api/fitness/summary/{userId}")
    suspend fun getFitnessSummary(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<SummaryResponse>

    // ========== BLOCKCHAIN ==========
    @GET("/api/blockchain/rewards/{userAddress}")
    suspend fun getRewards(
        @Path("userAddress") userAddress: String,
        @Header("Authorization") token: String
    ): Response<RewardsResponse>
}

// ========== DATA MODELS ==========
data class RegisterRequest(
    val firebaseUid: String,
    val email: String,
    val displayName: String
)

data class LoginRequest(
    val firebaseUid: String,
    val email: String
)

data class AuthResponse(
    val success: Boolean,
    val user: UserProfile,
    val token: String
)

data class UserProfile(
    val id: String,
    val firebaseUid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val walletAddress: String? = null
)

data class UpdateProfileRequest(
    val displayName: String? = null,
    val photoUrl: String? = null,
    val walletAddress: String? = null
)

data class FitnessData(
    val userId: String,
    val date: String,
    val steps: Int,
    val calories: Int,
    val distance: Float,
    val activeMinutes: Int
)

data class LogFitnessRequest(
    val userId: String,
    val date: String,
    val steps: Int,
    val calories: Int,
    val distance: Float,
    val activeMinutes: Int
)

data class StatsResponse(
    val period: String,
    val totalSteps: Int,
    val totalCalories: Int,
    val totalDistance: Float,
    val totalActiveMinutes: Int,
    val averageSteps: Int,
    val averageCalories: Int,
    val data: List<FitnessData>
)

data class SummaryResponse(
    val totalEntries: Int,
    val totalSteps: Int,
    val totalCalories: Int,
    val totalDistance: Float,
    val totalActiveMinutes: Int
)

data class RewardsResponse(
    val userAddress: String,
    val rewardBalance: String,
    val nftCount: Int,
    val network: String
)
```

#### **2. Create Retrofit Client**

Create: `app/src/main/java/com/example/fittrack/api/RetrofitClient.kt`

```kotlin
package com.example.fittrack.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.x.x:3000/" // Replace with your backend IP
    // For localhost testing: "http://10.0.2.2:3000/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
```

**Important:** Replace `192.168.x.x` with:
- Your computer's local IP address if testing on real device
- `10.0.2.2` if testing on Android emulator
- Find your IP: Run `ipconfig` in PowerShell

#### **3. Update HomeScreen to Use Real API**

In `HomeScreen.kt`, replace mock data with API calls:

```kotlin
// OLD - Mock data
val mockData = getMockFitnessData()

// NEW - Real API call
LaunchedEffect(userId) {
    try {
        val response = RetrofitClient.apiService.getTodayFitness(
            userId = userId,
            token = "Bearer $authToken"
        )
        if (response.isSuccessful) {
            fitnessData = response.body() ?: FitnessData(
                userId = userId,
                date = LocalDate.now().toString(),
                steps = 0,
                calories = 0,
                distance = 0f,
                activeMinutes = 0
            )
        }
    } catch (e: Exception) {
        println("Error fetching fitness data: ${e.message}")
    }
}
```

#### **4. Update StatsScreen**

```kotlin
LaunchedEffect(dateRange) {
    try {
        val response = RetrofitClient.apiService.getStats(
            userId = userId,
            range = dateRange.lowercase(),
            token = "Bearer $authToken"
        )
        if (response.isSuccessful) {
            statsData = response.body()
        }
    } catch (e: Exception) {
        println("Error fetching stats: ${e.message}")
    }
}
```

#### **5. Update ProfileScreen**

```kotlin
// Get profile on load
LaunchedEffect(Unit) {
    try {
        val response = RetrofitClient.apiService.getProfile(
            userId = userId,
            token = "Bearer $authToken"
        )
        if (response.isSuccessful) {
            userProfile = response.body()
        }
    } catch (e: Exception) {
        println("Error fetching profile: ${e.message}")
    }
}

// Update profile when user edits
Button(onClick = {
    viewModelScope.launch {
        try {
            val response = RetrofitClient.apiService.updateProfile(
                userId = userId,
                token = "Bearer $authToken",
                request = UpdateProfileRequest(
                    displayName = newDisplayName
                )
            )
            if (response.isSuccessful) {
                userProfile = response.body()
            }
        } catch (e: Exception) {
            println("Error updating profile: ${e.message}")
        }
    }
}) {
    Text("Save")
}
```

#### **6. Update AuthViewModel for Real Login**

```kotlin
fun loginWithFirebase(email: String, password: String) {
    viewModelScope.launch {
        _authState.value = AuthState.Loading
        try {
            // 1. Firebase authenticates
            val firebaseUser = auth.signInWithEmailAndPassword(email, password).await().user
            
            if (firebaseUser != null) {
                // 2. Get Firebase ID token
                val idToken = firebaseUser.getIdToken(false).await().token
                
                // 3. Login to backend
                val response = RetrofitClient.apiService.loginUser(
                    LoginRequest(
                        firebaseUid = firebaseUser.uid,
                        email = firebaseUser.email ?: email
                    )
                )
                
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        // Save token and user
                        saveTokenToPreferences(authResponse.token)
                        saveUserToPreferences(authResponse.user)
                        
                        _authState.value = AuthState.Success(authResponse.user)
                    }
                } else {
                    _authState.value = AuthState.Error("Backend login failed")
                }
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Login failed")
        }
    }
}
```

#### **7. Add Permissions in AndroidManifest.xml**

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

#### **8. Add Retrofit Dependency to build.gradle**

```gradle
dependencies {
    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    
    // GSON for JSON parsing
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1'
}
```

### Configuration Checklist:

- [ ] MongoDB installed and running
- [ ] Firebase project created and authenticated
- [ ] Backend `.env` configured with Firebase credentials
- [ ] Blockchain addresses added to `.env` (or placeholders)
- [ ] Kotlin app has Retrofit dependency
- [ ] `ApiService.kt` created with all endpoints
- [ ] `RetrofitClient.kt` created with correct base URL
- [ ] Backend IP/URL correct in RetrofitClient
- [ ] Internet permission added to manifest
- [ ] AuthViewModel updated to use real login

---

## 🧪 Testing Workflow

### 1. Start All Backend Services

Open 4 PowerShell windows:

```powershell
# Window 1 - API Gateway
cd c:/Users/Phanny/Desktop/FitTrack/Backend
npm start

# Window 2 - User Service
cd c:/Users/Phanny/Desktop/FitTrack/Backend
npm run services:user

# Window 3 - Fitness Service
cd c:/Users/Phanny/Desktop/FitTrack/Backend
npm run services:fitness

# Window 4 - Blockchain Service
cd c:/Users/Phanny/Desktop/FitTrack/Backend
npm run services:blockchain
```

### 2. Verify All Services Running

```powershell
curl http://localhost:3000/health
curl http://localhost:3001/health
curl http://localhost:3002/health
curl http://localhost:3003/health
```

All should return: `{"status":"... OK"}`

### 3. Test Registration Endpoint

```powershell
$body = @{
    firebaseUid = "test-user-123"
    email = "test@example.com"
    displayName = "Test User"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:3000/api/auth/register" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $body
```

### 4. Run Kotlin App

- Open your Kotlin project in Android Studio
- Update `BASE_URL` with your computer's IP
- Run on emulator or real device
- Try to register/login

---

## ⚠️ Common Issues & Fixes

### MongoDB Not Starting
```powershell
# Restart MongoDB service
Restart-Service MongoDB
# Or check status
Get-Service MongoDB
```

### Firebase Token Invalid
- Make sure `.env` has correct credentials
- Verify Firebase project is created and Email/Password enabled
- Check private key doesn't have escape characters

### Backend Not Reachable from Phone
- Make sure phone is on same WiFi network
- Use `ipconfig` to get your computer's IP
- Update Kotlin `BASE_URL` with that IP
- Disable Windows Firewall temporarily to test

### Contract Deployment Failed
- Make sure you have Sepolia testnet funds
- Get free testnet ETH from: https://sepoliafaucet.com/

---

## 📞 Need Help?

1. Check backend logs in PowerShell windows
2. Use Postman to test API endpoints
3. Use Android Logcat to see Kotlin app errors
4. Check MongoDB data: `mongo` then `show databases`

---

Done! Follow these steps in order and your full stack will be connected! 🎉
