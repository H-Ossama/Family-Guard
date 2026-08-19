package com.parentalguard.child.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ParentalGuardShapes = Shapes(
    // Extra small - For chips and tags
    extraSmall = RoundedCornerShape(12.dp),
    
    // Small - For buttons and small cards
    small = RoundedCornerShape(16.dp),
    
    // Medium - For cards and dialogs
    medium = RoundedCornerShape(22.dp),
    
    // Large - For bottom sheets and large cards
    large = RoundedCornerShape(30.dp),
    
    // Extra large - For full-screen modals
    extraLarge = RoundedCornerShape(40.dp)
)

// Custom shapes for specific components
val BottomSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val CardShape = RoundedCornerShape(28.dp)
val ButtonShape = RoundedCornerShape(22.dp)
val ChipShape = RoundedCornerShape(14.dp)
val FABShape = RoundedCornerShape(28.dp)
val DialogShape = RoundedCornerShape(30.dp)
val IndicatorShape = RoundedCornerShape(8.dp)

// Neumorphic tile shape
val NeumorphicShape = RoundedCornerShape(28.dp)
