const mongoose = require('mongoose');
require('dotenv').config();

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

async function checkDatabase() {
  try {
    console.log('\n🔍 Detailed MongoDB Verification\n');
    
    const uri = process.env.MONGODB_FITNESS_URI || 'mongodb://localhost:27017/fittrack_fitness';
    console.log('📍 Connection URI:', uri);
    
    await mongoose.connect(uri);
    console.log('✅ Connected to MongoDB\n');

    // Get all collections
    const collections = await mongoose.connection.db.listCollections().toArray();
    console.log('📦 Collections in database:');
    collections.forEach(col => console.log(`   - ${col.name}`));
    console.log('');

    // Count all documents in FitnessData model
    const count = await FitnessData.countDocuments();
    console.log(`📊 Total FitnessData documents: ${count}\n`);

    if (count > 0) {
      console.log('📋 All fitness records:\n');
      const records = await FitnessData.find({}).lean();
      console.log(JSON.stringify(records, null, 2));
    } else {
      console.log('❌ No fitness data in collection');
    }

    // Also check raw collection
    const rawCollection = mongoose.connection.db.collection('fitnessdata');
    const rawCount = await rawCollection.countDocuments();
    console.log(`\n🔗 Raw 'fitnessdata' collection count: ${rawCount}`);

    if (rawCount > 0) {
      const rawDocs = await rawCollection.find({}).toArray();
      console.log('\n📋 Raw documents:\n');
      console.log(JSON.stringify(rawDocs, null, 2));
    }

    await mongoose.connection.close();
    console.log('\n✅ Connection closed\n');

  } catch (error) {
    console.error('\n❌ Error:', error.message);
    process.exit(1);
  }
}

checkDatabase();
