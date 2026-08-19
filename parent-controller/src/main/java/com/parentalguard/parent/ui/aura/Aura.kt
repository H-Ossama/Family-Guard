package com.parentalguard.parent.ui.aura

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.parentalguard.parent.ui.theme.AuroraCyan
import com.parentalguard.parent.ui.theme.AuroraIndigo
import com.parentalguard.parent.ui.theme.AuroraViolet
import com.parentalguard.parent.ui.theme.AuroraDanger
import com.parentalguard.parent.ui.theme.LocalAuraDark
import com.parentalguard.parent.ui.theme.PorcelainCyan
import com.parentalguard.parent.ui.theme.PorcelainIndigo
import com.parentalguard.parent.ui.theme.PorcelainViolet

// ============================================================================
// AURA · locked tokens. Every colour/brush/motion value in the UI layer
// resolves through this object — nothing improvises mid-render.
// ============================================================================

object Aura {

    // --- Motion ---
    val EaseEmphasized = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EaseOutSoft = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    fun <T> springSoft() = spring<T>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
    fun <T> springSnap() = spring<T>(dampingRatio = 0.72f, stiffness = 420f)
    fun <T> tweenFast() = tween<T>(durationMillis = 160, easing = EaseEmphasized)
    fun <T> tweenMed() = tween<T>(durationMillis = 320, easing = EaseEmphasized)
    fun <T> tweenSlow() = tween<T>(durationMillis = 640, easing = EaseOutSoft)

    const val StaggerMs = 45

    // --- Scheme-aware accent ---
    val accent: Color @Composable get() = if (LocalAuraDark.current) AuroraIndigo else PorcelainIndigo
    val accent2: Color @Composable get() = if (LocalAuraDark.current) AuroraViolet else PorcelainViolet
    val accent3: Color @Composable get() = if (LocalAuraDark.current) AuroraCyan else PorcelainCyan

    // --- Glass surfaces ---
    val glass: Color
        @Composable get() = if (LocalAuraDark.current) Color.White.copy(alpha = 0.045f)
        else Color.White.copy(alpha = 0.72f)

    val glassStrong: Color
        @Composable get() = if (LocalAuraDark.current) Color.White.copy(alpha = 0.075f)
        else Color.White.copy(alpha = 0.9f)

    val hairline: Color
        @Composable get() = if (LocalAuraDark.current) Color.White.copy(alpha = 0.09f)
        else Color(0xFF0B1220).copy(alpha = 0.08f)

    val hairlineStrong: Color
        @Composable get() = if (LocalAuraDark.current) Color.White.copy(alpha = 0.16f)
        else Color(0xFF0B1220).copy(alpha = 0.14f)

    val scrimSoft: Color
        @Composable get() = if (LocalAuraDark.current) Color.Black.copy(alpha = 0.25f)
        else Color(0xFF8A97AC).copy(alpha = 0.18f)

    // --- Brushes ---
    val hero: Brush
        @Composable get() = Brush.linearGradient(listOf(accent, accent2, accent3))

    val heroSoft: Brush
        @Composable get() = Brush.linearGradient(
            listOf(accent.copy(alpha = 0.22f), accent2.copy(alpha = 0.16f), accent3.copy(alpha = 0.12f))
        )

    val shield: Brush
        @Composable get() = Brush.linearGradient(listOf(AuroraDanger, Color(0xFFF43F5E)))

    val calm: Brush
        @Composable get() = Brush.linearGradient(listOf(Color(0xFF34D399), accent3))

    fun tint(color: Color): Brush = Brush.linearGradient(
        listOf(color, color.copy(alpha = 0.72f))
    )

    // --- Text ---
    val textHi: Color @Composable get() = MaterialTheme.colorScheme.onBackground
    val textMid: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val textDim: Color @Composable get() = MaterialTheme.colorScheme.outline
}

// ============================================================================
// Shared formatting — one voice for durations across the whole app.
// ============================================================================

fun formatAuraDuration(ms: Long): String {
    if (ms <= 0) return "0m"
    val totalMinutes = ms / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

fun formatAuraCountdown(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%d:%02d", minutes, seconds)
}
