package com.example.fittrack.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Network diagnostics utility to test backend connectivity
 */
object NetworkDiagnostics {
    private const val TAG = "NetworkDiagnostics"

    /**
     * Test if backend is reachable
     * Call this in a coroutine scope
     */
    suspend fun testBackendConnection(baseUrl: String? = null): ConnectionResult {
        val url = baseUrl ?: if (isEmulator()) {
            "http://10.0.2.2:3000/"
        } else {
            "http://192.168.50.249:3000/"
        }

        // Test multiple endpoints to find which ones work
        val testEndpoints = listOf(
            Pair("Root", url),
            Pair("Health", "${url}health"),
            Pair("API Health", "${url}api/health"),
            Pair("Auth Health", "${url}api/auth/health"),
            Pair("Fitness Health", "${url}api/fitness/health"),
            Pair("Blockchain Health", "${url}api/blockchain/health"),
            Pair("Fitness Stats (actual data)", "${url}api/fitness/stats/test-user/week"),
            Pair("Auth Profile (actual data)", "${url}api/auth/profile/test-user")
        )

        val results = mutableListOf<String>()
        results.add("🔍 Backend Connection Test\n")

        for ((name, endpoint) in testEndpoints) {
            val result = testConnection(endpoint)
            when (result) {
                is ConnectionResult.Success -> {
                    results.add("✅ $name: OK")
                }
                is ConnectionResult.Error -> {
                    val errorMsg = result.message.split("\n").firstOrNull()?.take(50) ?: "Error"
                    results.add("❌ $name: $errorMsg")
                }
            }
        }

        results.add("\n💡 If health checks pass but data endpoints fail:")
        results.add("- Backend services are running")
        results.add("- But routes need to be implemented")
        results.add("- Check backend logs for details")

        return ConnectionResult.Success(results.joinToString("\n"))
    }

    private fun isEmulator(): Boolean {
        return (android.os.Build.FINGERPRINT.startsWith("google/sdk_gphone")
                || android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.contains("emulator")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic")
                || "google_sdk" == android.os.Build.PRODUCT)
    }

    private suspend fun testConnection(baseUrl: String): ConnectionResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Testing connection to: $baseUrl")

                val url = URL(baseUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000 // 5 seconds
                connection.readTimeout = 5000
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                val responseMessage = connection.responseMessage

                connection.disconnect()

                Log.d(TAG, "Response Code: $responseCode")
                Log.d(TAG, "Response Message: $responseMessage")

                when {
                    responseCode in 200..299 -> {
                        ConnectionResult.Success("Backend is reachable! Response: $responseCode")
                    }
                    responseCode in 400..499 -> {
                        ConnectionResult.Success("Backend is reachable but returned error: $responseCode $responseMessage")
                    }
                    responseCode in 500..599 -> {
                        ConnectionResult.Success("Backend is reachable but has server error: $responseCode $responseMessage")
                    }
                    else -> {
                        ConnectionResult.Error("Unexpected response: $responseCode $responseMessage")
                    }
                }
            } catch (e: java.net.ConnectException) {
                Log.e(TAG, "Connection failed: ${e.message}")
                ConnectionResult.Error(
                    "❌ CONNECTION REFUSED\n\n" +
                    "Possible causes:\n" +
                    "1. Backend is not running\n" +
                    "2. Windows Firewall is blocking port 3000\n" +
                    "3. Wrong IP address\n" +
                    "4. Phone and computer not on same WiFi\n\n" +
                    "Error: ${e.message}"
                )
            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "Connection timeout: ${e.message}")
                ConnectionResult.Error(
                    "❌ CONNECTION TIMEOUT\n\n" +
                    "Possible causes:\n" +
                    "1. IP address is wrong\n" +
                    "2. Firewall is blocking the connection\n" +
                    "3. Backend is too slow to respond\n\n" +
                    "Error: ${e.message}"
                )
            } catch (e: java.net.UnknownHostException) {
                Log.e(TAG, "Unknown host: ${e.message}")
                ConnectionResult.Error(
                    "❌ UNKNOWN HOST\n\n" +
                    "The IP address is incorrect or unreachable.\n" +
                    "Current: $baseUrl\n\n" +
                    "Check:\n" +
                    "1. Run 'ipconfig' on your computer\n" +
                    "2. Verify the IPv4 Address\n" +
                    "3. Update RetrofitClient.kt if needed\n\n" +
                    "Error: ${e.message}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error: ${e.message}")
                ConnectionResult.Error(
                    "❌ UNEXPECTED ERROR\n\n" +
                    "${e.javaClass.simpleName}: ${e.message}\n\n" +
                    "Check Logcat for full stack trace"
                )
            }
        }
    }

    /**
     * Test specific API endpoint
     */
    suspend fun testApiEndpoint(endpoint: String): ConnectionResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Testing endpoint: $endpoint")

                val url = URL(endpoint)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                val responseMessage = connection.responseMessage

                // Try to read response body
                val response = try {
                    connection.inputStream.bufferedReader().readText()
                } catch (e: Exception) {
                    "Unable to read response body"
                }

                connection.disconnect()

                Log.d(TAG, "Endpoint Response Code: $responseCode")
                Log.d(TAG, "Endpoint Response: $response")

                ConnectionResult.Success(
                    "Endpoint reachable!\n" +
                    "Code: $responseCode\n" +
                    "Response: ${response.take(200)}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Endpoint test failed: ${e.message}")
                ConnectionResult.Error("Endpoint test failed: ${e.message}")
            }
        }
    }
}

sealed class ConnectionResult {
    data class Success(val message: String) : ConnectionResult()
    data class Error(val message: String) : ConnectionResult()
}

