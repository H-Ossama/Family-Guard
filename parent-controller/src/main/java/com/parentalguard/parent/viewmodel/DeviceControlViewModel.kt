package com.parentalguard.parent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentalguard.common.model.*
import com.parentalguard.parent.network.DeviceClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.parentalguard.parent.data.ReportsRepository

class DeviceControlViewModel(application: Application) : AndroidViewModel(application) {
    private val client = DeviceClient()
    private val reportsRepository = ReportsRepository(application)

    private val _usageLogs = MutableStateFlow<List<AppUsageLog>>(emptyList())
    val usageLogs: StateFlow<List<AppUsageLog>> = _usageLogs.asStateFlow()

    private val _activeRules = MutableStateFlow<List<BlockingRule>>(emptyList())
    val activeRules: StateFlow<List<BlockingRule>> = _activeRules.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()
    
    private val _categoryLimits = MutableStateFlow<List<CategoryLimit>>(emptyList())
    val categoryLimits: StateFlow<List<CategoryLimit>> = _categoryLimits.asStateFlow()
    
    private val _dailyReport = MutableStateFlow<DailyUsageReport?>(null)
    val dailyReport: StateFlow<DailyUsageReport?> = _dailyReport.asStateFlow()
    
    private val _isDeviceLocked = MutableStateFlow(false)
    val isDeviceLocked: StateFlow<Boolean> = _isDeviceLocked.asStateFlow()

    private val _isAppIconHidden = MutableStateFlow(false)
    val isAppIconHidden: StateFlow<Boolean> = _isAppIconHidden.asStateFlow()

    private val _appTimers = MutableStateFlow<Map<String, Long>>(emptyMap())
    val appTimers: StateFlow<Map<String, Long>> = _appTimers.asStateFlow()

    private val _appIcons = MutableStateFlow<Map<String, String>>(emptyMap())

    fun fetchStats(device: ChildDevice) {
        viewModelScope.launch {
            // Check if we need to fetch icons (if map is empty)
            val includeIcons = _appIcons.value.isEmpty()
            val response = client.getStats(device.ip.hostAddress ?: "", device.port, includeIcons)
            
            if (response != null && response.success) {
                _usageLogs.value = response.stats?.usageLogs ?: emptyList()
                _activeRules.value = response.stats?.activeRules ?: emptyList()
                _isDeviceLocked.value = response.stats?.isLocked ?: false
                _isAppIconHidden.value = response.stats?.isIconHidden ?: false
                _appTimers.value = response.stats?.appTimers ?: emptyMap()
                
                // Process icons if included
                if (includeIcons) {
                    val newIcons = response.stats?.installedApps?.mapNotNull { app ->
                        if (app.iconBase64 != null) app.packageName to app.iconBase64!! else null
                    }?.toMap() ?: emptyMap()
                    
                    if (newIcons.isNotEmpty()) {
                        _appIcons.value = _appIcons.value + newIcons
                    }
                }
                
                _statusMessage.value = "Stats updated"
            } else {
                _statusMessage.value = "Failed to fetch stats"
            }
        }
    }
    
    // Helper to request icons explicitly (e.g. on pull to refresh if needed)
    fun refreshIcons(device: ChildDevice) {
        viewModelScope.launch {
            val response = client.getStats(device.ip.hostAddress ?: "", device.port, true)
            if (response != null && response.success) {
                val newIcons = response.stats?.installedApps?.mapNotNull { app ->
                    if (app.iconBase64 != null) app.packageName to app.iconBase64!! else null
                }?.toMap() ?: emptyMap()
                
                if (newIcons.isNotEmpty()) {
                    _appIcons.value = _appIcons.value + newIcons
                }
            }
        }
    }

    fun getAppIcon(packageName: String): String? {
        return _appIcons.value[packageName]
    }

    fun fetchDailyReport(device: ChildDevice) {
        viewModelScope.launch {
            val response = client.getDailyReport(device.ip.hostAddress ?: "", device.port)
            if (response != null && response.success && response.dailyReport != null) {
                _dailyReport.value = response.dailyReport
                // Save to historical reports
                reportsRepository.saveReport(response.dailyReport!!)
                _statusMessage.value = "Report loaded and saved"
            } else {
                _statusMessage.value = "Failed to fetch report"
            }
        }
    }

    fun toggleAppBlock(device: ChildDevice, packageName: String) {
        viewModelScope.launch {
            val currentRule = _activeRules.value.find { it.packageName == packageName }
            // Check if currently blocked (rule exists and not expired)
            val isBlocked = currentRule != null && (currentRule.blockEndTime > System.currentTimeMillis() || currentRule.isPermanentlyBlocked)
            
            val newRules = if (isBlocked) {
                // Unblock: remove the rule
                _activeRules.value.filter { it.packageName != packageName }
            } else {
                // Block: Add new rule (default 1 hour for now, or permanent)
                _activeRules.value + BlockingRule(packageName, 0, System.currentTimeMillis() + 3600000)
            }
            
            val response = client.updateRules(device.ip.hostAddress ?: "", device.port, newRules)
             if (response != null && response.success) {
                _activeRules.value = newRules
                _statusMessage.value = if (isBlocked) "App unblocked" else "App blocked"
                
                // CRITICAL: If unblocking, also clear any app timer that might be enforcing the block
                if (isBlocked) {
                    client.setAppTimer(device.ip.hostAddress ?: "", device.port, packageName, 0L)
                }
                
                fetchStats(device)
            } else {
                _statusMessage.value = "Failed to update app status"
            }
        }
    }

