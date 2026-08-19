package com.parentalguard.child.monitor

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import com.parentalguard.common.model.AppUsageLog
import com.parentalguard.common.model.AppCategory
import com.parentalguard.common.model.CategoryUsage
import com.parentalguard.common.model.DailyUsageReport
import com.parentalguard.common.model.mergedByPackage
import com.parentalguard.common.utils.CategoryMapper
import com.parentalguard.child.utils.DeviceUtils
import java.util.Calendar

class UsageMonitor(private val context: Context) {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    
    companion object {
        const val WARNING_THRESHOLD_MS = 10 * 60 * 1000L // 10 minutes
    }

    fun getTodayUsage(): List<AppUsageLog> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val usageLogs = mutableListOf<AppUsageLog>()
        if (stats != null) {
            for (usageStat in stats) {
                if (usageStat.totalTimeInForeground > 0) {
                    val appInfo = runCatching {
                        context.packageManager.getApplicationInfo(usageStat.packageName, 0)
                    }.getOrNull()
                    val appLabel = appInfo?.loadLabel(context.packageManager)?.toString()
                    val isSystem = appInfo?.let {
                        (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                    } == true
                    val category = if (isSystem) {
                        AppCategory.SYSTEM
                    } else {
                        com.parentalguard.child.data.RuleRepository.getCategory(
                            usageStat.packageName,
                            appLabel
                        )
                    }
                    usageLogs.add(
                        AppUsageLog(
                            packageName = usageStat.packageName,
                            totalTimeInForeground = usageStat.totalTimeInForeground,
                            lastTimeUsed = usageStat.lastTimeUsed,
                            date = android.text.format.DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString(),
                            category = category,
                            appLabel = appLabel
                        )
                    )
                }
            }
        }
        return usageLogs.mergedByPackage()
    }
    
    /**
     * Get usage for a specific app today
     */
    fun getAppUsageToday(packageName: String): Long {
        val todayUsage = getTodayUsage()
        return todayUsage.find { it.packageName == packageName }?.totalTimeInForeground ?: 0L
    }
    
    /**
     * Get total usage for a category today
     */
    fun getCategoryUsageToday(category: AppCategory): Long {
        val todayUsage = getTodayUsage()
        return todayUsage
            .filter { it.category == category }
            .sumOf { it.totalTimeInForeground }
    }
    
    /**
     * Check if warning threshold is reached for an app
     * Returns true if app usage is within WARNING_THRESHOLD_MS of the limit
     */
    fun shouldShowWarning(packageName: String, limitMs: Long): Boolean {
        val currentUsage = getAppUsageToday(packageName)
        val remaining = limitMs - currentUsage
        return remaining in 1..WARNING_THRESHOLD_MS
    }
    
    /**
     * Check if warning threshold is reached for a category
     */
    fun shouldShowCategoryWarning(category: AppCategory, limitMs: Long): Boolean {
        val currentUsage = getCategoryUsageToday(category)
        val remaining = limitMs - currentUsage
        return remaining in 1..WARNING_THRESHOLD_MS
    }
    
    /**
     * Generate daily usage report
     */
    fun generateDailyReport(): DailyUsageReport {
        val usageLogs = getTodayUsage()
        
        // Calculate category usages
        val categoryUsages = AppCategory.values()
            .map { category ->
                val appsInCategory = usageLogs.filter { it.category == category }
                CategoryUsage(
                    category = category,
                    totalTimeMs = appsInCategory.sumOf { it.totalTimeInForeground },
                    appCount = appsInCategory.size
                )
            }
            .filter { it.totalTimeMs > 0 }
        
        // Get top 5 most used apps
        val mostUsedApps = usageLogs
            .sortedByDescending { it.totalTimeInForeground }
            .take(5)
        
        val totalScreenTime = usageLogs.sumOf { it.totalTimeInForeground }
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val hourlyBreakdown = calculateHourlyUsage(startTime, System.currentTimeMillis())
        
        return DailyUsageReport(
            date = android.text.format.DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString(),
            deviceName = DeviceUtils.getDeviceName(context),
            totalScreenTimeMs = totalScreenTime,
            appUsages = usageLogs,
            categoryUsages = categoryUsages,
            mostUsedApps = mostUsedApps,
            hourlyBreakdown = hourlyBreakdown,
            blockedAttempts = 0 // This would be tracked separately
        )
    }

    fun getHourlyBreakdown(): List<com.parentalguard.common.model.HourlyUsage> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calculateHourlyUsage(calendar.timeInMillis, System.currentTimeMillis())
    }

    private fun calculateHourlyUsage(startTime: Long, endTime: Long): List<com.parentalguard.common.model.HourlyUsage> {
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = android.app.usage.UsageEvents.Event()
        
        val hourlyUsage = LongArray(24) { 0L }
        val lastEventTime = mutableMapOf<String, Long>()
        
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val packageName = event.packageName
            val eventType = event.eventType
            
            if (eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastEventTime[packageName] = event.timeStamp
            } else if (eventType == android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND) {
                val start = lastEventTime[packageName]
                if (start != null) {
                    // Credit each minute to the hour it actually occurred in,
                    // so a 22:50-23:10 session lands partly in hour 22 and partly in 23.
                    addSessionToHourlyUsage(hourlyUsage, start, event.timeStamp)
                    lastEventTime.remove(packageName)
                }
            }
        }
        
        return hourlyUsage.mapIndexed { index, timeMs ->
            com.parentalguard.common.model.HourlyUsage(index, timeMs)
        }
    }

    private fun addSessionToHourlyUsage(hourlyUsage: LongArray, startMs: Long, endMs: Long) {
        var cursor = startMs
        val calendar = Calendar.getInstance()
        while (cursor < endMs) {
            calendar.timeInMillis = cursor
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val hourEnd = calendar.timeInMillis + 3600_000L
            val chunkEnd = minOf(hourEnd, endMs)
            if (hour in 0..23) {
                hourlyUsage[hour] += (chunkEnd - cursor)
            }
            cursor = chunkEnd
        }
    }
}

