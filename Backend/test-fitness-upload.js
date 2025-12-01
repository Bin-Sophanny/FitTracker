/**
 * Test Fitness Data Upload
 * Simulates what the Kotlin app should be doing
 */

const axios = require('axios');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const API_BASE = 'http://localhost:3000';
const USER_ID = 'test-user-' + Date.now();

// Generate a test JWT token
const generateTestToken = () => {
  return jwt.sign(
    { uid: USER_ID, email: 'test@example.com', id: USER_ID },
    process.env.JWT_SECRET || 'test-secret-key',
    { expiresIn: '7d' }
  );
};

console.log('\n📱 Simulating Kotlin App Fitness Upload\n');
console.log('🔧 Config:');
console.log(`   Base URL: ${API_BASE}`);
console.log(`   User ID: ${USER_ID}\n`);

async function testFitnessUpload() {
  try {
    console.log('📤 Uploading fitness data...\n');

    const token = generateTestToken();
    console.log('🔐 Generated JWT Token:', token.substring(0, 30) + '...\n');

    const fitnessData = {
      userId: USER_ID,
      date: new Date().toISOString(),
      steps: Math.floor(Math.random() * 5000) + 1000,
      calories: Math.floor(Math.random() * 500) + 100,
      distance: (Math.random() * 5 + 1).toFixed(2),
      activeMinutes: Math.floor(Math.random() * 120) + 30,
      heartRate: Math.floor(Math.random() * 80) + 60
    };

    console.log('📊 Data being sent:');
    console.log(JSON.stringify(fitnessData, null, 2));
    console.log('\n');

    const response = await axios.post(`${API_BASE}/api/fitness/log`, fitnessData, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      timeout: 5000
    });

    console.log('✅ SUCCESS! Response:', response.status);
    console.log(JSON.stringify(response.data, null, 2));
    console.log('\n🎉 Data uploaded successfully!\n');

    // Now verify it's in MongoDB
    console.log('🔍 Checking MongoDB...\n');
    setTimeout(() => {
      require('./check-fitness-data.js');
    }, 500);

  } catch (error) {
    console.log('❌ ERROR uploading data:\n');
    if (error.response) {
      console.log(`Status: ${error.response.status}`);
      console.log('Data:', JSON.stringify(error.response.data, null, 2));
    } else if (error.code === 'ECONNREFUSED') {
      console.log('⚠️  Cannot connect to API Gateway on port 3000');
      console.log('Make sure the backend is running!');
    } else if (error.code === 'ENOTFOUND') {
      console.log('⚠️  Cannot resolve localhost');
    } else {
      console.log('Message:', error.message);
    }
    console.log('\n');
  }
}

testFitnessUpload();
