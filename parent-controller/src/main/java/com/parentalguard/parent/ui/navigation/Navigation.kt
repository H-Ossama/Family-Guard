package com.parentalguard.parent.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.parentalguard.parent.R

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Devices : Screen("devices")
    object DeviceControl : Screen("device/{deviceId}") {
        fun createRoute(deviceId: String) = "device/$deviceId"
    }
    object Settings : Screen("settings")
    object Reports : Screen("reports")
    object QRScanner : Screen("qr_scanner")
    object UnlockRequest : Screen("unlock_request/{deviceId}/{deviceName}/{requestType}?appPackageName={appPackageName}&appName={appName}") {
        fun createRoute(
            deviceId: String,
            deviceName: String,
            requestType: String,
            appPackageName: String? = null,
            appName: String? = null
        ): String {
            var route = "unlock_request/$deviceId/$deviceName/$requestType"
            val params = mutableListOf<String>()
            if (appPackageName != null) params.add("appPackageName=$appPackageName")
            if (appName != null) params.add("appName=$appName")
            
            if (params.isNotEmpty()) {
                route += "?" + params.joinToString("&")
            }
            return route
        }
    }
}

/**
 * Bottom navigation items
 */
sealed class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : BottomNavItem(
        route = Screen.Dashboard.route,
        titleResId = R.string.nav_dashboard,
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )
    
    object Devices : BottomNavItem(
        route = Screen.Devices.route,
        titleResId = R.string.nav_devices,
        selectedIcon = Icons.Filled.Devices,
        unselectedIcon = Icons.Outlined.Devices
    )
    
    object Settings : BottomNavItem(
        route = Screen.Settings.route,
        titleResId = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    object Reports : BottomNavItem(
        route = Screen.Reports.route,
        titleResId = R.string.nav_reports,
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Dashboard,
    BottomNavItem.Reports,
    BottomNavItem.Devices,
    BottomNavItem.Settings
)

/**
 * Device control tabs
 */
sealed class DeviceTab(val titleResId: Int, val icon: ImageVector) {
    object Overview : DeviceTab(R.string.tab_overview, Icons.Default.Info)
    object Apps : DeviceTab(R.string.tab_apps, Icons.Default.Apps)
    object Limits : DeviceTab(R.string.tab_limits, Icons.Default.Timer)
    object Reports : DeviceTab(R.string.tab_reports, Icons.Default.BarChart)
}

val deviceTabs = listOf(
    DeviceTab.Overview,
    DeviceTab.Apps,
    DeviceTab.Limits,
    DeviceTab.Reports
)
