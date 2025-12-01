# Backend Integration Status - 4 Services

## Running Backend Services

Your backend is currently running **4 services**:

1. **API Gateway** - Port 3000 (Main entry point)
2. **User Service** - Handles authentication and profiles
3. **Fitness Service** - Handles fitness data logging
4. **Blockchain Service** - Handles rewards and wallet

## API Endpoints Available

### User Service (via `/api/auth`)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `GET /api/auth/profile/{userId}` - Get user profile
- `PUT /api/auth/profile/{userId}` - Update user profile
- `POST /api/auth/link-wallet` - Link blockchain wallet

### Fitness Service (via `/api/fitness`)
- `GET /api/fitness/today/{userId}` - Get today's fitness data
- `GET /api/fitness/stats/{userId}/{range}` - Get stats (week/month/year)
- `POST /api/fitness/log` - Log fitness data
- `GET /api/fitness/summary/{userId}` - Get fitness summary

### Blockchain Service (via `/api/blockchain`)
- `GET /api/blockchain/rewards/{userAddress}` - Get user rewards

## Kotlin Frontend Configuration

### Base URL
```kotlin
http://192.168.50.249:3000/
```

### Updated Files
1. **FitTrackRepository.kt** - Updated to use the 4-service backend API
   - Converts between API models and local data models
   - Routes all calls through API Gateway
   - Handles Firebase authentication automatically

### Services NOT Currently Available
The following features will return empty data or throw exceptions:
- **Workout tracking** (no workout service running)
- **Goal management** (no goal service running)
- **AI recommendations** (no AI service running)
- **Transaction history** (not implemented in blockchain service yet)

## How It Works

1. **Firebase Auth**: Your app uses Firebase for authentication
2. **Token Injection**: Repository automatically adds Firebase token to all API calls
3. **User ID**: Gets user ID from Firebase Auth and passes to backend
4. **Data Conversion**: Converts between backend API models (`FitnessData`) and app models (`DailyStats`)

## Testing Your Backend

When you run the app:
1. User logs in via Firebase
2. App calls `/api/auth/login` to register user in backend
3. App can then:
   - View/update profile
   - Log fitness data (steps, calories, distance, active minutes)
   - View fitness stats and summary
   - Link wallet address
   - Check blockchain rewards (if wallet linked)

## Next Steps

If you want to enable the missing features, you need to:
1. Start **Workout Service** - for workout tracking
2. Start **Goal Service** - for goal management
3. Start **AI Service** - for AI recommendations

But for now, your app will work with the basic fitness tracking features using the 4 services you have running.

## Troubleshooting

If the app crashes:
1. Check that all 4 backend services are running
2. Verify the IP address `192.168.50.249` is correct
3. Check backend logs for errors
4. Ensure Firebase authentication is working
5. Check that your phone/emulator can reach the backend IP

## Backend Response Format

Your backend should return data in these formats:

### User Profile Response
```json
{
  "id": "user123",
  "firebaseUid": "firebase_uid",
  "email": "user@example.com",
  "displayName": "John Doe",
  "photoUrl": null,
  "walletAddress": null,
  "createdAt": "2025-11-24T10:00:00Z"
}
```

### Fitness Data Response
```json
{
  "userId": "user123",
  "date": "2025-11-24",
  "steps": 5000,
  "calories": 250,
  "distance": 3.5,
  "activeMinutes": 45,
  "heartRate": null,
  "notes": null,
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T10:00:00Z"
}
```

### Rewards Response
```json
{
  "userAddress": "0x123...",
  "rewardBalance": "100.5",
  "nftCount": 3,
  "network": "ethereum"
}
```