    fun setAppIconVisibility(device: ChildDevice, visible: Boolean) {
        viewModelScope.launch {
            val response = client.setAppIconVisibility(device.ip.hostAddress ?: "", device.port, visible)
            if (response != null && response.success) {
                _statusMessage.value = if (visible) "App unhidden on child device" else "App hidden on child device"
            } else {
                _statusMessage.value = if (visible) "Failed to unhide app" else "Failed to hide app"
            }
        }
    }

    fun renameDevice(device: ChildDevice, newName: String) {
        viewModelScope.launch {
            device.customName = newName
            val response = client.updateDeviceName(device.ip.hostAddress ?: "", device.port, newName)
            if (response != null && response.success) {
                _statusMessage.value = "Device renamed"
            } else {
                _statusMessage.value = "Failed to rename device (network error)"
            }
        }
    }
    
    fun setCategoryLimit(device: ChildDevice, category: AppCategory, durationMinutes: Int) {
        viewModelScope.launch {
            val limits = _categoryLimits.value.toMutableList()
            limits.removeAll { it.category == category }
            limits.add(CategoryLimit(category, durationMinutes * 60 * 1000L))
            _categoryLimits.value = limits
            
            val response = client.updateCategoryLimits(device.ip.hostAddress ?: "", device.port, limits)
            if (response != null && response.success) {
                _statusMessage.value = "Category limit set"
            } else {
                _statusMessage.value = "Failed to set category limit"
            }
        }
    }
    
    fun approveUnlockRequest(device: ChildDevice, durationMinutes: Int = 10, packageName: String? = null) {
        viewModelScope.launch {
            val response = client.approveUnlock(device.ip.hostAddress ?: "", device.port, durationMinutes * 60 * 1000L, packageName)
            if (response != null && response.success) {
                _statusMessage.value = "Unlock approved for $durationMinutes minutes"
                fetchStats(device)
            } else {
                _statusMessage.value = "Failed to approve unlock"
            }
        }
    }
    
    fun denyUnlockRequest(device: ChildDevice) {
        viewModelScope.launch {
            val response = client.denyUnlock(device.ip.hostAddress ?: "", device.port)
            if (response != null && response.success) {
                _statusMessage.value = "Unlock request denied"
                fetchStats(device)
            } else {
                _statusMessage.value = "Failed to deny unlock"
            }
        }
    }

    fun resetPin(device: ChildDevice) {
        viewModelScope.launch {
            val response = client.resetPin(device.ip.hostAddress ?: "", device.port)
            if (response != null && response.success) {
                _statusMessage.value = "PIN reset successfully"
            } else {
                _statusMessage.value = "Failed to reset PIN"
            }
        }
    }
    
    // ... existing code ...

    fun lockDevice(device: ChildDevice, locked: Boolean) {
        viewModelScope.launch {
            val response = client.setLock(device.ip.hostAddress ?: "", device.port, locked)
            if (response != null && response.success) {
                _isDeviceLocked.value = locked
                _statusMessage.value = if (locked) "Device LOCKED" else "Device UNLOCKED"
                fetchStats(device)
            } else {
                _statusMessage.value = "Failed to toggle lock"
            }
        }
    }
    fun setAppCategory(device: ChildDevice, packageName: String, category: AppCategory) {
        viewModelScope.launch {
            val response = client.setAppCategory(device.ip.hostAddress ?: "", device.port, packageName, category)
            if (response != null && response.success) {
                _statusMessage.value = "Category updated"
                // Ideally refresh stats to reflect change if immediate response needed, but device might take a moment
                fetchStats(device)
            } else {
                _statusMessage.value = "Failed to update category"
            }
        }
    }

    fun setAppTimer(device: ChildDevice, packageName: String, durationMinutes: Int) {
         viewModelScope.launch {
            val response = client.setAppTimer(device.ip.hostAddress ?: "", device.port, packageName, durationMinutes * 60 * 1000L)
            if (response != null && response.success) {
                _statusMessage.value = "Timer set for $durationMinutes mins"
                fetchStats(device)
            } else {
                _statusMessage.value = "Failed to set timer"
            }
        }
    }

    fun cancelAppTimer(device: ChildDevice, packageName: String) {
        viewModelScope.launch {
            val response = client.setAppTimer(device.ip.hostAddress ?: "", device.port, packageName, 0L)
            if (response != null && response.success) {
                _statusMessage.value = "Timer canceled"
                fetchStats(device)
            } else {
                _statusMessage.value = "Failed to cancel timer"
            }
        }
    }

    private val _categoryTimers = MutableStateFlow<Map<AppCategory, Long>>(emptyMap())
    val categoryTimers: StateFlow<Map<AppCategory, Long>> = _categoryTimers.asStateFlow()

    fun setCategoryTimer(device: ChildDevice, category: AppCategory, durationMinutes: Int) {
        viewModelScope.launch {
            val response = client.setCategoryTimer(device.ip.hostAddress ?: "", device.port, category, durationMinutes * 60 * 1000L)
            if (response != null && response.success) {
                _statusMessage.value = "Category timer set for $durationMinutes mins"
                fetchStats(device)
            } else {
                _statusMessage.value = "Failed to set category timer"
            }
        }
    }

    fun cancelCategoryTimer(device: ChildDevice, category: AppCategory) {
        viewModelScope.launch {
            val response = client.setCategoryTimer(device.ip.hostAddress ?: "", device.port, category, 0L)
            if (response != null && response.success) {
                _statusMessage.value = "Category timer canceled"
                fetchStats(device)
            } else {
                _statusMessage.value = "Failed to cancel category timer"
            }
        }
    }
}

