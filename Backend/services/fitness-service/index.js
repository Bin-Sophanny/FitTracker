const express = require('express');
const mongoose = require('mongoose');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const app = express();
const PORT = process.env.FITNESS_SERVICE_PORT || 3002;

// Middleware
app.use(express.json());

// MongoDB Connection
mongoose.connect(process.env.MONGODB_FITNESS_URI || 'mongodb://localhost:27017/fittrack_fitness')
  .then(() => console.log('✅ Fitness Service: MongoDB connected'))
  .catch(err => console.error('❌ MongoDB connection error:', err));

// Timezone helper for Cambodia (GMT+7)
const CAMBODIA_TIMEZONE_OFFSET = 7; // GMT+7

function getCambodiaDate() {
  const utcNow = new Date();
  // Convert UTC to Cambodia time (GMT+7)
  const cambodiaTime = new Date(utcNow.getTime() + (CAMBODIA_TIMEZONE_OFFSET * 60 * 60 * 1000));
  return cambodiaTime;
}

function getCambodiaDateOnly() {
  const cambodiaTime = getCambodiaDate();
  return new Date(cambodiaTime.getFullYear(), cambodiaTime.getMonth(), cambodiaTime.getDate(), 0, 0, 0, 0);
}

// Fitness Data Schema
const fitnessDataSchema = new mongoose.Schema({
  userId: { type: String, required: true, index: true },
  date: { type: Date, required: true, index: true },
  steps: { type: Number, default: 0 },
  calories: { type: Number, default: 0 },
  distance: { type: Number, default: 0 },
  activeMinutes: { type: Number, default: 0 },
  heartRate: Number,
  notes: String,
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
});

const FitnessData = mongoose.model('FitnessData', fitnessDataSchema);

// Middleware: Verify JWT token
const verifyToken = (req, res, next) => {
  const token = req.headers.authorization?.split('Bearer ')[1];
  if (!token) return res.status(401).json({ error: 'No token provided' });

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded;
    next();
  } catch (error) {
    res.status(403).json({ error: 'Invalid token' });
  }
};

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'Fitness Service OK' });
});

// Get today's fitness data
app.get('/fitness/today/:userId', verifyToken, async (req, res) => {
  try {
    const { userId } = req.params;
    // Use Cambodia timezone (GMT+7)
    const today = getCambodiaDateOnly();
    const tomorrow = new Date(today.getTime() + 86400000);

    const data = await FitnessData.findOne({
      userId,
      date: { $gte: today, $lt: tomorrow }
    });

    res.json(data || { userId, date: new Date(), steps: 0, calories: 0, distance: 0, activeMinutes: 0 });
  } catch (error) {
    console.error('Error fetching today data:', error);
    res.status(500).json({ error: 'Failed to fetch data' });
  }
});

// Get historical stats
app.get('/fitness/stats/:userId/:range', verifyToken, async (req, res) => {
  try {
    const { userId, range } = req.params;
    
    // Calculate date range using Cambodia timezone (GMT+7)
    const today = getCambodiaDateOnly();
    let startDate;

    switch (range) {
      case 'week':
        // 7 days ago in Cambodia timezone
        startDate = new Date(today.getTime() - (7 * 86400000));
        break;
      case 'month':
        // 30 days ago in Cambodia timezone
        startDate = new Date(today.getTime() - (30 * 86400000));
        break;
      case 'year':
        // 365 days ago in Cambodia timezone
        startDate = new Date(today.getTime() - (365 * 86400000));
        break;
      default:
        // Default to week
        startDate = new Date(today.getTime() - (7 * 86400000));
    }

    const data = await FitnessData.find({
      userId,
      date: { $gte: startDate }
    }).sort({ date: 1 });

    // Calculate statistics
    const stats = {
      period: range,
      totalSteps: 0,
      totalCalories: 0,
      totalDistance: 0,
      totalActiveMinutes: 0,
      averageSteps: 0,
      averageCalories: 0,
      data: data
    };

    if (data.length > 0) {
      stats.totalSteps = data.reduce((sum, d) => sum + d.steps, 0);
      stats.totalCalories = data.reduce((sum, d) => sum + d.calories, 0);
      stats.totalDistance = data.reduce((sum, d) => sum + d.distance, 0);
      stats.totalActiveMinutes = data.reduce((sum, d) => sum + d.activeMinutes, 0);
      stats.averageSteps = Math.round(stats.totalSteps / data.length);
      stats.averageCalories = Math.round(stats.totalCalories / data.length);
    }

    res.json(stats);
  } catch (error) {
    console.error('Error fetching stats:', error);
    res.status(500).json({ error: 'Failed to fetch statistics' });
  }
});

