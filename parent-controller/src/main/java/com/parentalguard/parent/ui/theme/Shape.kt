package com.parentalguard.parent.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// AURA · geometry — generous, calm radii. Cards breathe; pills are fully round.
val ParentalGuardShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

val BottomSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val CardShape = RoundedCornerShape(26.dp)
val ButtonShape = RoundedCornerShape(18.dp)
val ChipShape = RoundedCornerShape(14.dp)
val FABShape = RoundedCornerShape(20.dp)
val DialogShape = RoundedCornerShape(30.dp)
val IndicatorShape = RoundedCornerShape(3.dp)
