package com.parentalguard.parent.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.parentalguard.parent.R
import com.parentalguard.parent.ui.components.*
import com.parentalguard.parent.ui.theme.*
import com.parentalguard.parent.viewmodel.ChildDevice
import com.parentalguard.parent.viewmodel.DeviceStatusSummary


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    devices: List<ChildDevice>,
    deviceStatuses: Map<String, DeviceStatusSummary>,
    onDeviceClick: (ChildDevice) -> Unit,
    onViewAllDevices: () -> Unit,
    onScanQR: () -> Unit,
    onLockAll: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalScreenTimeMs = devices.sumOf { deviceStatuses[it.deviceId]?.todayScreenTimeMs ?: 0L }
    val onlineCount = devices.count { deviceStatuses[it.deviceId]?.isOnline == true }
    val deviceCount = devices.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = PremiumGradient)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header Section
            item {
                DashboardHeader(
                    onlineCount = onlineCount,
                    totalCount = deviceCount,
                    onRefresh = onRefresh
                )
            }
            
            // Metrics Section (Gauges)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-40).dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LiquidGlassCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = LiquidCardBackground
                    ) {
                        LiquidGlassGauge(
                            value = if (deviceCount > 0) onlineCount.toFloat() / deviceCount else 0f,
                            label = "Online",
                            size = 80.dp,
                            color = GlassSuccess,
                            subtext = "$onlineCount/$deviceCount"
                        )
                    }
                    
                    LiquidGlassCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = LiquidCardBackground
                    ) {
                        // Max 8 hours as goal
                        val goalMs = 8 * 60 * 60 * 1000L
                        LiquidGlassGauge(
                            value = (totalScreenTimeMs.toFloat() / goalMs).coerceIn(0f, 1f),
                            label = "Usage",
                            size = 80.dp,
                            color = GlassAccentPurple,
                            subtext = formatDuration(totalScreenTimeMs)
                        )
                    }
                }
            }
            
            // Devices Section
            item {
                SectionHeader(
                    title = stringResource(R.string.section_connected_devices),
                    actionText = if (devices.isNotEmpty()) stringResource(R.string.view_all) else null,
                    onAction = onViewAllDevices
                )
            }
            
            if (devices.isEmpty()) {
                item {
                    EmptyDevicesCard(
                        onScanQR = onScanQR,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(devices) { device ->
                    val status = deviceStatuses[device.deviceId] ?: DeviceStatusSummary()
                    LiquidDeviceCard(
                        device = device,
                        status = status,
                        onClick = { onDeviceClick(device) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
            
            // Quick Actions Section
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader(title = stringResource(R.string.section_quick_actions))
            }
            
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiquidGlassButton(
                        text = stringResource(R.string.action_lock_all),
                        onClick = onLockAll,
                        icon = Icons.Default.Lock,
                        gradient = listOf(GlassError, GlassAccentPink),
                        modifier = Modifier.weight(1f)
                    )
                    LiquidGlassButton(
                        text = "Add Device",
                        onClick = onScanQR,
                        icon = Icons.Default.Add,
                        gradient = LiquidGradientPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Tips Section
            item {
                Spacer(Modifier.height(32.dp))
                LiquidGlassCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    backgroundColor = GlassInfo.copy(alpha = 0.1f),
                    borderColor = GlassInfo.copy(alpha = 0.2f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = GlassInfo,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Pro Tip",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = GlassInfo
                            )
                            Text(
                                text = "Set up a bedtime schedule to automatically lock devices at night.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
        
        // FAB (Modernized)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            FloatingActionButton(
                onClick = onScanQR,
                containerColor = GlassPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    onlineCount: Int,
    totalCount: Int,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = LiquidGradientPrimary
                )
            )
            .padding(top = 48.dp, bottom = 80.dp) // Increased top padding for status bar
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Parental Guard",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(GlassSuccess)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "SYSTEM ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                }
                
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidDeviceCard(
    device: ChildDevice,
    status: DeviceStatusSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier.clickable { onClick() },
        padding = 12.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (status.isOnline) LiquidGradientPrimary else listOf(Color.Gray, Color.LightGray)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.customName.ifBlank { device.name },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (status.isOnline) "Online" else "Offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status.isOnline) Success else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (status.isOnline) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = if (status.connectionType == com.parentalguard.parent.viewmodel.ConnectionType.LOCAL) 
                                Icons.Default.Wifi else Icons.Default.Cloud,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                if (status.isOnline) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (status.isLocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Error,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            text = "${status.batteryLevel}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = formatDuration(status.todayScreenTimeMs),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = LiquidBlue
                )
            }
            
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalMinutes = ms / (1000 * 60)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionText != null && onAction != null) {
            Text(
                text = actionText,
                color = LiquidBlue,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

@Composable
private fun EmptyDevicesCard(
    onScanQR: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DevicesOther,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.no_devices_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = stringResource(R.string.no_devices_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(Modifier.height(24.dp))
            
            LiquidGlassButton(
                text = stringResource(R.string.scan_qr),
                onClick = onScanQR,
                icon = Icons.Default.QrCodeScanner,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
