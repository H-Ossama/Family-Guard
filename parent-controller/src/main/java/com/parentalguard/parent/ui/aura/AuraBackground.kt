package com.parentalguard.parent.ui.aura

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.parentalguard.parent.ui.theme.AuraBgBase
import com.parentalguard.parent.ui.theme.AuraBgDeep
import com.parentalguard.parent.ui.theme.LocalAuraDark
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ============================================================================
// AURA · Living background. Three aurora orbs drift slowly behind the glass.
// Motion is transform-only (paint positions) — no layout work per frame.
// ============================================================================

private data class Orb(
    val color: Color,
    val radius: Float,        // fraction of min(width, height)
    val baseX: Float,
    val baseY: Float,
    val driftX: Float,
    val driftY: Float,
    val speed: Float,
    val phase: Float,
    val darkAlpha: Float,
    val lightAlpha: Float
)

@Composable
fun AuraBackground(modifier: Modifier = Modifier, content: (@Composable () -> Unit)? = null) {
    val isDark = LocalAuraDark.current
    val baseColor = MaterialTheme.colorScheme.background

    val clock by rememberInfiniteTransition(label = "aura-clock").animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura-phase"
    )

    val orbs = listOf(
        Orb(Aura.accent, 0.95f, 0.85f, -0.05f, 0.16f, 0.10f, 1.00f, 0.0f, 0.34f, 0.20f),
        Orb(Aura.accent2, 0.80f, -0.10f, 0.28f, 0.14f, 0.12f, 0.72f, 2.1f, 0.28f, 0.16f),
        Orb(Aura.accent3, 0.70f, 0.55f, 1.05f, 0.18f, 0.09f, 0.55f, 4.2f, 0.20f, 0.12f)
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(baseColor)
        ) {
            val minDim = size.minDimension
            // Deep vertical falloff so the base never reads flat
            drawRect(
                brush = Brush.verticalGradient(
                    colors = if (isDark) listOf(AuraBgDeep, AuraBgBase) else listOf(baseColor, baseColor)
                )
            )
            orbs.forEach { orb ->
                val cx = (orb.baseX + orb.driftX * sin(clock * orb.speed + orb.phase)) * size.width
                val cy = (orb.baseY + orb.driftY * cos(clock * orb.speed + orb.phase)) * size.height
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            orb.color.copy(alpha = if (isDark) orb.darkAlpha else orb.lightAlpha),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = orb.radius * minDim
                    ),
                    center = Offset(cx, cy),
                    radius = orb.radius * minDim
                )
            }
        }
        content?.invoke()
    }
}
