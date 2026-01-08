package com.parentalguard.child.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PackageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: return
        
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                Log.i("PackageReceiver", "New app installed: $packageName")
                val i = Intent("com.parentalguard.child.INTERNAL_EVENT")
                i.setPackage(context.packageName)
                i.putExtra("type", "APP_INSTALLED")
                i.putExtra("payload", packageName)
                context.sendBroadcast(i)
            }
            Intent.ACTION_PACKAGE_REMOVED -> {
                Log.i("PackageReceiver", "App removed: $packageName")
            }
        }
    }
}
