# FitTrack Backend - Microservices Setup Guide

## 📊 Architecture Overview

Your backend is organized into **5 independent services** communicating through an API Gateway:

```
                    ┌─────────────────┐
                    │   Android App   │
                    │  (Kotlin/Compose)
                    └────────┬────────┘
                             │ REST/HTTP
                    ┌────────▼────────┐
                    │  API Gateway    │ (Port 3000)
                    │  (Express.js)   │
                    └────┬────┬────┬──┘
                    ┌───┴┐┌──┴─┐┌─┴───┐
        ┌───────────┘    ││    ││     └──────────┐
        │                ││    ││                │
   ┌────▼─────┐    ┌────▼─┴──┐│  ┌──────────────▼────┐
   │   User    │    │Fitness  ││  │     Blockchain    │
   │ Service   │    │ Service ││  │     Service       │
   │ (Port 3001)    │ (Port   ││  │   (Port 3004)     │
   │ MongoDB   │    │ 3002)   ││  │   (Ethereum)      │
   │ Firebase  │    │ MongoDB ││  │                   │
   └───────────┘    └────┬────┘│  └───────────────────┘
                         │     │
                    ┌────▼──┐┌─▼─────────┐
                    │  AI   ││  Shared   │
                    │Service││ Utilities │
                    │(Port  ││ & Models  │
                    │3003)  ││           │
                    └───────┘└───────────┘
```

## 🎯 Services Responsibilities

### 1. **API Gateway** (Port 3000)
- Entry point for all frontend requests
- Routes requests to appropriate microservices
- Handles CORS and common middleware
- Response aggregation

**Key Files:**
- `api-gateway/index.js` - Main gateway server

**Endpoints:**
- `/api/auth/*` → User Service
- `/api/fitness/*` → Fitness Service
- `/api/ai/*` → AI Service
- `/api/blockchain/*` → Blockchain Service

---

### 2. **User Service** (Port 3001)
- Firebase authentication integration
- User profile management
- Account creation & login
- Email verification

**Key Files:**
- `services/user-service/index.js` - Main service
- `shared/models/User.js` - User schema

**Endpoints:**
- `POST /auth/register` - Create new account
- `POST /auth/login` - Authenticate user
- `GET /auth/profile` - Get user profile
- `PUT /auth/profile` - Update profile

**Database:** MongoDB (users collection)

---

### 3. **Fitness Service** (Port 3002)
- Store and retrieve fitness metrics
- Daily activity logs (steps, calories, distance, active minutes)
- Historical data retrieval
- Statistics calculation

**Key Files:**
- `services/fitness-service/index.js` - Main service
- `shared/models/FitnessData.js` - Fitness data schema

**Endpoints:**
- `GET /fitness/today` - Today's metrics
- `GET /fitness/stats/:range` - Historical stats (daily/weekly/monthly)
- `POST /fitness/log` - Log new activity
- `GET /fitness/summary` - Overall summary

**Database:** MongoDB (fitness_data collection)

---

### 4. **AI Service** (Port 3003)
- ML-based recommendations
- Activity insights & patterns
- Health predictions
- Personalized suggestions

**Key Files:**
- `services/ai-service/index.js` - Main service

**Endpoints:**
- `POST /ai/recommend` - Get personalized recommendations
- `POST /ai/analyze` - Analyze fitness patterns
- `POST /ai/predict` - Generate health predictions

**Dependencies:**
- TensorFlow.js or Python backend
- ML model API

---

### 5. **Blockchain Service** (Port 3004)
- Achievement rewards tracking
- NFT minting
- Transaction verification
- Transparent reward system

**Key Files:**
- `services/blockchain-service/index.js` - Main service

**Endpoints:**
- `GET /blockchain/rewards/:userId` - Get user rewards
- `POST /blockchain/mint-nft` - Mint achievement NFT
- `GET /blockchain/verify/:txId` - Verify transaction
- `GET /blockchain/balance/:userId` - Check balance

**Blockchain Network:** Sepolia Testnet (Ethereum)

---

## 🚀 Getting Started

### Step 1: Install Dependencies
```powershell
npm install
```

### Step 2: Configure Environment
```powershell
cp .env.example .env
# Edit .env with your values:
# - MONGODB URIs
# - JWT_SECRET
# - API keys
```

### Step 3: Start Services

**Option A: Using Docker (Recommended)**
```powershell
npm run docker:build
npm run docker:up
```

**Option B: Individual Services**
```powershell
# Terminal 1 - API Gateway
npm start

# Terminal 2 - User Service
npm run services:user

# Terminal 3 - Fitness Service
npm run services:fitness

# Terminal 4 - AI Service
npm run services:ai

# Terminal 5 - Blockchain Service
npm run services:blockchain
```

