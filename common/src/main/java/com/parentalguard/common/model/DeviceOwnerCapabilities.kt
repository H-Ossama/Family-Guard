package com.parentalguard.common.model

import kotlinx.serialization.Serializable

@Serializable
enum class CapabilityState {
    UNKNOWN,
    AVAILABLE,
    UNAVAILABLE
}

enum class DeviceOwnerCapability {
    APP_SUSPENSION,
    UNINSTALL_PROTECTION,
    USER_RESTRICTIONS,
    DEVICE_LOCK,
    DEVICE_USAGE_LIMITS,
    APP_USAGE_LIMITS,
    WIFI_TOGGLE
}

@Serializable
data class DeviceOwnerCapabilities(
    val appSuspension: CapabilityState = CapabilityState.UNKNOWN,
    val uninstallProtection: CapabilityState = CapabilityState.UNKNOWN,
    val userRestrictions: CapabilityState = CapabilityState.UNKNOWN,
    val deviceLock: CapabilityState = CapabilityState.UNKNOWN,
    val deviceUsageLimits: CapabilityState = CapabilityState.UNKNOWN,
    val appUsageLimits: CapabilityState = CapabilityState.UNKNOWN,
    val wifiToggle: CapabilityState = CapabilityState.UNKNOWN
) {
    fun stateFor(capability: DeviceOwnerCapability): CapabilityState = when (capability) {
        DeviceOwnerCapability.APP_SUSPENSION -> appSuspension
        DeviceOwnerCapability.UNINSTALL_PROTECTION -> uninstallProtection
        DeviceOwnerCapability.USER_RESTRICTIONS -> userRestrictions
        DeviceOwnerCapability.DEVICE_LOCK -> deviceLock
        DeviceOwnerCapability.DEVICE_USAGE_LIMITS -> deviceUsageLimits
        DeviceOwnerCapability.APP_USAGE_LIMITS -> appUsageLimits
        DeviceOwnerCapability.WIFI_TOGGLE -> wifiToggle
    }
}
