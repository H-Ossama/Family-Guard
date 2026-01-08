package com.parentalguard.parent.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.parentalguard.parent.ui.theme.*

/**
 * Premium animated gradient background with subtle animation
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )
    
    val gradientBrush = if (animated) {
        Brush.linearGradient(
            colors = listOf(
                GradientStart,
                GradientMiddle,
                GradientEnd,
                GradientMiddle
            ),
            start = Offset(animatedOffset * 1000f, 0f),
            end = Offset(1000f + animatedOffset * 500f, 1500f)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(GradientStart, GradientMiddle, GradientEnd)
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
    ) {
        content()
    }
}

/**
 * Static gradient overlay for cards
 */
@Composable
fun CardGradientOverlay(
    modifier: Modifier = Modifier,
    alpha: Float = 0.1f
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = alpha),
                        Color.Transparent
                    )
                )
            )
    )
}

/**
 * Gradient header section for screens
 */
@Composable
fun GradientHeader(
    modifier: Modifier = Modifier,
    height: Int = 200,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Primary, Secondary)
                )
            )
    ) {
        content()
    }
}
