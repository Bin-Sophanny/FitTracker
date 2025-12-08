const axios = require('axios');
require('dotenv').config();

const BASE_URL = 'http://localhost:3000/api';

async function testWeeklyStats() {
  try {
    console.log('\n✅ TESTING WEEKLY STATS WITH TODAY\'S DATA\n');
    console.log('='.repeat(60));

    // Get token
    const loginRes = await axios.post(`${BASE_URL}/auth/login`, {
      firebaseUid: 'ghnrk8yLcGOn3rmaZFlCPGliJUI3',
      email: 'sophannykind@gmail.com'
    });

    const token = loginRes.data.token;
    const firebaseUid = loginRes.data.user.firebaseUid;

    console.log('\n1️⃣  Fetching WEEKLY stats...\n');

    const weeklyRes = await axios.get(`${BASE_URL}/fitness/stats/${firebaseUid}/week`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    console.log(`✅ Weekly Stats Retrieved:`);
    console.log(`   Period: ${weeklyRes.data.period}`);
    console.log(`   Documents in week: ${weeklyRes.data.data.length}`);
    console.log(`   Total Steps: ${weeklyRes.data.totalSteps}`);
    console.log(`   Total Calories: ${weeklyRes.data.totalCalories}`);
    console.log(`   Average Steps per day: ${weeklyRes.data.averageSteps}`);

    console.log('\n2️⃣  Breaking down by date:\n');
    
    const dateMap = {};
    weeklyRes.data.data.forEach(doc => {
      const date = new Date(doc.date).toLocaleDateString();
      if (!dateMap[date]) {
        dateMap[date] = { steps: 0, calories: 0, count: 0 };
      }
      dateMap[date].steps += doc.steps;
      dateMap[date].calories += doc.calories;
      dateMap[date].count += 1;
    });

    Object.keys(dateMap).sort().forEach(date => {
      const stats = dateMap[date];
      console.log(`   ${date}: ${stats.steps} steps, ${stats.calories} cal`);
    });

    console.log('\n3️⃣  Checking if TODAY\'S DATA is included:\n');
    
    const today = new Date();
    const todayStr = today.toLocaleDateString();
    
    if (dateMap[todayStr]) {
      console.log(`✅ TODAY (${todayStr}) DATA IS INCLUDED IN STATS!`);
      console.log(`   Steps: ${dateMap[todayStr].steps}`);
      console.log(`   Calories: ${dateMap[todayStr].calories}`);
    } else {
      console.log(`❌ TODAY (${todayStr}) DATA IS NOT IN STATS`);
      console.log(`   Available dates: ${Object.keys(dateMap).sort().join(', ')}`);
    }

    console.log('\n' + '='.repeat(60) + '\n');

  } catch (error) {
    console.error('\n❌ ERROR:', error.response?.data || error.message);
  }

  process.exit(0);
}

testWeeklyStats();
