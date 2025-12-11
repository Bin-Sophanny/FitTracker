# FitTrack Backend - Deployment Guide

## Deployment to Render

### Prerequisites
- Render.com account
- MongoDB Atlas account with connection string
- Firebase project with credentials

### Environment Variables on Render

Set the following environment variables in your Render service:

```
# API Gateway
API_GATEWAY_PORT=3000

# User Service
USER_SERVICE_PORT=3001
MONGODB_USER_URI=mongodb+srv://[username]:[password]@[cluster].mongodb.net/fittrack_users?retryWrites=true&w=majority

# Fitness Service
FITNESS_SERVICE_PORT=3002
MONGODB_FITNESS_URI=mongodb+srv://[username]:[password]@[cluster].mongodb.net/fittrack_fitness?retryWrites=true&w=majority

# Firebase Admin SDK
FIREBASE_PROJECT_ID=your_project_id
FIREBASE_PRIVATE_KEY=your_private_key (use \n for newlines)
FIREBASE_CLIENT_EMAIL=your_client_email

# JWT
JWT_SECRET=your_secure_secret_key
JWT_EXPIRATION=7d

# Environment
NODE_ENV=production
```

### Deployment Steps

1. **Create Render Services**
   - Create 3 new "Web Service" on Render
   - Point to your GitHub repository

2. **Service Configuration**

   **API Gateway Service:**
   - Name: fittrack-api-gateway
   - Build Command: `npm install`
   - Start Command: `npm start`
   - Port: 3000

   **User Service:**
   - Name: fittrack-user-service
   - Build Command: `npm install`
   - Start Command: `npm run services:user`
   - Port: 3001

   **Fitness Service:**
   - Name: fittrack-fitness-service
   - Build Command: `npm install`
   - Start Command: `npm run services:fitness`
   - Port: 3002

3. **Update API Gateway URLs**
   - In api-gateway/index.js, update service URLs to use Render URLs
   - Example: `http://fittrack-user-service.onrender.com` instead of localhost

### MongoDB Atlas Setup

1. Create two databases:
   - `fittrack_users`
   - `fittrack_fitness`

2. Create a MongoDB user with credentials
3. Add Render.com IP addresses to MongoDB Atlas whitelist (or use 0.0.0.0/0 for development)

### Firebase Setup

1. Go to Project Settings in Firebase Console
2. Create a service account
3. Generate private key (JSON format)
4. Copy `project_id`, `private_key`, and `client_email` to Render environment variables

### Health Check

After deployment, verify services are running:
- API Gateway: `https://fittrack-api-gateway.onrender.com/health`
- Should return: `{"status":"API Gateway OK","timestamp":"..."}`

### Monitoring

Check service logs in Render Dashboard for any errors. Common issues:
- MongoDB connection timeout: Verify IP whitelist in MongoDB Atlas
- Service discovery: Update internal URLs to use Render service names
- Port conflicts: Ensure ports don't exceed 3002

### Production Best Practices

✅ All debug console.logs removed
✅ Extensive error logging in place
✅ MongoDB connection pooling configured
✅ Environment variables properly managed
✅ .gitignore configured for sensitive files
✅ Blockchain service completely removed
