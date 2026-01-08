package com.parentalguard.child.utils

import android.content.Context
import android.os.BatteryManager

object DeviceUtils {
    fun getBatteryLevel(context: Context): Int {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            -1 // Unknown
        }
    }

    fun getInstalledApps(context: Context, includeIcons: Boolean = false): List<com.parentalguard.common.model.AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledPackages(0)
        return apps.map { packageInfo ->
            val label = packageInfo.applicationInfo?.loadLabel(pm)?.toString() ?: packageInfo.packageName
            val isSystem = (packageInfo.applicationInfo?.flags?.and(android.content.pm.ApplicationInfo.FLAG_SYSTEM)) != 0
            val category = com.parentalguard.common.utils.CategoryMapper.getCategoryForPackage(packageInfo.packageName)
            
            var iconBase64: String? = null
            if (includeIcons) {
                try {
                    val icon = packageInfo.applicationInfo?.loadIcon(pm)
                    if (icon != null) {
                        iconBase64 = encodeDrawableToBase64(icon)
                    }
                } catch (e: Exception) {
                    // Ignore icon error
                }
            }

            com.parentalguard.common.model.AppInfo(
                packageName = packageInfo.packageName,
                label = label,
                category = category,
                isSystem = isSystem,
                iconBase64 = iconBase64
            )
        }
    }

    private fun encodeDrawableToBase64(drawable: android.graphics.drawable.Drawable): String? {
        try {
            val bitmap = if (drawable is android.graphics.drawable.BitmapDrawable) {
                drawable.bitmap
            } else {
                val bitmap = android.graphics.Bitmap.createBitmap(
                    drawable.intrinsicWidth.coerceAtLeast(1),
                    drawable.intrinsicHeight.coerceAtLeast(1),
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
            
            val outputStream = java.io.ByteArrayOutputStream()
            // Resize if too big to save bandwidth
            val scaledBitmap = if (bitmap.width > 96 || bitmap.height > 96) {
                 android.graphics.Bitmap.createScaledBitmap(bitmap, 96, 96, true)
            } else {
                 bitmap
            }
            
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
            return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            return null
        }
    }
}
