const mongoose = require('mongoose');

mongoose.connect('mongodb://localhost:27017/fittrack_fitness')
  .then(async () => {
    const db = mongoose.connection.db;
    
    console.log('\n📚 CHECKING BOTH COLLECTIONS:\n');
    console.log('='.repeat(60));

    // Check fitnessdatas
    console.log('\n1️⃣  FITNESSDATAS COLLECTION:');
    const fitnessCount = await db.collection('fitnessdatas').countDocuments();
    console.log(`   Documents: ${fitnessCount}`);
    
    const fitnessDocs = await db.collection('fitnessdatas').find({}).limit(3).toArray();
    if (fitnessDocs.length > 0) {
      console.log('   Sample:');
      fitnessDocs.forEach(doc => {
        console.log(`   - ${new Date(doc.date).toLocaleDateString()}: ${doc.steps} steps`);
      });
    }

    // Check daily_stats
    console.log('\n2️⃣  DAILY_STATS COLLECTION:');
    const dailyCount = await db.collection('daily_stats').countDocuments();
    console.log(`   Documents: ${dailyCount}`);
    
    const dailyDocs = await db.collection('daily_stats').find({}).limit(3).toArray();
    if (dailyDocs.length > 0) {
      console.log('   Sample:');
      dailyDocs.forEach(doc => {
        console.log(`   - ${JSON.stringify(doc).substring(0, 100)}...`);
      });
    }

    console.log('\n' + '='.repeat(60));
    console.log('\n💡 EXPLANATION:\n');
    console.log('fitnessdatas:  Stores raw individual fitness entries');
    console.log('               (one document per day per user)');
    console.log('\ndaily_stats:   Pre-calculated aggregated stats');
    console.log('               (optional cache collection)');
    
    console.log('\n⚠️  ACTION NEEDED:\n');
    if (fitnessCount === 0 && dailyCount > 0) {
      console.log('Your data is in daily_stats, NOT in fitnessdatas!');
      console.log('This means the data was stored in the wrong collection.');
    } else if (fitnessCount > 0 && dailyCount === 0) {
      console.log('✅ Data is correctly stored in fitnessdatas collection.');
      console.log('daily_stats is empty (can be used for caching).');
    } else if (fitnessCount > 0 && dailyCount > 0) {
      console.log('Both collections have data.');
      console.log('Consider cleaning up duplicates.');
    }
    
    await mongoose.disconnect();
  });
