package com.parentalguard.parent.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.parentalguard.parent.ui.theme.*

/**
 * Simple bar chart with animation
 */
@Composable
fun BarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    barWidth: Dp = 12.dp,
    spacing: Dp = 6.dp,
    maxHeight: Dp = 150.dp,
    animated: Boolean = true,
    labelStep: Int = 1
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    
    Box(modifier = modifier.height(maxHeight + 40.dp)) {
        // Grid Lines
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 30.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val targetHeight = if (maxValue > 0) (item.value / maxValue) * maxHeight.value else 0f
                
                val animatedHeight by animateFloatAsState(
                    targetValue = targetHeight,
                    animationSpec = if (animated) tween(1000, easing = FastOutSlowInEasing) else snap(),
                    label = "bar_${item.label}"
                )
                
                Column(
                    modifier = Modifier.width(barWidth),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(animatedHeight.dp.coerceAtLeast(2.dp))
                            .background(
                                color = item.color,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp
                                )
                            )
                    )
                    
                    val index = data.indexOf(item)
                    if (index % labelStep == 0) {
                        Spacer(Modifier.height(8.dp))
                        
                        // Label
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            fontSize = androidx.compose.ui.unit.TextUnit.Unspecified,
                            softWrap = false
                        )
                    } else {
                        // Maintain spacing even if label is hidden
                        Spacer(Modifier.height(24.dp)) 
                    }
                }
            }
        }
    }
}

/**
 * Horizontal bar chart for category usage
 */
@Composable
fun HorizontalBarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 24.dp,
    spacing: Dp = 12.dp,
    animated: Boolean = true
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        data.forEach { item ->
            val targetWidth = if (maxValue > 0) item.value / maxValue else 0f
            
            val animatedWidth by animateFloatAsState(
                targetValue = targetWidth,
                animationSpec = if (animated) tween(800, easing = FastOutSlowInEasing) else snap(),
                label = "hbar_${item.label}"
            )
            
            Column {
                // Label and value row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.valueLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(Modifier.height(4.dp))
                
                // Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = IndicatorShape
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedWidth)
                            .fillMaxHeight()
                            .background(
                                color = item.color,
                                shape = IndicatorShape
                            )
                    )
                }
            }
        }
    }
}

/**
 * Simple donut/pie chart
 */
@Composable
fun DonutChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 32.dp,
    animated: Boolean = true
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    
    val animatedSweeps = data.map { item ->
        val targetSweep = if (total > 0) (item.value / total) * 360f else 0f
        animateFloatAsState(
            targetValue = targetSweep,
            animationSpec = if (animated) tween(1000, easing = FastOutSlowInEasing) else snap(),
            label = "donut_${item.label}"
        )
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            
            data.forEachIndexed { index, item ->
                val sweepAngle = animatedSweeps[index].value
                
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth.toPx()
                    ),
                    size = Size(
                        this.size.width - strokeWidth.toPx(),
                        this.size.height - strokeWidth.toPx()
                    ),
                    topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)
                )
                
                startAngle += sweepAngle
            }
        }
    }
}

/**
 * Legend for charts
 */
@Composable
fun ChartLegend(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(item.color, androidx.compose.foundation.shape.CircleShape)
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = item.valueLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Data class for chart items
 */
data class ChartData(
    val label: String,
    val value: Float,
    val color: Color,
    val valueLabel: String = ""
)
