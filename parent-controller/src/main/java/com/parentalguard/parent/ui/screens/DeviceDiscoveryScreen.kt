package com.parentalguard.parent.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parentalguard.parent.R
import com.parentalguard.parent.ui.components.*
import com.parentalguard.parent.ui.theme.*
import com.parentalguard.parent.viewmodel.ChildDevice
import com.parentalguard.parent.viewmodel.DeviceStatusSummary
import com.parentalguard.parent.viewmodel.DiscoveryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDiscoveryScreen(
    devices: List<ChildDevice>,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onDeviceSelected: (ChildDevice) -> Unit,
    onScanQR: () -> Unit,
    onResetAll: () -> Unit,
    viewModel: DiscoveryViewModel,
    modifier: Modifier = Modifier
) {
    val deviceStatuses by viewModel.deviceStatuses.collectAsState()
    
    LaunchedEffect(Unit) {
        onStartScan()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = PremiumGradient
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Stylized Top Bar
            DiscoveryTopBar(
                isScanning = isScanning,
                deviceCount = devices.size,
                onScanQR = onScanQR,
                onRefresh = { viewModel.refreshDevices() },
                onResetAll = onResetAll
            )
            
            // Content
            if (devices.isEmpty() && !isScanning) {
                EmptyDiscoveryState(
                    isScanning = false,
                    onScanQR = onScanQR,
                    onRetry = onStartScan
                )
            } else if (devices.isEmpty() && isScanning) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyDiscoveryState(
                        isScanning = true,
                        onScanQR = onScanQR,
                        onRetry = onStartScan
                    )
                }
            } else {
                DeviceList(
                    devices = devices,
                    deviceStatuses = deviceStatuses,
                    onDeviceSelected = onDeviceSelected,
                    isScanning = isScanning,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoveryTopBar(
    isScanning: Boolean,
    deviceCount: Int,
    onScanQR: () -> Unit,
    onRefresh: () -> Unit,
    onResetAll: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Devices?") },
            text = { Text("This will remove all saved devices and their data. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetAll()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Error)
                ) {
                    Text("Reset All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        color = LiquidCardBackground,
        shadowElevation = 0.dp
    ) {
        Column {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.title_discovery),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = if (isScanning) "Searching for devices..." else "$deviceCount devices linked",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isScanning) LiquidBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (deviceCount > 0) {
                        IconButton(onClick = { showResetDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, null, tint = Error.copy(alpha = 0.7f))
                        }
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, null, tint = LiquidBlue)
                    }
                    IconButton(onClick = onScanQR) {
                        Icon(Icons.Default.QrCodeScanner, null, tint = LiquidBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
            
            // Linear progress when scanning
            if (isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = LiquidBlue,
                    trackColor = Color.Transparent
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray.copy(alpha = 0.1f)))
            }
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<ChildDevice>,
    deviceStatuses: Map<String, DeviceStatusSummary>,
    onDeviceSelected: (ChildDevice) -> Unit,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isScanning) {
            item {
                LiquidGlassCard(
                    backgroundColor = LiquidBlue.copy(alpha = 0.05f),
                    padding = 12.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = LiquidBlue)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Looking for more devices...",
                            style = MaterialTheme.typography.labelMedium,
                            color = LiquidBlue
                        )
                    }
                }
            }
        }

        items(
            items = devices,
            key = { it.deviceId }
        ) { device ->
            val status = deviceStatuses[device.deviceId] ?: DeviceStatusSummary()
            DiscoveredDeviceCard(
                device = device,
                status = status,
                onClick = { onDeviceSelected(device) }
            )
        }
    }
}

