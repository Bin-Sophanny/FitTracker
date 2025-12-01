const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const proxy = require('express-http-proxy');
require('dotenv').config();

const app = express();
const PORT = process.env.API_GATEWAY_PORT || 3000;

// Middleware
app.use(cors());
app.use(morgan('dev'));
app.use(express.json());

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'API Gateway OK', timestamp: new Date() });
});

// Service proxies
app.use('/api/auth', proxy(`http://localhost:${process.env.USER_SERVICE_PORT || 3001}`, {
  proxyReqPathResolver: (req) => `/auth${req.url.replace('/api/auth', '')}`
}));

app.use('/api/fitness', proxy(`http://localhost:${process.env.FITNESS_SERVICE_PORT || 3002}`, {
  proxyReqPathResolver: (req) => `/fitness${req.url.replace('/api/fitness', '')}`
}));

app.use('/api/blockchain', proxy(`http://localhost:${process.env.BLOCKCHAIN_SERVICE_PORT || 3003}`, {
  proxyReqPathResolver: (req) => `/blockchain${req.url.replace('/api/blockchain', '')}`
}));

// Error handling
app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).json({ error: 'Internal Server Error' });
});

app.listen(PORT, () => {
  console.log(`🚀 API Gateway running on http://localhost:${PORT}`);
});
