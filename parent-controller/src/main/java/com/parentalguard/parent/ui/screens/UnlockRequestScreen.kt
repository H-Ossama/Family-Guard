package com.parentalguard.parent.ui.screens

import androidx.compose.ui.res.stringResource
import com.parentalguard.parent.R

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parentalguard.parent.viewmodel.ChildDevice
import com.parentalguard.parent.viewmodel.DeviceControlViewModel
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
    onBack: () -> Unit
) {
    val device = remember(deviceId) {
        ChildDevice(
            name = deviceName,
            ip = InetAddress.getByName(deviceId),
            port = 8080,
            customName = deviceName
        )
    }

    val statusMessage by viewModel.statusMessage.collectFlow()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showCustomTimerDialog by remember { mutableStateOf(false) }
    var customMinutes by remember { mutableStateOf("") }

    // Removed the automatic close on statusMessage to favor immediate closure on user action
    // but still keeping it as a fallback in case statusMessage is used for errors later.
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            if (it.contains("error", ignoreCase = true) || it.contains("failed", ignoreCase = true)) {
                 snackbarHostState.showSnackbar(it)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.title_unlock_request), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Device/App Icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(30.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (requestType == "APP") Icons.Default.Apps else Icons.Default.PhonelinkLock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Request Description
                Text(
                    text = deviceName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = stringResource(R.string.request_desc_part2),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (requestType == "APP") appName ?: stringResource(R.string.request_type_app) else stringResource(R.string.request_type_device),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Action Buttons
                Text(
                    text = stringResource(R.string.label_grant_access),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimerButton(
                        label = stringResource(R.string.label_10m),
                        modifier = Modifier.weight(1f),
                        onClick = { 
                            viewModel.approveUnlockRequest(device, 10, appPackageName)
                            onBack()
                        }
                    )
                    TimerButton(
                        label = stringResource(R.string.label_15m),
                        modifier = Modifier.weight(1f),
                        onClick = { 
                            viewModel.approveUnlockRequest(device, 15, appPackageName)
                            onBack()
                        }
                    )
                    TimerButton(
                        label = stringResource(R.string.label_custom),
                        modifier = Modifier.weight(1f),
                        onClick = { showCustomTimerDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { 
                        if (requestType == "APP" && appPackageName != null) {
                            viewModel.toggleAppBlock(device, appPackageName)
                        } else {
                            viewModel.lockDevice(device, false)
                        }
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.action_unlock), modifier = Modifier.padding(8.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { 
                        viewModel.denyUnlockRequest(device)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.action_deny_request), modifier = Modifier.padding(8.dp))
                }
            }
        }
    }

    if (showCustomTimerDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTimerDialog = false },
            title = { Text(stringResource(R.string.dialog_custom_duration_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.dialog_custom_duration_label))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { if (it.all { char -> char.isDigit() }) customMinutes = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.placeholder_duration_example)) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = customMinutes.toIntOrNull() ?: 0
                        if (mins > 0) {
                            viewModel.approveUnlockRequest(device, mins, appPackageName)
                            showCustomTimerDialog = false
                            onBack()
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_approve))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTimerDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun TimerButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Extension to collect StateFlow cleanly in Compose if not already available
@Composable
fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectFlow(): State<T> {
    return collectAsState()
}
