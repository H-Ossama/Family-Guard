package com.parentalguard.parent.ui.screens

import androidx.compose.ui.res.stringResource
import com.parentalguard.parent.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                brush = Brush.verticalGradient(
                    colors = listOf(Primary, Secondary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(64.dp)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.title_app_locked),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        text = stringResource(R.string.desc_enter_pin),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    label = { Text(stringResource(R.string.label_pin)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Primary.copy(alpha = 0.5f)
                    )
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val isValid = com.parentalguard.parent.security.PinManager.verifyPin(context, pinInput)
                            if (isValid) {
                                onUnlocked()
                            } else {
                                error = context.getString(R.string.error_invalid_pin)
                                pinInput = ""
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(
                            text = stringResource(R.string.action_unlock),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (biometricsAvailable) {
                        FilledTonalIconButton(
                            onClick = {
                                activity?.let {
                                    com.parentalguard.parent.security.BiometricHelper.showBiometricPrompt(
                                        activity = it,
                                        onSuccess = { onUnlocked() },
                                        onError = { msg -> error = msg }
                                    )
                                }
                            },
                            modifier = Modifier.size(56.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = stringResource(R.string.action_unlock_biometric),
                                tint = Primary
                            )
                        }
                    }
                }
            }
        }
    }
}
