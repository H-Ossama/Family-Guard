package com.parentalguard.parent.ui.theme

import android.content.Context
import android.content.SharedPreferences

/**
 * Theme preference manager for storing and retrieving user theme selection
 */
object ThemePreferences {
    private const val PREFS_NAME = "theme_preferences"
    private const val KEY_THEME_MODE = "theme_mode"
    
    enum class ThemeMode {
        SYSTEM,  // Follow system theme
        LIGHT,   // Always light theme
        DARK     // Always dark theme
    }
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Save the selected theme mode
     */
    fun saveThemeMode(context: Context, mode: ThemeMode) {
        getPreferences(context).edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
    }
    
    /**
     * Get the currently selected theme mode
     * @return ThemeMode (defaults to SYSTEM if not set)
     */
    fun getThemeMode(context: Context): ThemeMode {
        val modeName = getPreferences(context).getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(modeName ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
    
    /**
     * Get display name for theme mode
     */
    fun getThemeModeDisplayName(mode: ThemeMode): String {
        return when (mode) {
            ThemeMode.SYSTEM -> "System Default"
            ThemeMode.LIGHT -> "Light"
            ThemeMode.DARK -> "Dark"
        }
    }
}
