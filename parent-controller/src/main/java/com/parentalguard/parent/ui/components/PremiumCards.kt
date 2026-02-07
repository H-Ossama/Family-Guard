package com.parentalguard.parent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.parentalguard.parent.ui.theme.*

/**
 * Premium glassmorphism card with blur effect
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = GlassWhite,
    borderColor: Color = Color.White.copy(alpha = 0.3f),
    elevation: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
            )
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

/**
 * Premium elevated card with gradient accent
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    hasGradientAccent: Boolean = false,
    accentPosition: AccentPosition = AccentPosition.TOP,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    
    Column(
        modifier = modifier
            .shadow(8.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (hasGradientAccent && accentPosition == AccentPosition.TOP) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Primary, Secondary)
                        )
                    )
            )
        }
        
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
        
        if (hasGradientAccent && accentPosition == AccentPosition.BOTTOM) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Primary, Secondary)
                        )
                    )
            )
        }
    }
}

/**
 * Status card with colored left border
 */
@Composable
fun StatusCard(
    modifier: Modifier = Modifier,
    statusColor: Color = Success,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .shadow(4.dp, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(statusColor)
        )
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

enum class AccentPosition {
    TOP, BOTTOM
}

/**
 * Full-width device card for vertical scrolling lists
 */
@Composable
fun FullWidthDeviceCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector = androidx.compose.material.icons.Icons.Default.Smartphone,
    deviceName: String,
    statusText: String,
    isOnline: Boolean,
    batteryLevel: Int? = null,
    usageText: String? = null,
    isLocked: Boolean = false,
    connectionType: com.parentalguard.parent.viewmodel.ConnectionType = com.parentalguard.parent.viewmodel.ConnectionType.UNKNOWN
) {
    val shape = MaterialTheme.shapes.large
    
    Column(
        modifier = modifier
            .shadow(4.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                androidx.compose.material3.Text(
                    text = if (isOnline) statusText else "Offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOnline) Success else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isOnline) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = if (connectionType == com.parentalguard.parent.viewmodel.ConnectionType.LOCAL) 
                                androidx.compose.material.icons.Icons.Default.Wifi else 
                                androidx.compose.material.icons.Icons.Default.Cloud,
                            contentDescription = null,
                            tint = if (connectionType == com.parentalguard.parent.viewmodel.ConnectionType.LOCAL) Success else Info,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        androidx.compose.material3.Text(
                            text = if (connectionType == com.parentalguard.parent.viewmodel.ConnectionType.LOCAL) "Local Network" else "Server",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (connectionType == com.parentalguard.parent.viewmodel.ConnectionType.LOCAL) Success else Info
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    if (isOnline) {
                        if (isLocked) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                                contentDescription = null,
                                tint = Error,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        if (batteryLevel != null) {
                            androidx.compose.material3.Text(
                                text = "$batteryLevel%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (usageText != null) {
                    androidx.compose.material3.Text(
                        text = usageText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = Primary
                    )
                }
            }
            
            Spacer(Modifier.width(8.dp))
            
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Compact stat chip for horizontal dashboard overview
 */
@Composable
fun CompactStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = Primary
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                androidx.compose.material3.Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                androidx.compose.material3.Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
