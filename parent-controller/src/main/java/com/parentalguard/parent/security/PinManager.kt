package com.parentalguard.parent.security

import android.content.Context
import android.content.SharedPreferences

object PinManager {
    private const val PREFS_NAME = "parent_security_prefs"
    private const val KEY_PIN = "parent_pin"
    private const val KEY_PIN_ENABLED = "pin_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setPin(context: Context, pin: String) {
        getPrefs(context).edit()
            .putString(KEY_PIN, pin)
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
    }

    fun getPin(context: Context): String? {
        return getPrefs(context).getString(KEY_PIN, null)
    }

    fun isPinSet(context: Context): Boolean {
        return getPin(context) != null && getPrefs(context).getBoolean(KEY_PIN_ENABLED, false)
    }

    fun verifyPin(context: Context, input: String): Boolean {
        return getPin(context) == input
    }

    fun disablePin(context: Context) {
        getPrefs(context).edit()
            .putBoolean(KEY_PIN_ENABLED, false)
            .apply()
    }
}
