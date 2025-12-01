# Kotlin Connection Status Implementation

## Add This to Your HomeScreen.kt or Create New ConnectionStatusScreen.kt

### Option 1: Add to HomeScreen

```kotlin
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    navController: NavController
) {
    var connectionStatus by remember { mutableStateOf("Checking...") }
    var isConnected by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getHealth()
            isConnected = true
            connectionStatus = "✅ Connected to Backend"
        } catch (e: Exception) {
            isConnected = false
            connectionStatus = "❌ Not Connected\n${e.message}"
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Connection Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isConnected) Color(0xFF4CAF50) else Color(0xFFf44336)
            )
        ) {
            Text(
                text = connectionStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Show API Base URL
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.LightGray
            )
        ) {
            Text(
                text = "Backend URL:\nhttp://192.168.50.249:3000",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }
        
        // Rest of your HomeScreen content below...
    }
}
```

### Option 2: Create New ConnectionStatusScreen.kt

```kotlin
package com.example.fittrack.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrack.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun ConnectionStatusScreen() {
    var connectionStatus by remember { mutableStateOf("Checking connection...") }
    var isConnected by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        testConnection()
    }
    
    fun testConnection() {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getHealth()
                isConnected = true
                connectionStatus = "✅ Connected to Backend"
                data = response.body()?.toString() ?: "No data"
            } catch (e: Exception) {
                isConnected = false
                connectionStatus = "❌ Not Connected"
                data = "Error: ${e.message}"
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Connection Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isConnected) Color(0xFF4CAF50) else Color(0xFFf44336)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isConnected) "Connected" else "Not Connected",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = connectionStatus,
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // Backend URL
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Backend Configuration",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "URL: http://192.168.50.249:3000",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Status: ${if (isConnected) "Active" else "Inactive"}",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        // Response Data
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Response Data:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = data,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.DarkGray
                )
            }
        }
        
        // Retry Button
        Button(
            onClick = { testConnection() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Text("Test Connection Again", color = Color.White)
        }
    }
}
```

### Option 3: Add to ApiService.kt (if not already present)

Add this health check endpoint to your `ApiService.kt`:

```kotlin
// In ApiService.kt, add this if you don't have a health endpoint:

interface ApiService {
    
    @GET("/health")
    suspend fun getHealth(): Response<HealthResponse>
    
    // ... rest of your endpoints
}

// Add this data class
data class HealthResponse(
    val status: String,
    val timestamp: String
)
```

---

## Testing Steps:

1. **Copy one of the code snippets above** to your Kotlin project
2. **Make sure your backend is running** (all 4 services)
3. **Run your Android app** on emulator or device
4. **You should see:**
   - ✅ Connected to Backend (if backend is running)
   - ❌ Not Connected (if backend is not responding)

---

## Debug Tips:

If you see "❌ Not Connected":

1. **Check backend status:**
   ```powershell
   netstat -ano | findstr "3000\|3001\|3002\|3003"
   ```

2. **Test backend manually:**
   ```powershell
   curl http://localhost:3000/health
   ```

3. **Check IP address:**
   - Make sure `192.168.50.249:3000` is correct
   - Run `ipconfig` to verify your computer IP

4. **Check Android logs:**
   - Look in Logcat for connection errors
   - Search for "Connection" or "Error"

---

## Your Backend Endpoints Are Ready:

- ✅ `GET /health` - Check connection
- ✅ `POST /api/auth/register` - Create account
- ✅ `POST /api/auth/login` - Login
- ✅ `GET /api/fitness/today/{userId}` - Today's fitness
- ✅ `GET /api/fitness/stats/{userId}/{range}` - Statistics

All running at: `http://192.168.50.249:3000`
