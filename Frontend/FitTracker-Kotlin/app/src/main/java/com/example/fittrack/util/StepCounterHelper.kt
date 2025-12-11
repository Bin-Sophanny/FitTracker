package com.example.fittrack.util

import android.content.Context
import com.example.fittrack.data.model.DailyStats
import com.google.firebase.auth.FirebaseAuth

/**
 * Helper class to read current step count from SharedPreferences
 * Used by UI to display real-time step count
 * NOW USES USER-SPECIFIC STORAGE - matches StepCounterService
 */
object StepCounterHelper {

    private const val KEY_STEPS_TODAY = "steps_today"
    private const val KEY_LAST_SYNC_DATE = "last_sync_date"

    /**
     * Get user-specific SharedPreferences name based on Firebase user ID
     * MUST match the naming in StepCounterService!
     */
    private fun getPrefsName(): String {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        return "StepCounterPrefs_$userId"
    }

    fun getCurrentSteps(context: Context): Int {
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        val lastDate = prefs.getString(KEY_LAST_SYNC_DATE, DateUtils.getCurrentDate()) ?: DateUtils.getCurrentDate()

        // If date has changed, return 0
        if (lastDate != DateUtils.getCurrentDate()) {
            return 0
        }

        return prefs.getInt(KEY_STEPS_TODAY, 0)
    }

    fun getCurrentDailyStats(context: Context, userId: String): DailyStats {
        val steps = getCurrentSteps(context)
        return DailyStats(
            userId = userId,
            date = DateUtils.getCurrentDate(),
            steps = steps,
            calories = estimateCalories(steps),
            distance = estimateDistance(steps),
            activeMinutes = estimateActiveMinutes(steps)
        )
    }

    private fun estimateCalories(steps: Int): Int {
        return (steps * 0.04).toInt()
    }

    private fun estimateDistance(steps: Int): Float {
        return (steps * 0.762 / 1000).toFloat()
    }

    private fun estimateActiveMinutes(steps: Int): Int {
        return steps / 100
    }


    /**
     * Register a listener to be notified when steps change
     */
    fun registerStepListener(context: Context, listener: (Int) -> Unit) {
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_STEPS_TODAY) {
                listener(getCurrentSteps(context))
            }
        }
    }
}

