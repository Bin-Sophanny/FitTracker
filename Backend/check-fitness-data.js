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

async function checkData() {
  try {
    console.log('\n╔════════════════════════════════════════╗');
    console.log('║   FITTRACK FITNESS DATA CHECK           ║');
    console.log('╚════════════════════════════════════════╝\n');

    // Count total records
    const totalCount = await FitnessData.countDocuments();
    console.log(`📊 Total Records in Database: ${totalCount}\n`);

    // Get latest 10 records
    const latestRecords = await FitnessData.find()
      .sort({ createdAt: -1 })
      .limit(10);

    if (latestRecords.length === 0) {
      console.log('❌ No fitness data found in database\n');
      console.log('📝 When you log activities from Kotlin app, data will appear here!\n');
    } else {
      console.log('✅ Latest Fitness Records:\n');
      latestRecords.forEach((record, index) => {
        console.log(`${index + 1}. User: ${record.userId}`);
        console.log(`   Date: ${record.date || 'N/A'}`);
        console.log(`   Steps: ${record.steps}`);
        console.log(`   Calories: ${record.calories}`);
        console.log(`   Distance: ${record.distance} km`);
        console.log(`   Active Minutes: ${record.activeMinutes}`);
        console.log(`   Last Updated: ${record.updatedAt}`);
        console.log('');
      });
    }

    // Get statistics
    const pipeline = [
      {
        $group: {
          _id: '$userId',
          totalSteps: { $sum: '$steps' },
          totalCalories: { $sum: '$calories' },
          totalDistance: { $sum: '$distance' },
          recordCount: { $sum: 1 }
        }
      },
      { $sort: { recordCount: -1 } }
    ];

    const stats = await FitnessData.aggregate(pipeline);
    
    if (stats.length > 0) {
      console.log('📈 User Statistics:\n');
      stats.forEach((stat, index) => {
        console.log(`${index + 1}. User ID: ${stat._id}`);
        console.log(`   Total Steps: ${stat.totalSteps}`);
        console.log(`   Total Calories: ${stat.totalCalories}`);
        console.log(`   Total Distance: ${stat.totalDistance} km`);
        console.log(`   Records: ${stat.recordCount}`);
        console.log('');
      });
    }

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    mongoose.connection.close();
    console.log('Connection closed.\n');
  }
}

checkData();
