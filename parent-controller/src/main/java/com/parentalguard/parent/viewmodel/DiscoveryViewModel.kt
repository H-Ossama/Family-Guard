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
    val deviceId: String,      // Unique ID for cloud relay
    val name: String,          // Original device name
    var ip: InetAddress,       // Mutable to allow updating IP for same deviceId
    val port: Int,
    var customName: String = name  // User-defined custom name
)

enum class ConnectionType {
    LOCAL,      // Direct WiFi connection
    CLOUD,      // Via cloud relay server  
    UNKNOWN     // Status not yet determined
}

data class DeviceStatusSummary(
    val isOnline: Boolean = false,
    val isLocked: Boolean = false,
    val batteryLevel: Int = 0,
    val activeRulesCount: Int = 0,
    val todayScreenTimeMs: Long = 0,
    val lastUpdate: Long = 0,
    val connectionType: ConnectionType = ConnectionType.UNKNOWN
)

class DiscoveryViewModel(application: Application) : AndroidViewModel(application) {

    private val _devices = MutableStateFlow<List<ChildDevice>>(emptyList())
    val devices: StateFlow<List<ChildDevice>> = _devices.asStateFlow()

    private val _deviceStatuses = MutableStateFlow<Map<String, DeviceStatusSummary>>(emptyMap()) // Keyed by deviceId
    val deviceStatuses: StateFlow<Map<String, DeviceStatusSummary>> = _deviceStatuses.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val deviceClient = DeviceClient(application)
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

