package com.parentalguard.parent.ui.components

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.parentalguard.parent.MainActivity
import com.parentalguard.parent.R

object NotificationHelper {
    private const val CHANNEL_ID = "parental_events"
    private const val CHANNEL_NAME = "Parental Guard Events"
    private const val CHANNEL_DESC = "Notifications for child requests and status changes"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showUnlockRequestNotification(
        context: Context,
        deviceId: String,
        deviceName: String,
        requestType: String = "DEVICE",
        appPackageName: String? = null,
        appName: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_UNLOCK_REQUEST
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("deviceId", deviceId)
            putExtra("deviceName", deviceName)
            putExtra("requestType", requestType)
            putExtra("appPackageName", appPackageName)
            putExtra("appName", appName)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            deviceId.hashCode(), // Use unique request code per device
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (requestType) {
            "APP" -> "App Unlock Request"
            else -> "Device Unlock Request"
        }
        
        val text = when (requestType) {
            "APP" -> "$deviceName wants to unlock ${appName ?: "an app"}"
            else -> "$deviceName wants to unlock the device"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_parent_logo)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(pendingIntent, true) // For high priority
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(deviceId.hashCode(), builder.build())
    }
}
