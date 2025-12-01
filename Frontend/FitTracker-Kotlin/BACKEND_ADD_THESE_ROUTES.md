# Add These Routes to Your Backend

## Current Status:
- ✅ Health endpoints work: `/health`, `/api/auth/health`, `/api/fitness/health`
- ❌ Data endpoints missing: `/api/fitness/stats/{userId}/{range}`, `/api/fitness/today/{userId}`

## Add These to Your Backend (Fitness Service or API Gateway):

### For Express.js Backend:

```javascript
// In your fitness service (port 3002) or API Gateway (port 3000)

// Get fitness stats by range
app.get('/api/fitness/stats/:userId/:range', async (req, res) => {
  try {
    const { userId, range } = req.params;
    
    // Mock data for now - replace with real database query later
    const mockData = [
      {
        userId: userId,
        date: new Date().toISOString().split('T')[0],
        steps: 8500,
        calories: 425,
        distance: 6.5,
        activeMinutes: 60,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      },
      {
        userId: userId,
        date: new Date(Date.now() - 86400000).toISOString().split('T')[0],
        steps: 7200,
        calories: 360,
        distance: 5.2,
        activeMinutes: 45,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      },
      {
        userId: userId,
        date: new Date(Date.now() - 172800000).toISOString().split('T')[0],
        steps: 9100,
        calories: 455,
        distance: 7.1,
        activeMinutes: 70,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
    ];
    
    res.json({
      success: true,
      data: mockData
    });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

// Get today's fitness data
app.get('/api/fitness/today/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    
    const todayData = {
      userId: userId,
      date: new Date().toISOString().split('T')[0],
      steps: 8500,
      calories: 425,
      distance: 6.5,
      activeMinutes: 60,
      heartRate: null,
      notes: null,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    
    res.json(todayData);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Log fitness data
app.post('/api/fitness/log', async (req, res) => {
  try {
    const fitnessData = req.body;
    
    // Mock response - replace with real database insert later
    res.json({
      ...fitnessData,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get fitness summary
app.get('/api/fitness/summary/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    
    res.json({
      userId: userId,
      totalSteps: 45000,
      totalCalories: 2250,
      totalDistance: 35.5,
      totalActiveMinutes: 300,
      averageSteps: 7500,
      averageCalories: 375,
      averageDistance: 5.9,
      averageActiveMinutes: 50
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
```

### For Auth/User Service:

```javascript
// Get user profile
app.get('/api/auth/profile/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    
    // Mock user data
    res.json({
      id: userId,
      firebaseUid: userId,
      email: "user@example.com",
      displayName: "Test User",
      photoUrl: null,
      walletAddress: null,
      createdAt: new Date().toISOString()
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Login/Register
app.post('/api/auth/login', async (req, res) => {
  try {
    const { firebaseUid, email } = req.body;
    
    res.json({
      success: true,
      user: {
        id: firebaseUid,
        firebaseUid: firebaseUid,
        email: email,
        displayName: email.split('@')[0],
        photoUrl: null,
        walletAddress: null,
        createdAt: new Date().toISOString()
      },
      token: "mock-jwt-token-" + firebaseUid
    });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

app.post('/api/auth/register', async (req, res) => {
  try {
    const { firebaseUid, email, displayName } = req.body;
    
    res.json({
      success: true,
      user: {
        id: firebaseUid,
        firebaseUid: firebaseUid,
        email: email,
        displayName: displayName,
        photoUrl: null,
        walletAddress: null,
        createdAt: new Date().toISOString()
      },
      token: "mock-jwt-token-" + firebaseUid
    });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

// Update profile
app.put('/api/auth/profile/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    const updates = req.body;
    
    res.json({
      id: userId,
      firebaseUid: userId,
      email: "user@example.com",
      displayName: updates.displayName || "Test User",
      photoUrl: updates.photoUrl || null,
      walletAddress: updates.walletAddress || null,
      createdAt: new Date().toISOString()
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Link wallet
app.post('/api/auth/link-wallet', async (req, res) => {
  try {
    const { userId, walletAddress } = req.body;
    
    res.json({
      success: true,
      message: "Wallet linked successfully",
      walletAddress: walletAddress
    });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});
```

### For Blockchain Service:

```javascript
// Get rewards
app.get('/api/blockchain/rewards/:userAddress', async (req, res) => {
  try {
    const { userAddress } = req.params;
    
    res.json({
      userAddress: userAddress,
      rewardBalance: "150",
      nftCount: 2,
      network: "ethereum"
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
```

## After Adding These Routes:

1. **Restart your backend services**
2. **Test in browser**: `http://localhost:3000/api/fitness/stats/test-user/week`
3. **You should see JSON data** (not 404)
4. **Rebuild the Kotlin app**
5. **Run the app** - it should now show data!

## Quick Test Command:

```bash
curl http://localhost:3000/api/fitness/stats/test-user/week
```

Should return:
```json
{
  "success": true,
  "data": [...]
}
```

Instead of 404 error.

