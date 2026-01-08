package com.parentalguard.parent.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.parentalguard.parent.ui.navigation.*
import com.parentalguard.parent.ui.screens.UnlockRequestScreen
import com.parentalguard.parent.ui.screens.DashboardScreen
import com.parentalguard.parent.ui.screens.DeviceControlScreen
import com.parentalguard.parent.ui.screens.DeviceDiscoveryScreen
import com.parentalguard.parent.ui.screens.SettingsScreen
import com.parentalguard.parent.ui.theme.ParentalGuardTheme
import com.parentalguard.parent.ui.theme.Primary
import com.parentalguard.parent.viewmodel.ChildDevice
import com.parentalguard.parent.viewmodel.DeviceControlViewModel
import com.parentalguard.parent.viewmodel.DiscoveryViewModel
import com.parentalguard.parent.UnlockRequestData
import com.parentalguard.parent.data.ReportsRepository
import com.parentalguard.parent.ui.screens.ReportsHistoryScreen
import com.parentalguard.parent.ui.DailyReportScreen
import com.parentalguard.common.model.DailyUsageReport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentalControlApp(
    discoveryViewModel: DiscoveryViewModel = viewModel(),
    controlViewModel: DeviceControlViewModel = viewModel(),
    initialDeviceId: String? = null,
    initialUnlockRequest: UnlockRequestData? = null
) {
    ParentalGuardTheme {
        val navController = rememberNavController()
        val devices by discoveryViewModel.devices.collectAsState()
        val isScanning by discoveryViewModel.isScanning.collectAsState()
        
        // Track selected device for navigation
        var selectedDevice by remember { mutableStateOf<ChildDevice?>(null) }
        var selectedReport by remember { mutableStateOf<DailyUsageReport?>(null) }
        val reportsRepository = remember { ReportsRepository(discoveryViewModel.getApplication()) }
        
        // Handle initial navigation from notification
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        LaunchedEffect(initialDeviceId, initialUnlockRequest) {
            if (initialUnlockRequest != null) {
                navController.navigate(
                    Screen.UnlockRequest.createRoute(
                        deviceId = initialUnlockRequest.deviceId,
                        deviceName = initialUnlockRequest.deviceName,
                        requestType = initialUnlockRequest.requestType,
                        appPackageName = initialUnlockRequest.appPackageName,
                        appName = initialUnlockRequest.appName
                    )
                ) {
                    // Pop up to dashboard to avoid back stack mess
                    popUpTo(Screen.Dashboard.route)
                }
            } else if (initialDeviceId != null) {
                navController.navigate(Screen.DeviceControl.createRoute(initialDeviceId)) {
                    popUpTo(Screen.Dashboard.route)
                }
            }
        }
        
        // Explicit back handling to ensure system back button works with NavHost
        androidx.activity.compose.BackHandler(enabled = navController.previousBackStackEntry != null) {
            navController.popBackStack()
        }
        
        // Determine if bottom bar should be visible
        val showBottomBar = currentRoute in listOf(
            Screen.Dashboard.route,
            Screen.Devices.route,
            Screen.Settings.route
        )
        
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    PremiumBottomNavigation(
                        items = bottomNavItems,
                        currentRoute = currentRoute,
                        onItemClick = { item ->
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(paddingValues),
                enterTransition = {
                    fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                        initialOffsetX = { it / 4 },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                        targetOffsetX = { it / 4 },
                        animationSpec = tween(300)
                    )
                }
            ) {
                // Dashboard
                composable(Screen.Dashboard.route) {
                    val deviceStatuses by discoveryViewModel.deviceStatuses.collectAsState()
                    DashboardScreen(
                        devices = devices,
                        deviceStatuses = deviceStatuses,
                        onDeviceClick = { device ->
                            selectedDevice = device
                            navController.navigate(Screen.DeviceControl.createRoute(device.ip.hostAddress ?: "unknown"))
                        },
                        onScanQR = {
                            navController.navigate(Screen.QRScanner.route)
                        },
                        onLockAll = {
                            devices.forEach { device ->
                                controlViewModel.lockDevice(device, true)
                            }
                        }
                    )
                }
                
                // Devices / Discovery
                composable(Screen.Devices.route) {
                    DeviceDiscoveryScreen(
                        devices = devices,
                        isScanning = isScanning,
                        onStartScan = { discoveryViewModel.startDiscovery() },
                        onDeviceSelected = { device ->
                            selectedDevice = device
                            navController.navigate(Screen.DeviceControl.createRoute(device.ip.hostAddress ?: "unknown"))
                        },
                        onScanQR = {
                            navController.navigate(Screen.QRScanner.route)
                        },
                        viewModel = discoveryViewModel
                    )
                }
                
                // Device Control
                composable(
                    route = Screen.DeviceControl.route,
                    arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val deviceId = backStackEntry.arguments?.getString("deviceId")
                    val device = selectedDevice ?: devices.find { it.ip.hostAddress == deviceId }
                    
                    if (device != null) {
                        DeviceControlScreen(
                            device = device,
                            viewModel = controlViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    } else {
                        // Device not found, go back
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
                }
                
                // QR Scanner
                composable(Screen.QRScanner.route) {
                    QRScannerScreen(
                        onQrScanned = { code ->
                            discoveryViewModel.addManualDevice(code, 8080)
                            navController.popBackStack()
                        }
                    )
                }

                // Unlock Request
                composable(
                    route = Screen.UnlockRequest.route,
                    arguments = listOf(
                        navArgument("deviceId") { type = NavType.StringType },
                        navArgument("deviceName") { type = NavType.StringType },
                        navArgument("requestType") { type = NavType.StringType },
                        navArgument("appPackageName") { 
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        navArgument("appName") { 
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
                    val intentDeviceName = backStackEntry.arguments?.getString("deviceName") ?: ""
                    val requestType = backStackEntry.arguments?.getString("requestType") ?: "DEVICE"
                    val appPackageName = backStackEntry.arguments?.getString("appPackageName")
                    val appName = backStackEntry.arguments?.getString("appName")
                    
                    // Resolve the latest custom name from our devices list, or fallback to intent name
                    val resolvedDeviceName = remember(deviceId, devices) {
                        devices.find { it.ip.hostAddress == deviceId }?.customName ?: intentDeviceName
                    }
                    
                    UnlockRequestScreen(
                        deviceId = deviceId,
                        deviceName = resolvedDeviceName,
                        requestType = requestType,
                        appPackageName = appPackageName,
                        appName = appName,
                        viewModel = controlViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                
                // Settings
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onLanguageChanged = { languageCode ->
                            discoveryViewModel.syncLanguage(languageCode)
                        }
                    )
                }
                
                // Reports History
                composable(Screen.Reports.route) {
                    ReportsHistoryScreen(
                        reportsRepository = reportsRepository,
                        onReportClick = { report ->
                            selectedReport = report
                            navController.navigate("report_detail")
                        }
                    )
                }
                
                // Individual Report Detail (Reuse DailyReportScreen)
                composable("report_detail") {
                    DailyReportScreen(
                        report = selectedReport,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumBottomNavigation(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = androidx.compose.ui.res.stringResource(item.titleResId)
                    )
                },
                label = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(item.titleResId),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = selected,
                onClick = { onItemClick(item) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
