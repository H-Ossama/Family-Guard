package com.parentalguard.parent.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ParentalGuardShapes = Shapes(
    // Extra small - For chips and tags
    extraSmall = RoundedCornerShape(4.dp),
    
    // Small - For buttons and small cards
    small = RoundedCornerShape(8.dp),
    
    // Medium - For cards and dialogs
    medium = RoundedCornerShape(16.dp),
    
    // Large - For bottom sheets and large cards
    large = RoundedCornerShape(24.dp),
    
    // Extra large - For full-screen modals
    extraLarge = RoundedCornerShape(32.dp)
)

// Custom shapes for specific components
val BottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
val CardShape = RoundedCornerShape(16.dp)
val ButtonShape = RoundedCornerShape(12.dp)
val ChipShape = RoundedCornerShape(8.dp)
val FABShape = RoundedCornerShape(16.dp)
val DialogShape = RoundedCornerShape(24.dp)
val IndicatorShape = RoundedCornerShape(4.dp)
