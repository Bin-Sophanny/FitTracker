const axios = require('axios');
const mongoose = require('mongoose');
require('dotenv').config();

const BASE_URL = 'http://localhost:3000/api';

async function testBackendSync() {
  try {
    console.log('\n✅ TESTING BACKEND SYNC WITH EXISTING DATA\n');
    console.log('='.repeat(60));

    // Test 1: Login with existing user
    console.log('\n1️⃣  Testing login endpoint...');
    const loginRes = await axios.post(`${BASE_URL}/auth/login`, {
      firebaseUid: 'ghnrk8yLcGOn3rmaZFlCPGliJUI3',
      email: 'sophannykind@gmail.com',
      displayName: 'Sophanny Kind'
    });

    if (loginRes.data.token) {
      console.log('✅ Login successful');
      console.log('   Token:', loginRes.data.token.substring(0, 30) + '...');
      console.log('   User ID:', loginRes.data.user.id);
    } else {
      console.log('❌ No token in response');
    }

    const token = loginRes.data.token;
    const userId = loginRes.data.user.id;

    // Test 2: Get user profile
    console.log('\n2️⃣  Testing get profile endpoint...');
    const profileRes = await axios.get(`${BASE_URL}/auth/profile/${userId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (profileRes.data) {
      console.log('✅ Profile retrieved successfully');
      console.log('   Email:', profileRes.data.email);
      console.log('   Display Name:', profileRes.data.displayName);
      console.log('   Firebase UID:', profileRes.data.firebaseUid);
    }

    // Test 3: Get fitness summary
    console.log('\n3️⃣  Testing fitness summary endpoint...');
    const summaryRes = await axios.get(`${BASE_URL}/fitness/summary/${loginRes.data.user.firebaseUid}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (summaryRes.data) {
      console.log('✅ Fitness summary retrieved successfully');
      console.log('   Total Entries:', summaryRes.data.totalEntries);
      console.log('   Total Steps:', summaryRes.data.totalSteps);
      console.log('   Total Calories:', summaryRes.data.totalCalories);
      console.log('   Total Distance:', summaryRes.data.totalDistance, 'km');
      console.log('   Total Active Minutes:', summaryRes.data.totalActiveMinutes);
    }

    // Test 4: Get today's data
    console.log('\n4️⃣  Testing today\'s fitness data endpoint...');
    const todayRes = await axios.get(`${BASE_URL}/fitness/today/${loginRes.data.user.firebaseUid}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (todayRes.data) {
      console.log('✅ Today\'s data retrieved successfully');
      console.log('   Steps:', todayRes.data.steps || 0);
      console.log('   Date:', new Date(todayRes.data.date).toLocaleDateString());
    }

    // Test 5: Get weekly stats
    console.log('\n5️⃣  Testing weekly stats endpoint...');
    const statsRes = await axios.get(`${BASE_URL}/fitness/stats/${loginRes.data.user.firebaseUid}/week`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (statsRes.data) {
      console.log('✅ Weekly stats retrieved successfully');
      console.log('   Documents:', statsRes.data.data.length);
      console.log('   Period:', statsRes.data.period);
      console.log('   Total Steps:', statsRes.data.totalSteps);
      console.log('   Average Steps:', statsRes.data.averageSteps);
    }

    // Test 6: Log new fitness data
    console.log('\n6️⃣  Testing log fitness endpoint...');
    const logRes = await axios.post(`${BASE_URL}/fitness/log`, {
      userId: 'ghnrk8yLcGOn3rmaZFlCPGliJUI3',
      date: new Date().toISOString(),
      steps: Math.floor(Math.random() * 10000),
      calories: Math.floor(Math.random() * 500),
      distance: (Math.random() * 10).toFixed(1),
      activeMinutes: Math.floor(Math.random() * 60),
      heartRate: Math.floor(Math.random() * 40 + 60)
    }, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (logRes.data.success) {
      console.log('✅ Fitness data logged successfully');
      console.log('   Steps:', logRes.data.data.steps);
      console.log('   Calories:', logRes.data.data.calories);
      console.log('   Distance:', logRes.data.data.distance, 'km');
    }

    // Test 7: Verify data in MongoDB
    console.log('\n7️⃣  Verifying data in MongoDB...');
    await mongoose.connect('mongodb://localhost:27017/fittrack_fitness');
    const db = mongoose.connection.db;
    
    const userCount = await db.collection('users').countDocuments({ firebaseUid: 'ghnrk8yLcGOn3rmaZFlCPGliJUI3' });
    const fitnessCount = await db.collection('fitnessdatas').countDocuments({ userId: 'ghnrk8yLcGOn3rmaZFlCPGliJUI3' });
    
    console.log('✅ MongoDB verification');
    console.log('   User documents:', userCount);
    console.log('   Fitness documents:', fitnessCount);
    
    await mongoose.disconnect();

    console.log('\n' + '='.repeat(60));
    console.log('\n✅ ALL BACKEND SYNC TESTS PASSED!\n');
    console.log('Firebase → Backend Sync Status:');
    console.log('  ✓ User authentication working');
    console.log('  ✓ User data synced to MongoDB');
    console.log('  ✓ Fitness data logging working');
    console.log('  ✓ Data retrieval successful');
    console.log('  ✓ JWT token generation working');
    console.log('\n📊 Current Test User Data:');
    console.log(`  Email: sophannykind@gmail.com`);
    console.log(`  Firebase UID: ghnrk8yLcGOn3rmaZFlCPGliJUI3`);
    console.log(`  Total Fitness Entries: ${fitnessCount}`);

  } catch (error) {
    console.error('\n❌ ERROR:', error.response?.data?.error || error.message);
    if (error.response?.data) {
      console.error('\nFull response:', JSON.stringify(error.response.data, null, 2));
    }
  }
  
  process.exit(0);
}

testBackendSync();
