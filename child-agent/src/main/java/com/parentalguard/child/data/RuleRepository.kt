package com.parentalguard.child.data

import com.parentalguard.common.model.BlockingRule
import com.parentalguard.common.model.CategoryLimit
import com.parentalguard.common.model.AppCategory
import com.parentalguard.common.utils.CategoryMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RuleRepository {
    private val _rules = MutableStateFlow<List<BlockingRule>>(emptyList())
    val rules: StateFlow<List<BlockingRule>> = _rules.asStateFlow()
    
    private val _categoryLimits = MutableStateFlow<List<CategoryLimit>>(emptyList())
    val categoryLimits: StateFlow<List<CategoryLimit>> = _categoryLimits.asStateFlow()

    fun updateRules(newRules: List<BlockingRule>) {
        _rules.value = newRules
    }
    
    fun updateCategoryLimits(limits: List<CategoryLimit>) {
        _categoryLimits.value = limits
    }

    private val _globalLock = MutableStateFlow<Boolean>(false)
    val globalLock: StateFlow<Boolean> = _globalLock.asStateFlow()

    private val _globalLockUntil = MutableStateFlow<Long>(0)
    val globalLockUntil: StateFlow<Long> = _globalLockUntil.asStateFlow()

    fun setGlobalLock(locked: Boolean) {
        _globalLock.value = locked
        if (!locked) _globalLockUntil.value = 0
    }

    fun setGlobalLockUntil(timestamp: Long) {
        _globalLockUntil.value = timestamp
        if (timestamp > System.currentTimeMillis()) {
            _globalLock.value = true
        }
    }
    
    // Temporary unlock management
    private val _temporaryUnlockUntil = MutableStateFlow<Long>(0)
    val temporaryUnlockUntil: StateFlow<Long> = _temporaryUnlockUntil.asStateFlow()
    
    fun setTemporaryUnlock(untilTimestamp: Long) {
        _temporaryUnlockUntil.value = untilTimestamp
    }
    
    fun isTemporarilyUnlocked(): Boolean {
        return System.currentTimeMillis() < _temporaryUnlockUntil.value
    }

    fun getRuleForPackage(packageName: String): BlockingRule? {
        return _rules.value.find { it.packageName == packageName }
    }
    
    fun getCategoryLimit(category: AppCategory): CategoryLimit? {
        return _categoryLimits.value.find { it.category == category }
    }
    
    /**
     * Check if an app is whitelisted (system-level or user-defined)
     */
    fun isWhitelisted(packageName: String): Boolean {
        // Check system whitelist
        if (CategoryMapper.isWhitelisted(packageName)) return true
        
        // Check user-defined whitelist from rules
        val rule = getRuleForPackage(packageName)
        return rule?.isWhitelisted == true
    }
    
    // Track which apps/categories have shown warnings for the current day
    // Map of Identifier -> DateString (yyyy-MM-dd)
    private val _warningsShown = MutableStateFlow<Map<String, String>>(emptyMap())
    
    fun markWarningShown(identifier: String) {
        val current = _warningsShown.value.toMutableMap()
        val today = android.text.format.DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString()
        current[identifier] = today
        _warningsShown.value = current
    }
    
    fun hasWarningBeenShown(identifier: String): Boolean {
        val lastShownDate = _warningsShown.value[identifier] ?: return false
        val today = android.text.format.DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString()
        return lastShownDate == today
    }
    
    fun clearWarnings() {
        _warningsShown.value = emptyMap()
    }
    
    // Custom Categories Override
    private val _customCategories = MutableStateFlow<Map<String, AppCategory>>(emptyMap())
    
    fun setCustomCategory(packageName: String, category: AppCategory) {
        val current = _customCategories.value.toMutableMap()
        current[packageName] = category
        _customCategories.value = current
    }

    fun getCategory(packageName: String): AppCategory {
        return _customCategories.value[packageName] 
            ?: CategoryMapper.getCategoryForPackage(packageName)
    }

    // App Timers (One-time allowance)
    // Map of PackageName -> ExpirationTimestamp
    private val _appTimers = MutableStateFlow<Map<String, Long>>(emptyMap())
    val appTimers: StateFlow<Map<String, Long>> = _appTimers.asStateFlow()

    fun setAppTimer(packageName: String, durationMs: Long) {
        val current = _appTimers.value.toMutableMap()
        if (durationMs > 0) {
            current[packageName] = System.currentTimeMillis() + durationMs
        } else {
            current.remove(packageName)
        }
        _appTimers.value = current
    }

    // Category Timers
    private val _categoryTimers = MutableStateFlow<Map<AppCategory, Long>>(emptyMap())
    val categoryTimers: StateFlow<Map<AppCategory, Long>> = _categoryTimers.asStateFlow()

    fun setCategoryTimer(category: AppCategory, durationMs: Long) {
        val current = _categoryTimers.value.toMutableMap()
        if (durationMs > 0) {
            current[category] = System.currentTimeMillis() + durationMs
        } else {
            current.remove(category)
        }
        _categoryTimers.value = current
    }

    fun isCategoryTimerActive(category: AppCategory): Boolean {
        val expiration = _categoryTimers.value[category] ?: return false
        return System.currentTimeMillis() < expiration
    }

    fun isAppTimerActive(packageName: String): Boolean {
        val expiration = _appTimers.value[packageName] ?: return false
        return System.currentTimeMillis() < expiration
    }

    // Track last unlock request time to prevent spam
    private val _lastUnlockRequestTime = MutableStateFlow<Long>(0)
    val lastUnlockRequestTime: StateFlow<Long> = _lastUnlockRequestTime.asStateFlow()

    fun updateLastUnlockRequestTime() {
        _lastUnlockRequestTime.value = System.currentTimeMillis()
    }

    // For testing/bootstrap
    init {
        // Example rule: Block settings (just for test, danger!) or a game
        // _rules.value = listOf(BlockingRule("com.example.game", 0, isPermanentlyBlocked = true))
    }
}

