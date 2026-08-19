package com.parentalguard.parent.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object PinManager {
    private const val PREFS_NAME = "parent_security_prefs"
    private const val KEY_PIN_HASH = "parent_pin_hash"
    private const val KEY_PIN_SALT = "parent_pin_salt"
    private const val KEY_PIN_ENABLED = "pin_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setPin(context: Context, pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val prefs = getPrefs(context)
        prefs.edit()
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_PIN_HASH, hashPin(pin, salt))
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
    }

    fun isPinSet(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_PIN_ENABLED, false) &&
            !prefs.getString(KEY_PIN_HASH, null).isNullOrEmpty()
    }

    fun verifyPin(context: Context, input: String): Boolean {
        val prefs = getPrefs(context)
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val saltB64 = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val salt = try {
            Base64.decode(saltB64, Base64.NO_WRAP)
        } catch (e: Exception) {
            return false
        }
        val computed = hashPin(input, salt)
        return MessageDigest.isEqual(
            storedHash.toByteArray(Charsets.UTF_8),
            computed.toByteArray(Charsets.UTF_8)
        )
    }

    fun disablePin(context: Context) {
        getPrefs(context).edit()
            .putBoolean(KEY_PIN_ENABLED, false)
            .putString(KEY_PIN_HASH, null)
            .putString(KEY_PIN_SALT, null)
            .apply()
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        md.update(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
    }
}