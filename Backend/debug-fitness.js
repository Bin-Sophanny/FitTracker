const mongoose = require('mongoose');
require('dotenv').config();

console.log('\n╔════════════════════════════════════════╗');
console.log('║   FITTRACK DEBUG & TROUBLESHOOT         ║');
console.log('╚════════════════════════════════════════╝\n');

// Check environment variables
console.log('1️⃣ Environment Configuration:');
console.log('   MONGODB_FITNESS_URI:', process.env.MONGODB_FITNESS_URI || 'NOT SET');
console.log('   API_GATEWAY_PORT:', process.env.API_GATEWAY_PORT || '3000');
console.log('   FITNESS_SERVICE_PORT:', process.env.FITNESS_SERVICE_PORT || '3002');
console.log('');

// Try to connect
console.log('2️⃣ Attempting MongoDB Connection:');
mongoose.connect(process.env.MONGODB_FITNESS_URI || 'mongodb://localhost:27017/fittrack_fitness', {
  useNewUrlParser: true,
  useUnifiedTopology: true,
}).then(() => {
  console.log('   ✅ Successfully connected to MongoDB\n');
  checkDatabase();
}).catch(err => {
  console.log('   ❌ Failed to connect:', err.message, '\n');
  process.exit(1);
});

async function checkDatabase() {
  try {
    // Check all collections
    console.log('3️⃣ Collections in fittrack_fitness database:');
    const collections = await mongoose.connection.db.listCollections().toArray();
    
    if (collections.length === 0) {
      console.log('   ⚠️  No collections found (database is empty)');
    } else {
      for (const collection of collections) {
        const count = await mongoose.connection.db.collection(collection.name).countDocuments();
        console.log(`   - ${collection.name}: ${count} documents`);
      }
    }
    console.log('');

    // Recommendations
    console.log('4️⃣ Troubleshooting Steps:\n');
    console.log('If no data in MongoDB:\n');
    console.log('  1️⃣ Check Kotlin app logs in Android Studio Logcat');
    console.log('  2️⃣ Look for error messages like "Cannot connect" or "Network error"');
    console.log('  3️⃣ Verify backend IP: http://192.168.50.249:3000');
    console.log('  4️⃣ Make sure internet permission is in AndroidManifest.xml');
    console.log('  5️⃣ Check if app is on same network as backend computer\n');
    
    console.log('If app shows 0 steps:\n');
    console.log('  1️⃣ App might not have step counter permission enabled');
    console.log('  2️⃣ Phone accelerometer might not be working');
    console.log('  3️⃣ Need to check Android permissions\n');
    
    console.log('If backend is not saving:\n');
    console.log('  1️⃣ Check backend logs for POST /api/fitness/log errors');
    console.log('  2️⃣ Run: npm run services:fitness (check for errors)');
    console.log('  3️⃣ Verify MongoDB connection in Fitness Service\n');

  } catch (error) {
    console.error('Error during checks:', error.message);
  } finally {
    mongoose.connection.close();
    process.exit(0);
  }
}
