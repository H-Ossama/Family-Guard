package com.parentalguard.parent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.parentalguard.parent.ui.theme.*

/**
 * Premium glassmorphism card with blur effect
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = GlassWhite,
    borderColor: Color = Color.White.copy(alpha = 0.3f),
    elevation: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
            )
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

/**
 * Premium elevated card with gradient accent
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    hasGradientAccent: Boolean = false,
    accentPosition: AccentPosition = AccentPosition.TOP,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    
    Column(
        modifier = modifier
            .shadow(8.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (hasGradientAccent && accentPosition == AccentPosition.TOP) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Primary, Secondary)
                        )
                    )
            )
        }
        
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
        
        if (hasGradientAccent && accentPosition == AccentPosition.BOTTOM) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Primary, Secondary)
                        )
                    )
            )
        }
    }
}

/**
 * Status card with colored left border
 */
@Composable
fun StatusCard(
    modifier: Modifier = Modifier,
    statusColor: Color = Success,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .shadow(4.dp, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(statusColor)
        )
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

enum class AccentPosition {
    TOP, BOTTOM
}
