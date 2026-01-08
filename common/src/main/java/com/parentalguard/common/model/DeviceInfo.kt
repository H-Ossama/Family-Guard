package com.parentalguard.common.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(
    val deviceId: String, // Unique identifier (MAC address or UUID)
    val customName: String, // User-defined name
    val defaultName: String, // Original device name
    val ipAddress: String,
    val port: Int,
    val lastSeen: Long // Timestamp of last communication
)
