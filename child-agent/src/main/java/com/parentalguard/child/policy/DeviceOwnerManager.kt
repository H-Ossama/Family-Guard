package com.parentalguard.child.policy

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserManager
import android.provider.Settings
import android.net.wifi.WifiManager
import com.parentalguard.child.data.RuleRepository
import com.parentalguard.child.receiver.AdminReceiver
import com.parentalguard.common.model.CapabilityState
import com.parentalguard.common.model.DeviceOwnerCapabilities

data class PolicyResult(val success: Boolean, val message: String)

object DeviceOwnerManager {
    private val supportedRestrictions = setOf(
        UserManager.DISALLOW_INSTALL_APPS,
        UserManager.DISALLOW_MODIFY_ACCOUNTS,
        UserManager.DISALLOW_ADD_USER
    )

    fun isDeviceOwner(context: Context): Boolean {
        val manager = context.getSystemService(DevicePolicyManager::class.java)
        return manager?.isDeviceOwnerApp(context.packageName) == true
    }

    fun capabilities(context: Context): DeviceOwnerCapabilities {
        if (!isDeviceOwner(context)) {
            return DeviceOwnerCapabilities(
                appSuspension = CapabilityState.UNAVAILABLE,
                uninstallProtection = CapabilityState.UNAVAILABLE,
                userRestrictions = CapabilityState.UNAVAILABLE,
                deviceLock = CapabilityState.UNAVAILABLE,
                deviceUsageLimits = CapabilityState.UNAVAILABLE,
                appUsageLimits = CapabilityState.UNAVAILABLE,
                wifiToggle = CapabilityState.UNAVAILABLE
            )
        }

        val usageAvailable = hasUsageAccess(context)
        return DeviceOwnerCapabilities(
            appSuspension = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) CapabilityState.AVAILABLE else CapabilityState.UNAVAILABLE,
            uninstallProtection = CapabilityState.AVAILABLE,
            userRestrictions = CapabilityState.AVAILABLE,
            deviceLock = CapabilityState.AVAILABLE,
            deviceUsageLimits = if (usageAvailable) CapabilityState.AVAILABLE else CapabilityState.UNAVAILABLE,
            appUsageLimits = if (usageAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) CapabilityState.AVAILABLE else CapabilityState.UNAVAILABLE,
            wifiToggle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) CapabilityState.AVAILABLE else CapabilityState.UNAVAILABLE
        )
    }

    fun setAppSuspended(context: Context, packageName: String, suspended: Boolean): PolicyResult {
        val gate = requireOwner(context) ?: return gateResult()
        if (!isValidManagedPackage(context, packageName)) return PolicyResult(false, "Invalid managed app package")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return PolicyResult(false, "App suspension is not supported")

        return try {
            val failed = gate.setPackagesSuspended(ComponentName(context, AdminReceiver::class.java), arrayOf(packageName), suspended)
            if (failed.isNullOrEmpty()) {
                PolicyResult(true, if (suspended) "App suspended" else "App resumed")
            } else {
                PolicyResult(false, "Android rejected app suspension")
            }
        } catch (e: SecurityException) {
            PolicyResult(false, "Device Owner rejected app suspension")
        } catch (e: IllegalArgumentException) {
            PolicyResult(false, "Invalid app package")
        }
    }

    fun setUninstallProtection(context: Context, packageName: String, enabled: Boolean): PolicyResult {
        val gate = requireOwner(context) ?: return gateResult()
        if (!isValidManagedPackage(context, packageName)) return PolicyResult(false, "Invalid managed app package")
        return try {
            gate.setUninstallBlocked(ComponentName(context, AdminReceiver::class.java), packageName, enabled)
            PolicyResult(true, if (enabled) "Uninstall protection enabled" else "Uninstall protection disabled")
        } catch (e: SecurityException) {
            PolicyResult(false, "Device Owner rejected uninstall protection")
        } catch (e: IllegalArgumentException) {
            PolicyResult(false, "Invalid app package")
        }
    }

    fun setUserRestriction(context: Context, restrictionKey: String, enabled: Boolean): PolicyResult {
        val gate = requireOwner(context) ?: return gateResult()
        if (restrictionKey !in supportedRestrictions) return PolicyResult(false, "Unsupported user restriction")
        return try {
            val admin = ComponentName(context, AdminReceiver::class.java)
            if (enabled) gate.addUserRestriction(admin, restrictionKey)
            else gate.clearUserRestriction(admin, restrictionKey)
            PolicyResult(true, "Device restriction updated")
        } catch (e: SecurityException) {
            PolicyResult(false, "Device Owner rejected this restriction")
        }
    }

    fun setDeviceLocked(context: Context, locked: Boolean): PolicyResult {
        val gate = requireOwner(context) ?: return gateResult()
        return try {
            if (locked) {
                gate.lockNow()
                RuleRepository.setGlobalLock(true, "DEVICE_OWNER_LOCK")
                PolicyResult(true, "Device locked")
            } else {
                // Android does not expose a public Device Owner unlock API.
                if (RuleRepository.lockReason.value == "DEVICE_OWNER_LOCK") {
                    RuleRepository.setGlobalLock(false)
                }
                PolicyResult(true, "Kid Guard lock cleared; unlock the keyguard on the device")
            }
        } catch (e: SecurityException) {
            PolicyResult(false, "Device Owner rejected device lock")
        }
    }

    fun lockNow(context: Context): PolicyResult {
        val gate = requireOwner(context) ?: return gateResult()
        return try {
            gate.lockNow()
            PolicyResult(true, "Device locked")
        } catch (e: SecurityException) {
            PolicyResult(false, "Device Owner rejected device lock")
        }
    }

    fun setWifiEnabled(context: Context, enabled: Boolean): PolicyResult {
        val gate = requireOwner(context) ?: return gateResult()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return PolicyResult(false, "Wi-Fi control is not supported")
        return try {
            gate.setGlobalSetting(
                ComponentName(context, AdminReceiver::class.java),
                Settings.Global.WIFI_ON,
                if (enabled) "1" else "0"
            )
            val wifiManager = context.getSystemService(WifiManager::class.java)
            if (wifiManager?.isWifiEnabled == enabled) {
                PolicyResult(true, if (enabled) "Wi-Fi enabled" else "Wi-Fi disabled")
            } else {
                PolicyResult(false, "Wi-Fi state could not be confirmed")
            }
        } catch (e: SecurityException) {
            PolicyResult(false, "Device Owner rejected Wi-Fi control")
        } catch (e: IllegalArgumentException) {
            PolicyResult(false, "Wi-Fi control is not supported on this device")
        }
    }

    fun isValidManagedPackage(context: Context, packageName: String): Boolean {
        return packageName.isNotBlank() &&
            packageName != context.packageName &&
            packageExists(context, packageName)
    }

    private fun requireOwner(context: Context): DevicePolicyManager? {
        val manager = context.getSystemService(DevicePolicyManager::class.java) ?: return null
        return manager.takeIf { it.isDeviceOwnerApp(context.packageName) }
    }

    private fun gateResult(): PolicyResult =
        PolicyResult(false, "Device Owner privilege is not enabled")

    @Suppress("DEPRECATION")
    private fun packageExists(context: Context, packageName: String): Boolean = try {
        context.packageManager.getApplicationInfo(packageName, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    @Suppress("DEPRECATION")
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        return appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        ) == AppOpsManager.MODE_ALLOWED
    }
}
