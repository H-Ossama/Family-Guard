package com.parentalguard.parent.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.parentalguard.parent.R

// ============================================================================
// AURA · Typography — Poppins display · Inter body · JetBrains Mono stats.
// System-backed families keep the APK offline-safe; hierarchy comes from the
// display/body/mono roles, weight, tracking, and scale.
// ============================================================================

val DisplayFontFamily: FontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)
val BodyFontFamily: FontFamily = FontFamily.Default
val MonoFontFamily: FontFamily = FontFamily.Monospace

val AppFontFamily = BodyFontFamily

val ParentalGuardTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold, letterSpacing = (-1.5).sp),
        displayMedium = displayMedium.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
        displaySmall = displaySmall.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
        headlineLarge = headlineLarge.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
        headlineSmall = headlineSmall.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontFamily = BodyFontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = BodyFontFamily),
        bodySmall = bodySmall.copy(fontFamily = BodyFontFamily),
        labelLarge = labelLarge.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold),
        labelMedium = labelMedium.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium),
        labelSmall = labelSmall.copy(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium)
    )
}
