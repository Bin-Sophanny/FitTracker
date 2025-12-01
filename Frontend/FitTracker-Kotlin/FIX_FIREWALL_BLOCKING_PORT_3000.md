# FIX: Windows Firewall Blocking Port 3000

## ⚠️ PROBLEM IDENTIFIED
**"HTTP traffic to 192.168.50.249 not permitted"**

Your Windows Firewall is blocking incoming connections on port 3000, preventing your Android app from connecting to your backend.

---

## ✅ SOLUTION - Allow Port 3000 in Windows Firewall

### METHOD 1: PowerShell Command (Fastest)

1. **Right-click Start Menu** → Click **"Windows PowerShell (Admin)"** or **"Terminal (Admin)"**
2. Copy and paste this command:

```powershell
New-NetFirewallRule -DisplayName "FitTracker Backend Port 3000" -Direction Inbound -LocalPort 3000 -Protocol TCP -Action Allow
```

3. Press **Enter**
4. You should see:
```
Name                  : {GUID}
DisplayName           : FitTracker Backend Port 3000
Description           : 
DisplayGroup          : 
Group                 : 
Enabled               : True
...
```

5. ✅ **Done!** Your app should now connect.

---

### METHOD 2: Windows Firewall GUI (If PowerShell doesn't work)

1. **Open Windows Defender Firewall:**
   - Press `Windows + R`
   - Type: `wf.msc`
   - Press Enter

2. **Create New Inbound Rule:**
   - Click **"Inbound Rules"** in left sidebar
   - Click **"New Rule..."** in right sidebar

3. **Configure Rule:**
   - **Rule Type:** Select **"Port"** → Click **Next**
   - **Protocol:** Select **"TCP"**
   - **Specific local ports:** Type `3000` → Click **Next**
   - **Action:** Select **"Allow the connection"** → Click **Next**
   - **Profile:** Check all three (Domain, Private, Public) → Click **Next**
   - **Name:** Type `FitTracker Backend Port 3000` → Click **Finish**

4. ✅ **Done!** The rule is now active.

---

### METHOD 3: Temporarily Disable Firewall (Testing Only)

⚠️ **Only use this to test if firewall is the issue, then re-enable it!**

1. Press `Windows + R`
2. Type: `firewall.cpl`
3. Press Enter
4. Click **"Turn Windows Defender Firewall on or off"**
5. Select **"Turn off Windows Defender Firewall"** for Private network
6. Click **OK**

**Test your app now.** If it works, the firewall was the problem!

**Then IMMEDIATELY:**
- Go back and turn the firewall back on
- Use Method 1 or 2 to allow port 3000 properly

---

## 🔍 VERIFY THE FIX

After creating the firewall rule:

### Step 1: Check Firewall Rule Exists
```powershell
Get-NetFirewallRule -DisplayName "FitTracker Backend Port 3000"
```

You should see the rule listed.

### Step 2: Test Backend Connection

**On your phone, open browser:**
```
http://192.168.50.249:3000/
```

You should now see some response (not "Connection refused")

### Step 3: Run Your Kotlin App
1. Open your FitTracker app
2. The red banner should still appear (because you haven't registered the user yet)
3. Click **"Diagnose"** button
4. You should now see: **"✅ Backend is reachable! Response: 200"** or similar

---

## 🎯 NEXT STEPS AFTER FIREWALL IS FIXED

Once the firewall is allowing traffic, you'll still see empty data because:

**Issue:** Your user exists in Firebase but NOT in your backend database

**Solution:** Sign out and sign in again
1. Open your app
2. Go to Profile tab
3. Click **"Logout"**
4. Sign in again with your credentials
5. This will now register you in the backend (I already fixed the code to do this)
6. You should now see data!

---

## 🐛 IF IT STILL DOESN'T WORK

### Check Backend is Listening on 0.0.0.0

Your backend must listen on **all interfaces**, not just localhost.

**In your API Gateway (app.js or server.js):**

```javascript
// ❌ WRONG - Only accessible locally
app.listen(3000, 'localhost');

// ✅ CORRECT - Accessible from network
app.listen(3000, '0.0.0.0', () => {
  console.log('Server running on http://0.0.0.0:3000');
});

// OR simply (defaults to 0.0.0.0)
app.listen(3000, () => {
  console.log('Server running on port 3000');
});
```

### Check Both Devices on Same WiFi

- Your phone: Check WiFi settings
- Your computer: `ipconfig` → Look at "Wireless LAN adapter WiFi"
- Both should be on the same network (e.g., 192.168.50.x)

### Check Backend Console

When you run the app, you should see requests in your backend console:
```
POST /api/auth/login 200 45ms
GET /api/fitness/stats/... 200 123ms
```

If you see nothing, the app still can't reach the backend.

---

## 📋 QUICK CHECKLIST

- [ ] Run PowerShell as Admin
- [ ] Execute firewall rule command
- [ ] Verify rule was created
- [ ] Test backend in phone browser (http://192.168.50.249:3000/)
- [ ] Run Kotlin app
- [ ] Click "Diagnose" button - should show "Backend is reachable"
- [ ] Sign out and sign in again to register user
- [ ] Check if data appears

---

## 💡 WHY THIS HAPPENED

By default, Windows Firewall blocks ALL incoming connections unless explicitly allowed. When your backend starts listening on port 3000, it can receive requests from your own computer (localhost), but external devices (like your phone) are blocked by the firewall.

This is a security feature to protect your computer from unwanted network access.

---

## ✅ EXPECTED RESULT

After fixing the firewall:
- ✅ Your phone can connect to http://192.168.50.249:3000/
- ✅ Diagnostic shows "Backend is reachable"
- ✅ After sign out/sign in, user is registered in backend
- ✅ Data appears in your app

---

**Run the PowerShell command now and your app should work!** 🚀

