# Backend Connection Troubleshooting Guide

## ⚠️ BACKEND NOT CONNECTING - Common Issues and Solutions

Even though your 4 backend services are running, the Kotlin app might not be connecting due to several possible reasons:

---

## 🔍 DIAGNOSIS CHECKLIST

### 1. **Network/IP Address Issues** (Most Common)

#### Issue: Wrong IP Address or Device Can't Reach Backend
**Current Configuration:** `http://192.168.50.249:3000/`

**Check This:**
- [ ] Is `192.168.50.249` the correct IP of your computer running the backend?
- [ ] Are your phone/emulator and computer on the **same WiFi network**?
- [ ] Is Windows Firewall blocking port 3000?

**How to Verify:**
```cmd
# On your computer, run in PowerShell:
ipconfig

# Look for "IPv4 Address" - should match 192.168.50.249
# Example output:
# IPv4 Address. . . . . . . . . . . : 192.168.50.249
```

**Test Backend Reachability:**
```cmd
# On your phone/computer, open browser and go to:
http://192.168.50.249:3000/

# You should see SOME response (not "This site can't be reached")
```

**If IP Changed:**
- Your computer's IP might have changed!
- Run `ipconfig` and update the IP in RetrofitClient.kt

---

### 2. **Windows Firewall Blocking Port 3000**

**Symptoms:**
- Backend works on computer (localhost)
- Phone/device can't reach it
- Browser shows "Connection refused" or "Timeout"

**Solution - Allow Port 3000 in Firewall:**

```powershell
# Run PowerShell as Administrator, then run:
New-NetFirewallRule -DisplayName "Node.js Backend Port 3000" -Direction Inbound -LocalPort 3000 -Protocol TCP -Action Allow
```

**OR Manually:**
1. Open Windows Defender Firewall
2. Click "Advanced settings"
3. Click "Inbound Rules" → "New Rule"
4. Choose "Port" → Next
5. Choose "TCP" → Specific local ports: `3000` → Next
6. Choose "Allow the connection" → Next
7. Check all profiles → Next
8. Name it "Backend Port 3000" → Finish

---

### 3. **Backend Running on Wrong Interface**

**Issue:** Backend only listening on `localhost` (127.0.0.1) instead of all interfaces (0.0.0.0)

**Check Your Backend Code:**
Your API Gateway should be listening on `0.0.0.0`, not `localhost`:

```javascript
// ❌ WRONG - Only accessible from same computer
app.listen(3000, 'localhost', () => {
  console.log('Server running on http://localhost:3000');
});

// ✅ CORRECT - Accessible from network
app.listen(3000, '0.0.0.0', () => {
  console.log('Server running on http://0.0.0.0:3000');
});

// OR simply (defaults to 0.0.0.0)
app.listen(3000, () => {
  console.log('Server running on port 3000');
});
```

---

### 4. **Backend Not Handling CORS Properly**

**Issue:** Backend rejecting requests from Android app

**Check Backend Logs for:**
```
Access-Control-Allow-Origin error
CORS policy blocked
```

**Fix in Your API Gateway:**
```javascript
const cors = require('cors');

// Allow all origins (for development)
app.use(cors());

// OR allow specific origin
app.use(cors({
  origin: '*',
  credentials: true
}));
```

---

### 5. **Emulator vs Real Device Configuration**

**Current IP:** `http://192.168.50.249:3000/`

**Issue:** Wrong URL for emulator vs real device

| Device Type | Correct URL |
|-------------|-------------|
| **Android Emulator** | `http://10.0.2.2:3000/` |
| **Real Android Device** | `http://192.168.50.249:3000/` |
| **iOS Simulator** | `http://localhost:3000/` |

**Are you using an emulator?** If yes, change to `http://10.0.2.2:3000/`

---

### 6. **Backend Authentication Token Issues**

**Issue:** Firebase token not being accepted by backend

**Check Backend Logs for:**
```
401 Unauthorized
Token verification failed
Invalid token
```

**Backend Should:**
- Either skip Firebase verification (for testing)
- Or properly verify Firebase tokens

**Quick Test - Disable Auth Temporarily:**
```javascript
// In your backend middleware, comment out auth check:
// app.use('/api/*', verifyFirebaseToken);  // Disable temporarily
```

