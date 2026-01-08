package com.parentalguard.parent.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    viewModel: DiscoveryViewModel,
    modifier: Modifier = Modifier
) {
    val deviceStatuses by viewModel.deviceStatuses.collectAsState()
    LaunchedEffect(Unit) {
        onStartScan()
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        DiscoveryHeader(
            isScanning = isScanning,
            deviceCount = devices.size,
            onScanQR = onScanQR,
            onRefresh = { viewModel.refreshDevices() }
        )
        
        // Content
        if (devices.isEmpty()) {
            EmptyDiscoveryState(
                isScanning = isScanning,
                onScanQR = onScanQR,
                onRetry = onStartScan
            )
        } else {
            DeviceList(
                devices = devices,
                deviceStatuses = deviceStatuses,
                onDeviceSelected = onDeviceSelected,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DiscoveryHeader(
    isScanning: Boolean,
    deviceCount: Int,
    onScanQR: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Primary, Secondary)
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info column
            Column {
                Text(
                    text = stringResource(R.string.title_discovery),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isScanning) stringResource(R.string.status_scanning) else stringResource(R.string.status_devices_saved, deviceCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Refresh button
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh_stats),
                        tint = Color.White
                    )
                }
                
                // QR Scan button
                FilledTonalIconButton(
                    onClick = onScanQR,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = stringResource(R.string.scan_qr)
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Scanning indicator
        AnimatedVisibility(
            visible = isScanning,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.indicator_searching),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<ChildDevice>,
    deviceStatuses: Map<String, DeviceStatusSummary>,
    onDeviceSelected: (ChildDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = devices,
            key = { "${it.ip.hostAddress}:${it.port}" }
        ) { device ->
            val status = deviceStatuses[device.ip.hostAddress ?: ""] ?: DeviceStatusSummary()
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device icon with status
            Box {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Primary.copy(alpha = 0.1f), Secondary.copy(alpha = 0.1f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                // Online indicator
                StatusDot(
                    isOnline = status.isOnline,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Device info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.customName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (status.isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (status.isOnline) Success else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (status.isOnline) stringResource(R.string.status_online) else stringResource(R.string.status_offline),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status.isOnline) Success else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (status.isOnline) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (status.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = if (status.isLocked) Error else Success
                                )
                                Icon(
                                    imageVector = Icons.Default.BatteryChargingFull,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = if (status.batteryLevel < 20) Error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${status.batteryLevel}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Connect button
            FilledTonalButton(
                onClick = onClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Primary.copy(alpha = 0.1f),
                    contentColor = Primary
                )
            ) {
                Text(stringResource(R.string.connect))
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
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
        // Animated radar illustration
        RadarAnimation(
            isScanning = isScanning,
            modifier = Modifier.size(200.dp)
        )
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = if (isScanning) stringResource(R.string.empty_searching_title) else stringResource(R.string.empty_no_devices_found),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            text = if (isScanning)
                stringResource(R.string.empty_scanning_desc)
            else
                stringResource(R.string.empty_no_devices_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(32.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GradientOutlinedButton(
                text = stringResource(R.string.action_retry),
                onClick = onRetry,
                icon = Icons.Default.Refresh
            )
            GradientButton(
                text = stringResource(R.string.scan_qr),
                onClick = onScanQR,
                icon = Icons.Default.QrCodeScanner
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
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )
    
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )
    
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale1"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Radar circles
        if (isScanning) {
            Canvas(modifier = Modifier.fillMaxSize().scale(scale1)) {
                drawCircle(
                    color = Primary.copy(alpha = alpha1),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        
        // Static circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2
            
            // Draw concentric circles
            for (i in 1..3) {
                drawCircle(
                    color = Primary.copy(alpha = 0.1f),
                    radius = maxRadius * i / 3,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            
            // Draw sweep line if scanning
            if (isScanning) {
                val sweepRadians = Math.toRadians(sweep.toDouble()).toFloat()
                val endX = center.x + maxRadius * kotlin.math.cos(sweepRadians)
                val endY = center.y + maxRadius * kotlin.math.sin(sweepRadians)
                
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Primary, Primary.copy(alpha = 0f)),
                        start = center,
                        end = Offset(endX, endY)
                    ),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        
        // Center icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Primary, Secondary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isScanning) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
