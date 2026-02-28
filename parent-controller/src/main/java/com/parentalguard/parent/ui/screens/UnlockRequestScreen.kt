package com.parentalguard.parent.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parentalguard.parent.R
import com.parentalguard.parent.ui.components.*
import com.parentalguard.parent.ui.theme.*
import com.parentalguard.parent.viewmodel.ChildDevice
import com.parentalguard.parent.viewmodel.DeviceControlViewModel
import com.parentalguard.parent.viewmodel.DiscoveryViewModel
import java.net.InetAddress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockRequestScreen(
    deviceId: String,
    deviceName: String,
    requestType: String,
    appPackageName: String? = null,
    appName: String? = null,
    viewModel: DeviceControlViewModel,
    discoveryViewModel: DiscoveryViewModel? = null,
    onBack: () -> Unit
) {
    val device = remember(deviceId) {
        ChildDevice(
            deviceId = deviceId,
            name = deviceName,
            ip = InetAddress.getByName(deviceId),
            port = 8080,
            customName = deviceName
        )
    }

    val statusMessage by viewModel.statusMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showCustomTimerDialog by remember { mutableStateOf(false) }
    var customMinutes by remember { mutableStateOf("") }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            if (it.contains("error", ignoreCase = true) || it.contains("failed", ignoreCase = true)) {
                 snackbarHostState.showSnackbar(it)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        LiquidBlue.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            stringResource(R.string.title_unlock_request), 
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(20.dp), tint = LiquidBlue)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Large Icon with Pulse
                Box(contentAlignment = Alignment.Center) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                        label = "scale"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(LiquidBlue.copy(alpha = 0.1f))
                    )
                    
                    LiquidGlassCard(
                        modifier = Modifier.size(120.dp),
                        backgroundColor = Color.White.copy(alpha = 0.5f),
                        padding = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = if (requestType == "APP") Icons.Default.Apps else Icons.Default.PhonelinkLock,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = LiquidBlue
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = deviceName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = when(requestType) {
                            "EXTENSION" -> "is asking for more time"
                            "APP" -> "wants to use $appName"
                            else -> "is asking to unlock device"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (requestType == "EXTENSION") {
                    Spacer(Modifier.height(16.dp))
                    LiquidGlassButton(
                        text = "Approve (+1 Minute)",
                        onClick = { 
                            viewModel.approveExtension(device)
                            onBack()
                        },
                        icon = Icons.Default.Add,
                        gradient = LiquidGradientPrimary,
                        modifier = Modifier.fillMaxWidth().height(64.dp)
                    )
                    
                    LiquidGlassButton(
                        text = "Deny Request",
                        onClick = { 
                            viewModel.denyExtension(device)
                            onBack()
                        },
                        icon = Icons.Default.Close,
                        backgroundColor = Color.White,
                        textColor = Error,
                        modifier = Modifier.fillMaxWidth().height(64.dp)
                    )
                } else {
                    LiquidGlassSection(title = "Grant Temporary Access") {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LiquidTimerChip(
                                label = "10m",
                                modifier = Modifier.weight(1f),
                                onClick = { 
                                    viewModel.approveUnlockRequest(device, 10, appPackageName, discoveryViewModel)
                                    onBack()
                                }
                            )
                            LiquidTimerChip(
                                label = "30m",
                                modifier = Modifier.weight(1f),
                                onClick = { 
                                    viewModel.approveUnlockRequest(device, 30, appPackageName, discoveryViewModel)
                                    onBack()
                                }
                            )
                            LiquidTimerChip(
                                label = "Other",
                                modifier = Modifier.weight(1f),
                                onClick = { showCustomTimerDialog = true }
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LiquidGlassButton(
                            text = if (requestType == "APP") "Unlock Permantly" else "Unlock Device",
                            onClick = { 
                                if (requestType == "APP" && appPackageName != null) {
                                    viewModel.toggleAppBlock(device, appPackageName, discoveryViewModel)
                                } else {
                                    viewModel.lockDevice(device, false, discoveryViewModel)
                                }
                                onBack()
                            },
                            icon = Icons.Default.LockOpen,
                            gradient = LiquidGradientPrimary,
                            modifier = Modifier.fillMaxWidth().height(60.dp)
                        )
                        
                        LiquidGlassButton(
                            text = "Deny Request",
                            onClick = { 
                                viewModel.denyUnlockRequest(device)
                                onBack()
                            },
                            icon = Icons.Default.Block,
                            backgroundColor = Color.White,
                            textColor = Error,
                            modifier = Modifier.fillMaxWidth().height(60.dp)
                        )
                    }
                }
            }
        }
    }

    if (showCustomTimerDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTimerDialog = false },
            title = { Text("Custom Duration", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customMinutes,
                    onValueChange = { if (it.all { char -> char.isDigit() }) customMinutes = it },
                    label = { Text("Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val mins = customMinutes.toIntOrNull() ?: 0
                        if (mins > 0) {
                            viewModel.approveUnlockRequest(device, mins, appPackageName, discoveryViewModel)
                            showCustomTimerDialog = false
                            onBack()
                        }
                    }
                ) {
                    Text("Approve", fontWeight = FontWeight.Bold, color = LiquidBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTimerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun LiquidTimerChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = modifier.clickable { onClick() },
        backgroundColor = Color.White.copy(alpha = 0.5f),
        padding = 12.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = LiquidBlue
            )
        }
    }
}
