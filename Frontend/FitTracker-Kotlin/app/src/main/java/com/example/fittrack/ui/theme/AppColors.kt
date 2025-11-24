package com.example.fittrack.ui.theme

import androidx.compose.ui.graphics.Color

// Light Mode Colors
object LightColors {
    val background = Color(0xFFB8A9FF)
    val surface = Color.White
    val primary = Color(0xFF667eea)
    val textPrimary = Color(0xFF2D3748)
    val textSecondary = Color(0xFF718096)
    val error = Color(0xFFE53E3E)
    val success = Color(0xFF48BB78)
    val cardBackground = Color.White
    val divider = Color(0xFFE2E8F0)
}

// Dark Mode Colors
object DarkColors {
    val background = Color(0xFF1A202C)
    val surface = Color(0xFF2D3748)
    val primary = Color(0xFF667eea)
    val textPrimary = Color(0xFFF7FAFC)
    val textSecondary = Color(0xFFA0AEC0)
    val error = Color(0xFFFB8B8B)
    val success = Color(0xFF68D391)
    val cardBackground = Color(0xFF2D3748)
    val divider = Color(0xFF4A5568)
}

data class AppColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val error: Color,
    val success: Color,
    val cardBackground: Color,
    val divider: Color
)

fun getAppColors(isDarkMode: Boolean): AppColors {
    return if (isDarkMode) {
        AppColors(
            background = DarkColors.background,
            surface = DarkColors.surface,
            primary = DarkColors.primary,
            textPrimary = DarkColors.textPrimary,
            textSecondary = DarkColors.textSecondary,
            error = DarkColors.error,
            success = DarkColors.success,
            cardBackground = DarkColors.cardBackground,
            divider = DarkColors.divider
        )
    } else {
        AppColors(
            background = LightColors.background,
            surface = LightColors.surface,
            primary = LightColors.primary,
            textPrimary = LightColors.textPrimary,
            textSecondary = LightColors.textSecondary,
            error = LightColors.error,
            success = LightColors.success,
            cardBackground = LightColors.cardBackground,
            divider = LightColors.divider
        )
    }
}

