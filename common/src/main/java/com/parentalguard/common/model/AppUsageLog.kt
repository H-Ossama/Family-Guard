package com.parentalguard.common.model

import kotlinx.serialization.Serializable

@Serializable
data class AppUsageLog(
    val packageName: String,
    val totalTimeInForeground: Long, // milliseconds
    val lastTimeUsed: Long, // timestamp
    val date: String, // YYYY-MM-DD for easy aggregation
    val category: AppCategory = AppCategory.OTHER
)

@Serializable
data class CategoryUsage(
    val category: AppCategory,
    val totalTimeMs: Long,
    val appCount: Int
)

@Serializable
data class HourlyUsage(
    val hour: Int, // 0-23
    val usageTimeMs: Long
)

@Serializable
data class DailyUsageReport(
    val date: String, // YYYY-MM-DD
    val deviceName: String,
    val totalScreenTimeMs: Long,
    val appUsages: List<AppUsageLog>,
    val categoryUsages: List<CategoryUsage>,
    val mostUsedApps: List<AppUsageLog>, // Top 5 apps
    val hourlyBreakdown: List<HourlyUsage> = emptyList(),
    val blockedAttempts: Int = 0
)

@Serializable
data class DeviceStats(
    val batteryLevel: Int,
    val lastSeenTimestamp: Long,
    val usageLogs: List<AppUsageLog>,
    val installedApps: List<AppInfo> = emptyList(),
    val hourlyBreakdown: List<HourlyUsage> = emptyList(),
    val activeRules: List<BlockingRule> = emptyList(),
    val isLocked: Boolean = false,
    val isIconHidden: Boolean = false,
    val appTimers: Map<String, Long> = emptyMap(), // Map of PackageName -> ExpirationTimestamp
    val categoryTimers: Map<AppCategory, Long> = emptyMap() // Map of Category -> ExpirationTimestamp
)

@Serializable
data class AppInfo(
    val packageName: String,
    val label: String,
    val category: AppCategory = AppCategory.OTHER,
    val isSystem: Boolean = false,
    val iconBase64: String? = null // Base64 encoded icon
)