### Step 4: Test Gateway
```powershell
curl http://localhost:3000/health
```

---

## 📡 Frontend Integration

### Update your Kotlin app to use backend APIs:

**Replace mock data with real API calls:**

```kotlin
// Before (Kotlin - Mock)
val mockData = getMockFitnessData()

// After (Kotlin - Real API)
val client = OkHttpClient()
val request = Request.Builder()
    .url("http://YOUR_BACKEND_IP:3000/api/fitness/today")
    .addHeader("Authorization", "Bearer $token")
    .build()

val response = client.newCall(request).execute()
val fitnessData = parseResponse(response)
```

---

## 🗄️ Database Setup

### MongoDB Collections:

**User Service DB:** `fittrack_users`
```javascript
db.users.insertOne({
  firebaseUid: "user123",
  email: "user@example.com",
  displayName: "John Doe",
  createdAt: new Date()
})
```

**Fitness Service DB:** `fittrack_fitness`
```javascript
db.fitnessData.insertOne({
  userId: "user123",
  date: new Date("2024-11-24"),
  steps: 8500,
  calories: 2100,
  distance: 6.5,
  activeMinutes: 45
})
```

---

## 🔐 Security Setup

### 1. Firebase Configuration
- Add Firebase Admin SDK to User Service
- Enable Email/Password authentication
- Configure CORS for your frontend

### 2. JWT Tokens
- User Service generates JWT on login
- All services verify JWT in Authorization header
- Token expiration: 7 days (configurable)

### 3. Environment Variables
```
JWT_SECRET=your-secret-key-minimum-32-chars
NODE_ENV=production
CORS_ORIGIN=http://localhost:5000
```

---

## 📝 Development Workflow

### Adding a New Endpoint:

1. **Create route in service:**
   ```javascript
   // services/fitness-service/routes/fitness.js
   app.get('/fitness/weekly', authenticateToken, async (req, res) => {
     // Implementation
   });
   ```

2. **Update API Gateway (if new path):**
   ```javascript
   app.use('/api/fitness', createProxyMiddleware({...}));
   ```

3. **Test locally:**
   ```powershell
   curl -H "Authorization: Bearer $token" http://localhost:3000/api/fitness/weekly
   ```

---

## 🧪 Testing

### Health Check All Services:
```powershell
curl http://localhost:3000/health    # Gateway
curl http://localhost:3001/health    # User Service
curl http://localhost:3002/health    # Fitness Service
curl http://localhost:3003/health    # AI Service
curl http://localhost:3004/health    # Blockchain Service
```

---

## 📦 Docker Commands

```powershell
# Build images
npm run docker:build

# Start services
npm run docker:up

# Stop services
npm run docker:down

# View logs
docker-compose logs -f

# Access specific service logs
docker-compose logs -f user-service
```

---

## 🔗 Service Communication

Services can call each other directly:

```javascript
// Fitness Service calling User Service
const axios = require('axios');
const userResponse = await axios.get('http://user-service:3001/user/123');
```

---

## 📈 Next Steps

1. ✅ Implement authentication with Firebase
2. ✅ Connect Fitness Service to wearable device APIs
3. ✅ Integrate ML models in AI Service
4. ✅ Deploy blockchain contracts to testnet
5. ✅ Connect frontend app to API Gateway
6. ✅ Set up monitoring and logging
7. ✅ Configure CI/CD pipeline

---

## 📞 Troubleshooting

**Port already in use:**
```powershell
# Kill process on specific port
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

**MongoDB connection failed:**
- Ensure MongoDB is running
- Check connection string in .env
- Verify network connectivity

**CORS errors:**
- Update CORS_ORIGIN in .env
- Check API Gateway CORS middleware

---

## 📄 Directory Tree

```
Backend/
├── api-gateway/
│   └── index.js
├── services/
│   ├── user-service/
│   │   └── index.js
│   ├── fitness-service/
│   │   └── index.js
│   ├── ai-service/
│   │   └── index.js
│   └── blockchain-service/
│       └── index.js
├── shared/
│   ├── middleware.js
│   ├── utils.js
│   ├── models/
│   │   ├── User.js
│   │   └── FitnessData.js
│   └── README.md
├── docker/
│   ├── Dockerfile.gateway
│   ├── Dockerfile.user-service
│   ├── Dockerfile.fitness-service
│   ├── Dockerfile.ai-service
│   └── Dockerfile.blockchain-service
├── docker-compose.yml
├── package.json
├── .env.example
├── .gitignore
└── README.md
```

---

## 🎓 Resources

- [Express.js Documentation](https://expressjs.com/)
- [MongoDB Mongoose](https://mongoosejs.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)
- [Ethereum Web3.js](https://web3js.readthedocs.io/)

---

**Happy Coding! 🚀**
