const mongoose = require('mongoose');

mongoose.connect('mongodb://localhost:27017/fittrack_fitness', { useNewUrlParser: true, useUnifiedTopology: true })
  .then(async () => {
    const db = mongoose.connection.db;
    const data = await db.collection('fitnessdatas').find({ userId: 'ghnrk8yLcGOn3rmaZFlCPGliJUI3' }).toArray();
    
    console.log('\n📊 FITTRACK FITNESS DATA FOUND:\n');
    console.log('Documents Count:', data.length);
    console.log('');
    
    if (data.length > 0) {
      data.forEach((doc, i) => {
        const date = new Date(doc.date).toLocaleDateString();
        console.log(`Day ${i+1}: ${date} - ${doc.steps} steps, ${doc.calories} cal, ${doc.distance}km`);
      });
      
      const totals = {
        steps: data.reduce((s, d) => s + d.steps, 0),
        calories: data.reduce((s, d) => s + d.calories, 0),
        distance: data.reduce((s, d) => s + d.distance, 0)
      };
      
      console.log('\n📈 TOTALS:');
      console.log('Total Steps:', totals.steps);
      console.log('Total Calories:', totals.calories);
      console.log('Total Distance:', totals.distance, 'km\n');
    }
    
    await mongoose.disconnect();
  })
  .catch(err => {
    console.error('Error:', err.message);
    process.exit(1);
  });
