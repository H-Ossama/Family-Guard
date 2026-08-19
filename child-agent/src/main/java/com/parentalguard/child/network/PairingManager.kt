package com.parentalguard.child.network

import android.content.Context
import android.util.Base64
import java.security.SecureRandom

/**
 * Generates and persists the pairing token used to authenticate the Parent
 * Controller's LAN commands. The token is generated on the child, embedded in
 * the pairing QR code, stored by the parent, and echoed back on every request.
 */
object PairingManager {
    private const val PREFS_NAME = "pair_prefs"
    private const val KEY_TOKEN = "pair_token"
    private const val TOKEN_BYTES = 16

    fun getOrCreateToken(context: Context): String {
        getToken(context)?.let { return it }
        val token = generateToken()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, token)
            .apply()
        return token
    }

    fun getToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)
    }

    private fun generateToken(): String {
        val bytes = ByteArray(TOKEN_BYTES)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
    }
}