---

### 7. **Backend Routes Not Matching**

**Issue:** API endpoints don't match what Kotlin app is calling

**Kotlin App Calls:**
- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/fitness/stats/{userId}/{range}`
- `GET /api/fitness/today/{userId}`
- `POST /api/fitness/log`
- `GET /api/blockchain/rewards/{userAddress}`

**Verify Backend Has These Routes:**
Check your API Gateway routes match exactly (including `/api/` prefix)

---

## 🔧 DEBUGGING STEPS

### Step 1: Check Android Logcat
Run your app and check Logcat for errors:

**Filter by:** `OkHttp` or `Retrofit`

**Look for:**
```
❌ java.net.ConnectException: Failed to connect
❌ java.net.SocketTimeoutException: timeout
❌ java.net.UnknownHostException
❌ HTTP 404 Not Found
❌ HTTP 500 Internal Server Error
```

### Step 2: Test Backend with Browser
On your phone, open browser and try:
```
http://192.168.50.249:3000/
```

**Results:**
- ✅ See some response → Backend is reachable
- ❌ "Can't reach this page" → Network/Firewall issue
- ❌ Takes forever then times out → Wrong IP or backend down

### Step 3: Test with cURL or Postman
```bash
# From your computer:
curl http://192.168.50.249:3000/api/auth/login

# Should return something (even an error is OK)
```

### Step 4: Check Backend Console Logs
When you run the Kotlin app, watch your backend console. You should see:
```
POST /api/auth/login
GET /api/fitness/stats/...
```

**If you see nothing:** App isn't reaching backend at all

---

## 🚀 QUICK FIX CHECKLIST

Try these in order:

1. **Verify IP Address:**
   ```cmd
   ipconfig
   ```
   Update if changed!

2. **Test Backend in Browser on Phone:**
   ```
   http://192.168.50.249:3000/
   ```

3. **Disable Windows Firewall (Temporarily):**
   - Test if it works with firewall off
   - If yes, add firewall rule for port 3000

4. **Check Backend is on 0.0.0.0:**
   ```javascript
   app.listen(3000, '0.0.0.0')
   ```

5. **Enable CORS in Backend:**
   ```javascript
   app.use(cors());
   ```

6. **If Using Emulator, Use Emulator IP:**
   ```
   http://10.0.2.2:3000/
   ```

---

## 📱 LOGCAT ERRORS AND SOLUTIONS

### Error: "Failed to connect to /192.168.50.249:3000"
**Cause:** Backend not reachable from phone
**Solution:** Check firewall, verify IP, ensure same WiFi

### Error: "timeout"
**Cause:** Backend taking too long to respond
**Solution:** Already fixed (60s timeout), check backend performance

### Error: "HTTP 404 Not Found"
**Cause:** Route doesn't exist in backend
**Solution:** Check backend routes match Kotlin API calls

### Error: "HTTP 500 Internal Server Error"
**Cause:** Backend code error
**Solution:** Check backend console for error stack trace

### Error: "UnknownHostException"
**Cause:** DNS can't resolve IP (typo in IP address)
**Solution:** Double-check IP address spelling

### Error: "Connection refused"
**Cause:** Nothing listening on port 3000
**Solution:** Make sure all 4 backend services are running

---

## 🔍 WHAT TO CHECK IN LOGCAT NOW

Run your Kotlin app and look in Logcat for lines containing:

```
OkHttp
Retrofit
ConnectException
SocketTimeoutException
HTTP
```

**Send me the error lines you see and I can tell you exactly what's wrong!**

---

## 💡 MOST LIKELY CAUSES (In Order of Probability)

1. **Windows Firewall blocking port 3000** (80% of cases)
2. **Wrong IP address or IP changed** (15% of cases)
3. **Using emulator but not using 10.0.2.2** (3% of cases)
4. **Backend listening on localhost only** (2% of cases)

---

## ✅ VERIFY CONNECTION WORKING

Once fixed, you should see in Logcat:
```
D/OkHttp: --> POST http://192.168.50.249:3000/api/auth/login
D/OkHttp: <-- 200 OK (123ms)
```

And in your backend console:
```
POST /api/auth/login 200 123ms
```

