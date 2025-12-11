const mongoose = require('mongoose');

const fitnessDataSchema = new mongoose.Schema({
  userId: { type: String, required: true, index: true },
  date: { type: String, required: true, index: true },  // Store as ISO string to preserve GMT+7 timezone
  steps: { type: Number, default: 0 },
  calories: { type: Number, default: 0 },
  distance: { 
    type: Number, 
    default: 0,
    get: (value) => parseFloat(value).toFixed(2)  // Always display with 2 decimal places
  },
  activeMinutes: { type: Number, default: 0 },
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
});

// Enable getters for JSON serialization
fitnessDataSchema.set('toJSON', { getters: true });

module.exports = mongoose.model('FitnessData', fitnessDataSchema);
