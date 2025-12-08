package com.example.fittrack.auth

import android.content.Context
import android.content.SharedPreferences

/**
 * Token manager to store and retrieve JWT token from backend
 */
object TokenManager {
    private const val PREFS_NAME = "fittrack_auth"
    private const val KEY_JWT_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String, userId: String) {
        getPrefs(context).edit().apply {
            putString(KEY_JWT_TOKEN, token)
            putString(KEY_USER_ID, userId)
            apply()
        }
    }

    fun getToken(context: Context): String? {
        return getPrefs(context).getString(KEY_JWT_TOKEN, null)
    }

    fun getUserId(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_ID, null)
    }

    fun clearToken(context: Context) {
        getPrefs(context).edit().apply {
            remove(KEY_JWT_TOKEN)
            remove(KEY_USER_ID)
            apply()
        }
    }

    fun hasToken(context: Context): Boolean {
        return getToken(context) != null
    }
}

