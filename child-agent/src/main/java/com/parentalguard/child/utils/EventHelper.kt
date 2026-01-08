package com.parentalguard.child.utils

import android.content.Context
import android.content.Intent
import com.parentalguard.common.network.EventType

object EventHelper {
    fun sendInternalEvent(
        context: Context,
        type: EventType,
        payload: String? = null,
        deviceName: String? = null,
        requestType: String? = null,
        appPackageName: String? = null,
        appName: String? = null
    ) {
        val intent = Intent("com.parentalguard.child.INTERNAL_EVENT").apply {
            setPackage(context.packageName)
            putExtra("type", type.name)
            putExtra("payload", payload)
            putExtra("deviceName", deviceName)
            putExtra("requestType", requestType)
            putExtra("appPackageName", appPackageName)
            putExtra("appName", appName)
        }
        context.sendBroadcast(intent)
    }
    
    fun sendUnlockRequest(
        context: Context,
        requestType: String = "DEVICE",
        appPackageName: String? = null,
        appName: String? = null
    ) {
        // Get device name from SharedPreferences
        val prefs = context.getSharedPreferences("child_prefs", Context.MODE_PRIVATE)
        val deviceName = prefs.getString("device_name", null) ?: android.os.Build.MODEL
        
        sendInternalEvent(
            context = context,
            type = EventType.UNLOCK_REQUESTED,
            payload = if (requestType == "APP") "App unlock request: $appName" else "Device unlock request",
            deviceName = deviceName,
            requestType = requestType,
            appPackageName = appPackageName,
            appName = appName
        )
    }
}
