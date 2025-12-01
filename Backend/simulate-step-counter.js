const mongoose = require('mongoose');
require('dotenv').config();

// Connect to MongoDB
mongoose.connect(process.env.MONGODB_FITNESS_URI || 'mongodb://localhost:27017/fittrack_fitness', {
  useNewUrlParser: true,
  useUnifiedTopology: true,
});

// Define Fitness Data Schema
const fitnessSchema = new mongoose.Schema({
  userId: String,
  date: Date,
  steps: { type: Number, default: 0 },
  calories: { type: Number, default: 0 },
  distance: { type: Number, default: 0 },
  activeMinutes: { type: Number, default: 0 },
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
});

const FitnessData = mongoose.model('daily_stats', fitnessSchema);

async function simulateStepCounter() {
  try {
    console.log('\n╔════════════════════════════════════════╗');
    console.log('║   SIMULATING STEP COUNTER               ║');
    console.log('║   (Like your Kotlin app would)          ║');
    console.log('╚════════════════════════════════════════╝\n');

    const userId = 'user-demo-001';
    let currentSteps = 0;
    const stepsPerUpdate = 500;
    const totalUpdates = 5;

    console.log(`📱 Simulating ${totalUpdates} step updates...\n`);

    for (let i = 1; i <= totalUpdates; i++) {
      currentSteps += stepsPerUpdate;
      const calories = Math.round(currentSteps * 0.045);
      const distance = (currentSteps * 0.0008).toFixed(2);

      const update = {
        userId: userId,
        date: new Date(),
        steps: currentSteps,
        calories: calories,
        distance: parseFloat(distance),
        activeMinutes: Math.round(currentSteps / 100)
      };

      // Upsert - update if exists, create if not
      const result = await FitnessData.updateOne(
        { userId: userId, date: { $gte: new Date().setHours(0, 0, 0, 0) } },
        { $set: update },
        { upsert: true }
      );

      console.log(`Update ${i}/${totalUpdates}`);
      console.log(`  Steps: ${currentSteps}`);
      console.log(`  Calories: ${calories}`);
      console.log(`  Distance: ${distance} km`);
      console.log(`  Active Minutes: ${Math.round(currentSteps / 100)}`);
      console.log('');

      // Small delay to simulate real-time updates
      await new Promise(resolve => setTimeout(resolve, 500));
    }

    // Show final data
    const finalRecord = await FitnessData.findOne({ userId: userId }).sort({ updatedAt: -1 });
    
    console.log('╔════════════════════════════════════════╗');
    console.log('║   FINAL STEP COUNT                     ║');
    console.log('╚════════════════════════════════════════╝\n');
    
    if (finalRecord) {
      console.log(`✅ Total Steps: ${finalRecord.steps}`);
      console.log(`✅ Calories: ${finalRecord.calories}`);
      console.log(`✅ Distance: ${finalRecord.distance} km`);
      console.log(`✅ Active Minutes: ${finalRecord.activeMinutes}`);
      console.log(`✅ Last Updated: ${finalRecord.updatedAt}\n`);
    }

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    mongoose.connection.close();
    console.log('Connection closed.\n');
  }
}

simulateStepCounter();
