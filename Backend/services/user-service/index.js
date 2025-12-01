const express = require('express');
const mongoose = require('mongoose');
const admin = require('firebase-admin');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const app = express();
const PORT = process.env.USER_SERVICE_PORT || 3001;

// Middleware
app.use(express.json());

// MongoDB Connection
mongoose.connect(process.env.MONGODB_USER_URI || 'mongodb://localhost:27017/fittrack_users')
  .then(() => console.log('✅ User Service: MongoDB connected'))
  .catch(err => console.error('❌ MongoDB connection error:', err));

// Initialize Firebase Admin SDK
const serviceAccount = {
  projectId: process.env.FIREBASE_PROJECT_ID,
  privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n'),
  clientEmail: process.env.FIREBASE_CLIENT_EMAIL
};

if (process.env.FIREBASE_PROJECT_ID) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

// User Schema
const userSchema = new mongoose.Schema({
  firebaseUid: { type: String, unique: true, required: true },
  email: { type: String, unique: true, required: true },
  displayName: String,
  photoUrl: String,
  walletAddress: String,
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
});

const User = mongoose.model('User', userSchema);

// Generate JWT token
const generateToken = (user) => {
  return jwt.sign(
    { uid: user.firebaseUid, email: user.email, id: user._id },
    process.env.JWT_SECRET,
    { expiresIn: process.env.JWT_EXPIRATION || '7d' }
  );
};

// Middleware: Verify Firebase token
const verifyFirebaseToken = async (req, res, next) => {
  const token = req.headers.authorization?.split('Bearer ')[1];
  if (!token) return res.status(401).json({ error: 'No token provided' });

  try {
    const decodedToken = await admin.auth().verifyIdToken(token);
    req.user = decodedToken;
    next();
  } catch (error) {
    res.status(403).json({ error: 'Invalid token' });
  }
};

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'User Service OK' });
});

// Register user (Firebase handles auth, we store in MongoDB)
app.post('/auth/register', async (req, res) => {
  try {
    const { firebaseUid, email, displayName } = req.body;

    if (!firebaseUid || !email) {
      return res.status(400).json({ error: 'firebaseUid and email required' });
    }

    // Check if user exists
    const existingUser = await User.findOne({ firebaseUid });
    if (existingUser) {
      return res.status(400).json({ error: 'User already registered' });
    }

    // Create user in MongoDB
    const user = new User({
      firebaseUid,
      email,
      displayName: displayName || email.split('@')[0]
    });

    await user.save();

    const token = generateToken(user);

    res.status(201).json({
      success: true,
      user: {
        id: user._id,
        firebaseUid: user.firebaseUid,
        email: user.email,
        displayName: user.displayName
      },
      token
    });
  } catch (error) {
    console.error('Registration error:', error);
    res.status(500).json({ error: 'Registration failed', details: error.message });
  }
});

// Login user (Firebase handles auth)
app.post('/auth/login', async (req, res) => {
  try {
    const { firebaseUid, email } = req.body;

    if (!firebaseUid || !email) {
      return res.status(400).json({ error: 'firebaseUid and email required' });
    }

    // Find or create user
    let user = await User.findOne({ firebaseUid });

    if (!user) {
      user = new User({
        firebaseUid,
        email,
        displayName: email.split('@')[0]
      });
      await user.save();
    }

    const token = generateToken(user);

    res.json({
      success: true,
      user: {
        id: user._id,
        firebaseUid: user.firebaseUid,
        email: user.email,
        displayName: user.displayName,
        walletAddress: user.walletAddress
      },
      token
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ error: 'Login failed', details: error.message });
  }
});

// Get user profile
app.get('/auth/profile/:userId', verifyFirebaseToken, async (req, res) => {
  try {
    const user = await User.findById(req.params.userId);

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    res.json({
      id: user._id,
      firebaseUid: user.firebaseUid,
      email: user.email,
      displayName: user.displayName,
      photoUrl: user.photoUrl,
      walletAddress: user.walletAddress,
      createdAt: user.createdAt
    });
  } catch (error) {
    console.error('Error fetching profile:', error);
    res.status(500).json({ error: 'Failed to fetch profile' });
  }
});

// Update user profile
app.put('/auth/profile/:userId', verifyFirebaseToken, async (req, res) => {
  try {
    const { displayName, photoUrl, walletAddress } = req.body;

    const user = await User.findByIdAndUpdate(
      req.params.userId,
      {
        ...(displayName && { displayName }),
        ...(photoUrl && { photoUrl }),
        ...(walletAddress && { walletAddress }),
        updatedAt: new Date()
      },
      { new: true }
    );

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    res.json({
      success: true,
      user: {
        id: user._id,
        email: user.email,
        displayName: user.displayName,
        photoUrl: user.photoUrl,
        walletAddress: user.walletAddress
      }
    });
  } catch (error) {
    console.error('Error updating profile:', error);
    res.status(500).json({ error: 'Failed to update profile' });
  }
});

// Link wallet address
app.post('/auth/link-wallet', verifyFirebaseToken, async (req, res) => {
  try {
    const { userId, walletAddress } = req.body;

    if (!walletAddress) {
      return res.status(400).json({ error: 'Wallet address required' });
    }

    const user = await User.findByIdAndUpdate(
      userId,
      { walletAddress, updatedAt: new Date() },
      { new: true }
    );

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    res.json({
      success: true,
      message: 'Wallet linked successfully',
      walletAddress: user.walletAddress
    });
  } catch (error) {
    console.error('Error linking wallet:', error);
    res.status(500).json({ error: 'Failed to link wallet' });
  }
});

app.listen(PORT, () => {
  console.log(`🚀 User Service running on http://localhost:${PORT}`);
});
