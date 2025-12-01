# Backend API Gateway Configuration Issue - SOLUTION

## Problem Found ✅

Your backend is running but returning 404 for all API routes.

### What Works:
- ✅ `GET http://localhost:3000/health` → `{"status":"API Gateway OK"}`

### What Doesn't Work:
- ❌ `GET /api/fitness/stats/{userId}/{range}` → 404
- ❌ `GET /api/auth/profile/{userId}` → 404
- ❌ `POST /api/auth/login` → 404
- ❌ All other `/api/*` routes → 404

## Root Cause:

Your API Gateway is running but **NOT routing requests to the microservices**.

## Fix Your Backend (API Gateway):

Your API Gateway needs to proxy requests to the individual services. Here's what it should look like:

### Option 1: Express.js API Gateway (Recommended)

```javascript
// In your backend/api-gateway/index.js or server.js

const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express();

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'API Gateway OK', timestamp: new Date() });
});

// Route to User/Auth Service
app.use('/api/auth', createProxyMiddleware({
  target: 'http://localhost:3001', // Your User Service port
  changeOrigin: true,
  pathRewrite: {
    '^/api/auth': '/api/auth' // Keep the path
  }
}));

// Route to Fitness Service
app.use('/api/fitness', createProxyMiddleware({
  target: 'http://localhost:3002', // Your Fitness Service port
  changeOrigin: true,
  pathRewrite: {
    '^/api/fitness': '/api/fitness'
  }
}));

// Route to Blockchain Service
app.use('/api/blockchain', createProxyMiddleware({
  target: 'http://localhost:3003', // Your Blockchain Service port
  changeOrigin: true,
  pathRewrite: {
    '^/api/blockchain': '/api/blockchain'
  }
}));

app.listen(3000, () => {
  console.log('API Gateway running on port 3000');
});
```

### Install Required Package:

```bash
npm install http-proxy-middleware
```

## Check Your Microservices:

Make sure all 4 services are actually running:

```bash
# Check what's running on each port
netstat -ano | findstr :3000   # API Gateway
netstat -ano | findstr :3001   # User Service
netstat -ano | findstr :3002   # Fitness Service
netstat -ano | findstr :3003   # Blockchain Service
```

## Test After Fix:

```bash
# Should return user data (or auth error if no user exists)
curl http://localhost:3000/api/fitness/stats/test-user/week

# Should return auth response
curl -X POST http://localhost:3000/api/auth/login -H "Content-Type: application/json" -d "{\"firebaseUid\":\"test\",\"email\":\"test@test.com\"}"
```

## Alternative: Check Your Current Backend Code

If you already have routing configured, check:

1. **Are the service URLs correct?** (localhost:3001, 3002, 3003, etc.)
2. **Are all 4 services actually running?** (not just API Gateway)
3. **Check console logs** - does the API Gateway show any errors when starting?
4. **Check service health** individually:
   ```bash
   curl http://localhost:3001/health  # User Service
   curl http://localhost:3002/health  # Fitness Service
   curl http://localhost:3003/health  # Blockchain Service
   ```

## Quick Temporary Fix (For Testing):

If you just want to test the Kotlin app while fixing the backend, add mock routes to your API Gateway:

```javascript
// Temporary mock routes for testing
app.get('/api/fitness/stats/:userId/:range', (req, res) => {
  res.json({
    success: true,
    data: [
      {
        userId: req.params.userId,
        date: new Date().toISOString().split('T')[0],
        steps: 5000,
        calories: 250,
        distance: 3.5,
        activeMinutes: 45
      }
    ]
  });
});

app.get('/api/fitness/today/:userId', (req, res) => {
  res.json({
    userId: req.params.userId,
    date: new Date().toISOString().split('T')[0],
    steps: 5000,
    calories: 250,
    distance: 3.5,
    activeMinutes: 45
  });
});
```

## After Fixing:

1. Restart your backend
2. Test: `curl http://localhost:3000/api/fitness/stats/test/week`
3. If you see data (not 404), rebuild and run the Kotlin app
4. The banner should disappear and you'll see real data

## Current Kotlin App Status:

- ✅ App is configured correctly
- ✅ Network security allows HTTP
- ✅ App handles 404 gracefully (shows empty data)
- ✅ Emulator can reach backend (10.0.2.2:3000)
- ⏳ **Waiting for backend routes to be fixed**

Once you fix the backend routing, the app will work immediately!

