package com.parentalguard.child.ui.screens

import androidx.compose.ui.res.stringResource
import com.parentalguard.child.R
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
    NeumorphicBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Progress Indicators (Neumorphic pills)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(4) { index ->
                    val isActive = index == currentStep
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .height(10.dp)
                            .width(if (isActive) 30.dp else 10.dp)
                            .neumorphic(
                                shape = RoundedCornerShape(8.dp),
                                backgroundColor = if (isActive) NeumorphicPrimary else NeumorphicSurface,
                                elevation = if (isActive) 5.dp else 3.dp
                            )
                    )
                }
            }

            // Content tile
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 8.dp
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
                NeumorphicButton(
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
        NeumorphicIconTile(
            icon = Icons.Default.Security,
            tint = NeumorphicPrimary,
            size = 96.dp,
            iconSize = 44.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.onboarding_intro_title),
            style = MaterialTheme.typography.headlineMedium,
            color = NeumorphicOnSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.onboarding_intro_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = NeumorphicOnSurfaceMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionStep(
    title: String,
    description: String,
    icon: ImageVector,
    buttonText: String,
    onAction: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        NeumorphicIconTile(
            icon = icon,
            tint = NeumorphicPrimary,
            size = 96.dp,
            iconSize = 44.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = NeumorphicOnSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = NeumorphicOnSurfaceMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        NeumorphicButton(
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
        NeumorphicIconTile(
            icon = Icons.Default.CheckCircle,
            tint = NeumorphicSuccess,
            size = 96.dp,
            iconSize = 44.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.onboarding_done_title),
            style = MaterialTheme.typography.headlineMedium,
            color = NeumorphicOnSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.onboarding_done_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = NeumorphicOnSurfaceMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        NeumorphicButton(
            text = stringResource(R.string.onboarding_enter_btn),
            onClick = onFinish,
            icon = Icons.Default.Login,
            modifier = Modifier.fillMaxWidth()
        )
    }
}