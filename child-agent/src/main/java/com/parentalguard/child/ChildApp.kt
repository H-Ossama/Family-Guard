package com.parentalguard.child

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class ChildApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Restore Language
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("language_code", null)
        if (languageCode != null) {
            val locale = java.util.Locale(languageCode)
            java.util.Locale.setDefault(locale)
            val config = android.content.res.Configuration()
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
        
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "System Services", // Disguised name
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Running core system services"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "system_service_channel"
    }
}
