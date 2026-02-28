package com.parentalguard.parent.ui.theme

import androidx.compose.ui.graphics.Color

// ========================================================================
// 🎨 ENHANCED GLASSMORPHISM PALETTE (2026 REFRESH)
// ========================================================================

// Primary Brand Colors - Deep & Trustworthy but Modern
val GlassPrimary = Color(0xFF4F46E5)      // Electric Indigo
val GlassPrimaryVariant = Color(0xFF3730A3) // Deep Indigo
val GlassPrimaryLight = Color(0xFF818CF8)   // Soft Indigo

// Secondary Accent Colors - Vibrant & Energetic
val GlassSecondary = Color(0xFF06B6D4)    // Cyan Neon
val GlassSecondaryVariant = Color(0xFF0891B2)
val GlassSecondaryLight = Color(0xFF67E8F9)

// Tertiary/Accent Colors - Playful pops
val GlassAccentPink = Color(0xFFEC4899)   // Hot Pink
val GlassAccentPurple = Color(0xFF8B5CF6) // Violet
val GlassAccentOrange = Color(0xFFF97316) // Bright Orange

// Backgrounds & Surfaces - Clean & Deep
val GlassBackgroundLight = Color(0xFFF0FDF4) // Very subtle cool mint/white tint
val GlassBackgroundDark = Color(0xFF0F172A)  // Deep Slate Navy (Premium Dark)
val GlassSurfaceLight = Color(0xFFFFFFFF)
val GlassSurfaceDark = Color(0xFF1E293B)     // Slate 800

// Status Colors - Clear & Accessible
val GlassSuccess = Color(0xFF10B981)      // Emerald
val GlassWarning = Color(0xFFF59E0B)      // Amber
val GlassError = Color(0xFFEF4444)        // Red 500
val GlassInfo = Color(0xFF3B82F6)         // Blue 500

// ========================================================================
// 💧 LIQUID GLASS SPECIFIC TOKENS
// ========================================================================

// Updated Liquid colors to match new vibrant theme
val LiquidBlue = GlassPrimary
val LiquidPurple = GlassAccentPurple
val LiquidPink = GlassAccentPink
val LiquidTeal = GlassSecondary
val LiquidIndigo = GlassPrimaryVariant
val LiquidOrange = GlassAccentOrange

// Glass Effect Tokens
val LiquidGlassBackground = Color(0xFFFFFFFF).copy(alpha = 0.15f)
val LiquidGlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.25f)
val LiquidGlassBorderDark = Color(0xFFFFFFFF).copy(alpha = 0.10f)

val LiquidCardBackground = Color(0xFFFFFFFF).copy(alpha = 0.85f) // Frosted Light
val LiquidCardBackgroundDark = Color(0xFF1E293B).copy(alpha = 0.75f) // Frosted Dark

// ========================================================================
// 🌈 GRADIENTS
// ========================================================================

// Premium Gradients
val GradientPrimaryStart = GlassPrimary
val GradientPrimaryEnd = GlassAccentPurple
val LiquidGradientPrimary = listOf(GradientPrimaryStart, GradientPrimaryEnd)

val GradientAccentStart = GlassSecondary
val GradientAccentEnd = GlassSuccess
val LiquidGradientAccent = listOf(GradientAccentStart, GradientAccentEnd)

val GradientWarmStart = GlassAccentOrange
val GradientWarmEnd = GlassAccentPink
val LiquidGradientWarm = listOf(GradientWarmStart, GradientWarmEnd)

val LiquidGradientGlass = listOf(
    Color.White.copy(alpha = 0.3f),
    Color.White.copy(alpha = 0.05f)
)

// ========================================================================
// 🔄 LEGACY COMPATIBILITY MAPPING
// ========================================================================
// Mapping old names to new palette for backward compatibility

val PremiumPrimary = GlassPrimary
val PremiumPrimaryVariant = GlassPrimaryVariant
val PremiumPrimaryLight = GlassPrimaryLight
val PremiumSecondary = GlassSecondary
val PremiumSecondaryVariant = GlassSecondaryVariant

val Primary = GlassPrimary
val PrimaryDark = GlassPrimaryVariant
val PrimaryLight = GlassPrimaryLight
val Secondary = GlassSecondary
val SecondaryDark = GlassSecondaryVariant
val SecondaryLight = GlassSecondaryLight

val BackgroundLight = Color(0xFFF8FAFC) // Slate 50
val BackgroundDark = GlassBackgroundDark
val SurfaceLight = GlassSurfaceLight
val SurfaceDark = GlassSurfaceDark

val SurfaceVariantLight = Color(0xFFF1F5F9) // Slate 100
val SurfaceVariantDark = Color(0xFF334155)  // Slate 700

val CardLight = GlassSurfaceLight
val CardDark = GlassSurfaceDark

val Error = GlassError
val ErrorLight = Color(0xFFFEE2E2)
val Success = GlassSuccess
val SuccessLight = Color(0xFFD1FAE5)
val Warning = GlassWarning
val WarningLight = Color(0xFFFEF3C7)
val Info = GlassInfo
val InfoLight = Color(0xFFDBEAFE)

val TextPrimaryLight = Color(0xFF0F172A)   // Slate 900
val TextSecondaryLight = Color(0xFF475569) // Slate 600
val TextTertiaryLight = Color(0xFF94A3B8)  // Slate 400
val TextPrimaryDark = Color(0xFFF8FAFC)    // Slate 50
val TextSecondaryDark = Color(0xFFCBD5E1)  // Slate 300
val TextTertiaryDark = Color(0xFF64748B)   // Slate 500

// Categories
val CategorySocial = GlassInfo
val CategoryGames = GlassAccentPink
val CategoryEducation = GlassSuccess
val CategoryProductivity = GlassAccentPurple
val CategoryEntertainment = GlassWarning
val CategoryOther = Color(0xFF6B7280)

val GradientStart = GradientPrimaryStart
val GradientMiddle = Color(0xFF6366F1) // Indigo 500
val GradientEnd = GradientAccentStart

val OnlineGreen = GlassSuccess
val OfflineRed = GlassError
val WarningOrange = GlassWarning
