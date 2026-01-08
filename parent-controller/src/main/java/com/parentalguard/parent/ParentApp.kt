package com.parentalguard.parent

import android.app.Application

class ParentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        com.parentalguard.parent.ui.components.NotificationHelper.createNotificationChannel(this)
    }
}