    fun addManualDevice(qrCode: String, defaultPort: Int, onDeviceAdded: ((ChildDevice) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val parts = qrCode.split("|")
                val (deviceId, hostPort, deviceName) = if (parts.size == 3) {
                    Triple(parts[0], parts[1], parts[2])
                } else {
                    // Fallback for old IP-only QR codes
                    Triple("legacy_${qrCode.replace(".", "_")}", qrCode, "Legacy Device")
                }

                val hostParts = hostPort.split(":")
                val ip = hostParts[0]
                val port = hostParts.getOrNull(1)?.toIntOrNull() ?: defaultPort
                
                val host = withContext(Dispatchers.IO) { InetAddress.getByName(ip) }
                
                val existingCustomName = withContext(Dispatchers.IO) {
                    deviceRepository.getDeviceName(hostAddress = ip, deviceId = deviceId)
                }
                
                val newDevice = ChildDevice(
                    deviceId = deviceId,
                    name = deviceName,
                    ip = host,
                    port = port,
                    customName = existingCustomName ?: deviceName
                )
                
                val currentList = _devices.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { it.deviceId == newDevice.deviceId }
                
                if (existingIndex == -1) {
                    currentList.add(newDevice)
                    _devices.value = currentList
                    saveDevices()
                    observeDeviceEvents(newDevice)
                } else {
                    // Update IP/Port if they changed (same device, new network)
                    currentList[existingIndex].ip = newDevice.ip
                    _devices.value = currentList
                    saveDevices()
                }
                refreshDevices()
                onDeviceAdded?.invoke(newDevice)
            } catch (e: Exception) {
                Log.e("DiscoveryViewModel", "Failed to add device from QR: $qrCode", e)
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
                    deviceRepository.getDeviceName(hostAddress, "nsd_$hostAddress")
                }
                
                @Suppress("DEPRECATION")
                val host = serviceInfo.host
                
                val newDevice = ChildDevice(
                    deviceId = "nsd_$hostAddress",
                    name = serviceInfo.serviceName,
                    ip = host,
                    port = serviceInfo.port,
                    customName = existingCustomName ?: serviceInfo.serviceName
                )
                
                val currentList = _devices.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { it.deviceId == newDevice.deviceId }
                
                if (existingIndex == -1) {
                    currentList.add(newDevice)
                    _devices.value = currentList
                    saveDevices() // Persist discovered device
                    observeDeviceEvents(newDevice)
                } else {
                    // Update IP in case it changed on the local network
                    currentList[existingIndex].ip = newDevice.ip
                    _devices.value = currentList
                    saveDevices()
                }
                refreshDevices()
            }
        }
    }
    
    fun updateDeviceName(device: ChildDevice, newName: String) {
        val currentList = _devices.value.toMutableList()
        val index = currentList.indexOfFirst { it.deviceId == device.deviceId }
        
        if (index != -1) {
            val updatedDevice = currentList[index].copy(customName = newName)
            currentList[index] = updatedDevice
            _devices.value = currentList
            saveDevices() // Persist the name change
            observeDeviceEvents(updatedDevice) // Refresh service cache with new name
            Log.i("DiscoveryViewModel", "Updated device name to: $newName")
        }
    }
    
    fun removeDevice(device: ChildDevice) {
        val currentList = _devices.value.toMutableList()
        currentList.removeAll { it.deviceId == device.deviceId }
        _devices.value = currentList
        saveDevices() // Persist the removal
        Log.i("DiscoveryViewModel", "Removed device: ${device.customName}")
    }
    
    fun resetAllDevices() {
        _devices.value = emptyList()
        _deviceStatuses.value = emptyMap()
        saveDevices()
        Log.i("DiscoveryViewModel", "Reset all devices")
    }
    
    fun updateDeviceStatus(deviceId: String, isLocked: Boolean) {
        val currentStatuses = _deviceStatuses.value.toMutableMap()
        val existing = currentStatuses[deviceId] ?: DeviceStatusSummary()
        currentStatuses[deviceId] = existing.copy(
            isLocked = isLocked,
            lastUpdate = System.currentTimeMillis()
        )
        _deviceStatuses.value = currentStatuses
    }

    private val connectionHealthCache = ConcurrentHashMap<String, Long>() // deviceId -> lastFailTime

    fun refreshDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            val devices = _devices.value
            val results = devices.map { device ->
                async {
                    val status = fetchDeviceStatus(device)
                    device.deviceId to status
                }
            }.awaitAll()

            _deviceStatuses.value = results.toMap()
        }
    }

    private suspend fun fetchDeviceStatus(device: ChildDevice): DeviceStatusSummary {
        return withTimeoutOrNull(10000) { // Increased to 10s for Cloud Relay fallback
            val ip = device.ip.hostAddress ?: ""
            Log.d("DiscoveryViewModel", "Fetching status for ${device.customName} (${device.deviceId}) at $ip:${device.port}")
            
            // Check cache for recent failures to decide if we should try direct or go straight to relay
            val lastFail = connectionHealthCache[device.deviceId] ?: 0L
            val skipDirect = System.currentTimeMillis() - lastFail < 60_000 // Skip direct for 1 min after failure

            val responseResult = deviceClient.getStatsWithConnectionType(
                ip, 
                device.port, 
                device.deviceId, 
                includeIcons = false,
                skipDirect = skipDirect
            )

            Log.d("DiscoveryViewModel", "Response for ${device.deviceId}: success=${responseResult.response?.success}, connectionType=${responseResult.connectionType}")

            if (responseResult.response != null && responseResult.response.success) {
                val response = responseResult.response
                val stats = response.stats
                
                // Check if device name was renamed from child side
                stats?.deviceName?.let { remoteName ->
                    if (remoteName.isNotBlank() && remoteName != device.customName) {
                        Log.i("DiscoveryViewModel", "Syncing new device name from child: $remoteName")
                        viewModelScope.launch {
                            updateDeviceName(device, remoteName)
                        }
                    }
                }

                val screenTime = stats?.usageLogs?.sumOf { it.totalTimeInForeground } ?: 0L
                Log.d("DiscoveryViewModel", "Successfully got stats for ${device.deviceId}: battery=${stats?.batteryLevel}")
                DeviceStatusSummary(
                    isOnline = true,
                    isLocked = stats?.isLocked ?: false,
                    batteryLevel = stats?.batteryLevel ?: 0,
                    activeRulesCount = stats?.activeRules?.size ?: 0,
                    todayScreenTimeMs = screenTime,
                    lastUpdate = System.currentTimeMillis(),
                    connectionType = responseResult.connectionType
                )
            } else {
                Log.w("DiscoveryViewModel", "Failed to get stats for ${device.deviceId}: response=${responseResult.response?.message}, skipDirect=$skipDirect")
                if (!skipDirect) {
                    connectionHealthCache[device.deviceId] = System.currentTimeMillis()
                }
                DeviceStatusSummary(isOnline = false, connectionType = ConnectionType.UNKNOWN)
            }
        } ?: run {
            Log.e("DiscoveryViewModel", "Timeout fetching status for ${device.deviceId}")
            DeviceStatusSummary(isOnline = false, connectionType = ConnectionType.UNKNOWN)
        }
    }
    fun syncLanguage(languageCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _devices.value.forEach { device ->
                device.ip.hostAddress?.let { ip ->
                    launch {
                        val response = deviceClient.setLanguage(ip, device.port, device.deviceId, languageCode)
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
