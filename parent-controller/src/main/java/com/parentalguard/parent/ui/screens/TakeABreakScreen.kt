package com.parentalguard.parent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parentalguard.parent.R
import com.parentalguard.parent.ui.components.*
import com.parentalguard.parent.ui.theme.*
import com.parentalguard.parent.viewmodel.ChildDevice
import com.parentalguard.parent.viewmodel.DeviceControlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeABreakScreen(
    device: ChildDevice,
    viewModel: DeviceControlViewModel,
    onBack: () -> Unit
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

    Scaffold(
        topBar = {
            Surface(
                color = LiquidCardBackground
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Health Breaks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(20.dp), tint = LiquidBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(brush = PremiumGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (isBreakActive) {
                    LiquidGlassCard(
                        backgroundColor = LiquidPink.copy(alpha = 0.1f),
                        borderColor = LiquidPink.copy(alpha = 0.2f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(LiquidPink.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.HourglassBottom, null, tint = LiquidPink, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Break Active", fontWeight = FontWeight.Black, color = LiquidPink)
                                Text("The device is currently locked.", style = MaterialTheme.typography.labelSmall)
                            }
                            LiquidGlassButton(
                                text = "End",
                                onClick = { viewModel.stopBreak(device) },
                                modifier = Modifier.height(36.dp).width(80.dp),
                                gradient = listOf(Error, LiquidPink)
                            )
                        }
                    }
                }

                LiquidGlassSection(title = "Usage Threshold") {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Locked after $usageLimitMin min",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.Timer, null, tint = LiquidBlue, modifier = Modifier.size(20.dp))
                        }
                        Slider(
                            value = usageLimitMin.toFloat(),
                            onValueChange = { usageLimitMin = it.toInt() },
                            valueRange = 5f..120f,
                            steps = 22,
                            colors = SliderDefaults.colors(thumbColor = LiquidBlue, activeTrackColor = LiquidBlue)
                        )
                    }
                }

                LiquidGlassSection(title = "Rest Duration") {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Duration: $breakDurationMin min",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.Snooze, null, tint = LiquidTeal, modifier = Modifier.size(20.dp))
                        }
                        Slider(
                            value = breakDurationMin.toFloat(),
                            onValueChange = { breakDurationMin = it.toInt() },
                            valueRange = 1f..60f,
                            steps = 59,
                            colors = SliderDefaults.colors(thumbColor = LiquidTeal, activeTrackColor = LiquidTeal)
                        )
                    }
                }

                LiquidGlassSection(title = "Advanced Mode") {
                    LiquidGlassSwitch(
                        checked = warningEnabled,
                        onCheckedChange = { warningEnabled = it },
                        label = "Pre-break Warning",
                        sublabel = "Remind child before lock",
                        icon = Icons.Default.NotificationsActive,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    if (warningEnabled) {
                        Slider(
                            value = warningMin.toFloat(),
                            onValueChange = { warningMin = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 9,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            colors = SliderDefaults.colors(thumbColor = LiquidBlue, activeTrackColor = LiquidBlue)
                        )
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
                        label = "One More Minute",
                        sublabel = "Allow extension requests",
                        icon = Icons.Default.AddAlarm,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(Modifier.height(100.dp))
            }

            // Bottom Floating Save Button
            LiquidGlassCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                padding = 8.dp,
                backgroundColor = Color.White.copy(alpha = 0.8f)
            ) {
                LiquidGlassButton(
                    text = "Save All Settings",
                    onClick = {
                        viewModel.updateBreakRules(
                            device = device,
                            usageLimitMs = usageLimitMin * 60000L,
                            breakDurationMs = breakDurationMin * 60000L,
                            breakWarningMs = if (warningEnabled) warningMin * 60000L else 0L,
                            educationOnly = eduOnly,
                            allowExtensions = extensionsAllowed
                        )
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    icon = Icons.Default.Save,
                    gradient = LiquidGradientPrimary
                )
            }
        }
    }
}
