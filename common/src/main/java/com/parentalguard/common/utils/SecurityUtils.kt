package com.parentalguard.common.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object SecurityUtils {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    
    // In a real production app, this key would be exchanged via QR code/User input during pairing.
    // For this prototype, we will use a derived key from a shared secret phrase.
    private const val SHARED_SECRET = "ParentalGuardSecretKey2025" 

    private fun getKey(): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = SHARED_SECRET.toByteArray(StandardCharsets.UTF_8)
        digest.update(bytes, 0, bytes.size)
        val key = digest.digest()
        return SecretKeySpec(key, "AES")
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(16) // Default zero IV for simplicity in this prototype. Better to randomize and prepend.
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, getKey(), ivSpec)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    fun decrypt(encryptedText: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(16)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), ivSpec)
        val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
}
