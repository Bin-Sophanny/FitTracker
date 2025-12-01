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

    // Check fitness service endpoint
    console.log('4️⃣ Testing Fitness Service API:');
    console.log('   Testing: GET http://localhost:3002/fitness/health');
    
    try {
      const response = await fetch('http://localhost:3002/fitness/health');
      if (response.ok) {
        const data = await response.json();
        console.log('   ✅ Fitness Service is running');
        console.log('   Response:', data);
      } else {
        console.log('   ❌ Fitness Service returned:', response.status);
      }
    } catch (e) {
      console.log('   ❌ Cannot reach Fitness Service:', e.message);
      console.log('   Make sure to run: npm run services:fitness');
    }
    console.log('');

    // Check API Gateway routing
    console.log('5️⃣ Testing API Gateway Routing:');
    console.log('   Testing: GET http://localhost:3000/api/fitness/health');
    
    try {
      const response = await fetch('http://localhost:3000/api/fitness/health');
      if (response.ok) {
        const data = await response.json();
        console.log('   ✅ API Gateway routing works');
        console.log('   Response:', data);
      } else {
        console.log('   ❌ API Gateway returned:', response.status);
      }
    } catch (e) {
      console.log('   ❌ Cannot reach API Gateway:', e.message);
      console.log('   Make sure to run: npm start');
    }
    console.log('');

    // Recommendations
    console.log('6️⃣ Troubleshooting Steps:');
    console.log('');
    console.log('If no data in MongoDB:');
    console.log('  1. Check Kotlin app logs in Android Studio Logcat');
    console.log('  2. Look for error messages like "Cannot connect" or "Network error"');
    console.log('  3. Verify backend IP: http://192.168.50.249:3000');
    console.log('  4. Make sure internet permission is in AndroidManifest.xml');
    console.log('  5. Check if app is on same network as backend computer');
    console.log('');
    console.log('If app shows 0 steps:');
    console.log('  1. App might not have step counter permission enabled');
    console.log('  2. Phone accelerometer might not be working');
    console.log('  3. Need to check Android permissions');
    console.log('');
    console.log('If backend is not saving:');
    console.log('  1. Check backend logs for POST /api/fitness/log errors');
    console.log('  2. Run: npm run services:fitness (check for errors)');
    console.log('  3. Verify MongoDB connection in Fitness Service');
    console.log('');

  } catch (error) {
    console.error('Error during checks:', error.message);
  } finally {
    mongoose.connection.close();
    process.exit(0);
  }
}
