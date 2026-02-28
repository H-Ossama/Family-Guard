package com.parentalguard.parent.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DeviceConnectionStatus {
    ONLINE,
    OFFLINE
}

object DeviceStatusRepository {
    private val _deviceStatus = MutableStateFlow<Map<String, DeviceConnectionStatus>>(emptyMap())
    val deviceStatus: StateFlow<Map<String, DeviceConnectionStatus>> = _deviceStatus.asStateFlow()

    fun updateStatus(deviceId: String, isOnline: Boolean) {
        val currentStatus = _deviceStatus.value.toMutableMap()
        currentStatus[deviceId] = if (isOnline) DeviceConnectionStatus.ONLINE else DeviceConnectionStatus.OFFLINE
        _deviceStatus.value = currentStatus
    }
}
