package com.parentalguard.parent.ui.screens

import androidx.compose.animation.*
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
    modifier: Modifier = Modifier
) {
    val totalScreenTimeMs = devices.sumOf { deviceStatuses[it.deviceId]?.todayScreenTimeMs ?: 0L }
    val onlineCount = devices.count { deviceStatuses[it.deviceId]?.isOnline == true }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                DashboardHeader(
                    onlineCount = onlineCount,
                    totalCount = devices.size
                )
            }
            
            // Stats Overview
            item {
                StatsOverviewRow(
                    deviceCount = devices.size,
                    onlineCount = onlineCount,
                    totalScreenTimeMs = totalScreenTimeMs,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .offset(y = (-32).dp)
                        .zIndex(1f)
                )
            }
            
            // Connected Devices Section
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
                    FullWidthDeviceCard(
                        deviceName = device.customName.ifBlank { device.name },
                        statusText = if (status.isOnline) stringResource(R.string.status_online) else stringResource(R.string.status_offline),
                        isOnline = status.isOnline,
                        batteryLevel = status.batteryLevel,
                        usageText = formatDuration(status.todayScreenTimeMs),
                        isLocked = status.isLocked,
                        connectionType = status.connectionType,
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
                QuickActionsRow(
                    onLockAll = onLockAll,
                    onScanQR = onScanQR,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Tips Section
            item {
                Spacer(Modifier.height(24.dp))
                TipsCard(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(24.dp))
            }
        }
        
        // FAB
        GradientFAB(
            icon = Icons.Default.QrCodeScanner,
            onClick = onScanQR,
            contentDescription = stringResource(R.string.scan_qr),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )
    }
}

@Composable
private fun DashboardHeader(
    onlineCount: Int,
    totalCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PremiumPrimary, PremiumPrimaryVariant)
                )
            )
            .padding(top = 24.dp, bottom = 64.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Success.copy(alpha = 0.2f),
                            modifier = Modifier.size(8.dp).border(1.dp, Success, androidx.compose.foundation.shape.CircleShape)
                        ) {}
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.status_active).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Success,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                }
                
                // Active indicator
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "$onlineCount/$totalCount Devices Protected",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun StatsOverviewRow(
    deviceCount: Int,
    onlineCount: Int,
    totalScreenTimeMs: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CompactStatChip(
            icon = Icons.Default.Smartphone,
            value = "$deviceCount",
            label = "Devices",
            modifier = Modifier.weight(1f)
        )
        CompactStatChip(
            icon = Icons.Default.Wifi,
            value = "$onlineCount",
            label = "Online",
            color = Success,
            modifier = Modifier.weight(1f)
        )
        CompactStatChip(
            icon = Icons.Default.AccessTime,
            value = formatDuration(totalScreenTimeMs),
            label = "Usage",
            color = AccentOrange,
            modifier = Modifier.weight(1.3f)
        )
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
                color = Primary,
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
    PremiumCard(modifier = modifier.fillMaxWidth()) {
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
            
            GradientButton(
                text = stringResource(R.string.scan_qr),
                onClick = onScanQR,
                icon = Icons.Default.QrCodeScanner,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onLockAll: () -> Unit,
    onScanQR: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            icon = Icons.Default.Lock,
            title = stringResource(R.string.action_lock_all),
            subtitle = "Lock all devices",
            color = Error,
            onClick = onLockAll,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            icon = Icons.Default.Add,
            title = "Add Device",
            subtitle = "Scan child QR",
            color = Primary,
            onClick = onScanQR,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TipsCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = InfoLight.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Info.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TipsAndUpdates,
                contentDescription = null,
                tint = Info,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = stringResource(R.string.tip_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Info
                )
                Text(
                    text = stringResource(R.string.tip_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
