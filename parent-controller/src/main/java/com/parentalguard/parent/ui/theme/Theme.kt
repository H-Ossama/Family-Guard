package com.parentalguard.parent.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GlassPrimary,
    onPrimary = Color.White,
    primaryContainer = GlassPrimaryLight.copy(alpha = 0.2f),
    onPrimaryContainer = GlassPrimaryVariant,
    
    secondary = GlassSecondary,
    onSecondary = Color.White,
    secondaryContainer = GlassSecondaryLight.copy(alpha = 0.2f),
    onSecondaryContainer = GlassSecondaryVariant,
    
    tertiary = GlassAccentPurple,
    onTertiary = Color.White,
    tertiaryContainer = GlassAccentPurple.copy(alpha = 0.2f),
    onTertiaryContainer = Color(0xFF581C87), // Deep purple
    
    error = GlassError,
    onError = Color.White,
    errorContainer = GlassError.copy(alpha = 0.1f),
    onErrorContainer = GlassError,
    
    background = GlassBackgroundLight,
    onBackground = TextPrimaryLight,
    
    surface = GlassSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    
    outline = TextTertiaryLight,
    outlineVariant = Color(0xFFE2E8F0),
    
    inverseSurface = GlassSurfaceDark,
    inverseOnSurface = TextPrimaryDark,
    inversePrimary = GlassPrimaryLight,
    
    surfaceTint = GlassPrimary,
    scrim = Color.Black.copy(alpha = 0.6f)
)

private val DarkColorScheme = darkColorScheme(
    primary = GlassPrimaryLight,
    onPrimary = GlassPrimaryVariant,
    primaryContainer = GlassPrimary.copy(alpha = 0.3f),
    onPrimaryContainer = Color.White,
    
    secondary = GlassSecondary,
    onSecondary = GlassBackgroundDark,
    secondaryContainer = GlassSecondary.copy(alpha = 0.3f),
    onSecondaryContainer = Color.White,
    
    tertiary = GlassAccentPurple,
    onTertiary = Color.White,
    tertiaryContainer = GlassAccentPurple.copy(alpha = 0.3f),
    onTertiaryContainer = Color.White,
    
    error = GlassError,
    onError = Color.White,
    errorContainer = GlassError.copy(alpha = 0.2f),
    onErrorContainer = Color(0xFFFECACA),
    
    background = GlassBackgroundDark,
    onBackground = TextPrimaryDark,
    
    surface = GlassSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    
    outline = TextTertiaryDark,
    outlineVariant = Color(0xFF334155),
    
    inverseSurface = GlassSurfaceLight,
    inverseOnSurface = TextPrimaryLight,
    inversePrimary = GlassPrimary,
    
    surfaceTint = GlassPrimaryLight,
    scrim = Color.Black.copy(alpha = 0.8f)
)

// Premium gradient brush for backgrounds
val PremiumGradient = Brush.verticalGradient(
    colors = listOf(GradientPrimaryStart.copy(alpha = 0.15f), GlassBackgroundLight)
)

val PremiumDarkGradient = Brush.verticalGradient(
    colors = listOf(GlassBackgroundDark, Color(0xFF1E1B4B)) // To deep indigo
)

val PremiumHorizontalGradient = Brush.horizontalGradient(
    colors = listOf(GradientPrimaryStart, GradientPrimaryEnd)
)

val CardGradient = Brush.linearGradient(
    colors = listOf(
        GlassPrimary.copy(alpha = 0.9f),
        GlassAccentPurple.copy(alpha = 0.9f)
    )
)

@Composable
fun ParentalGuardTheme(
    themeMode: ThemePreferences.ThemeMode? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDarkTheme = isSystemInDarkTheme()
    
    // Determine if dark theme should be used
    val darkTheme = when (themeMode ?: ThemePreferences.getThemeMode(context)) {
        ThemePreferences.ThemeMode.LIGHT -> false
        ThemePreferences.ThemeMode.DARK -> true
        ThemePreferences.ThemeMode.SYSTEM -> systemInDarkTheme
    }
    
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activityContext = view.context
            if (activityContext is Activity) {
                val window = activityContext.window
                window.statusBarColor = Color.Transparent.toArgb() // Transparent for edge-to-edge
                WindowCompat.setDecorFitsSystemWindows(window, false) // Enable edge-to-edge
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ParentalGuardTypography,
        shapes = ParentalGuardShapes,
        content = content
    )
}

// Extension function to get category color
fun getCategoryColor(category: com.parentalguard.common.model.AppCategory): Color {
    return when (category) {
        com.parentalguard.common.model.AppCategory.SOCIAL -> CategorySocial
        com.parentalguard.common.model.AppCategory.GAMES -> CategoryGames
        com.parentalguard.common.model.AppCategory.EDUCATION -> CategoryEducation
        com.parentalguard.common.model.AppCategory.PRODUCTIVITY -> CategoryProductivity
        com.parentalguard.common.model.AppCategory.ENTERTAINMENT -> CategoryEntertainment
        com.parentalguard.common.model.AppCategory.OTHER -> CategoryOther
    }
}
