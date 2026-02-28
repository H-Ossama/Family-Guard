package com.parentalguard.parent.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parentalguard.common.model.AppCategory
import com.parentalguard.common.model.AppUsageLog
import com.parentalguard.parent.R
import com.parentalguard.parent.ui.components.*
import com.parentalguard.parent.ui.navigation.DeviceTab
import com.parentalguard.parent.ui.navigation.deviceTabs
import com.parentalguard.parent.ui.theme.*
import com.parentalguard.parent.viewmodel.ChildDevice
import com.parentalguard.parent.viewmodel.DeviceControlViewModel
import com.parentalguard.parent.viewmodel.DiscoveryViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DeviceControlScreen(
    device: ChildDevice,
    viewModel: DeviceControlViewModel,
    discoveryViewModel: DiscoveryViewModel? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val usageLogs by viewModel.usageLogs.collectAsState()
    val activeRules by viewModel.activeRules.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val dailyReport by viewModel.dailyReport.collectAsState()
    val appTimers by viewModel.appTimers.collectAsState()
    val categoryTimers by viewModel.categoryTimers.collectAsState()
    val connectionType by viewModel.connectionType.collectAsState()
    
    var showRenameDialog by remember { mutableStateOf(false) }

    if (showRenameDialog) {
        RenameDeviceDialog(
            currentName = device.customName,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                viewModel.renameDevice(device, newName)
                showRenameDialog = false
            }
        )
    }
    
    val pagerState = rememberPagerState(pageCount = { deviceTabs.size })
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(device) {
        viewModel.fetchStats(device)
        viewModel.fetchDailyReport(device)
    }
    
    Scaffold(
        topBar = {
            DeviceControlTopBar(
                device = device,
                statusMessage = statusMessage,
                connectionType = connectionType,
                onBack = onBack,
                onRename = { showRenameDialog = true },
                onRefresh = {
                    viewModel.fetchStats(device)
                    viewModel.fetchDailyReport(device)
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(brush = PremiumGradient)
        ) {
            // Tab Row (Modernized)
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = LiquidBlue,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                .height(3.dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(
                                    brush = Brush.horizontalGradient(LiquidGradientPrimary)
                                )
                        )
                    }
                },
                divider = {}
            ) {
                deviceTabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = stringResource(tab.titleResId),
                                fontWeight = if (pagerState.currentPage == index) 
                                    FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        selectedContentColor = LiquidBlue,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray.copy(alpha = 0.2f))
            )
            
            // Tab Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (deviceTabs[page]) {
                    DeviceTab.Overview -> OverviewTab(
                        device = device,
                        viewModel = viewModel,
                        discoveryViewModel = discoveryViewModel,
                        usageLogs = usageLogs,
                        activeRules = activeRules
                    )
                    DeviceTab.Apps -> AppsTab(
                        device = device,
                        viewModel = viewModel,
                        discoveryViewModel = discoveryViewModel,
                        usageLogs = usageLogs,
                        activeRules = activeRules
                    )
                    DeviceTab.Limits -> LimitsTab(
                        device = device,
                        viewModel = viewModel,
                        usageLogs = usageLogs,
                        categoryTimers = categoryTimers
                    )
                    DeviceTab.Break -> BreakTab(
                        device = device,
                        viewModel = viewModel
                    )
                    DeviceTab.Reports -> ReportsTab(
                        device = device,
                        viewModel = viewModel,
                        dailyReport = dailyReport
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceControlTopBar(
    device: ChildDevice,
    statusMessage: String?,
    connectionType: com.parentalguard.parent.viewmodel.ConnectionType,
    onBack: () -> Unit,
    onRename: () -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        color = LiquidCardBackground,
        shadowElevation = 0.dp
    ) {
        TopAppBar(
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.customName.ifBlank { device.name },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            letterSpacing = (-0.5).sp
                        )
                        IconButton(
                            onClick = onRename,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.rename_device),
                                modifier = Modifier.size(14.dp),
                                tint = LiquidBlue
                            )
                        }
                    }
                    if (statusMessage != null) {
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = LiquidBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            actions = {
                val isOnline = connectionType != com.parentalguard.parent.viewmodel.ConnectionType.UNKNOWN
                
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh_stats),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                (if (connectionType == com.parentalguard.parent.viewmodel.ConnectionType.LOCAL) Success else LiquidBlue).copy(alpha = 0.1f)
                            )
                            .border(
                                1.dp,
                                (if (connectionType == com.parentalguard.parent.viewmodel.ConnectionType.LOCAL) Success else LiquidBlue).copy(alpha = 0.2f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (connectionType == com.parentalguard.parent.viewmodel.ConnectionType.LOCAL) 
                                    Icons.Default.Wifi else Icons.Default.Cloud,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (connectionType == com.parentalguard.parent.viewmodel.ConnectionType.LOCAL) Success else LiquidBlue
                            )
                            Text(
                                text = if (connectionType == com.parentalguard.parent.viewmodel.ConnectionType.LOCAL) "LOCAL" else "RELAY",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (connectionType == com.parentalguard.parent.viewmodel.ConnectionType.LOCAL) Success else LiquidBlue,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
                
                StatusDot(
                    isOnline = isOnline,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun RenameDeviceDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_rename_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.dialog_rename_label)) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun EditCategoryDialog(
    currentCategory: AppCategory,
    onDismiss: () -> Unit,
    onConfirm: (AppCategory) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(currentCategory) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_edit_category_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppCategory.values().forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCategory = category }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = getCategoryIcon(category),
                            contentDescription = null,
                            tint = getCategoryColor(category),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(getCategoryNameResId(category)))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedCategory) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SetAppTimerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var durationMinutes by remember { mutableIntStateOf(15) }
    val options = listOf(15, 30, 45, 60, 90, 120)
    var showCustomDialog by remember { mutableStateOf(false) }

    if (showCustomDialog) {
        CustomTimerDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { mins ->
                onConfirm(mins)
                showCustomDialog = false
            }
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_timer_title)) },
        text = {
            Column {
                Text(stringResource(R.string.dialog_timer_desc))
                Spacer(Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                   items(options) { minutes ->
                       val isSelected = durationMinutes == minutes
                       Row(
                           modifier = Modifier
                               .fillMaxWidth()
                               .clip(RoundedCornerShape(8.dp))
                               .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                               .clickable { durationMinutes = minutes }
                               .padding(12.dp),
                           verticalAlignment = Alignment.CenterVertically
                       ) {
                           RadioButton(selected = isSelected, onClick = { durationMinutes = minutes })
                           Spacer(Modifier.width(8.dp))
                           Text(stringResource(R.string.label_limit_mins, minutes))
                       }
                   }
                   
                   item {
                       Row(
                           modifier = Modifier
                               .fillMaxWidth()
                               .clickable { showCustomDialog = true }
                               .padding(12.dp),
                           verticalAlignment = Alignment.CenterVertically
                       ) {
                           Icon(Icons.Default.Add, contentDescription = null, tint = Primary)
                           Spacer(Modifier.width(12.dp))
                           Text(stringResource(R.string.dialog_timer_custom), color = Primary, fontWeight = FontWeight.Bold)
                       }
                   }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(durationMinutes) }) {
                Text(stringResource(R.string.action_set_timer))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SetDailyLimitDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var durationMinutes by remember { mutableIntStateOf(60) }
    val options = listOf(30, 60, 120, 180, 240, 300)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Daily Limit") },
        text = {
            Column {
                Text("App will be blocked after reaching this usage time today.")
                Spacer(Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 300.dp)) {
                   items(options) { minutes ->
                       val isSelected = durationMinutes == minutes
                       Row(
                           modifier = Modifier
                               .fillMaxWidth()
                               .clip(RoundedCornerShape(8.dp))
                               .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                               .clickable { durationMinutes = minutes }
                               .padding(12.dp),
                           verticalAlignment = Alignment.CenterVertically
                       ) {
                           RadioButton(selected = isSelected, onClick = { durationMinutes = minutes })
                           Spacer(Modifier.width(8.dp))
                           Text(if (minutes >= 60) "${minutes/60}h ${minutes%60}m" else "${minutes}m")
                       }
                   }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(durationMinutes) }) {
                Text("Set Limit")
            }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(0) }) {
                Text("Remove Limit", color = Error)
            }
        }
    )
}

