package com.parentalguard.parent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parentalguard.common.model.DailyUsageReport
import com.parentalguard.parent.R
import com.parentalguard.parent.data.ReportsRepository
import com.parentalguard.parent.ui.components.LiquidGlassCard
import com.parentalguard.parent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsHistoryScreen(
    reportsRepository: ReportsRepository,
    onReportClick: (DailyUsageReport) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reports = remember { reportsRepository.getAllReports().sortedByDescending { it.date } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = PremiumGradient
            )

    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(color = LiquidCardBackground) {
                    TopAppBar(
                        title = { 
                            Text(
                                stringResource(R.string.nav_reports),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(20.dp), tint = LiquidBlue)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            }
        ) { paddingValues ->
            if (reports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LiquidGlassCard(
                            backgroundColor = LiquidBlue.copy(alpha = 0.1f),
                            padding = 24.dp,
                            modifier = Modifier.size(120.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = LiquidBlue
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "No history available",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Usage logs will start appearing here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val groupedReports = reports.groupBy { it.date }
                    
                    groupedReports.forEach { (date, reportsForDate) ->
                        item {
                            Text(
                                text = date.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = LiquidBlue,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 8.dp),
                                letterSpacing = 1.sp
                            )
                        }
                        
                        items(reportsForDate) { report ->
                            ReportItem(
                                report = report,
                                onClick = { onReportClick(report) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportItem(
    report: DailyUsageReport,
    onClick: () -> Unit
) {
    val totalMinutes = report.totalScreenTimeMs / (1000 * 60)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val timeLabel = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        padding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.deviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(LiquidTeal, CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Usage: $timeLabel",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(LiquidBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = LiquidBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
