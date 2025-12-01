package com.example.fittrack.util

import android.content.Context
import android.content.SharedPreferences
import com.example.fittrack.data.model.DailyStats
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class to read current step count from SharedPreferences
 * Used by UI to display real-time step count
 */
object StepCounterHelper {

    private const val PREFS_NAME = "StepCounterPrefs"
    private const val KEY_STEPS_TODAY = "steps_today"
    private const val KEY_LAST_SYNC_DATE = "last_sync_date"

    fun getCurrentSteps(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastDate = prefs.getString(KEY_LAST_SYNC_DATE, getCurrentDate()) ?: getCurrentDate()

        // If date has changed, return 0
        if (lastDate != getCurrentDate()) {
            return 0
        }

        return prefs.getInt(KEY_STEPS_TODAY, 0)
    }

    fun getCurrentDailyStats(context: Context): DailyStats {
        val steps = getCurrentSteps(context)
        return DailyStats(
            date = getCurrentDate(),
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

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /**
     * Register a listener to be notified when steps change
     */
    fun registerStepListener(context: Context, listener: (Int) -> Unit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_STEPS_TODAY) {
                listener(getCurrentSteps(context))
            }
        }
    }
}