@Composable
fun CustomTimerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf("15") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_custom_timer_title)) },
        text = {
            Column {
                Text(stringResource(R.string.dialog_custom_timer_label))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { if (it.all { char -> char.isDigit() }) textValue = it },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    val mins = textValue.toIntOrNull() ?: 0
                    if (mins > 0) onConfirm(mins)
                },
                enabled = textValue.isNotBlank() && (textValue.toIntOrNull() ?: 0) > 0
            ) {
                Text(stringResource(R.string.action_set))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.back))
            }
        }
    )
}

// ==================== OVERVIEW TAB ====================

@Composable
private fun OverviewTab(
    device: ChildDevice,
    viewModel: DeviceControlViewModel,
    discoveryViewModel: DiscoveryViewModel?,
    usageLogs: List<AppUsageLog>,
    activeRules: List<com.parentalguard.common.model.BlockingRule>
) {
    val totalScreenTime = usageLogs.sumOf { it.totalTimeInForeground }
    val isLocked by viewModel.isDeviceLocked.collectAsState()
    val isAppIconHidden by viewModel.isAppIconHidden.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Section: Gauge & Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LiquidGlassCard(
                    modifier = Modifier.weight(1.2f),
                    backgroundColor = LiquidCardBackground
                ) {
                    LiquidGlassGauge(
                        value = (totalScreenTime / (8 * 60 * 60 * 1000f)).coerceIn(0f, 1f),
                        label = "Daily Goal",
                        size = 120.dp,
                        color = LiquidBlue,
                        subtext = formatDuration(totalScreenTime)
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        padding = 12.dp
                    ) {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isLocked) Error else Success)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isLocked) "LOCKED" else "SAFE",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isLocked) Error else Success
                            )
                        }
                    }
                    
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        padding = 12.dp,
                        backgroundColor = (if (isAppIconHidden) LiquidPurple else LiquidTeal).copy(alpha = 0.1f)
                    ) {
                        Icon(
                            imageVector = if (isAppIconHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = if (isAppIconHidden) LiquidPurple else LiquidTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isAppIconHidden) "Hidden" else "Visible",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isAppIconHidden) LiquidPurple else LiquidTeal
                        )
                    }
                }
            }
        }
        
        // Quick Controls
        item {
            LiquidGlassSection(title = "Device Controls") {
                Column(modifier = Modifier.padding(16.dp)) {
                    LiquidGlassButton(
                        text = if (isLocked) stringResource(R.string.unlock_device) else stringResource(R.string.lock_device),
                        onClick = { viewModel.lockDevice(device, !isLocked, discoveryViewModel) },
                        icon = if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        gradient = if (isLocked) LiquidGradientPrimary else listOf(Error, LiquidPink),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LiquidGlassButton(
                            text = if (isAppIconHidden) "Show App" else "Hide App",
                            onClick = { viewModel.setAppIconVisibility(device, isAppIconHidden) },
                            icon = if (isAppIconHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            gradient = listOf(LiquidTeal, LiquidBlue),
                            modifier = Modifier.weight(1f)
                        )
                        LiquidGlassButton(
                            text = "Refresh",
                            onClick = { viewModel.refreshIcons(device) },
                            icon = Icons.Default.Refresh,
                            gradient = listOf(Color.Gray, Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        // Top Apps Section
        item {
            Text(
                text = stringResource(R.string.label_top_apps).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                letterSpacing = 1.sp
            )
        }
        
        val topApps = usageLogs.sortedByDescending { it.totalTimeInForeground }.take(5)
        
        if (topApps.isEmpty()) {
            item {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.label_no_usage_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(topApps) { app ->
                val isBlocked = activeRules.any { it.packageName == app.packageName && (it.blockEndTime > System.currentTimeMillis() || it.isPermanentlyBlocked) }
                LiquidTopAppItem(
                    app = app,
                    isBlocked = isBlocked,
                    totalScreenTime = totalScreenTime, 
                    onBlock = { viewModel.toggleAppBlock(device, app.packageName, discoveryViewModel) },
                    activeRules = activeRules,
                    viewModel = viewModel,
                    device = device,
                    discoveryViewModel = discoveryViewModel,
                    iconBase64 = viewModel.getAppIcon(app.packageName)
                )
            }
        }
    }
}

@Composable
private fun LiquidTopAppItem(
    app: AppUsageLog,
    isBlocked: Boolean,
    totalScreenTime: Long,
    onBlock: () -> Unit,
    activeRules: List<com.parentalguard.common.model.BlockingRule>,
    viewModel: DeviceControlViewModel,
    device: ChildDevice,
    discoveryViewModel: DiscoveryViewModel? = null,
    iconBase64: String? = null
) {
    val progress = if (totalScreenTime > 0) 
        (app.totalTimeInForeground.toFloat() / totalScreenTime) else 0f
    
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(getCategoryColor(app.category).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (iconBase64 != null) {
                    val bitmap = remember(iconBase64) {
                        try {
                            val decodedString = android.util.Base64.decode(iconBase64, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size).asImageBitmap()
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = getCategoryIcon(app.category),
                            contentDescription = null,
                            tint = getCategoryColor(app.category),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = getCategoryIcon(app.category),
                        contentDescription = null,
                        tint = getCategoryColor(app.category),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.packageName.substringAfterLast("."),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = getCategoryColor(app.category),
                    trackColor = Color.LightGray.copy(alpha = 0.2f)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatDuration(app.totalTimeInForeground),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = LiquidBlue
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBlocked) "Blocked" else "Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isBlocked) Error else Success,
                        modifier = Modifier.clickable { onBlock() }
                    )
                    Spacer(Modifier.width(8.dp))
                    val isInternetBlocked = activeRules.any { it.packageName == app.packageName && it.isInternetBlocked }
                    Icon(
                        imageVector = if (isInternetBlocked) Icons.Default.WifiOff else Icons.Default.Wifi,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp).clickable { viewModel.toggleInternetBlock(device, app.packageName, discoveryViewModel) },
                        tint = if (isInternetBlocked) Error else Success
                    )
                }
            }
        }
    }
}

// ==================== APPS TAB ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppsTab(
    device: ChildDevice,
    viewModel: DeviceControlViewModel,
    discoveryViewModel: DiscoveryViewModel?,
    usageLogs: List<AppUsageLog>,
    activeRules: List<com.parentalguard.common.model.BlockingRule>
) {
    val appTimers by viewModel.appTimers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<AppCategory?>(null) }
    
    // Dialog States
    var showEditCategoryDialog by remember { mutableStateOf<Pair<String, AppCategory>?>(null) }
    var showTimerDialog by remember { mutableStateOf<String?>(null) }
    var showDailyLimitDialog by remember { mutableStateOf<String?>(null) }

    if (showEditCategoryDialog != null) {
        val (pkg, currentCat) = showEditCategoryDialog!!
        EditCategoryDialog(
            currentCategory = currentCat,
            onDismiss = { showEditCategoryDialog = null },
            onConfirm = { newCat ->
                viewModel.setAppCategory(device, pkg, newCat)
                showEditCategoryDialog = null
            }
        )
    }

    if (showTimerDialog != null) {
        val pkg = showTimerDialog!!
        SetAppTimerDialog(
            onDismiss = { showTimerDialog = null },
            onConfirm = { minutes ->
                viewModel.setAppTimer(device, pkg, minutes)
                showTimerDialog = null
            }
        )
    }
    
    if (showDailyLimitDialog != null) {
        val pkg = showDailyLimitDialog!!
        SetDailyLimitDialog(
            onDismiss = { showDailyLimitDialog = null },
            onConfirm = { minutes ->
                viewModel.setAppDailyLimit(device, pkg, minutes)
                showDailyLimitDialog = null
            }
        )
    }
    
    val filteredApps = usageLogs
        .filter { log ->
            (searchQuery.isEmpty() || log.packageName.contains(searchQuery, ignoreCase = true)) &&
            (selectedCategory == null || log.category == selectedCategory)
        }
        .sortedByDescending { it.totalTimeInForeground }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Search and filter (Stylized)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.placeholder_search_apps)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LiquidBlue) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LiquidBlue,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f),
                    focusedContainerColor = Color.White.copy(alpha = 0.5f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.3f)
                )
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Category filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(null, AppCategory.SOCIAL, AppCategory.GAMES, AppCategory.ENTERTAINMENT).forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { 
                            Text(
                                if (category == null) "All" 
                                else stringResource(getCategoryNameResId(category))
                            ) 
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LiquidBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
        
        // App list
        if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "No apps found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredApps) { app ->
                    val expiration = appTimers[app.packageName] ?: 0L
                    val isTimerExpired = expiration > 0 && expiration < System.currentTimeMillis()
                    val rule = activeRules.find { it.packageName == app.packageName }
                    val isBlocked = (rule != null && (rule.blockEndTime > System.currentTimeMillis() || rule.isPermanentlyBlocked)) || isTimerExpired
                    
                    LiquidAppListItem(
                        app = app,
                        isBlocked = isBlocked,
                        activeRules = activeRules,
                        viewModel = viewModel,
                        device = device,
                        discoveryViewModel = discoveryViewModel,
                        onBlock = { viewModel.toggleAppBlock(device, app.packageName, discoveryViewModel) },
                        onEditCategory = { showEditCategoryDialog = app.packageName to app.category },
                        onSetTimer = { showTimerDialog = app.packageName },
                        onCancelTimer = { viewModel.cancelAppTimer(device, app.packageName) },
                        onSetDailyLimit = { showDailyLimitDialog = app.packageName },
                        dailyLimitMs = rule?.maxDailyTimeMs ?: 0L,
                        iconBase64 = viewModel.getAppIcon(app.packageName),
                        timerExpiration = expiration
                    )
                }
            }
        }
        
        // Footer Actions
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            padding = 12.dp
        ) {
            LiquidGlassButton(
                text = "Refresh App List",
                onClick = { viewModel.refreshIcons(device) },
                icon = Icons.Default.Refresh,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LiquidAppListItem(
    app: AppUsageLog,
    isBlocked: Boolean,
    activeRules: List<com.parentalguard.common.model.BlockingRule>,
    viewModel: DeviceControlViewModel,
    device: ChildDevice,
    discoveryViewModel: DiscoveryViewModel? = null,
    onBlock: () -> Unit,
    onEditCategory: () -> Unit,
    onSetTimer: () -> Unit,
    onCancelTimer: () -> Unit,
    onSetDailyLimit: () -> Unit,
    dailyLimitMs: Long = 0L,
    iconBase64: String? = null,
    timerExpiration: Long = 0L
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    if (timerExpiration > currentTime) {
        LaunchedEffect(timerExpiration) {
            while (currentTime < timerExpiration) {
                delay(1000)
                currentTime = System.currentTimeMillis()
            }
        }
    }

    val remainingMs = (timerExpiration - currentTime).coerceAtLeast(0)
    val hasActiveTimer = remainingMs > 0
    
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(getCategoryColor(app.category).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (iconBase64 != null) {
                    val bitmap = remember(iconBase64) {
                        try {
                            val decodedString = android.util.Base64.decode(iconBase64, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size).asImageBitmap()
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(getCategoryIcon(app.category), null, tint = getCategoryColor(app.category))
                    }
                } else {
                    Icon(getCategoryIcon(app.category), null, tint = getCategoryColor(app.category))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.packageName.substringAfterLast("."),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(getCategoryNameResId(app.category)),
                        style = MaterialTheme.typography.labelSmall,
                        color = getCategoryColor(app.category),
                        modifier = Modifier.clickable { onEditCategory() }
                    )
                    if (dailyLimitMs > 0) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Timer, null, modifier = Modifier.size(10.dp), tint = Success)
                        Text(
                            text = "Limit: ${dailyLimitMs / 60000}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = Success
                        )
                    }
                    if (hasActiveTimer) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Timer, null, modifier = Modifier.size(10.dp), tint = LiquidBlue)
                        Text(
                            text = formatCountdown(remainingMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = LiquidBlue
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                val isInternetBlocked = activeRules.any { it.packageName == app.packageName && it.isInternetBlocked }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("App", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        LiquidGlassSwitch(
                            checked = !isBlocked,
                            onCheckedChange = { onBlock() },
                            label = "",
                            modifier = Modifier.width(48.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Web", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        LiquidGlassSwitch(
                            checked = !isInternetBlocked,
                            onCheckedChange = { viewModel.toggleInternetBlock(device, app.packageName, discoveryViewModel) },
                            label = "",
                            modifier = Modifier.width(48.dp)
                        )
                    }
                }
            }
        }
        
        // Quick Action Row for Apps
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = if (hasActiveTimer) onCancelTimer else onSetTimer,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (hasActiveTimer) Error else LiquidBlue
                )
            ) {
                Icon(
                    imageVector = if (hasActiveTimer) Icons.Default.TimerOff else Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (hasActiveTimer) "Cancel Allowance" else "Allowance",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = onSetDailyLimit,
                colors = ButtonDefaults.textButtonColors(contentColor = Success)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Daily Limit",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AppListItem(
    app: AppUsageLog,
    isBlocked: Boolean,
    onBlock: () -> Unit,
    onEditCategory: () -> Unit,
    onSetTimer: () -> Unit,
    onCancelTimer: () -> Unit,
    iconBase64: String? = null,
    timerExpiration: Long = 0L
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    // Ticking effect for the countdown
    if (timerExpiration > currentTime) {
        LaunchedEffect(timerExpiration) {
            while (currentTime < timerExpiration) {
                delay(1000)
                currentTime = System.currentTimeMillis()
            }
        }
    }

    val remainingMs = (timerExpiration - currentTime).coerceAtLeast(0)
    val hasActiveTimer = remainingMs > 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(getCategoryColor(app.category).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconBase64 != null) {
                        val bitmap = remember(iconBase64) {
                            try {
                                val decodedString = android.util.Base64.decode(iconBase64, android.util.Base64.DEFAULT)
                                android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size).asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                             androidx.compose.foundation.Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = getCategoryIcon(app.category),
                                contentDescription = null,
                                tint = getCategoryColor(app.category),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = getCategoryIcon(app.category),
                            contentDescription = null,
                            tint = getCategoryColor(app.category),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.packageName.substringAfterLast("."),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatDuration(app.totalTimeInForeground),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        )
                        // Category Chip with Edit capability
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = getCategoryColor(app.category).copy(alpha = 0.1f),
                            modifier = Modifier.clickable { onEditCategory() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(getCategoryNameResId(app.category)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = getCategoryColor(app.category)
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Category",
                                    tint = getCategoryColor(app.category),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
                
                if (hasActiveTimer) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                         shape = MaterialTheme.shapes.extraSmall,
                         color = Primary
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = formatCountdown(remainingMs),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasActiveTimer) {
                    TextButton(
                        onClick = onCancelTimer,
                        colors = ButtonDefaults.textButtonColors(contentColor = Error)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TimerOff,
                            contentDescription = stringResource(R.string.action_cancel_timer),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.action_cancel_timer), style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    TextButton(onClick = onSetTimer) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = stringResource(R.string.action_set_timer),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.action_set_timer), style = MaterialTheme.typography.labelMedium)
                    }
                }
                
                Spacer(Modifier.width(8.dp))
                
                TextButton(
                    onClick = onBlock,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isBlocked) MaterialTheme.colorScheme.primary else Error
                    )
                ) {
                     Icon(
                        imageVector = if (isBlocked) Icons.Default.LockOpen else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isBlocked) stringResource(R.string.action_unblock) else stringResource(R.string.action_block), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

// ==================== LIMITS TAB ====================

@Composable
private fun LimitsTab(
    device: ChildDevice,
    viewModel: DeviceControlViewModel,
    usageLogs: List<AppUsageLog>,
    categoryTimers: Map<AppCategory, Long>
) {
    val categoryLimits by viewModel.categoryLimits.collectAsState()
    val usageLimitMs by viewModel.usageLimitMs.collectAsState()
    val breakDurationMs by viewModel.breakDurationMs.collectAsState()
    val isLocked by viewModel.isDeviceLocked.collectAsState()
    val lockReason by viewModel.lockReason.collectAsState()
    
    val isBreakActive = isLocked && lockReason == "BREAK"
    
    var showCategoryTimerDialog by remember { mutableStateOf<AppCategory?>(null) }
    
    if (showCategoryTimerDialog != null) {
        val cat = showCategoryTimerDialog!!
        SetCategoryTimerDialog(
            onDismiss = { showCategoryTimerDialog = null },
            onConfirm = { minutes ->
                viewModel.setCategoryTimer(device, cat, minutes)
                showCategoryTimerDialog = null
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {


        item {
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = stringResource(R.string.title_category_limits),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.desc_category_limits),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        items(AppCategory.values().toList()) { category ->
            val appsInCategory = usageLogs.filter { it.category == category }
            val expiration = categoryTimers[category] ?: 0L
            CategoryLimitCard(
                category = category,
                currentLimit = categoryLimits.find { it.category == category }?.maxDailyTimeMs ?: 0,
                apps = appsInCategory,
                onLimitChanged = { minutes ->
                    viewModel.setCategoryLimit(device, category, minutes)
                },
                timerExpiration = expiration,
                onSetTimer = { showCategoryTimerDialog = category },
                onCancelTimer = { viewModel.cancelCategoryTimer(device, category) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryLimitCard(
    category: AppCategory,
    currentLimit: Long,
    apps: List<AppUsageLog>,
    onLimitChanged: (Int) -> Unit,
    timerExpiration: Long = 0L,
    onSetTimer: () -> Unit = {},
    onCancelTimer: () -> Unit = {}
) {
    var sliderValue by remember { mutableFloatStateOf((currentLimit / 60000f).coerceIn(0f, 480f)) }
    var expanded by remember { mutableStateOf(false) }
    
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    // Ticking effect for the countdown
    if (timerExpiration > currentTime) {
        LaunchedEffect(timerExpiration) {
            while (currentTime < timerExpiration) {
                delay(1000)
                currentTime = System.currentTimeMillis()
            }
        }
    }

    val remainingMs = (timerExpiration - currentTime).coerceAtLeast(0)
    val hasActiveTimer = remainingMs > 0
    
    // Update slider if external limit changes (e.g. initial load)
    LaunchedEffect(currentLimit) {
        sliderValue = (currentLimit / 60000f).coerceIn(0f, 480f)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                 Icon(
                    imageVector = getCategoryIcon(category),
                    contentDescription = null,
                    tint = getCategoryColor(category),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(getCategoryNameResId(category)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (sliderValue > 0) stringResource(R.string.label_limit_mins, sliderValue.toInt()) else stringResource(R.string.label_no_limit),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (hasActiveTimer) {
                    Surface(
                         shape = MaterialTheme.shapes.extraSmall,
                         color = Primary,
                         modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = formatCountdown(remainingMs),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Apps List
                    if (apps.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.label_apps_in_category),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            apps.take(5).forEach { app ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = app.packageName.substringAfterLast("."),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            if (apps.size > 5) {
                                Text(
                                    text = stringResource(R.string.label_more_apps, apps.size - 5),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                
                    // Preset buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(30, 60, 120, 0).forEach { minutes ->
                            FilterChip(
                                selected = sliderValue.toInt() == minutes,
                                onClick = { 
                                    sliderValue = minutes.toFloat()
                                    onLimitChanged(minutes)
                                },
                                label = { 
                                    Text(
                                        if (minutes == 0) stringResource(R.string.label_none) 
                                        else if (minutes < 60) "${minutes}m" 
                                        else "${minutes / 60}h"
                                    ) 
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Slider
                    Text(
                        text = stringResource(R.string.label_daily_limit, sliderValue.toInt()),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = { onLimitChanged(sliderValue.toInt()) },
                        valueRange = 0f..480f,
                        steps = 31, // (480/15) - 1 => 15 min increments
                        colors = SliderDefaults.colors(
                            thumbColor = Primary,
                            activeTrackColor = Primary
                        )
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Timer Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasActiveTimer) {
                            TextButton(
                                onClick = onCancelTimer,
                                colors = ButtonDefaults.textButtonColors(contentColor = Error)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TimerOff,
                                    contentDescription = stringResource(R.string.action_cancel_timer),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.action_cancel_timer), style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            TextButton(onClick = onSetTimer) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = stringResource(R.string.action_set_timer),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.action_set_timer), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== REPORTS TAB ====================

@Composable
private fun ReportsTab(
    device: ChildDevice,
    viewModel: DeviceControlViewModel,
    dailyReport: com.parentalguard.common.model.DailyUsageReport?
) {
    if (dailyReport == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Primary)
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.status_loading_report))
            }
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Card
        item {
            PremiumCard(hasGradientAccent = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.daily_report),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = { 
                                    // Navigation to history
                                    // We need to pass the device to the history screen or use a shared state
                                    // But ParentApp.kt handles navigation.
                                    // For now, let's assume we have a callback or just use a generic route
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "View History",
                                    tint = Primary
                                )
                            }
                        }
                        Text(
                            text = dailyReport.date,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    UsageGauge(
                        progress = (dailyReport.totalScreenTimeMs / (8 * 60 * 60 * 1000f)).coerceIn(0f, 1f),
                        size = 100.dp,
                        valueText = formatDuration(dailyReport.totalScreenTimeMs),
                        label = stringResource(R.string.label_total)
                    )
                }
            }
        }
        
        // Hourly Usage Chart
        item {
            PremiumCard {
                Text(
                    text = stringResource(R.string.title_hourly_timeline),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.desc_hourly_timeline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(16.dp))
                
                if (dailyReport.hourlyBreakdown.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.label_no_hourly_data),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Show Every hour if data exists, BarChart will handle spacing
                    BarChart(
                        data = dailyReport.hourlyBreakdown
                            .map { usage ->
                                ChartData(
                                    label = "${usage.hour}h",
                                    value = usage.usageTimeMs.toFloat(),
                                    color = if (usage.usageTimeMs > 30 * 60 * 1000L) Primary else Secondary,
                                    valueLabel = formatDuration(usage.usageTimeMs)
                                )
                            },
                        maxHeight = 150.dp, // Increased height for better visibility
                        barWidth = 12.dp,
                        spacing = 6.dp,
                        labelStep = 4
                    )
                }
            }
        }
        
        // Category Usage Chart
        item {
            PremiumCard {
                Text(
                    text = stringResource(R.string.title_usage_by_category),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DonutChart(
                        data = dailyReport.categoryUsages.map { usage ->
                            ChartData(
                                label = stringResource(getCategoryNameResId(usage.category)),
                                value = usage.totalTimeMs.toFloat(),
                                color = getCategoryColor(usage.category)
                            )
                        },
                        size = 120.dp,
                        strokeWidth = 24.dp
                    )
                    
                    ChartLegend(
                        data = dailyReport.categoryUsages
                            .sortedByDescending { it.totalTimeMs }
                            .map { usage ->
                            ChartData(
                                label = stringResource(getCategoryNameResId(usage.category)),
                                value = usage.totalTimeMs.toFloat(),
                                color = getCategoryColor(usage.category),
                                valueLabel = formatDuration(usage.totalTimeMs)
                            )
                        }
                    )
                }
            }
        }
        
        // Most Used Apps
        item {
            PremiumCard {
                Text(
                    text = stringResource(R.string.most_used_apps),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(12.dp))
                
                HorizontalBarChart(
                    data = dailyReport.mostUsedApps.map { app ->
                        ChartData(
                            label = app.packageName.substringAfterLast("."),
                            value = app.totalTimeInForeground.toFloat(),
                            color = getCategoryColor(app.category),
                            valueLabel = formatDuration(app.totalTimeInForeground)
                        )
                    }
                )
            }
        }
        
        // Refresh
        item {
            OutlinedButton(
                onClick = { viewModel.fetchDailyReport(device) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_refresh_report))
            }
        }
    }
}

// ==================== HELPER FUNCTIONS ====================

private fun formatDuration(ms: Long): String {
    val hours = ms / (1000 * 60 * 60)
    val minutes = (ms % (1000 * 60 * 60)) / (1000 * 60)
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

private fun formatCountdown(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

private fun getCategoryNameResId(category: AppCategory): Int {
    return when (category) {
        AppCategory.SOCIAL -> R.string.category_social
        AppCategory.GAMES -> R.string.category_games
        AppCategory.EDUCATION -> R.string.category_education
        AppCategory.PRODUCTIVITY -> R.string.category_productivity
        AppCategory.ENTERTAINMENT -> R.string.category_entertainment
        AppCategory.OTHER -> R.string.category_other
    }
}

private fun getCategoryIcon(category: AppCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        AppCategory.SOCIAL -> Icons.Default.People
        AppCategory.GAMES -> Icons.Default.SportsEsports
        AppCategory.EDUCATION -> Icons.Default.School
        AppCategory.PRODUCTIVITY -> Icons.Default.Work
        AppCategory.ENTERTAINMENT -> Icons.Default.Movie
        AppCategory.OTHER -> Icons.Default.Apps
    }
}

@Composable
fun SetCategoryTimerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    if (showCustomDialog) {
        CustomTimerDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { minutes ->
                onConfirm(minutes)
                showCustomDialog = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_category_timer_title)) },
        text = {
            Column {
                Text(stringResource(R.string.dialog_category_timer_desc))
                Spacer(Modifier.height(16.dp))
                
                val options = listOf(
                    15 to stringResource(R.string.label_15m),
                    30 to "30m",
                    60 to "1h",
                    120 to "2h"
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.chunked(2).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowOptions.forEach { (minutes, label) ->
                                OutlinedButton(
                                    onClick = { onConfirm(minutes); onDismiss() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { showCustomDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.dialog_timer_custom))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun BreakTab(
    device: ChildDevice,
    viewModel: DeviceControlViewModel
) {
    val usageLimitMs by viewModel.usageLimitMs.collectAsState()
    val breakDurationMs by viewModel.breakDurationMs.collectAsState()
    val breakWarningMs by viewModel.breakWarningMs.collectAsState()
    val educationOnly by viewModel.educationOnly.collectAsState()
    val allowExtensions by viewModel.allowExtensions.collectAsState()
    val isLocked by viewModel.isDeviceLocked.collectAsState()
    val lockReason by viewModel.lockReason.collectAsState()
    
    val isBreakActive = isLocked && lockReason == "BREAK"

    // Local states
    var usageLimitMin by remember(usageLimitMs) { mutableStateOf((usageLimitMs / 60000).toInt().coerceIn(1, 120)) }
    var breakDurationMin by remember(breakDurationMs) { mutableStateOf((breakDurationMs / 60000).toInt().coerceIn(1, 60)) }
    var warningEnabled by remember(breakWarningMs) { mutableStateOf(breakWarningMs > 0) }
    var warningMin by remember(breakWarningMs) { mutableStateOf(if (breakWarningMs > 0) (breakWarningMs / 60000).toInt() else 2) }
    var eduOnly by remember(educationOnly) { mutableStateOf(educationOnly) }
    var extensionsAllowed by remember(allowExtensions) { mutableStateOf(allowExtensions) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Card
        item {
            LiquidGlassCard(
                backgroundColor = when {
                    isBreakActive -> LiquidPink.copy(alpha = 0.1f)
                    usageLimitMs > 0 -> Success.copy(alpha = 0.1f)
                    else -> LiquidBlue.copy(alpha = 0.1f)
                },
                borderColor = when {
                    isBreakActive -> LiquidPink.copy(alpha = 0.2f)
                    usageLimitMs > 0 -> Success.copy(alpha = 0.2f)
                    else -> LiquidBlue.copy(alpha = 0.2f)
                }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Health Breaks",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = when {
                                    isBreakActive -> "Status: ON BREAK"
                                    usageLimitMs > 0 -> "Status: Active & Monitoring"
                                    else -> "Status: Disabled"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isBreakActive -> LiquidPink
                                    usageLimitMs > 0 -> Success
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        
                        Icon(
                            imageVector = if (isBreakActive) Icons.Default.Snooze else if (usageLimitMs > 0) Icons.Default.Timer else Icons.Default.Coffee, 
                            contentDescription = null, 
                            tint = if (isBreakActive) LiquidPink else if (usageLimitMs > 0) Success else LiquidBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    if (isBreakActive) {
                        Text(
                            text = "Child device is currently locked for a break.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))
                        LiquidGlassButton(
                            text = "End Break Manually",
                            onClick = { viewModel.stopBreak(device) },
                            icon = Icons.Default.Cancel,
                            gradient = listOf(Error, LiquidPink),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (usageLimitMs > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Usage Limit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${usageLimitMin}m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Break Duration", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${breakDurationMin}m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Next Action", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Lock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LiquidPink)
                            }
                        }
                    } else {
                         Text(
                            text = "Schedule automatic breaks to protect your child's eyes and health.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Config Section
        item {
            LiquidGlassSection(title = "Auto-Trigger Rules") {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Break after $usageLimitMin min of use",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = usageLimitMin.toFloat(),
                        onValueChange = { usageLimitMin = it.toInt() },
                        valueRange = 5f..120f,
                        steps = 11,
                        colors = SliderDefaults.colors(thumbColor = LiquidBlue, activeTrackColor = LiquidBlue)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "Rest duration: $breakDurationMin min",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = breakDurationMin.toFloat(),
                        onValueChange = { breakDurationMin = it.toInt() },
                        valueRange = 1f..60f,
                        steps = 59,
                        colors = SliderDefaults.colors(thumbColor = LiquidTeal, activeTrackColor = LiquidTeal)
                    )
                }
            }
        }

        // Advanced Mode
        item {
            LiquidGlassSection(title = "Advanced Options") {
                LiquidGlassSwitch(
                    checked = warningEnabled,
                    onCheckedChange = { warningEnabled = it },
                    label = "Notify Before Break",
                    sublabel = "Remind child before lock",
                    icon = Icons.Default.NotificationsActive,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                if (warningEnabled) {
                     Column(modifier = Modifier.padding(start = 56.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)) {
                        Text(
                            text = "Warning: $warningMin min before",
                            style = MaterialTheme.typography.labelMedium,
                            color = LiquidBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = warningMin.toFloat(),
                            onValueChange = { warningMin = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = LiquidBlue, activeTrackColor = LiquidBlue)
                        )
                    }
                }
                
                LiquidGlassDivider()
                
                LiquidGlassSwitch(
                    checked = eduOnly,
                    onCheckedChange = { eduOnly = it },
                    label = "Learning Only",
                    sublabel = "Keep education apps open",
                    icon = Icons.Default.School,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                LiquidGlassDivider()
                
                LiquidGlassSwitch(
                    checked = extensionsAllowed,
                    onCheckedChange = { extensionsAllowed = it },
                    label = "Allow Extensions",
                    sublabel = "Child can request +5m",
                    icon = Icons.Default.AddAlarm,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                 if (usageLimitMs > 0) {
                     LiquidGlassButton(
                        text = "Turn Off",
                        onClick = {
                             viewModel.updateBreakRules(
                                device = device,
                                usageLimitMs = 0,
                                breakDurationMs = 0,
                                breakWarningMs = 0,
                                educationOnly = false,
                                allowExtensions = false
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        icon = Icons.Default.Delete,
                        gradient = listOf(Color.Gray, Color.DarkGray)
                    )
                 }
                
                LiquidGlassButton(
                    text = "Save Changes",
                    onClick = {
                        viewModel.updateBreakRules(
                            device = device,
                            usageLimitMs = usageLimitMin * 60000L,
                            breakDurationMs = breakDurationMin * 60000L,
                            breakWarningMs = if (warningEnabled) warningMin * 60000L else 0L,
                            educationOnly = eduOnly,
                            allowExtensions = extensionsAllowed
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    icon = Icons.Default.Save,
                    gradient = LiquidGradientPrimary
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun InfoStat(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
