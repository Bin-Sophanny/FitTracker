/**
 * Insert 1 Week of Test Fitness Data
 * For user: sophannykind (Firebase UID: ghnrk8yLcGOn3rmaZFlCPGliJUI3)
 */

const mongoose = require('mongoose');
require('dotenv').config();

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

// User data
const userId = 'ghnrk8yLcGOn3rmaZFlCPGliJUI3';
const email = 'sophannykind@gmail.com';
const displayName = 'sophannykind';

// Generate 7 days of test data
function generateWeekData() {
  const data = [];
  const today = new Date();
  
  // Sample data for each day of the week
  const dailyData = [
    { steps: 8450, calories: 420, distance: 6.2, activeMinutes: 45, heartRate: 72 },
    { steps: 10250, calories: 520, distance: 7.5, activeMinutes: 58, heartRate: 75 },
    { steps: 6780, calories: 310, distance: 5.0, activeMinutes: 32, heartRate: 68 },
    { steps: 12500, calories: 650, distance: 9.2, activeMinutes: 75, heartRate: 80 },
    { steps: 9100, calories: 450, distance: 6.7, activeMinutes: 50, heartRate: 73 },
    { steps: 11200, calories: 580, distance: 8.2, activeMinutes: 65, heartRate: 77 },
    { steps: 7650, calories: 380, distance: 5.6, activeMinutes: 40, heartRate: 70 }
  ];

  // Create dates for past 7 days
  for (let i = 6; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    date.setHours(0, 0, 0, 0);

    data.push({
      userId: userId,
      date: date,
      steps: dailyData[6 - i].steps,
      calories: dailyData[6 - i].calories,
      distance: dailyData[6 - i].distance,
      activeMinutes: dailyData[6 - i].activeMinutes,
      heartRate: dailyData[6 - i].heartRate,
      notes: `Sample data for ${date.toLocaleDateString()}`,
      createdAt: new Date(),
      updatedAt: new Date()
    });
  }

  return data;
}

async function insertWeekData() {
  try {
    console.log('\n📊 Inserting 1 Week of Test Fitness Data\n');
    
    const uri = process.env.MONGODB_FITNESS_URI || 'mongodb://localhost:27017/fittrack_fitness';
    console.log('📍 Connecting to:', uri);
    
    await mongoose.connect(uri);
    console.log('✅ Connected to MongoDB\n');

    // Generate data
    const weekData = generateWeekData();
    
    console.log(`📤 Inserting ${weekData.length} records for user: ${displayName}`);
    console.log(`   Firebase UID: ${userId}`);
    console.log(`   Email: ${email}\n`);

    // Insert data
    const result = await FitnessData.insertMany(weekData);
    
    console.log(`✅ Successfully inserted ${result.length} records!\n`);
    
    // Display inserted data
    console.log('📋 Inserted Records:\n');
    weekData.forEach((record, index) => {
      console.log(`Day ${index + 1}:`);
      console.log(`  📅 Date: ${record.date.toLocaleDateString()}`);
      console.log(`  👟 Steps: ${record.steps}`);
      console.log(`  🔥 Calories: ${record.calories}`);
      console.log(`  📏 Distance: ${record.distance} km`);
      console.log(`  ⏱️  Active Minutes: ${record.activeMinutes}`);
      console.log(`  ❤️  Heart Rate: ${record.heartRate} bpm`);
      console.log('');
    });

    // Show summary
    const totalSteps = weekData.reduce((sum, d) => sum + d.steps, 0);
    const totalCalories = weekData.reduce((sum, d) => sum + d.calories, 0);
    const totalDistance = weekData.reduce((sum, d) => sum + d.distance, 0);
    const totalActiveMinutes = weekData.reduce((sum, d) => sum + d.activeMinutes, 0);
    const avgHeartRate = Math.round(weekData.reduce((sum, d) => sum + d.heartRate, 0) / weekData.length);

    console.log('📊 Weekly Summary:');
    console.log(`  Total Steps: ${totalSteps.toLocaleString()}`);
    console.log(`  Total Calories: ${totalCalories}`);
    console.log(`  Total Distance: ${totalDistance.toFixed(1)} km`);
    console.log(`  Total Active Minutes: ${totalActiveMinutes}`);
    console.log(`  Average Heart Rate: ${avgHeartRate} bpm\n`);

    // Verify data
    const count = await FitnessData.countDocuments({ userId });
    console.log(`✅ Verification: ${count} total records for this user in database\n`);

    await mongoose.connection.close();
    console.log('✅ Connection closed\n');

  } catch (error) {
    console.error('\n❌ Error:', error.message);
    process.exit(1);
  }
}

insertWeekData();
