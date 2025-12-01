# FitTrack Backend - Microservices & Blockchain

A Node.js microservices backend for the FitTrack fitness tracking application, featuring an API Gateway, User Service with Firebase authentication, Fitness Service, and Blockchain Service for rewards & NFTs.

## 📋 Project Structure

```
Backend/
├── api-gateway/              # Express API Gateway (Port 3000)
├── services/
│   ├── user-service/         # User management & Firebase auth (Port 3001)
│   ├── fitness-service/      # Fitness data & analytics (Port 3002)
│   └── blockchain-service/   # Rewards & NFT verification (Port 3003)
├── shared/                   # Shared utilities, models, middleware
└── README.md
```

## 🚀 Quick Start

### Prerequisites
- Node.js >= 16
- MongoDB (local or Atlas)
- Firebase project setup
- Ethereum wallet & Sepolia testnet funds

### Installation

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Configure environment:**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Run services individually:**
   ```bash
   npm start                    # API Gateway
   npm run services:user        # User Service
   npm run services:fitness     # Fitness Service
   npm run services:blockchain  # Blockchain Service
   ```

Or run all in development mode with hot-reload:
   ```bash
   npm run dev                  # API Gateway with nodemon
   npm run services:user        # (in separate terminal)
   npm run services:fitness     # (in separate terminal)
   npm run services:blockchain  # (in separate terminal)
   ```

## 📡 API Endpoints

### API Gateway (Port 3000)
All requests go through the gateway which routes to appropriate services.

**Authentication:**
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `GET /api/auth/profile/:userId` - Get user profile
- `PUT /api/auth/profile/:userId` - Update profile
- `POST /api/auth/link-wallet` - Link blockchain wallet

**Fitness:**
- `GET /api/fitness/today/:userId` - Today's metrics
- `GET /api/fitness/stats/:userId/:range` - Historical stats (week/month/year)
- `POST /api/fitness/log` - Log new activity
- `GET /api/fitness/summary/:userId` - Overall summary

**Blockchain:**
- `GET /api/blockchain/rewards/:userAddress` - Get reward balance & NFTs
- `POST /api/blockchain/transfer-rewards` - Transfer rewards
- `POST /api/blockchain/mint-nft` - Mint achievement NFT
- `GET /api/blockchain/verify/:transactionHash` - Verify transaction

## 🔧 Services Overview

### User Service (Port 3001)
Handles user authentication and profile management using Firebase.

**Features:**
- Firebase email/password authentication
- User profile management
- Wallet address linking
- JWT token generation

**Endpoints:**
- `POST /auth/register` - Create account
- `POST /auth/login` - Authenticate user
- `GET /auth/profile/:userId` - Get profile
- `PUT /auth/profile/:userId` - Update profile
- `POST /auth/link-wallet` - Link wallet

**Database:** MongoDB (fittrack_users)

---

### Fitness Service (Port 3002)
Manages fitness data and analytics.

**Features:**
- Daily activity logging (steps, calories, distance, active minutes)
- Historical data retrieval
- Statistics calculation (averages, totals)
- Flexible time ranges (daily/weekly/monthly/yearly)

**Endpoints:**
- `GET /fitness/today/:userId` - Today's data
- `GET /fitness/stats/:userId/:range` - Historical stats
- `POST /fitness/log` - Log activity
- `GET /fitness/summary/:userId` - Summary stats

**Database:** MongoDB (fittrack_fitness)

---

### Blockchain Service (Port 3003)
Manages cryptocurrency rewards and NFT achievements on Ethereum.

**Features:**
- Reward token transfers
- NFT minting for achievements
- Transaction verification
- Wallet balance checking

**Endpoints:**
- `GET /blockchain/rewards/:userAddress` - Get balance & NFTs
- `POST /blockchain/transfer-rewards` - Send rewards
- `POST /blockchain/mint-nft` - Mint achievement
- `GET /blockchain/verify/:transactionHash` - Verify transaction
- `GET /blockchain/wallet` - Get service wallet info