// Log fitness activity
app.post('/fitness/log', verifyToken, async (req, res) => {
  try {
    const { userId, date, steps, calories, distance, activeMinutes, heartRate, notes } = req.body;

    if (!userId || !date) {
      return res.status(400).json({ error: 'userId and date required' });
    }

    // Check if entry already exists for this date
    // Handle timezone properly - use Cambodia timezone (GMT+7) not UTC
    const inputDate = new Date(date);
    // Convert to Cambodia time by adding 7 hours
    const cambodiaInputDate = new Date(inputDate.getTime() + (CAMBODIA_TIMEZONE_OFFSET * 60 * 60 * 1000));
    const startOfDay = new Date(cambodiaInputDate.getFullYear(), cambodiaInputDate.getMonth(), cambodiaInputDate.getDate(), 0, 0, 0, 0);
    const endOfDay = new Date(cambodiaInputDate.getFullYear(), cambodiaInputDate.getMonth(), cambodiaInputDate.getDate() + 1, 0, 0, 0, 0);

    let fitnessData = await FitnessData.findOne({
      userId,
      date: { $gte: startOfDay, $lt: endOfDay }
    });

    if (fitnessData) {
      // Update existing entry
      fitnessData.steps = steps || fitnessData.steps;
      fitnessData.calories = calories || fitnessData.calories;
      fitnessData.distance = distance || fitnessData.distance;
      fitnessData.activeMinutes = activeMinutes || fitnessData.activeMinutes;
      fitnessData.heartRate = heartRate || fitnessData.heartRate;
      fitnessData.notes = notes || fitnessData.notes;
      fitnessData.updatedAt = new Date();
    } else {
      // Create new entry
      fitnessData = new FitnessData({
        userId,
        date: startOfDay,
        steps: steps || 0,
        calories: calories || 0,
        distance: distance || 0,
        activeMinutes: activeMinutes || 0,
        heartRate,
        notes
      });
    }

    await fitnessData.save();

    res.status(201).json({
      success: true,
      data: fitnessData
    });
  } catch (error) {
    console.error('Error logging fitness data:', error);
    res.status(500).json({ error: 'Failed to log data', details: error.message });
  }
});

// Get summary statistics
app.get('/fitness/summary/:userId', verifyToken, async (req, res) => {
  try {
    const { userId } = req.params;

    // Get all-time stats
    const allData = await FitnessData.find({ userId });

    const summary = {
      totalEntries: allData.length,
      totalSteps: 0,
      totalCalories: 0,
      totalDistance: 0,
      totalActiveMinutes: 0,
      lastUpdate: null
    };

    if (allData.length > 0) {
      summary.totalSteps = allData.reduce((sum, d) => sum + d.steps, 0);
      summary.totalCalories = allData.reduce((sum, d) => sum + d.calories, 0);
      summary.totalDistance = allData.reduce((sum, d) => sum + d.distance, 0);
      summary.totalActiveMinutes = allData.reduce((sum, d) => sum + d.activeMinutes, 0);
      summary.lastUpdate = allData[allData.length - 1].updatedAt;
    }

    res.json(summary);
  } catch (error) {
    console.error('Error fetching summary:', error);
    res.status(500).json({ error: 'Failed to fetch summary' });
  }
});

app.listen(PORT, () => {
  console.log(`🚀 Fitness Service running on http://localhost:${PORT}`);
});
