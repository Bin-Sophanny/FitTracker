# MongoDB Atlas Setup Guide

## Step-by-Step Instructions

### 1. Create MongoDB Atlas Account
- Go to: https://www.mongodb.com/cloud/atlas
- Click "Sign Up"
- Create free account with email/password
- Verify email

### 2. Create a Project
- Click "Create Project"
- Name it: `FitTrack`
- Click "Create Project"

### 3. Create a Cluster
- Click "Create" (or "Build a Database")
- Choose Free tier (M0)
- Select region closest to Cambodia: **Singapore** or **Tokyo**
- Click "Create Cluster"
- Wait 5-10 minutes for creation

### 4. Create Database User
- In left menu: "Database Access"
- Click "Add New Database User"
- Username: `fittrack_user` (or your choice)
- Password: Generate strong password (save it!)
- Set privileges: "Read and write to any database"
- Click "Add User"

### 5. Add IP Whitelist
- In left menu: "Network Access"
- Click "Add IP Address"
- Choose "Allow access from anywhere" (for testing)
- Or enter your IP address (for production)
- Click "Confirm"

### 6. Get Connection String
- Go back to "Clusters"
- Click "Connect"
- Choose "Drivers" → "Node.js"
- Copy the connection string
- It will look like:
  ```
  mongodb+srv://fittrack_user:PASSWORD@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
  ```

### 7. Update Your .env File
Replace the MongoDB URIs in your `.env` file:

**OLD (Local):**
```env
MONGODB_USER_URI=mongodb://localhost:27017/fittrack_users
MONGODB_FITNESS_URI=mongodb://localhost:27017/fittrack_fitness
```

**NEW (Atlas):**
```env
MONGODB_USER_URI=mongodb+srv://fittrack_user:YOUR_PASSWORD@cluster0.xxxxx.mongodb.net/fittrack_users?retryWrites=true&w=majority
MONGODB_FITNESS_URI=mongodb+srv://fittrack_user:YOUR_PASSWORD@cluster0.xxxxx.mongodb.net/fittrack_fitness?retryWrites=true&w=majority
```

**Important:** Replace `YOUR_PASSWORD` with your actual database user password!

### 8. Test Connection
Run this to test if your connection works:
```bash
npm start
npm run services:user
npm run services:fitness
npm run services:blockchain
```

If you get connection errors, check:
- ✓ IP address is whitelisted in Network Access
- ✓ Database user username and password are correct
- ✓ Connection string includes username and password
- ✓ MongoDB Atlas cluster is in "Running" state

### 9. Optional: Migrate Existing Data
If you have local MongoDB data you want to move to Atlas:

**Export from local:**
```bash
mongodump --db fittrack_users --out ./backup
mongodump --db fittrack_fitness --out ./backup
```

**Import to Atlas:**
```bash
mongorestore --uri "mongodb+srv://user:password@cluster.mongodb.net/" ./backup
```

## Important Security Notes

- ⚠️ **Never commit .env file to Git** (add to .gitignore)
- ⚠️ **Never share your password** publicly
- ⚠️ For production: Use specific IP addresses instead of "Allow from anywhere"
- ⚠️ Rotate passwords regularly
- ⚠️ Use environment variables for deployment (Vercel, Heroku, etc.)

## Backup Reminder

MongoDB Atlas automatically backs up your data. But you should also:
- Regular manual exports for safety
- Keep snapshots of important data
- Monitor your cluster usage (free tier has limits)

## Need Help?

- MongoDB Atlas Docs: https://docs.atlas.mongodb.com/
- Connection String Format: https://docs.mongodb.com/manual/reference/connection-string/
- Troubleshooting: https://docs.atlas.mongodb.com/troubleshooting/