**Blockchain:** Ethereum Sepolia Testnet

---

## 🗄️ Database

### MongoDB Collections:

**fittrack_users:**
```javascript
{
  firebaseUid: String,
  email: String,
  displayName: String,
  photoUrl: String,
  walletAddress: String,
  createdAt: Date,
  updatedAt: Date
}
```

**fittrack_fitness:**
```javascript
{
  userId: String,
  date: Date,
  steps: Number,
  calories: Number,
  distance: Number,
  activeMinutes: Number,
  heartRate: Number,
  notes: String,
  createdAt: Date,
  updatedAt: Date
}
```

## 🔐 Security Setup

### Firebase Configuration
1. Create Firebase project
2. Enable Email/Password authentication
3. Get service account key JSON
4. Set environment variables: `FIREBASE_PROJECT_ID`, `FIREBASE_PRIVATE_KEY`, `FIREBASE_CLIENT_EMAIL`

### Blockchain Configuration
1. Create Ethereum wallet
2. Get funds on Sepolia testnet
3. Deploy reward token & NFT contracts
4. Set `BLOCKCHAIN_PRIVATE_KEY`, `BLOCKCHAIN_CONTRACT_ADDRESS`, `NFT_CONTRACT_ADDRESS`

### JWT Tokens
- Issued by User Service on login
- Verified by all services
- Default expiration: 7 days

## 📡 Frontend Integration

### Update Kotlin app to call backend APIs:

```kotlin
// User registration/login
val retrofitService = RetrofitClient.create()
retrofitService.registerUser(RegisterRequest(firebaseUid, email, displayName))

// Get fitness data
retrofitService.getTodayFitness(userId, token)

// Log activity
retrofitService.logFitness(LogRequest(userId, date, steps, calories))

// Get blockchain rewards
retrofitService.getRewards(walletAddress, token)
```

## 🧪 Testing Health Checks

```bash
curl http://localhost:3000/health    # API Gateway
curl http://localhost:3001/health    # User Service
curl http://localhost:3002/health    # Fitness Service
curl http://localhost:3003/health    # Blockchain Service
```

## 📝 Development Workflow

### Adding a new endpoint:

1. Create route in service
2. Add middleware if needed (authentication)
3. Implement handler function
4. Test with curl or Postman
5. Update API Gateway if new path needed

Example - Add new fitness endpoint:
```javascript
app.get('/fitness/weekly/:userId', verifyToken, async (req, res) => {
  // Implementation
});
```

## 🚦 Environment Variables

See `.env.example` for complete configuration:

```
API_GATEWAY_PORT=3000
USER_SERVICE_PORT=3001
FITNESS_SERVICE_PORT=3002
BLOCKCHAIN_SERVICE_PORT=3003

MONGODB_USER_URI=mongodb://localhost:27017/fittrack_users
MONGODB_FITNESS_URI=mongodb://localhost:27017/fittrack_fitness

FIREBASE_PROJECT_ID=your_project_id
FIREBASE_PRIVATE_KEY=your_key
FIREBASE_CLIENT_EMAIL=your_email

BLOCKCHAIN_RPC_URL=https://sepolia.infura.io/v3/your_key
BLOCKCHAIN_PRIVATE_KEY=your_wallet_key
BLOCKCHAIN_CONTRACT_ADDRESS=0x...
NFT_CONTRACT_ADDRESS=0x...

JWT_SECRET=your_secret_key
JWT_EXPIRATION=7d
NODE_ENV=development
```

## 📚 Resources

- [Express.js](https://expressjs.com/)
- [MongoDB Mongoose](https://mongoosejs.com/)
- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)
- [Web3.js](https://web3js.readthedocs.io/)
- [Ethereum Sepolia](https://www.alchemy.com/overviews/sepolia-testnet)

## 📄 License

ISC

