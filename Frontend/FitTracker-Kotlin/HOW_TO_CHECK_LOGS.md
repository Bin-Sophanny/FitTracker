# How to Check Logs in Android Studio

## Method 1: Logcat Window (RECOMMENDED)

### Step-by-Step:
1. **Open Logcat**:
   - Bottom panel in Android Studio → Click **"Logcat"** tab
   - Or: Menu → View → Tool Windows → Logcat
   - Or: Press `Alt + 6`

2. **Select Your Device**:
   - Top left dropdown: Select your emulator (e.g., "Pixel 5 API 34")

3. **Select Your App**:
   - Next dropdown: Select `com.example.fittrack`

4. **Filter Logs**:
   - In the search box at the top, type one of these:
     - `FitTrackRepo` - to see backend API calls
     - `HomeScreen` - to see screen state
     - `tag:FitTrackRepo` - more specific filtering

5. **Log Levels** (dropdown next to search):
   - **Verbose** - Shows everything (recommended for debugging)
   - **Debug** - Shows Debug, Info, Warn, Error
   - **Error** - Shows only errors

---

## Method 2: Run Window

1. Bottom panel → **"Run"** tab
2. This shows app startup logs and crashes

---

## What Logs to Look For

### Backend Connection Logs:

```
D/FitTrackRepo: getDailyStats - URL: /api/fitness/stats/{userId}/week
D/FitTrackRepo: getDailyStats - Response code: 404
E/FitTrackRepo: getDailyStats ERROR 404: {"message":"Route not found"}
W/FitTrackRepo: Backend route not found - returning empty stats
```

### HomeScreen State Logs:

```
D/HomeScreen: API State: Success
D/HomeScreen: Success - Data size: 0
```

OR

```
D/HomeScreen: API State: Error
E/HomeScreen: Error: HTTP 404 Not Found
```

---

## How to Copy Logs

1. **Select the log lines** in Logcat
2. **Right-click** → Copy
3. **Paste** into a text file or message

---

## Troubleshooting: Can't See Logs?

### If Logcat is empty:
1. Make sure your app is **running** on the emulator
2. Check that the **correct device** is selected in the dropdown
3. Check that **Log level** is set to "Verbose" or "Debug"
4. Try clearing Logcat (trash icon) and running the app again

### If you see too many logs:
1. Use the **search filter**: `package:mine` (shows only your app)
2. Or type: `FitTrackRepo` in the search box
3. Or type: `tag:FitTrackRepo|tag:HomeScreen`

---

## Quick Test Command

You can also use ADB command line to see logs:

```bash
# Open PowerShell or Command Prompt
adb logcat -s FitTrackRepo:D HomeScreen:D
```

This will show only the logs we care about.

---

## Expected Output When Backend is Working:

```
D/HomeScreen: API State: Success
D/HomeScreen: Success - Data size: 5
D/FitTrackRepo: getDailyStats - URL: /api/fitness/stats/abc123/week
D/FitTrackRepo: getDailyStats - Response code: 200
```

## Expected Output When Backend Returns 404:

```
D/HomeScreen: API State: Success
D/HomeScreen: Success - Data size: 0
D/FitTrackRepo: getDailyStats - URL: /api/fitness/stats/abc123/week
D/FitTrackRepo: getDailyStats - Response code: 404
E/FitTrackRepo: getDailyStats ERROR 404: Cannot GET /api/fitness/stats/abc123/week
W/FitTrackRepo: Backend route not found - returning empty stats
```

---

## Screenshot Locations in Android Studio:

```
┌─────────────────────────────────────────────┐
│  Android Studio                              │
│  ┌──────────────────────────────────────┐  │
│  │                                       │  │
│  │         Code Editor                   │  │
│  │                                       │  │
│  └──────────────────────────────────────┘  │
│  ┌──────────────────────────────────────┐  │
│  │ Run | Logcat | Terminal | Build      │← Click here
│  ├──────────────────────────────────────┤  │
│  │ [Device ▼] [App ▼] [Verbose ▼]      │  │
│  │ [🔍 Search: FitTrackRepo            ]│← Filter here
│  ├──────────────────────────────────────┤  │
│  │ D/FitTrackRepo: getDailyStats...     │  │
│  │ D/FitTrackRepo: Response code: 404   │← Your logs here
│  │ E/FitTrackRepo: ERROR 404...         │  │
│  └──────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

---

## Next Steps After Checking Logs:

1. **Copy the log output** (especially the ERROR lines)
2. **Share the logs** so we can identify the exact issue
3. Look for:
   - The **URL** being called
   - The **Response code** (404, 500, 200, etc.)
   - The **Error message** from backend

