package com.parentalguard.common.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppCategory {
    SOCIAL,      // Social media apps (Facebook, Instagram, WhatsApp, etc.)
    GAMES,       // Gaming apps
    EDUCATION,   // Educational apps
    PRODUCTIVITY,// Productivity apps (Office, Notes, etc.)
    ENTERTAINMENT, // Entertainment (YouTube, Netflix, etc.)
    OTHER        // Uncategorized apps
}

@Serializable
data class TimeRange(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7) // 1 = Monday, 7 = Sunday
)

@Serializable
data class BlockingRule(
    val packageName: String,
    val maxDailyTimeMs: Long,
    val blockEndTime: Long = 0, // Timestamp when block expires. 0 if not blocked.
    val isPermanentlyBlocked: Boolean = false,
    val category: AppCategory = AppCategory.OTHER,
    val isWhitelisted: Boolean = false, // If true, never block this app
    val schedule: List<TimeRange> = emptyList() // Periods when the app is blocked
)

@Serializable
data class CategoryLimit(
    val category: AppCategory,
    val maxDailyTimeMs: Long
)

@Serializable
data class RuleSet(
    val rules: List<BlockingRule>,
    val categoryLimits: List<CategoryLimit> = emptyList(),
    val globalLockUntil: Long = 0, // Device-wide lock timestamp
    val temporaryUnlockUntil: Long = 0 // Temporary unlock timestamp
)
