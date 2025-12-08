const axios = require('axios');
const mongoose = require('mongoose');
require('dotenv').config();

const BASE_URL = 'http://localhost:3000/api';

async function testTodayDataStorage() {
  try {
    console.log('\n✅ TESTING TODAY DATA STORAGE\n');
    console.log('='.repeat(60));

    // Step 1: Login
    console.log('\n1️⃣  Getting authentication token...');
    const loginRes = await axios.post(`${BASE_URL}/auth/login`, {
      firebaseUid: 'ghnrk8yLcGOn3rmaZFlCPGliJUI3',
      email: 'sophannykind@gmail.com'
    });

    const token = loginRes.data.token;
    const firebaseUid = loginRes.data.user.firebaseUid;
    console.log('✅ Logged in successfully');
    console.log(`   Token: ${token.substring(0, 30)}...`);
    console.log(`   UID: ${firebaseUid}`);

    // Step 2: Get today's date
    const today = new Date();
    const todayLocal = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 0, 0, 0, 0);
    const todayISO = todayLocal.toISOString();

    console.log('\n2️⃣  Logging 50 steps for TODAY...');
    console.log(`   Today's local date: ${today.toLocaleDateString()}`);
    console.log(`   ISO string being sent: ${todayISO}`);

    // Step 3: Log today's data
    const logRes = await axios.post(`${BASE_URL}/fitness/log`, {
      userId: firebaseUid,
      date: todayISO,
      steps: 50,
      calories: 25,
      distance: 0.04,
      activeMinutes: 2,
      heartRate: 70
    }, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    console.log('✅ Data logged successfully');
    console.log(`   Steps: ${logRes.data.data.steps}`);
    console.log(`   Stored date: ${new Date(logRes.data.data.date).toLocaleDateString()}`);

    // Step 4: Retrieve today's data via API
    console.log('\n3️⃣  Retrieving today\'s data from backend...');
    const todayRes = await axios.get(`${BASE_URL}/fitness/today/${firebaseUid}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    console.log('✅ Today\'s data retrieved');
    console.log(`   Steps: ${todayRes.data.steps}`);
    console.log(`   Date: ${new Date(todayRes.data.date).toLocaleDateString()}`);

    // Step 5: Verify in MongoDB directly
    console.log('\n4️⃣  Verifying in MongoDB...');
    await mongoose.connect('mongodb://localhost:27017/fittrack_fitness');
    const db = mongoose.connection.db;

    const todayDocs = await db.collection('fitnessdatas')
      .find({
        userId: firebaseUid,
        date: { $gte: todayLocal, $lt: new Date(todayLocal.getTime() + 86400000) }
      })
      .toArray();

    console.log(`✅ MongoDB search complete`);
    console.log(`   Documents found: ${todayDocs.length}`);

    if (todayDocs.length > 0) {
      todayDocs.forEach(doc => {
        console.log(`   - ${doc.steps} steps (${new Date(doc.date).toLocaleDateString()})`);
      });
    } else {
      console.log('   ❌ No documents found for today!');
      
      // Check all documents for this user
      console.log('\n5️⃣  Checking ALL documents for this user...');
      const allDocs = await db.collection('fitnessdatas')
        .find({ userId: firebaseUid })
        .sort({ date: -1 })
        .limit(5)
        .toArray();

      console.log(`   Total documents: ${allDocs.length}`);
      if (allDocs.length > 0) {
        console.log('   Last 5 documents:');
        allDocs.forEach(doc => {
          console.log(`   - ${new Date(doc.date).toLocaleDateString()}: ${doc.steps} steps`);
        });
      }
    }

    await mongoose.disconnect();

    console.log('\n' + '='.repeat(60));
    if (todayDocs.length > 0) {
      console.log('\n✅ TODAY\'S 50 STEPS HAVE BEEN STORED!\n');
    } else {
      console.log('\n❌ DATA NOT STORED - DEBUGGING INFO ABOVE\n');
    }

  } catch (error) {
    console.error('\n❌ ERROR:', error.response?.data || error.message);
    if (error.response?.data) {
      console.error('Response:', JSON.stringify(error.response.data, null, 2));
    }
  }

  process.exit(0);
}

testTodayDataStorage();
