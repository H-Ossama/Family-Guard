package com.parentalguard.child.ui.screens

import android.graphics.Bitmap
import androidx.compose.ui.res.stringResource
import com.parentalguard.child.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.parentalguard.child.ui.components.*
import com.parentalguard.child.ui.theme.*

@Composable
fun MainScreen(
    connectionString: String,
    status: String,
    qrBitmap: Bitmap?,
    deviceName: String,
    onRequestUnlock: () -> Unit,
    onHideIcon: () -> Unit,
    onRenameDevice: () -> Unit
) {
    var showFakeSettings by remember { mutableStateOf(true) }
    var clickCount by remember { mutableStateOf(0) }

    if (showFakeSettings) {
        FakeSystemSettings(
            onTitleClick = {
                clickCount++
                if (clickCount >= 7) {
                    showFakeSettings = false
                }
            }
        )
    } else {
        GradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.child_agent_title),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                        Text(
                            text = deviceName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { 
                                // Secret way to go back to fake mode if needed
                                showFakeSettings = true
                                clickCount = 0
                            }
                        )
                    }
                    
                    IconButton(
                        onClick = onHideIcon,
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Hide Icon",
                            tint = Color.White
                        )
                    }
                }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // System Status Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "حالة التشغيل",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (status.contains("Unavailable", true)) "غير متصل" else status,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    
                    val isConnected = !status.contains("Unavailable", true) && !status.contains("No Network", true)
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = if (isConnected) OnlineGreen else OfflineRed,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .shadow(
                                elevation = 8.dp,
                                shape = androidx.compose.foundation.shape.CircleShape,
                                ambientColor = if (isConnected) OnlineGreen else OfflineRed,
                                spotColor = if (isConnected) OnlineGreen else OfflineRed
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pairing Code and QR Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.12f),
                elevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "كود الربط مع الوالد",
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = connectionString,
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                        color = AccentGold
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp)),
                        color = Color.White
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                CircularProgressIndicator(color = Primary)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(R.string.scan_instruction_child),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GradientButton(
                    text = "تبديل الإسم",
                    onClick = onRenameDevice,
                    icon = Icons.Default.Edit,
                    modifier = Modifier.weight(1f)
                )
                
                GradientButton(
                    text = "طلب فتح",
                    onClick = onRequestUnlock,
                    icon = Icons.Default.LockOpen,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Secure Guard Service v1.0.4",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}
}
