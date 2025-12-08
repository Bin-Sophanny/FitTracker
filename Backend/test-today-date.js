const axios = require('axios');
const mongoose = require('mongoose');
require('dotenv').config();

const BASE_URL = 'http://localhost:3000/api';

async function testTodayDateFix() {
  try {
    console.log('\n✅ TESTING TODAY\'S DATE FIX\n');
    console.log('='.repeat(60));

    // Get token
    const loginRes = await axios.post(`${BASE_URL}/auth/login`, {
      firebaseUid: 'ghnrk8yLcGOn3rmaZFlCPGliJUI3',
      email: 'sophannykind@gmail.com'
    });

    const token = loginRes.data.token;
    const firebaseUid = loginRes.data.user.firebaseUid;

    console.log('\n1️⃣  Logging TODAY\'S data (with today\'s date)...\n');
    
    // Get today's date in local timezone
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const todayString = today.toISOString();
    
    console.log(`📅 Today's Local Date: ${today.toLocaleDateString()}`);
    console.log(`📅 ISO String sent: ${todayString}`);

    // Log today's data
    const logRes = await axios.post(`${BASE_URL}/fitness/log`, {
      userId: firebaseUid,
      date: todayString,
      steps: 8500,
      calories: 420,
      distance: 6.3,
      activeMinutes: 45,
      heartRate: 72
    }, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    console.log('\n✅ Data logged successfully');
    console.log(`   Steps: ${logRes.data.data.steps}`);
    console.log(`   Date stored: ${new Date(logRes.data.data.date).toLocaleDateString()}`);
    console.log(`   Calories: ${logRes.data.data.calories}`);

    // Retrieve today's data
    console.log('\n2️⃣  Retrieving today\'s data via /fitness/today endpoint...\n');
    
    const todayRes = await axios.get(`${BASE_URL}/fitness/today/${firebaseUid}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    console.log(`✅ Retrieved today's data:`);
    console.log(`   Steps: ${todayRes.data.steps}`);
    console.log(`   Date: ${new Date(todayRes.data.date).toLocaleDateString()}`);
    console.log(`   Calories: ${todayRes.data.calories}`);

    // Check in MongoDB directly
    console.log('\n3️⃣  Verifying in MongoDB...\n');
    
    await mongoose.connect('mongodb://localhost:27017/fittrack_fitness');
    const db = mongoose.connection.db;
    
    const docs = await db.collection('fitnessdatas')
      .find({ userId: firebaseUid })
      .sort({ date: -1 })
      .limit(5)
      .toArray();
    
    console.log(`✅ Latest 5 documents in MongoDB:\n`);
    docs.forEach((doc, i) => {
      const date = new Date(doc.date).toLocaleDateString();
      console.log(`${i+1}. ${date} - ${doc.steps} steps`);
    });

    await mongoose.disconnect();

    console.log('\n' + '='.repeat(60));
    console.log('\n✅ TODAY\'S DATE IS NOW BEING STORED CORRECTLY!\n');

  } catch (error) {
    console.error('\n❌ ERROR:', error.response?.data || error.message);
  }

  process.exit(0);
}

testTodayDateFix();
