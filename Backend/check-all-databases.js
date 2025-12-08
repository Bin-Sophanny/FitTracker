const mongoose = require('mongoose');

async function checkAllDatabases() {
  try {
    console.log('\n📊 CHECKING ALL DATABASES:\n');
    console.log('='.repeat(60));

    // Check fittrack_fitness
    console.log('\n1️⃣  fittrack_fitness DATABASE:');
    let conn = await mongoose.connect('mongodb://localhost:27017/fittrack_fitness');
    let collections = await conn.connection.db.listCollections().toArray();
    if (collections.length > 0) {
      collections.forEach(c => console.log(`   - ${c.name}`));
      for (const col of collections) {
        const count = await conn.connection.db.collection(col.name).countDocuments();
        console.log(`      (${count} documents)`);
      }
    } else {
      console.log('   (empty - no collections)');
    }
    await conn.disconnect();

    // Check fittrack_users
    console.log('\n2️⃣  fittrack_users DATABASE:');
    conn = await mongoose.connect('mongodb://localhost:27017/fittrack_users');
    collections = await conn.connection.db.listCollections().toArray();
    if (collections.length > 0) {
      collections.forEach(c => console.log(`   - ${c.name}`));
      for (const col of collections) {
        const count = await conn.connection.db.collection(col.name).countDocuments();
        console.log(`      (${count} documents)`);
      }
    } else {
      console.log('   (empty - no collections)');
    }
    await conn.disconnect();

    console.log('\n' + '='.repeat(60));
  } catch (err) {
    console.error('Error:', err.message);
  }
}

checkAllDatabases();
