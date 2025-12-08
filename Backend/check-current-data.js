const mongoose = require('mongoose');

mongoose.connect('mongodb://localhost:27017/fittrack_fitness')
  .then(async () => {
    const db = mongoose.connection.db;
    const data = await db.collection('fitnessdatas')
      .find({ userId: 'ghnrk8yLcGOn3rmaZFlCPGliJUI3' })
      .sort({ date: -1 })
      .toArray();
    
    console.log('\n📊 CURRENT FITNESS DATA:\n');
    data.forEach((doc, i) => {
      const date = new Date(doc.date).toLocaleDateString();
      console.log(`${i+1}. ${date} - ${doc.steps} steps, ${doc.calories} cal, ${doc.distance}km`);
    });
    
    console.log('\n⏰ DATE RANGE:');
    if (data.length > 0) {
      const oldest = new Date(data[data.length - 1].date).toLocaleDateString();
      const newest = new Date(data[0].date).toLocaleDateString();
      console.log(`From: ${oldest} to ${newest}`);
      console.log(`Total entries: ${data.length}\n`);
    }
    
    await mongoose.disconnect();
  });
