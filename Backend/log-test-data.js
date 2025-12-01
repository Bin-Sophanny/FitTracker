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

async function logTestData() {
  try {
    console.log('\n╔════════════════════════════════════════╗');
    console.log('║   LOGGING TEST FITNESS DATA             ║');
    console.log('╚════════════════════════════════════════╝\n');

    const testData = {
      userId: 'user-demo-001',
      date: new Date(),
      steps: 5423,
      calories: 245,
      distance: 3.2,
      activeMinutes: 45
    };

    console.log('📝 Creating test record...');
    console.log('Data:', testData);
    console.log('');

    const result = await FitnessData.create(testData);
    
    console.log('✅ Successfully saved to MongoDB!\n');
    console.log('Record Details:');
    console.log(`  User ID: ${result.userId}`);
    console.log(`  Date: ${result.date}`);
    console.log(`  Steps: ${result.steps}`);
    console.log(`  Calories: ${result.calories}`);
    console.log(`  Distance: ${result.distance} km`);
    console.log(`  Active Minutes: ${result.activeMinutes}`);
    console.log(`  Created At: ${result.createdAt}`);
    console.log('');

    // Verify by reading back
    const verification = await FitnessData.findById(result._id);
    if (verification) {
      console.log('✅ Data verified - Successfully retrieved from database!\n');
    }

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    mongoose.connection.close();
    console.log('Connection closed.\n');
  }
}

logTestData();
