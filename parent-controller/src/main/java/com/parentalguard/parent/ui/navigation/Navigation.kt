package com.parentalguard.parent.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.parentalguard.parent.R

/**
 * AURA routes — new names, new structure. Logic (VMs, intents) unchanged.
 */
sealed class Screen(val route: String) {
    object Pulse : Screen("pulse")
    object Circle : Screen("circle")
    object Console : Screen("console/{deviceId}") {
        fun createRoute(deviceId: String) = "console/$deviceId"
    }
    object Insights : Screen("insights")
    object ReportDetail : Screen("report_detail")
    object Control : Screen("control")
    object DeviceOwnerGuide : Screen("device_owner_guide")
    object About : Screen("about")
    object HelpSupport : Screen("help_support")
    object Pair : Screen("pair")
    object Request : Screen("request/{deviceId}/{deviceName}/{requestType}?appPackageName={appPackageName}&appName={appName}") {
        fun createRoute(
            deviceId: String,
            deviceName: String,
            requestType: String,
            appPackageName: String? = null,
            appName: String? = null
        ): String {
            var route = "request/$deviceId/$deviceName/$requestType"
            val params = mutableListOf<String>()
            if (appPackageName != null) params.add("appPackageName=$appPackageName")
            if (appName != null) params.add("appName=$appName")
            if (params.isNotEmpty()) route += "?" + params.joinToString("&")
            return route
        }
    }
}

/**
 * Floating dock destinations.
 */
sealed class DockItem(
    val route: String,
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Pulse : DockItem(Screen.Pulse.route, R.string.nav_dashboard, Icons.Rounded.MonitorHeart, Icons.Rounded.MonitorHeart)
    object Circle : DockItem(Screen.Circle.route, R.string.nav_devices, Icons.Rounded.Groups, Icons.Rounded.Groups)
    object Insights : DockItem(Screen.Insights.route, R.string.nav_reports, Icons.Rounded.Insights, Icons.Rounded.Insights)
    object Control : DockItem(Screen.Control.route, R.string.nav_settings, Icons.Rounded.Tune, Icons.Rounded.Tune)
}

val dockItems = listOf(DockItem.Pulse, DockItem.Circle, DockItem.Insights, DockItem.Control)

/**
 * Console segments (Now · Apps · Boundaries · Rhythm · Activity).
 */
enum class ConsoleSegment(val titleResId: Int) {
    Now(R.string.console_now),
    Apps(R.string.console_apps),
    Boundaries(R.string.console_boundaries),
    Rhythm(R.string.console_rhythm),
    Activity(R.string.console_activity),
    DeviceOwner(R.string.console_device_owner)
}

val consoleSegments = ConsoleSegment.values().toList()
