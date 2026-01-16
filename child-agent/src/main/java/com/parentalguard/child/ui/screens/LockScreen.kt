package com.parentalguard.child.ui.screens

import androidx.compose.ui.res.stringResource
import com.parentalguard.child.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.parentalguard.child.data.RuleRepository
import com.parentalguard.child.ui.components.*
import com.parentalguard.child.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    onRequestUnlock: () -> Unit
) {
    val lockUntil by RuleRepository.globalLockUntil.collectAsState()
    
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            GlassCard(
                modifier = Modifier.size(120.dp),
                cornerRadius = 60.dp,
                elevation = 24.dp,
                backgroundColor = PremiumDanger.copy(alpha = 0.2f)
            ) {
                 Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                     Icon(
                         imageVector = Icons.Default.Lock,
                         contentDescription = null,
                         tint = Color.White,
                         modifier = Modifier.size(64.dp)
                     )
                 }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = stringResource(R.string.lock_screen_title),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (lockUntil > 0) {
                // Countdown Logic
                var timeRemaining by remember(lockUntil) { mutableStateOf("") }
                
                LaunchedEffect(lockUntil) {
                    while(true) {
                        val diff = lockUntil - System.currentTimeMillis()
                        if (diff <= 0) {
                            timeRemaining = "00:00:00"
                        } else {
                            val hours = diff / 3600000
                            val minutes = (diff % 3600000) / 60000
                            val seconds = (diff % 60000) / 1000
                            timeRemaining = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        }
                        delay(1000)
                    }
                }
                
                Text(
                    text = stringResource(R.string.lock_screen_timer_prefix, timeRemaining),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = stringResource(R.string.lock_screen_message),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            val lastRequestTime by RuleRepository.lastUnlockRequestTime.collectAsState()
            val cooldownMs = 15 * 60 * 1000L
            var cooldownRemaining by remember(lastRequestTime) { 
                mutableLongStateOf((lastRequestTime + cooldownMs - System.currentTimeMillis()).coerceAtLeast(0)) 
            }
            
            LaunchedEffect(lastRequestTime) {
                while (cooldownRemaining > 0) {
                    delay(1000)
                    cooldownRemaining = (lastRequestTime + cooldownMs - System.currentTimeMillis()).coerceAtLeast(0)
                }
            }
            
            val isCooldownActive = cooldownRemaining > 0
            
            GradientButton(
                text = if (isCooldownActive) {
                    val mins = cooldownRemaining / 60000
                    val secs = (cooldownRemaining % 60000) / 1000
                    stringResource(R.string.lock_screen_cooldown_btn, String.format("%02d:%02d", mins, secs))
                } else {
                    stringResource(R.string.lock_screen_request_btn)
                },
                onClick = {
                    RuleRepository.updateLastUnlockRequestTime()
                    onRequestUnlock()
                },
                enabled = !isCooldownActive,
                icon = if (isCooldownActive) Icons.Default.Lock else Icons.Default.LockOpen,
                modifier = Modifier.fillMaxWidth(),
                gradient = Brush.horizontalGradient(
                    colors = listOf(PremiumPrimary, PremiumPrimaryVariant)
                )
            )
        }
    }
}
