package com.parentalguard.child.ui.theme

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
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = SecondaryDark,
    
    tertiary = AccentPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE7F6),
    onTertiaryContainer = Color(0xFF4A148C),
    
    error = Error,
    onError = Color.White,
    errorContainer = ErrorLight,
    onErrorContainer = Error,
    
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    
    outline = TextTertiaryLight,
    outlineVariant = Color(0xFFE0E0E0),
    
    inverseSurface = SurfaceDark,
    inverseOnSurface = TextPrimaryDark,
    inversePrimary = PrimaryLight,
    
    surfaceTint = Primary,
    scrim = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = PrimaryDark,
    primaryContainer = Primary,
    onPrimaryContainer = Color.White,
    
    secondary = SecondaryLight,
    onSecondary = SecondaryDark,
    secondaryContainer = Secondary,
    onSecondaryContainer = Color.White,
    
    tertiary = AccentPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4A148C),
    onTertiaryContainer = Color(0xFFEDE7F6),
    
    error = Color(0xFFEF5350),
    onError = Color.Black,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    
    outline = TextTertiaryDark,
    outlineVariant = Color(0xFF3D3D3D),
    
    inverseSurface = SurfaceLight,
    inverseOnSurface = TextPrimaryLight,
    inversePrimary = Primary,
    
    surfaceTint = PrimaryLight,
    scrim = Color.Black
)

// Premium gradient brush for backgrounds
val PremiumGradient = Brush.verticalGradient(
    colors = listOf(GradientStart, GradientMiddle, GradientEnd)
)

val PremiumHorizontalGradient = Brush.horizontalGradient(
    colors = listOf(GradientStart, GradientEnd)
)

val CardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1A237E).copy(alpha = 0.9f),
        Color(0xFF00897B).copy(alpha = 0.9f)
    )
)

@Composable
fun ParentalGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            if (context is Activity) {
                val window = context.window
                window.statusBarColor = if (darkTheme) BackgroundDark.toArgb() else Primary.toArgb()
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
