const mongoose = require('mongoose');

mongoose.connect('mongodb://localhost:27017/fittrack_fitness')
  .then(async () => {
    const db = mongoose.connection.db;
    const userId = 'ghnrk8yLcGOn3rmaZFlCPGliJUI3';
    
    // Get data from the past 7 days
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
    sevenDaysAgo.setHours(0, 0, 0, 0);
    
    console.log('\n📊 WEEKLY AGGREGATION (Last 7 Days):\n');
    console.log('Calculate date range:');
    console.log('Today:', new Date().toLocaleDateString());
    console.log('7 days ago:', sevenDaysAgo.toLocaleDateString());
    
    // Method 1: Simple find and sum
    const data = await db.collection('fitnessdatas')
      .find({ 
        userId,
        date: { $gte: sevenDaysAgo }
      })
      .sort({ date: 1 })
      .toArray();
    
    console.log(`\n✅ Found ${data.length} documents in last 7 days\n`);
    
    // Method 2: Using MongoDB aggregation (recommended)
    const aggregationResult = await db.collection('fitnessdatas').aggregate([
      {
        $match: {
          userId,
          date: { $gte: sevenDaysAgo }
        }
      },
      {
        $group: {
          _id: null,
          totalSteps: { $sum: '$steps' },
          totalCalories: { $sum: '$calories' },
          totalDistance: { $sum: '$distance' },
          totalActiveMinutes: { $sum: '$activeMinutes' },
          averageHeartRate: { $avg: '$heartRate' },
          count: { $sum: 1 }
        }
      }
    ]).toArray();
    
    console.log('📈 AGGREGATED STATS:\n');
    if (aggregationResult.length > 0) {
      const stats = aggregationResult[0];
      console.log(`Total Steps: ${stats.totalSteps}`);
      console.log(`Total Calories: ${stats.totalCalories}`);
      console.log(`Total Distance: ${stats.totalDistance} km`);
      console.log(`Total Active Minutes: ${stats.totalActiveMinutes}`);
      console.log(`Average Heart Rate: ${Math.round(stats.averageHeartRate)} bpm`);
      console.log(`Days with data: ${stats.count}`);
    }
    
    // Method 3: Daily breakdown
    const dailyStats = await db.collection('fitnessdatas').aggregate([
      {
        $match: {
          userId,
          date: { $gte: sevenDaysAgo }
        }
      },
      {
        $group: {
          _id: {
            $dateToString: { format: '%Y-%m-%d', date: '$date' }
          },
          steps: { $sum: '$steps' },
          calories: { $sum: '$calories' },
          distance: { $sum: '$distance' }
        }
      },
      {
        $sort: { _id: 1 }
      }
    ]).toArray();
    
    console.log('\n📅 DAILY BREAKDOWN:\n');
    dailyStats.forEach(day => {
      console.log(`${day._id}: ${day.steps} steps, ${day.calories} cal, ${day.distance}km`);
    });
    
    await mongoose.disconnect();
  });
