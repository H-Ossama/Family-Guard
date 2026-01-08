package com.parentalguard.child.ui.screens

import androidx.compose.ui.res.stringResource
import com.parentalguard.child.R
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.parentalguard.child.ui.components.*
import com.parentalguard.child.ui.theme.*

@Composable
fun OnboardingScreen(
    currentStep: Int,
    onNext: () -> Unit,
    onRequestUsageAccess: () -> Unit,
    onRequestOverlayAccess: () -> Unit,
    onFinish: () -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // Progress Indicators (Dots)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(4) { index ->
                    val isActive = index == currentStep
                    val width = if (isActive) 24.dp else 8.dp
                    val color = if (isActive) Color.White else Color.White.copy(alpha = 0.5f)
                    
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .height(8.dp)
                            .width(width)
                            .background(color, IndicatorShape)
                    )
                }
            }
            
            // Content
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 16.dp
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    label = "step_transition"
                ) { step ->
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (step) {
                            0 -> IntroStep()
                            1 -> PermissionStep(
                                title = stringResource(R.string.onboarding_usage_title),
                                description = stringResource(R.string.onboarding_usage_desc),
                                icon = Icons.Default.DataUsage,
                                buttonText = stringResource(R.string.onboarding_usage_btn),
                                onAction = onRequestUsageAccess
                            )
                            2 -> PermissionStep(
                                title = stringResource(R.string.onboarding_overlay_title),
                                description = stringResource(R.string.onboarding_overlay_desc),
                                icon = Icons.Default.Layers,
                                buttonText = stringResource(R.string.onboarding_overlay_btn),
                                onAction = onRequestOverlayAccess
                            )
                            3 -> DoneStep(onFinish)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (currentStep == 0) {
                GradientButton(
                    text = stringResource(R.string.onboarding_start_btn),
                    onClick = onNext,
                    icon = Icons.Default.ArrowForward,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun IntroStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.onboarding_intro_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.onboarding_intro_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionStep(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonText: String,
    onAction: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        GradientButton(
            text = buttonText,
            onClick = onAction,
            icon = Icons.Default.Settings,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DoneStep(onFinish: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Success,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.onboarding_done_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.onboarding_done_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        GradientButton(
            text = stringResource(R.string.onboarding_enter_btn),
            onClick = onFinish,
            icon = Icons.Default.Login,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
