package com.example.fittrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.fittrack.navigation.FitTrackNavigation
import com.example.fittrack.ui.theme.FitTrackTheme
import com.example.fittrack.util.PermissionHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Request step counting permission (Android 10+)
        if (!PermissionHelper.hasActivityRecognitionPermission(this)) {
            PermissionHelper.requestActivityRecognitionPermission(this) { granted ->
                if (granted) {
                    android.util.Log.d("MainActivity", "Activity recognition permission granted")
                } else {
                    android.util.Log.w("MainActivity", "Activity recognition permission denied - step counting may not work")
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            FitTrackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FitTrackNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
