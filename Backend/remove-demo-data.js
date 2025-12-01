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

async function removeData() {
  try {
    console.log('\n╔════════════════════════════════════════╗');
    console.log('║   REMOVING DEMO DATA FROM MONGODB      ║');
    console.log('╚════════════════════════════════════════╝\n');

    // Count records before deletion
    const countBefore = await FitnessData.countDocuments();
    console.log(`📊 Records before deletion: ${countBefore}\n`);

    if (countBefore === 0) {
      console.log('✅ Database is already empty - nothing to delete!\n');
    } else {
      // Delete all demo data
      const result = await FitnessData.deleteMany({});
      
      console.log(`🗑️  Deleting all records...`);
      console.log(`✅ Deleted: ${result.deletedCount} records\n`);

      // Verify deletion
      const countAfter = await FitnessData.countDocuments();
      console.log(`📊 Records after deletion: ${countAfter}`);
      
      if (countAfter === 0) {
        console.log('\n✅ SUCCESS! All demo data removed from MongoDB!\n');
      }
    }

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    mongoose.connection.close();
    console.log('Connection closed.\n');
  }
}

removeData();
