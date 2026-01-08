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
import androidx.compose.ui.unit.dp
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
    onScanQR: () -> Unit,
    onLockAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalScreenTimeMs = devices.sumOf { deviceStatuses[it.ip.hostAddress ?: ""]?.todayScreenTimeMs ?: 0L }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header with gradient
            item {
                 DashboardHeader(
                    deviceCount = devices.size
                )
            }
            
            // Quick Stats Card
            item {
                QuickStatsCard(
                    devices = devices,
                    totalScreenTimeMs = totalScreenTimeMs,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Connected Devices Section
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader(
                    title = stringResource(R.string.section_connected_devices),
                    actionText = if (devices.isEmpty()) null else stringResource(R.string.view_all),
                    onAction = { }
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
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(devices) { device ->
                            val status = deviceStatuses[device.ip.hostAddress ?: ""] ?: DeviceStatusSummary()
                            DeviceCard(
                                device = device,
                                status = status,
                                onClick = { onDeviceClick(device) }
                            )
                        }
                    }
                }
            }
            
            // Quick Actions Section
            item {
                Spacer(Modifier.height(24.dp))
                Spacer(Modifier.height(24.dp))
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
            }
        }
        
        // FAB
        GradientFAB(
            icon = Icons.Default.QrCodeScanner,
            onClick = onScanQR,
            contentDescription = stringResource(R.string.scan_qr),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun DashboardHeader(
    deviceCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Primary, Secondary)
                )
            )
            .padding(top = 16.dp, bottom = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.welcome_back),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Profile/Settings icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Status summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusChip(
                    icon = Icons.Default.Devices,
                    label = "$deviceCount",
                    sublabel = stringResource(R.string.status_devices),
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    icon = Icons.Default.CheckCircle,
                    label = "$deviceCount",
                    sublabel = stringResource(R.string.status_online),
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    icon = Icons.Default.Shield,
                    label = stringResource(R.string.status_active),
                    sublabel = stringResource(R.string.status_protection),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    sublabel: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.15f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun QuickStatsCard(
    devices: List<ChildDevice>,
    totalScreenTimeMs: Long,
    modifier: Modifier = Modifier
) {
    PremiumCard(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = (-16).dp),
        hasGradientAccent = false
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                value = devices.size.toString(),
                label = stringResource(R.string.stat_total_devices),
                icon = Icons.Default.Smartphone
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            StatItem(
                value = "0",
                label = stringResource(R.string.stat_blocked_today),
                icon = Icons.Default.Block
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            StatItem(
                value = formatDuration(totalScreenTimeMs),
                label = stringResource(R.string.stat_screen_time),
                icon = Icons.Default.AccessTime
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
private fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
            TextButton(onClick = onAction) {
                Text(
                    text = actionText,
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: ChildDevice,
    status: DeviceStatusSummary,
    onClick: () -> Unit
) {
    PremiumCard(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        hasGradientAccent = true
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(PrimaryLight.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                StatusDot(isOnline = status.isOnline)
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = device.customName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "${device.ip.hostAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (status.isOnline) stringResource(R.string.status_online) else stringResource(R.string.status_offline),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (status.isOnline) Success else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (status.isOnline) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (status.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (status.isLocked) Error else Success,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${status.batteryLevel}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
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
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DevicesOther,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.no_devices_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = stringResource(R.string.no_devices_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))
            
            GradientButton(
                text = stringResource(R.string.scan_qr),
                onClick = onScanQR,
                icon = Icons.Default.QrCodeScanner
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
            subtitle = stringResource(R.string.action_lock_all_desc),
            color = Error,
            onClick = onLockAll,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            icon = Icons.Default.QrCodeScanner,
            title = stringResource(R.string.action_add_device),
            subtitle = stringResource(R.string.action_add_device_desc),
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
    Card(
        modifier = modifier.clickable { onClick() },
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = InfoLight
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Info,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = stringResource(R.string.tip_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Info
                )
                Text(
                    text = stringResource(R.string.tip_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = Info.copy(alpha = 0.8f)
                )
            }
        }
    }
}
