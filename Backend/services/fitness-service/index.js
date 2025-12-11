const express = require('express');
const mongoose = require('mongoose');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const app = express();
const PORT = process.env.FITNESS_SERVICE_PORT || 3002;

// Middleware
app.use(express.json());

// MongoDB Connection with retry options
mongoose.connect(process.env.MONGODB_FITNESS_URI || 'mongodb://localhost:27017/fittrack_fitness', {
  maxPoolSize: 10,
  minPoolSize: 2,
  serverSelectionTimeoutMS: 5000,
  socketTimeoutMS: 45000,
  connectTimeoutMS: 10000,
  retryWrites: true,
  retryReads: true
})
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

// Helper function to generate date range pattern for regex
function generateDateRange(startDateStr, endDateStr) {
  const dates = [];
  
  // Parse dates from "YYYY-MM-DD" format
  const [startYear, startMonth, startDay] = startDateStr.split('-').map(Number);
  const [endYear, endMonth, endDay] = endDateStr.split('-').map(Number);
  
  const current = new Date(startYear, startMonth - 1, startDay);
  const end = new Date(endYear, endMonth - 1, endDay);
  
  while (current <= end) {
    const year = current.getFullYear();
    const month = String(current.getMonth() + 1).padStart(2, '0');
    const day = String(current.getDate()).padStart(2, '0');
    dates.push(`${year}-${month}-${day}`);
    current.setDate(current.getDate() + 1);
  }
  
  console.log(`[generateDateRange] Generated dates: ${dates.join(', ')}`);
  return dates.join('|');
}

// Import FitnessData model from shared models
const FitnessData = require('../../shared/models/FitnessData.js');

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
    
    // Get today's date in Cambodia timezone
    const cambodiaToday = getCambodiaDateOnly();
    
    // Convert to UTC for MongoDB query
    // Subtract 7 hours to convert Cambodia local to UTC
    const todayUTC = new Date(cambodiaToday.getTime() - (CAMBODIA_TIMEZONE_OFFSET * 60 * 60 * 1000));
    const startOfDayUTC = new Date(todayUTC);
    startOfDayUTC.setUTCHours(0, 0, 0, 0);
    
    const endOfDayUTC = new Date(startOfDayUTC);
    endOfDayUTC.setUTCDate(endOfDayUTC.getUTCDate() + 1);

    const data = await FitnessData.findOne({
      userId,
      date: { $gte: startOfDayUTC, $lt: endOfDayUTC }
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
    const cambodiaToday = getCambodiaDateOnly();
    let dayCount;

    switch (range) {
      case 'week':
        dayCount = 7;
        break;
      case 'month':
        dayCount = 30;
        break;
      case 'year':
        dayCount = 365;
        break;
      default:
        dayCount = 7;
    }

    // Calculate Cambodia date range
    const cambodiaStartDate = new Date(cambodiaToday.getTime() - (dayCount * 86400000));

    // Since date is stored as string (ISO format with +07:00), we need to query by string prefix
    // Format dates as "YYYY-MM-DD" for string comparison
    const dateFormat = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    const startDateStr = dateFormat(cambodiaStartDate);
    const todayStr = dateFormat(cambodiaToday);

    // Query using regex pattern to match date strings in range
    const datePattern = generateDateRange(startDateStr, todayStr);
    
    const data = await FitnessData.find({
      userId,
      date: { $regex: `^(${datePattern})` }
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
    const { userId, date, steps, calories, distance, activeMinutes, notes } = req.body;

    if (!userId || !date) {
      return res.status(400).json({ error: 'userId and date required' });
    }

    console.log(`\n=== /fitness/log REQUEST ===`);
    console.log(`User ID: ${userId}`);
    console.log(`Date received from client: ${date} (type: ${typeof date})`);
    console.log(`Note: Client sends Cambodia local date, we convert to UTC for storage`);

    let cambodiaLocalDate;
    let isGmt7DateReceived = false;

    // Handle three date formats:
    // 1. ISO format with GMT+7: "2025-12-10T00:00:00.000+07:00" ← PREFER THIS
    // 2. ISO format with UTC: "2025-12-10T00:00:00.000Z" or "2025-12-10T00:00:00.000+00:00"
    // 3. Simple date string: "2025-12-10"
    
    if (date.includes('T')) {
      // ISO format - check if it has GMT+7 timezone
      if (date.includes('+07:00') || date.includes('+0700')) {
        // Already in GMT+7! Just parse and use directly
        cambodiaLocalDate = new Date(date);
        isGmt7DateReceived = true;
      } else {
        // UTC format - just parse it
        cambodiaLocalDate = new Date(date);
      }
    } else {
      // Simple date format "yyyy-MM-dd" - client sent Cambodia local date
      // Parse it as local midnight in Cambodia
      const dateParts = date.split('-');
      const year = parseInt(dateParts[0]);
      const month = parseInt(dateParts[1]) - 1; // Month is 0-based
      const day = parseInt(dateParts[2]);
      // This creates a "naive" date that represents midnight in Cambodia timezone
      cambodiaLocalDate = new Date(year, month, day, 0, 0, 0, 0);
    }

    // Convert back to ISO string with GMT+7 timezone for storage
    // This preserves the +07:00 offset and ensures MongoDB stores the correct date
    let dateForStorage;
    
    if (isGmt7DateReceived) {
      // Already received as GMT+7, use the original date string
      dateForStorage = date;
    } else {
      // Need to format the date as ISO string with GMT+7 offset
      const year = cambodiaLocalDate.getFullYear();
      const month = String(cambodiaLocalDate.getMonth() + 1).padStart(2, '0');
      const day = String(cambodiaLocalDate.getDate()).padStart(2, '0');
      const hours = String(cambodiaLocalDate.getHours()).padStart(2, '0');
      const minutes = String(cambodiaLocalDate.getMinutes()).padStart(2, '0');
      const seconds = String(cambodiaLocalDate.getSeconds()).padStart(2, '0');
      const milliseconds = String(cambodiaLocalDate.getMilliseconds()).padStart(3, '0');
      
      // Format as ISO string with GMT+7 timezone: "2025-12-10T00:00:00.000+07:00"
      dateForStorage = `${year}-${month}-${day}T${hours}:${minutes}:${seconds}.${milliseconds}+07:00`;
    }

    // For querying, extract just the date part (yyyy-MM-dd) to find records for this day
    // Since we now store dates as strings like "2025-12-10T00:00:00.000+07:00"
    // We can use string comparison or extract the date prefix
    const datePrefix = dateForStorage.substring(0, 10); // Extract "2025-12-10"

    // Find existing entry for this specific userId and date
    // Since date is now a string, we use regex to match the date prefix
    let fitnessData = await FitnessData.findOne({
      userId: userId,
      date: { $regex: `^${datePrefix}` }
    });

    if (fitnessData) {
      // Update existing entry for this date
      fitnessData.steps = steps;
      fitnessData.calories = calories;
      fitnessData.distance = parseFloat(distance).toFixed(2);
      fitnessData.activeMinutes = activeMinutes;
      if (notes) fitnessData.notes = notes;
      fitnessData.updatedAt = new Date();
      await fitnessData.save();
    } else {
      // Create new entry for this date
      fitnessData = new FitnessData({
        userId: userId,
        date: dateForStorage,  // Store GMT+7 date directly in MongoDB
        steps: steps || 0,
        calories: calories || 0,
        distance: parseFloat(distance || 0).toFixed(2),
        activeMinutes: activeMinutes || 0,
        notes: notes || null,
        createdAt: new Date(),
        updatedAt: new Date()
      });
      await fitnessData.save();
    }

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
