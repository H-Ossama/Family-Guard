package com.parentalguard.parent.viewmodel

import android.app.Application
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.parentalguard.common.utils.DiscoveryUtils
import com.parentalguard.parent.data.DeviceRepository
import com.parentalguard.parent.data.ReportsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.*
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import com.parentalguard.parent.network.DeviceClient
import com.parentalguard.parent.ui.components.NotificationHelper
import com.parentalguard.common.network.*

data class ChildDevice(
    val name: String,          // Original device name
    val ip: InetAddress,
    val port: Int,
    var customName: String = name  // User-defined custom name
)

data class DeviceStatusSummary(
    val isOnline: Boolean = false,
    val isLocked: Boolean = false,
    val batteryLevel: Int = 0,
    val activeRulesCount: Int = 0,
    val todayScreenTimeMs: Long = 0,
    val lastUpdate: Long = 0
)

class DiscoveryViewModel(application: Application) : AndroidViewModel(application) {

    private val _devices = MutableStateFlow<List<ChildDevice>>(emptyList())
    val devices: StateFlow<List<ChildDevice>> = _devices.asStateFlow()

    private val _deviceStatuses = MutableStateFlow<Map<String, DeviceStatusSummary>>(emptyMap())
    val deviceStatuses: StateFlow<Map<String, DeviceStatusSummary>> = _deviceStatuses.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val deviceClient = DeviceClient()
    private val observationJobs = ConcurrentHashMap<String, Job>()
    
    // Repositories for persistence
    private val deviceRepository = DeviceRepository(application)
    private val reportsRepository = ReportsRepository(application)

    init {
        // Load saved devices on startup
        loadSavedDevices()
    }

    private fun loadSavedDevices() {
        viewModelScope.launch {
            try {
                val savedDevices = withContext(Dispatchers.IO) {
                    deviceRepository.loadDevices()
                }
                
                if (savedDevices.isNotEmpty()) {
                    _devices.value = savedDevices
                    Log.i("DiscoveryViewModel", "Loaded ${savedDevices.size} saved devices")
                    
                    // Start monitoring all saved devices
                    savedDevices.forEach { device ->
                        observeDeviceEvents(device)
                    }
                    
                    // Initial sync of statuses
                    refreshDevices()
                } else {
                    Log.i("DiscoveryViewModel", "No saved devices found")
                }
            } catch (e: Exception) {
                Log.e("DiscoveryViewModel", "Error loading saved devices", e)
            }
        }
    }

    private fun saveDevices() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    deviceRepository.saveDevices(_devices.value)
                }
                Log.i("DiscoveryViewModel", "Saved ${_devices.value.size} devices")
            } catch (e: Exception) {
                Log.e("DiscoveryViewModel", "Error saving devices", e)
            }
        }
    }

    fun addManualDevice(ip: String, port: Int) {
        viewModelScope.launch {
            val host = withContext(Dispatchers.IO) { InetAddress.getByName(ip) }
            
            // Check if device already exists in saved devices
            val existingCustomName = withContext(Dispatchers.IO) {
                deviceRepository.getDeviceName(ip)
            }
            
            val newDevice = ChildDevice(
                name = "Manual: $ip",
                ip = host,
                port = port,
                customName = existingCustomName ?: "Manual: $ip"
            )
            val currentList = _devices.value.toMutableList()
            if (currentList.none { it.ip == newDevice.ip }) {
                currentList.add(newDevice)
                _devices.value = currentList
                saveDevices() // Persist the new device
                observeDeviceEvents(newDevice)
            }
        }
    }

    private fun observeDeviceEvents(device: ChildDevice) {
        // Delegate observation to Foreground Service to ensure it runs in background
        com.parentalguard.parent.service.NotificationService.startMonitoring(getApplication(), device)
    }

    fun startDiscovery() {
        if (_isScanning.value) return
        _isScanning.value = true
        
        viewModelScope.launch {
            DiscoveryUtils.discoverServices(getApplication()).collect { serviceInfo ->
                // Check if device already has a saved custom name
                @Suppress("DEPRECATION")
                val hostAddress = serviceInfo.host?.hostAddress ?: ""
                val existingCustomName = withContext(Dispatchers.IO) {
                    deviceRepository.getDeviceName(hostAddress)
                }
                
                @Suppress("DEPRECATION")
                val host = serviceInfo.host
                val newDevice = ChildDevice(
                    name = serviceInfo.serviceName,
                    ip = host,
                    port = serviceInfo.port,
                    customName = existingCustomName ?: serviceInfo.serviceName
                )
                
                val currentList = _devices.value.toMutableList()
                if (currentList.none { it.ip == newDevice.ip }) {
                    currentList.add(newDevice)
                    _devices.value = currentList
                    saveDevices() // Persist discovered device
                    observeDeviceEvents(newDevice)
                }
            }
        }
    }
    
    fun updateDeviceName(device: ChildDevice, newName: String) {
        val currentList = _devices.value.toMutableList()
        val index = currentList.indexOfFirst { it.ip == device.ip && it.port == device.port }
        
        if (index != -1) {
            currentList[index].customName = newName
            _devices.value = currentList
            saveDevices() // Persist the name change
            observeDeviceEvents(currentList[index]) // Refresh service cache with new name
            Log.i("DiscoveryViewModel", "Updated device name to: $newName")
        }
    }
    
    fun removeDevice(device: ChildDevice) {
        val currentList = _devices.value.toMutableList()
        currentList.removeAll { it.ip == device.ip && it.port == device.port }
        _devices.value = currentList
        saveDevices() // Persist the removal
        Log.i("DiscoveryViewModel", "Removed device: ${device.customName}")
    }
    
    fun refreshDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            _devices.value.forEach { device ->
                device.ip.hostAddress?.let { ip ->
                    launch {
                        val response = deviceClient.getStats(ip, device.port)
                        val status = if (response != null && response.success) {
                            val screenTime = response.stats?.usageLogs?.sumOf { it.totalTimeInForeground } ?: 0L
                            DeviceStatusSummary(
                                isOnline = true,
                                isLocked = response.stats?.isLocked ?: false,
                                batteryLevel = response.stats?.batteryLevel ?: 0,
                                activeRulesCount = response.stats?.activeRules?.size ?: 0,
                                todayScreenTimeMs = screenTime,
                                lastUpdate = System.currentTimeMillis()
                            )
                        } else {
                            DeviceStatusSummary(isOnline = false)
                        }
                        
                        val currentStatuses = _deviceStatuses.value.toMutableMap()
                        currentStatuses[ip] = status
                        _deviceStatuses.value = currentStatuses
                    }
                }
            }
        }
    }
    fun syncLanguage(languageCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _devices.value.forEach { device ->
                device.ip.hostAddress?.let { ip ->
                    launch {
                        val response = deviceClient.setLanguage(ip, device.port, languageCode)
                        if (response != null && response.success) {
                            Log.i("DiscoveryViewModel", "Synced language $languageCode to ${device.customName}")
                        } else {
                            Log.e("DiscoveryViewModel", "Failed to sync language to ${device.customName}")
                        }
                    }
                }
            }
        }
    }
}
