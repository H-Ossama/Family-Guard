package com.parentalguard.child.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.parentalguard.child.R
import com.parentalguard.child.data.RuleRepository
import com.parentalguard.child.ui.components.*
import com.parentalguard.child.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    onRequestUnlock: () -> Unit
) {
    val lockUntil by RuleRepository.globalLockUntil.collectAsState()
    val lockReason by RuleRepository.lockReason.collectAsState()
    val isBreak = lockReason == "BREAK"
    
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Animated Header Icon
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(32.dp, androidx.compose.foundation.shape.CircleShape, ambientColor = if(isBreak) Secondary else Primary, spotColor = if(isBreak) Secondary else Primary)
                        .background(
                            Brush.radialGradient(
                                colors = if (isBreak) listOf(Secondary.copy(0.3f), Color.Transparent) 
                                         else listOf(Primary.copy(0.3f), Color.Transparent)
                            )
                        )
                )
                
                GlassCard(
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 80.dp,
                    elevation = 0.dp,
                    backgroundColor = Color.White.copy(alpha = 0.12f),
                    borderColor = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isBreak) Icons.Default.Warning else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = if (isBreak) stringResource(R.string.take_a_break_title) else stringResource(R.string.lock_screen_title),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).dp.toSp()
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val breakDurationMinutes = remember(lockUntil) {
                val diff = lockUntil - System.currentTimeMillis()
                (diff / 60000).coerceAtLeast(1)
            }

            Text(
                text = if (isBreak) 
                    stringResource(R.string.take_a_break_message, breakDurationMinutes)
                    else stringResource(R.string.lock_screen_message),
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.dp.toSp(),
                    letterSpacing = 0.25.dp.toSp()
                ),
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            if (lockUntil > 0) {
                Spacer(modifier = Modifier.height(40.dp))
                
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.Black.copy(alpha = 0.2f),
                    borderColor = Color.White.copy(alpha = 0.1f),
                    cornerRadius = 28.dp,
                    elevation = 12.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        var timeRemaining by remember(lockUntil) { mutableStateOf("00:00:00") }
                        
                        LaunchedEffect(lockUntil) {
                            while(true) {
                                val diff = lockUntil - System.currentTimeMillis()
                                if (diff <= 0) {
                                    timeRemaining = "00:00:00"
                                    break
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
                            text = if (isBreak) "FREE IN" else "LOCKED FOR",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.dp.toSp()
                            ),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        
                        Spacer(Modifier.height(4.dp))
                        
                        Text(
                            text = timeRemaining,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.dp.toSp()
                            ),
                            color = AccentGold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1.2f))
            
            val lastRequestTime by RuleRepository.lastUnlockRequestTime.collectAsState()
            val allowExtensions by RuleRepository.allowExtensions.collectAsState()
            val educationOnly by RuleRepository.educationOnly.collectAsState()
            
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
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isBreak && allowExtensions) {
                    GradientOutlinedButton(
                        text = stringResource(R.string.btn_one_more_minute),
                        onClick = {
                            RuleRepository.updateLastUnlockRequestTime()
                            com.parentalguard.child.utils.EventHelper.sendExtensionRequest(com.parentalguard.child.ChildApp.instance)
                        },
                        // reuse cooldown if it's the same request type, but usually extensions are once per break
                        enabled = !isCooldownActive,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    )
                }

                if (isBreak && educationOnly) {
                    GradientButton(
                        text = stringResource(R.string.btn_learning_mode),
                        onClick = {
                            // The monitor service will allow EDUCATION apps.
                            // Tapping this just hides the overlay so the user can go use them.
                            onRequestUnlock() // We reuse the callback to hide the overlay
                        },
                        icon = Icons.Default.LockOpen,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    )
                }

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
