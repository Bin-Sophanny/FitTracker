# 🔧 Kotlin App Authentication & JWT Token Problems - FIXED

## Date: December 8, 2025

---

## 📋 Problems Identified and Fixed

### ❌ **Problem 1: AuthViewModel.kt - Completely Broken Structure**
**Issue:** The file had malformed/duplicated methods with broken syntax
- Duplicate function definitions (`signIn` appeared twice with different signatures)
- Incomplete code blocks with missing braces
- `validateName()` method body appeared inside `signIn()` method
- Missing `validateName()` method declaration
- `signOut()` method missing Context parameter but calling `TokenManager.clearToken(context)`

**✅ Fixed:**
- Completely restructured AuthViewModel.kt with proper method definitions
- Added proper `signIn(email, password, context)` method that:
  1. Validates input
  2. Authenticates with Firebase
  3. Calls backend `/api/auth/login` to get JWT token
  4. Saves JWT token using TokenManager
  5. Logs Firebase UID for tracking
- Added proper `signUp(name, email, password, context)` method that:
  1. Validates all inputs
  2. Registers with Firebase
  3. Calls backend `/api/auth/register` to get JWT token
  4. Saves JWT token using TokenManager
- Fixed `signOut(context)` to properly clear JWT tokens
- Added `validateName()` method properly

---

### ❌ **Problem 2: FitTrackRepository.kt - Using Firebase Token Instead of JWT**
**Issue:** Repository was trying to get Firebase tokens instead of using JWT tokens from backend
- `getAuthToken()` method was calling `auth.currentUser?.getIdToken()` (Firebase token)
- Comment said "TODO: Inject Context or use a better token management solution"
- All API calls would fail because backend expects JWT, not Firebase tokens

**✅ Fixed:**
- Updated `getAuthToken(context)` to use `TokenManager.getToken(context)` for JWT tokens
- Changed signature from `getAuthToken()` to `getAuthToken(context: Context)` to accept Context
- Now properly formats token as `"Bearer $jwtToken"`
- Added logging to show JWT token is being retrieved from TokenManager

---

### ❌ **Problem 3: Missing Backend Auth Methods in Repository**
**Issue:** `loginUser()` and `registerUser()` methods were completely missing
- AuthViewModel tried to call `backendRepository.loginUser()` and `backendRepository.registerUser()`
- These methods didn't exist, causing compilation errors

**✅ Fixed:**
- Added `suspend fun registerUser(request: RegisterRequest): Response<AuthResponse>`
- Added `suspend fun loginUser(request: LoginRequest): Response<AuthResponse>`
- Both methods properly call the ApiService endpoints

---

### ❌ **Problem 4: Repository Methods Missing Context Parameter**
**Issue:** All repository methods needed Context to get JWT tokens but didn't accept it
- `getUserProfile()`, `updateProfile()`, `getDailyStats()`, `getTodayStats()`, `logDailyStats()`
- Would crash when trying to call `getAuthToken()` without Context

**✅ Fixed:**
- Updated ALL repository methods to accept Context parameter:
  - `getUserProfile(context: Context)`
  - `updateProfile(context: Context, profile: UpdateProfileRequest)`
  - `getDailyStats(context: Context, limit: Int = 5)`
  - `getTodayStats(context: Context)`
  - `logDailyStats(context: Context, stats: DailyStats)`

---

### ❌ **Problem 5: FitnessViewModel Not Passing Context**
**Issue:** FitnessViewModel called repository methods without Context parameter

**✅ Fixed:**
- Added `context: Context` parameter to FitnessViewModel constructor
- Updated all repository method calls to pass context:
  - `repository.getDailyStats(context, limit)`
  - `repository.logDailyStats(context, stats)`
  - `repository.getUserProfile(context)`
  - `repository.updateProfile(context, profile)`

---

### ❌ **Problem 6: StepCounterService Not Passing Context**
**Issue:** StepCounterService called `repository.logDailyStats(stats)` without Context

**✅ Fixed:**
- Updated to `repository.logDailyStats(this@StepCounterService, stats)`
- Service now properly passes itself as Context to repository

---

## 🎯 Current Implementation Summary

### ✅ **Authentication Flow (Correct)**
1. User enters email/password
2. AuthViewModel validates inputs
3. Firebase Authentication creates/authenticates user
4. Backend API called with Firebase UID:
   - `/api/auth/register` for new users
   - `/api/auth/login` for existing users
5. Backend returns JWT token
6. JWT token saved via `TokenManager.saveToken(context, token, firebaseUid)`
7. Firebase UID stored alongside JWT token

### ✅ **API Request Flow (Correct)**
1. User triggers API action (e.g., log fitness data)
2. Repository's `getAuthToken(context)` retrieves JWT from TokenManager
3. JWT formatted as `"Bearer $token"`
4. Request sent with header: `Authorization: Bearer {jwt_token}`
5. Firebase UID used as `userId` in all fitness data logs

### ✅ **Token Management (Correct)**
- **Storage:** SharedPreferences via TokenManager
- **Keys Stored:**
  - `jwt_token` - JWT from backend
  - `user_id` - Firebase UID
- **Retrieval:** `TokenManager.getToken(context)` returns JWT string
- **Clearing:** `TokenManager.clearToken(context)` on sign out

---

## 📝 Key Points

### ✅ **Firebase UID as userId**
- All fitness data logged with Firebase UID as the `userId` field
- Backend receives: `{ userId: "firebase_uid_here", steps: 1000, ... }`
- This ensures user data isolation and proper authentication

### ✅ **JWT Token in Authorization Header**
- All protected API endpoints receive: `Authorization: Bearer {jwt_token}`
- Backend validates JWT and extracts user info
- No Firebase tokens sent to backend (Firebase is only for authentication)

### ✅ **Proper Context Passing**
- All components that need JWT tokens receive Context parameter
- ViewModels take Context in constructor
- Services use `this@ServiceName` to pass themselves as Context

---

## 🔍 Files Modified

1. ✅ **AuthViewModel.kt** - Completely restructured, fixed all authentication logic
2. ✅ **FitTrackRepository.kt** - Recreated with proper JWT token handling
3. ✅ **FitnessViewModel.kt** - Added Context parameter, updated all repository calls
4. ✅ **StepCounterService.kt** - Fixed repository call to pass Context

---

## ✅ All Problems Fixed!

The app now properly:
- ✅ Authenticates with Firebase (email/password)
- ✅ Gets JWT token from backend login/register
- ✅ Stores JWT token via TokenManager
- ✅ Sends JWT token in Authorization header on every request
- ✅ Uses Firebase UID as userId for all fitness data
- ✅ Properly passes Context to all methods that need tokens

---

## 🚀 Ready for Testing

The authentication and API integration is now properly implemented. Users can:
1. Sign up with email/password
2. Automatically get JWT token from backend
3. All API calls use JWT token for authentication
4. Step counting syncs to backend with proper user identification
5. All fitness data tied to Firebase UID

---

**Status: ✅ ALL PROBLEMS RESOLVED**

