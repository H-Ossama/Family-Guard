package com.parentalguard.parent.ui.aura

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parentalguard.parent.ui.theme.MonoFontFamily

// ============================================================================
// AURA · data ink. Rings sweep, bars rise, arcs bloom — all transform-driven.
// ============================================================================

data class AuraDatum(val label: String, val value: Float, val color: Color, val valueLabel: String = "")

// --------------------------------------------------------------------------
// Ring — 270° gauge with animated sweep
// --------------------------------------------------------------------------

@Composable
fun AuraRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 148.dp,
    strokeWidth: Dp = 13.dp,
    brush: Brush = Aura.hero,
    trackColor: Color = Aura.glassStrong,
    startAngle: Float = 135f,
    sweepMax: Float = 270f,
    center: (@Composable () -> Unit)? = null
) {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animated.animateTo(progress.coerceIn(0f, 1f), tween(1000, easing = Aura.EaseOutSoft))
    }
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2f
            val arcSize = Size(this.size.width - strokeWidth.toPx(), this.size.height - strokeWidth.toPx())
            val topLeft = Offset(inset, inset)
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepMax,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
            drawArc(
                brush = brush,
                startAngle = startAngle,
                sweepAngle = sweepMax * animated.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
        }
        center?.invoke()
    }
}

// --------------------------------------------------------------------------
// Bars — rounded columns that rise from the baseline
// --------------------------------------------------------------------------

@Composable
fun AuraBars(
    data: List<AuraDatum>,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp,
    labelStep: Int = 4
) {
    val maxValue = (data.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
    val anim = remember { Animatable(0f) }
    LaunchedEffect(data) { anim.animateTo(1f, tween(900, easing = Aura.EaseOutSoft)) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().height(height),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { datum ->
                val fraction = (datum.value / maxValue).coerceIn(0.02f, 1f) * anim.value
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(height * fraction)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(Brush.verticalGradient(listOf(datum.color, datum.color.copy(alpha = 0.55f))))
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            data.forEachIndexed { index, datum ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (index % labelStep == 0) {
                        Text(
                            datum.label,
                            color = Aura.textDim,
                            fontFamily = MonoFontFamily,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// Donut — proportional arcs that bloom in
// --------------------------------------------------------------------------

@Composable
fun AuraDonut(
    data: List<AuraDatum>,
    modifier: Modifier = Modifier,
    size: Dp = 132.dp,
    strokeWidth: Dp = 22.dp
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
    val anim = remember { Animatable(0f) }
    LaunchedEffect(data) { anim.animateTo(1f, tween(1000, easing = Aura.EaseOutSoft)) }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            val inset = strokeWidth.toPx() / 2f
            val arcSize = Size(this.size.width - strokeWidth.toPx(), this.size.height - strokeWidth.toPx())
            var start = -90f
            data.forEach { datum ->
                val fullSweep = (datum.value / total) * 360f
                val sweep = fullSweep * anim.value
                if (sweep > 0f) {
                    drawArc(
                        color = datum.color,
                        startAngle = start,
                        sweepAngle = (sweep - 2f).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = stroke
                    )
                }
                start += fullSweep
            }
        }
    }
}

// --------------------------------------------------------------------------
// Usage row — label, track, value. The workhorse of every "top apps" list.
// --------------------------------------------------------------------------

@Composable
fun UsageBarRow(
    label: String,
    valueLabel: String,
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(800, easing = Aura.EaseOutSoft),
        label = "usage-row"
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                color = Aura.textHi,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Text(
                valueLabel,
                color = Aura.textMid,
                fontFamily = MonoFontFamily,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(7.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Aura.glassStrong, CircleShape)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(6.dp)
                    .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.65f))), CircleShape)
            )
        }
    }
}
