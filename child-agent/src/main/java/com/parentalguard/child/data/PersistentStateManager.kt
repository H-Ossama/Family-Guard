package com.parentalguard.child.data

import android.content.Context
import android.content.SharedPreferences
import com.parentalguard.common.model.BlockingRule
import com.parentalguard.common.model.CategoryLimit
import com.parentalguard.common.model.AppCategory
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class PersistentStateManager(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("lock_state_prefs", Context.MODE_PRIVATE)
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        prettyPrint = false
    }
    
    companion object {
        private const val KEY_GLOBAL_LOCK = "global_lock"
        private const val KEY_GLOBAL_LOCK_UNTIL = "global_lock_until"
        private const val KEY_RULES = "blocking_rules"
        private const val KEY_CATEGORY_LIMITS = "category_limits"
        private const val KEY_TEMP_UNLOCK_UNTIL = "temp_unlock_until"
        private const val KEY_APP_TIMERS = "app_timers"
        private const val KEY_CATEGORY_TIMERS = "category_timers"
        private const val KEY_LAST_UNLOCK_REQUEST = "last_unlock_request"
    }
    
    fun saveGlobalLock(isLocked: Boolean, lockUntil: Long) {
        prefs.edit()
            .putBoolean(KEY_GLOBAL_LOCK, isLocked)
            .putLong(KEY_GLOBAL_LOCK_UNTIL, lockUntil)
            .apply()
    }
    
    fun loadGlobalLock(): Pair<Boolean, Long> {
        val isLocked = prefs.getBoolean(KEY_GLOBAL_LOCK, false)
        val lockUntil = prefs.getLong(KEY_GLOBAL_LOCK_UNTIL, 0L)
        return Pair(isLocked, lockUntil)
    }
    
    fun saveRules(rules: List<BlockingRule>) {
        try {
            val jsonString = json.encodeToString(rules)
            prefs.edit().putString(KEY_RULES, jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun loadRules(): List<BlockingRule> {
        val jsonString = prefs.getString(KEY_RULES, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<BlockingRule>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveCategoryLimits(limits: List<CategoryLimit>) {
        try {
            val jsonString = json.encodeToString(limits)
            prefs.edit().putString(KEY_CATEGORY_LIMITS, jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun loadCategoryLimits(): List<CategoryLimit> {
        val jsonString = prefs.getString(KEY_CATEGORY_LIMITS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<CategoryLimit>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveTemporaryUnlockUntil(timestamp: Long) {
        prefs.edit().putLong(KEY_TEMP_UNLOCK_UNTIL, timestamp).apply()
    }
    
    fun loadTemporaryUnlockUntil(): Long {
        return prefs.getLong(KEY_TEMP_UNLOCK_UNTIL, 0L)
    }
    
    fun saveAppTimers(timers: Map<String, Long>) {
        try {
            val jsonString = json.encodeToString(timers)
            prefs.edit().putString(KEY_APP_TIMERS, jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun loadAppTimers(): Map<String, Long> {
        val jsonString = prefs.getString(KEY_APP_TIMERS, null) ?: return emptyMap()
        return try {
            json.decodeFromString<Map<String, Long>>(jsonString)
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    fun saveCategoryTimers(timers: Map<AppCategory, Long>) {
        try {
            val jsonString = json.encodeToString(timers)
            prefs.edit().putString(KEY_CATEGORY_TIMERS, jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun loadCategoryTimers(): Map<AppCategory, Long> {
        val jsonString = prefs.getString(KEY_CATEGORY_TIMERS, null) ?: return emptyMap()
        return try {
            json.decodeFromString<Map<AppCategory, Long>>(jsonString)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveLastUnlockRequestTime(time: Long) {
        prefs.edit().putLong(KEY_LAST_UNLOCK_REQUEST, time).apply()
    }

    fun loadLastUnlockRequestTime(): Long {
        return prefs.getLong(KEY_LAST_UNLOCK_REQUEST, 0L)
    }
}
