package com.parentalguard.parent.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parentalguard.parent.R
import com.parentalguard.parent.ui.components.LiquidGlassButton
import com.parentalguard.parent.ui.components.LiquidGlassCard
import com.parentalguard.parent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinLockScreen(
    onUnlocked: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = PremiumGradient
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background decorative circles
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-100).dp, y = (-200).dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 150.dp, y = 250.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )

        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            backgroundColor = Color.White.copy(alpha = 0.9f),
            padding = 32.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Lock Icon with Pulse
                Box(contentAlignment = Alignment.Center) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                        label = "scale"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(LiquidBlue.copy(alpha = 0.1f))
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(LiquidBlue, LiquidTeal))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "App Locked",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = LiquidBlue
                    )
                    Text(
                        text = "Enter your security PIN to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { 
                        if (it.length <= 6) {
                            pinInput = it
                            error = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("6-digit PIN", color = Color.Gray) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LiquidBlue,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f),
                        focusedContainerColor = Color.White.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.3f)
                    ),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        letterSpacing = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = Error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                val activity = context as? androidx.fragment.app.FragmentActivity
                val biometricsAvailable = remember { activity?.let { com.parentalguard.parent.security.BiometricHelper.isBiometricAvailable(it) } ?: false }

                LaunchedEffect(Unit) {
                    if (biometricsAvailable && activity != null) {
                        com.parentalguard.parent.security.BiometricHelper.showBiometricPrompt(
                            activity = activity,
                            onSuccess = { onUnlocked() },
                            onError = { /* Keep using PIN as fallback */ }
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LiquidGlassButton(
                        text = "Unlock",
                        onClick = {
                            val isValid = com.parentalguard.parent.security.PinManager.verifyPin(context, pinInput)
                            if (isValid) {
                                onUnlocked()
                            } else {
                                error = "Invalid PIN, please try again."
                                pinInput = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        icon = Icons.Default.LockOpen,
                        gradient = LiquidGradientPrimary
                    )

                    if (biometricsAvailable) {
                        LiquidGlassButton(
                            text = "Use Biometrics",
                            onClick = {
                                activity?.let {
                                    com.parentalguard.parent.security.BiometricHelper.showBiometricPrompt(
                                        activity = it,
                                        onSuccess = { onUnlocked() },
                                        onError = { msg -> error = msg }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            icon = Icons.Default.Fingerprint,
                            backgroundColor = Color.White,
                            textColor = LiquidBlue
                        )
                    }
                }
            }
        }
    }
}
