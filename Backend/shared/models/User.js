const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  firebaseUid: String,
  email: { type: String, unique: true, required: true },
  displayName: String,
  photoUrl: String,
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', userSchema);
