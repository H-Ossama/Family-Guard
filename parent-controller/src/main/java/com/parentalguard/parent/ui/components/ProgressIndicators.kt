package com.parentalguard.parent.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.parentalguard.parent.ui.theme.*

/**
 * Animated circular progress indicator with percentage text
 */
@Composable
fun CircularProgressCard(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    progressColor: Color = Primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    label: String = "",
    showPercentage: Boolean = true,
    animated: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animated) progress.coerceIn(0f, 1f) else progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "progress"
    )
    
    val gradientColors = listOf(progressColor, Secondary)
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            // Background circle
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = backgroundColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            
            // Progress arc with gradient
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    brush = Brush.sweepGradient(gradientColors),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            
            // Center text
            if (showPercentage) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        if (label.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Semi-circular gauge for usage display
 */
@Composable
fun UsageGauge(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 16.dp,
    label: String = "",
    valueText: String = ""
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "gauge"
    )
    
    // Color based on progress
    val progressColor = when {
        animatedProgress < 0.5f -> Success
        animatedProgress < 0.8f -> Warning
        else -> Error
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Background arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.2f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            
            // Progress arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = progressColor,
                    startAngle = 180f,
                    sweepAngle = animatedProgress * 180f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            
            // Value text at bottom center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                if (valueText.isNotEmpty()) {
                    Text(
                        text = valueText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Horizontal progress bar with gradient
 */
@Composable
fun GradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    animated: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (animated) tween(1000, easing = FastOutSlowInEasing) else snap(),
        label = "bar"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                color = backgroundColor,
                shape = IndicatorShape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(
                    brush = Brush.horizontalGradient(listOf(Primary, Secondary)),
                    shape = IndicatorShape
                )
        )
    }
}

/**
 * Animated pulsing dot for status indicators
 */
@Composable
fun StatusDot(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val color = if (isOnline) OnlineGreen else OfflineRed
    
    Box(modifier = modifier) {
        // Outer glow (only when online)
        if (isOnline) {
            Canvas(modifier = Modifier.size(size * scale)) {
                drawCircle(
                    color = color.copy(alpha = 0.3f),
                    radius = this.size.minDimension / 2
                )
            }
        }
        
        // Inner dot
        Canvas(modifier = Modifier.size(size)) {
            drawCircle(
                color = color,
                radius = this.size.minDimension / 2
            )
        }
    }
}