@Composable
private fun DiscoveredDeviceCard(
    device: ChildDevice,
    status: DeviceStatusSummary,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        padding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device Icon with animated glow if online
            Box(contentAlignment = Alignment.Center) {
                if (status.isOnline) {
                    val infiniteTransition = rememberInfiniteTransition(label = "glow")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 0.5f,
                        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(LiquidBlue.copy(alpha = alpha))
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (status.isOnline) 
                                    listOf(LiquidBlue, LiquidTeal) 
                                else 
                                    listOf(Color.Gray.copy(alpha = 0.2f), Color.LightGray.copy(alpha = 0.2f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = if (status.isOnline) Color.White else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Status dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(if (status.isOnline) Success else Color.Gray)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.customName.ifBlank { "Mobile Device" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (status.isOnline) "Connected" else "Disconnected",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (status.isOnline) Success else Color.Gray
                    )
                    
                    if (status.isOnline) {
                        Icon(
                            imageVector = if (status.connectionType == com.parentalguard.parent.viewmodel.ConnectionType.LOCAL) 
                                Icons.Default.Wifi else Icons.Default.Cloud,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = LiquidBlue
                        )
                        Text(
                            text = if (status.connectionType == com.parentalguard.parent.viewmodel.ConnectionType.LOCAL) "Local" else "Remote",
                            style = MaterialTheme.typography.labelSmall,
                            color = LiquidBlue
                        )
                    }
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
        
        if (status.isOnline) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DeviceQuickStat(
                    icon = Icons.Default.BatteryChargingFull,
                    value = "${status.batteryLevel}%",
                    color = if (status.batteryLevel < 20) Error else Success
                )
                DeviceQuickStat(
                    icon = if (status.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    value = if (status.isLocked) "Locked" else "Active",
                    color = if (status.isLocked) Error else Success
                )
            }
        }
    }
}

@Composable
private fun DeviceQuickStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
        Spacer(Modifier.width(4.dp))
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun EmptyDiscoveryState(
    isScanning: Boolean,
    onScanQR: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RadarAnimation(
            isScanning = isScanning,
            modifier = Modifier.size(240.dp)
        )
        
        Spacer(Modifier.height(40.dp))
        
        Text(
            text = if (isScanning) "Searching for Devices" else "No Devices Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(12.dp))
        
        Text(
            text = if (isScanning)
                "Make sure your child's device is nearby and the app is open."
            else
                "We couldn't find any devices on your network. Try scanning the QR code on the child's app instead.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(40.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LiquidGlassButton(
                text = "Scan QR Code",
                onClick = onScanQR,
                icon = Icons.Default.QrCodeScanner,
                gradient = LiquidGradientPrimary,
                modifier = Modifier.fillMaxWidth()
            )
            
            LiquidGlassButton(
                text = if (isScanning) "Stop Search" else "Search Again",
                onClick = onRetry,
                icon = if (isScanning) Icons.Default.Stop else Icons.Default.Wifi,
                backgroundColor = Color.White,
                textColor = LiquidBlue,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RadarAnimation(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    
    val sweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )
    
    val pulses = listOf(
        infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2000, delayMillis = 0), RepeatMode.Restart), label = "p1"
        ),
        infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2000, delayMillis = 600), RepeatMode.Restart), label = "p2"
        ),
        infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2000, delayMillis = 1200), RepeatMode.Restart), label = "p3"
        )
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Radar Pulses
        if (isScanning) {
            pulses.forEach { pulse ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulse.value)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(LiquidBlue.copy(alpha = 0.2f * (1f - pulse.value)), Color.Transparent)
                            )
                        )
                )
            }
        }
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2
            
            // Concentric circles (Glass style)
            for (i in 1..4) {
                drawCircle(
                    color = LiquidBlue.copy(alpha = 0.05f * i),
                    radius = maxRadius * i / 4,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            
            // Sweep line
            if (isScanning) {
                val sweepRadians = Math.toRadians(sweep.toDouble()).toFloat()
                val endX = center.x + maxRadius * kotlin.math.cos(sweepRadians)
                val endY = center.y + maxRadius * kotlin.math.sin(sweepRadians)
                
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(LiquidBlue, LiquidTeal.copy(alpha = 0f)),
                        start = center,
                        end = Offset(endX, endY)
                    ),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
        
        // Center Core
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(LiquidBlue, LiquidTeal)
                    )
                )
                .shadow(10.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isScanning) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
