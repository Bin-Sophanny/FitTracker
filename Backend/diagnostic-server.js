const express = require('express');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = 9999;  // Diagnostic server on different port

app.use(cors());
app.use(express.json());

console.log('\n╔════════════════════════════════════════╗');
console.log('║   BACKEND CONNECTION DIAGNOSTIC SERVER  ║');
console.log('║   Running on port 9999                  ║');
console.log('╚════════════════════════════════════════╝\n');

// Test endpoint that logs everything
app.get('/test-connection', (req, res) => {
  console.log('✅ Connection test received from:', req.ip);
  res.json({
    status: 'OK',
    message: 'Backend is responding!',
    timestamp: new Date(),
    clientIP: req.ip,
    headers: req.headers
  });
});

// Echo endpoint
app.post('/test-post', (req, res) => {
  console.log('✅ POST request received:', req.body);
  res.json({
    status: 'OK',
    receivedData: req.body,
    timestamp: new Date()
  });
});

// Health check
app.get('/health', (req, res) => {
  console.log('✅ Health check from:', req.ip);
  res.json({ status: 'Backend diagnostic server OK' });
});

// Detailed diagnosis
app.get('/diagnose', (req, res) => {
  res.json({
    status: 'OK',
    diagnostics: {
      timestamp: new Date(),
      uptime: process.uptime(),
      node_version: process.version,
      platform: process.platform,
      env: {
        NODE_ENV: process.env.NODE_ENV,
        API_GATEWAY_PORT: process.env.API_GATEWAY_PORT || '3000',
        MONGODB_FITNESS_URI: process.env.MONGODB_FITNESS_URI ? 'CONFIGURED' : 'NOT SET'
      },
      ports_in_use: {
        api_gateway: 3000,
        user_service: 3001,
        fitness_service: 3002,
        blockchain_service: 3003,
        ganache: 8545,
        this_diagnostic: 9999
      }
    }
  });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log('🚀 Diagnostic server started');
  console.log(`📍 Listen on: 0.0.0.0:${PORT}`);
  console.log('\n📱 From your Android phone, test:');
  console.log(`   http://192.168.50.249:9999/test-connection`);
  console.log(`   http://192.168.50.249:9999/health`);
  console.log(`   http://192.168.50.249:9999/diagnose\n`);
  console.log('Waiting for connections...\n');
});
