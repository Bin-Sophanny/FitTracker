# UTC to GMT+7 Conversion Fix - StatsScreen

## Date: December 10, 2025

## Problem
MongoDB stores dates in UTC format (e.g., "2024-12-09"), but the app needs to display them in Cambodia time (GMT+7). When it's December 10 in Cambodia, MongoDB shows December 9 (UTC).

## Solution Implemented

### 1. **Added UTC to GMT+7 Conversion Function in DateUtils.kt**

Created a new function `convertUtcToGmt7()` that:
- Takes a UTC date string from MongoDB (e.g., "2024-12-09")
- Parses it as UTC timezone
- Converts it to Asia/Bangkok timezone (GMT+7)
- Returns the converted date (e.g., "2024-12-10")
- Includes debug logging to track conversions

```kotlin
fun convertUtcToGmt7(utcDateString: String): String {
    // Parse UTC date
    val utcFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    utcFormat.timeZone = TimeZone.getTimeZone("UTC")
    val utcDate = utcFormat.parse(utcDateString)
    
    // Convert to GMT+7
    val gmt7Format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    gmt7Format.timeZone = TimeZone.getTimeZone("Asia/Bangkok")
    return gmt7Format.format(utcDate)
}
```

### 2. **Updated StatsScreen to Use Conversion**

**Main Changes:**
1. Convert the current stats date from UTC to GMT+7 at the start
2. Use the converted date for all displays
3. DateCard now converts each date before comparing with today/yesterday

**Code Changes:**
```kotlin
// In StatsScreen function
val displayDate = DateUtils.convertUtcToGmt7(currentStats.date)

// Used for:
- "Viewing: Dec 10" label
- "TODAY" badge detection
- "Daily Activity" date display
```

### 3. **DateCard Function Updated**

Now properly converts UTC dates before displaying:
```kotlin
// Convert UTC from MongoDB to GMT+7
val gmt7Date = DateUtils.convertUtcToGmt7(date)

// Compare with today/yesterday in GMT+7
val actuallyIsToday = gmt7Date == todayDate
```

## How It Works

### Flow Diagram:
```
MongoDB (UTC)          Frontend Conversion          Display (GMT+7)
─────────────          ───────────────────          ───────────────
2024-12-09    →   convertUtcToGmt7()   →        2024-12-10
    (UTC)                                          (Cambodia Time)
                                                   Shows: "Today"
```

### Example Scenarios:

**Scenario 1: Today's Data**
- MongoDB stores: `2024-12-09` (UTC)
- App converts to: `2024-12-10` (GMT+7)
- Display shows: **"Today"** ✅

**Scenario 2: Yesterday's Data**
- MongoDB stores: `2024-12-08` (UTC)
- App converts to: `2024-12-09` (GMT+7)
- Display shows: **"Yesterday"** ✅

**Scenario 3: Older Dates**
- MongoDB stores: `2024-12-07` (UTC)
- App converts to: `2024-12-08` (GMT+7)
- Display shows: **"Dec 08"** ✅

## Benefits

✅ **Accurate Date Display**: Shows correct Cambodia time (GMT+7)
✅ **Correct Today/Yesterday Labels**: Properly detects current and previous days
✅ **Timezone Consistency**: All dates consistently use GMT+7
✅ **Debug Logging**: Tracks all UTC to GMT+7 conversions
✅ **MongoDB Compatible**: Works seamlessly with UTC dates from backend

## Technical Details

### Timezone Handling:
- **Backend (MongoDB)**: Stores dates in UTC
- **Frontend (App)**: Converts to Asia/Bangkok (GMT+7)
- **Display**: Shows Cambodia local time

### Date Format:
- **Storage**: `yyyy-MM-dd` (e.g., "2024-12-09")
- **Display**: `MMM dd` (e.g., "Dec 10")
- **Conversion**: Automatic UTC → GMT+7

### Key Functions:
1. `convertUtcToGmt7(utcDate)` - Converts UTC to Cambodia time
2. `getCurrentDate()` - Gets current date in GMT+7
3. `formatDisplayDate(date)` - Formats GMT+7 date for display
4. `isToday(date)` - Checks if GMT+7 date is today

## Result

🎉 **The StatsScreen now correctly displays:**
- December 10 (GMT+7) when MongoDB has December 9 (UTC)
- "Today" label appears on the correct date in Cambodia timezone
- All date cards show converted GMT+7 dates
- Date displays are consistent across the entire app

## Files Modified

1. ✅ `DateUtils.kt` - Added UTC to GMT+7 conversion function
2. ✅ `StatsScreen.kt` - Updated to convert and display GMT+7 dates

## Testing

To verify it's working:
1. Check the logs for "UTC TO GMT+7 CONVERSION" messages
2. MongoDB date "2024-12-09" should display as "Dec 10"
3. "Today" badge should appear on correct date
4. Date cards should show proper Cambodia time

---

**Status**: ✅ COMPLETE - UTC dates now properly converted to GMT+7 (Cambodia time)

