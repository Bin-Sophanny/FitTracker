package com.example.fittrack.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import com.example.fittrack.data.model.DailyStats
import com.example.fittrack.data.repository.FitTrackRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Background service to count steps using device accelerometer/step counter sensor
 * Automatically syncs with backend every 50 steps or every 5 minutes
 * Now uses USER-SPECIFIC storage - each Firebase user has their own step data
 */
class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val repository = FitTrackRepository()
    private val auth = FirebaseAuth.getInstance()

    // SharedPreferences keys - prefs name will be user-specific
    private val KEY_STEPS_TODAY = "steps_today"
    private val KEY_LAST_SYNC_DATE = "last_sync_date"
    private val KEY_INITIAL_STEPS = "initial_steps"
    private val KEY_LAST_BACKEND_SYNC = "last_backend_sync"

    // Get user-specific SharedPreferences name based on Firebase user ID
    private fun getPrefsName(): String {
        val userId = auth.currentUser?.uid ?: "anonymous"
        return "StepCounterPrefs_$userId"
    }

    private var stepsToday = 0
    private var initialSteps = 0
    private var lastSyncDate = ""
    private var lastBackendSync = 0L

    // Sync configuration
    private val SYNC_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes
    private val SYNC_STEP_THRESHOLD = 50 // Sync every 50 steps

    companion object {
        private const val TAG = "StepCounterService"

        fun start(context: Context) {
            val intent = Intent(context, StepCounterService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, StepCounterService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "StepCounterService created")
        Log.d(TAG, "👤 User: ${auth.currentUser?.email ?: "Anonymous"}")
        Log.d(TAG, "📂 Storage: ${getPrefsName()}")

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Try to use Step Counter sensor first (more accurate)
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Fallback to Step Detector if Step Counter not available
        if (stepCounterSensor == null) {
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            Log.d(TAG, "Using Step Detector sensor")
        } else {
            Log.d(TAG, "Using Step Counter sensor")
        }

        loadStepsFromPrefs()
        registerSensorListeners()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "🚀 StepCounterService started")
        Log.d(TAG, "👤 Current user: ${auth.currentUser?.email ?: "Anonymous"}")
        Log.d(TAG, "📊 Current steps loaded: $stepsToday")
        Log.d(TAG, "📅 Last sync date: $lastSyncDate")
        Log.d(TAG, "⏰ Last backend sync: ${if (lastBackendSync > 0) SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(lastBackendSync)) else "Never"}")

        checkAndResetDailySteps()

        // Force an immediate sync if we have steps and haven't synced recently
        if (stepsToday > 0 && (System.currentTimeMillis() - lastBackendSync) > 60000) {
            Log.d(TAG, "🔄 Forcing immediate sync of $stepsToday steps on service start")
            syncToBackend()
        }

        return START_STICKY
    }

    private fun registerSensorListeners() {
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Step Counter sensor registered")
        }

        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Step Detector sensor registered")
        }

        if (stepCounterSensor == null && stepDetectorSensor == null) {
            Log.e(TAG, "No step sensors available on this device")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    // Step Counter gives cumulative steps since reboot
                    val totalSteps = it.values[0].toInt()

                    if (initialSteps == 0) {
                        // First reading - set as baseline
                        initialSteps = totalSteps
                        saveStepsToPrefs()
                    }

                    val previousSteps = stepsToday
                    stepsToday = totalSteps - initialSteps
                    Log.d(TAG, "👟 Steps today: $stepsToday (Total: $totalSteps, Initial: $initialSteps)")
                    saveStepsToPrefs()

                    // Sync every 50 steps or every 5 minutes
                    val shouldSync = shouldSyncToBackend(previousSteps)
                    if (shouldSync) {
                        Log.d(TAG, "⏰ Sync condition met! Previous: $previousSteps, Current: $stepsToday")
                        syncToBackend()
                    }
                }

                Sensor.TYPE_STEP_DETECTOR -> {
                    // Step Detector fires once per step
                    val previousSteps = stepsToday
                    stepsToday++
                    Log.d(TAG, "Step detected. Total: $stepsToday")
                    saveStepsToPrefs()

                    // Sync every 50 steps or every 5 minutes
                    if (shouldSyncToBackend(previousSteps)) {
                        syncToBackend()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }

    private fun checkAndResetDailySteps() {
        val currentDate = getCurrentDate()

        if (lastSyncDate != currentDate) {
            // New day - reset steps
            Log.d(TAG, "🌅 New day detected. Resetting steps.")
            stepsToday = 0
            initialSteps = 0
            lastSyncDate = currentDate
            saveStepsToPrefs()
        }
    }

    private fun shouldSyncToBackend(previousSteps: Int): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastSync = currentTime - lastBackendSync

        // Sync if:
        // 1. Reached step threshold (every 50 steps)
        // 2. 5 minutes passed since last sync
        // 3. First sync of the day
        return (stepsToday % SYNC_STEP_THRESHOLD == 0 && stepsToday != previousSteps) ||
                timeSinceLastSync >= SYNC_INTERVAL_MS ||
                lastBackendSync == 0L
    }

    private fun loadStepsFromPrefs() {
        val prefsName = getPrefsName()
        Log.d(TAG, "📂 Loading steps from user-specific prefs: $prefsName")
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        stepsToday = prefs.getInt(KEY_STEPS_TODAY, 0)
        lastSyncDate = prefs.getString(KEY_LAST_SYNC_DATE, getCurrentDate()) ?: getCurrentDate()
        initialSteps = prefs.getInt(KEY_INITIAL_STEPS, 0)
        lastBackendSync = prefs.getLong(KEY_LAST_BACKEND_SYNC, 0L)

        Log.d(TAG, "✅ Loaded steps from prefs: $stepsToday (Date: $lastSyncDate)")
        checkAndResetDailySteps()
    }

    private fun saveStepsToPrefs() {
        val prefsName = getPrefsName()
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        prefs.edit().apply {
            putInt(KEY_STEPS_TODAY, stepsToday)
            putString(KEY_LAST_SYNC_DATE, lastSyncDate)
            putInt(KEY_INITIAL_STEPS, initialSteps)
            putLong(KEY_LAST_BACKEND_SYNC, lastBackendSync)
            apply()
        }
    }

    private fun syncToBackend() {
        serviceScope.launch {
            try {
                Log.d(TAG, "🔄 Starting sync to backend...")
                Log.d(TAG, "👤 User: ${auth.currentUser?.email}")
                Log.d(TAG, "📊 Current steps: $stepsToday")

                val stats = DailyStats(
                    date = getCurrentDate(),
                    steps = stepsToday,
                    calories = estimateCalories(stepsToday),
                    distance = estimateDistance(stepsToday),
                    activeMinutes = estimateActiveMinutes(stepsToday)
                )

                Log.d(TAG, "📤 Stats to sync: date=${stats.date}, steps=${stats.steps}, calories=${stats.calories}, distance=${stats.distance}km")

                val response = repository.logDailyStats(stats)

                if (response.isSuccessful) {
                    lastBackendSync = System.currentTimeMillis()
                    saveStepsToPrefs()
                    Log.d(TAG, "✅ Successfully synced $stepsToday steps to backend at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ Failed to sync: HTTP ${response.code()} - ${response.message()}")
                    Log.e(TAG, "❌ Error body: $errorBody")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception during sync: ${e.javaClass.simpleName}")
                Log.e(TAG, "❌ Error message: ${e.message}")
                Log.e(TAG, "❌ Stack trace: ${e.stackTraceToString()}")
            }
        }
    }

    private fun estimateCalories(steps: Int): Int {
        // Rough estimate: 0.04 calories per step
        return (steps * 0.04).toInt()
    }

    private fun estimateDistance(steps: Int): Float {
        // Average step length: 0.762 meters (2.5 feet)
        return (steps * 0.762 / 1000).toFloat() // Convert to kilometers
    }

    private fun estimateActiveMinutes(steps: Int): Int {
        // Rough estimate: 100 steps per minute of walking
        return steps / 100
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        Log.d(TAG, "StepCounterService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

