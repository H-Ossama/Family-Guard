package com.parentalguard.child.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.parentalguard.child.R

object WarningNotificationManager {
    
    private const val CHANNEL_ID = "usage_warning_channel"
    private const val CHANNEL_NAME = "Usage Warnings"
    private const val WARNING_NOTIFICATION_ID = 1001
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for app usage warnings"
                setSound(null, null) // Optional: customize sound
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Show a warning notification when approaching time limit
     * @param appName The name of the app or category
     * @param minutesRemaining Minutes remaining before block
     */
    fun showWarning(context: Context, appName: String, minutesRemaining: Int) {
        createNotificationChannel(context)
        
        val message = context.getString(R.string.warning_message, minutesRemaining, appName)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(context.getString(R.string.warning_title))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(WARNING_NOTIFICATION_ID, notification)
    }
    
    /**
     * Cancel warning notification
     */
    fun cancelWarning(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(WARNING_NOTIFICATION_ID)
    }
}
