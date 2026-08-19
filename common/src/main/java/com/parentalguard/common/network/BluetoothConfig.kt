package com.parentalguard.common.network

/**
 * Shared constants for the Bluetooth (RFCOMM/SPP) transport between
 * the Parent Controller and the Child Agent.
 */
object BluetoothConfig {
    // Well-known SPP service UUID shared by both apps.
    val SPP_UUID: java.util.UUID = java.util.UUID.fromString("f392c2a0-7d3c-4b8f-9a2c-1b3d4e5f6a7b")

    // The child names its Bluetooth adapter (best-effort) with this prefix so the
    // parent can identify it during discovery: "PG_Child_<deviceId>".
    const val SERVICE_NAME_PREFIX = "PG_Child_"

    // Max size of a single framed Packet payload (bound to avoid memory exhaustion).
    const val MAX_PACKET_SIZE = 64 * 1024

    fun bluetoothNameFor(deviceId: String): String = "$SERVICE_NAME_PREFIX$deviceId"

    fun bluetoothNameFor(deviceId: String, displayName: String): String {
        val safeName = displayName.trim().ifBlank { "Kid Guard" }
            .replace("|", " ")
            .replace("\n", " ")
        return "$safeName - $SERVICE_NAME_PREFIX$deviceId"
    }

    fun deviceIdFromBluetoothName(name: String): String? {
        val markerIndex = name.indexOf(SERVICE_NAME_PREFIX)
        if (markerIndex < 0) return null
        return name.substring(markerIndex + SERVICE_NAME_PREFIX.length)
            .substringBefore(' ')
            .substringBefore('-')
            .trim()
            .takeIf { it.isNotEmpty() }
    }
}
