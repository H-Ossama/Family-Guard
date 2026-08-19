package com.parentalguard.parent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parentalguard.common.model.DailyUsageReport
import com.parentalguard.parent.R
import com.parentalguard.parent.data.ReportsRepository
import com.parentalguard.parent.ui.aura.auraEnter
import com.parentalguard.parent.ui.aura.formatAuraDuration
import com.parentalguard.parent.ui.neumorphic.NeumorphicBackground
import com.parentalguard.parent.ui.neumorphic.NeumorphicCard
import com.parentalguard.parent.ui.neumorphic.NeumorphicEmptyState
import com.parentalguard.parent.ui.neumorphic.NeumorphicShimmer
import com.parentalguard.parent.ui.neumorphic.NeumorphicStat
import com.parentalguard.parent.ui.neumorphic.Nm
import com.parentalguard.parent.ui.theme.MonoFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun InsightsScreen(
    reportsRepository: ReportsRepository,
    onReportClick: (DailyUsageReport) -> Unit,
    modifier: Modifier = Modifier
) {
    val reports by produceState<List<DailyUsageReport>?>(initialValue = null) {
        value = withContext(Dispatchers.IO) { reportsRepository.getAllReports() }
            .sortedByDescending { it.date }
    }

    NeumorphicBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(Modifier.auraEnter(0)) {
                    Text(
                        text = stringResource(R.string.insights_title),
                        color = Nm.onSurface,
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.insights_subtitle),
                        color = Nm.onSurfaceMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            val list = reports
            if (list == null) {
                items(3) { index ->
                    NeumorphicShimmer(
                        Modifier.fillMaxWidth().height(84.dp).auraEnter(1 + index),
                        corner = 22.dp
                    )
                }
            } else if (list.isEmpty()) {
                item {
                    NeumorphicEmptyState(
                        icon = Icons.Default.AutoGraph,
                        title = stringResource(R.string.insights_empty_title),
                        description = stringResource(R.string.insights_empty_desc),
                        modifier = Modifier.auraEnter(1)
                    )
                }
            } else {
                // Weekly digest — derived strictly from saved reports
                val week = list.take(7)
                val weekTotal = week.sumOf { it.totalScreenTimeMs }
                val weekAvg = if (week.isNotEmpty()) weekTotal / week.size else 0L

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().auraEnter(1),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeumorphicCard(modifier = Modifier.weight(1f), padding = 16.dp, corner = 22.dp) {
                            NeumorphicStat(
                                label = stringResource(R.string.insights_week_total),
                                value = formatAuraDuration(weekTotal),
                                valueColor = Nm.primary
                            )
                        }
                        NeumorphicCard(modifier = Modifier.weight(1f), padding = 16.dp, corner = 22.dp) {
                            NeumorphicStat(
                                label = stringResource(R.string.insights_week_avg),
                                value = formatAuraDuration(weekAvg),
                                valueColor = Nm.cyan
                            )
                        }
                    }
                }

                itemsIndexed(list, key = { _, r -> "${r.deviceName}-${r.date}" }) { index, report ->
                    Box(Modifier.auraEnter(2 + index.coerceAtMost(8))) {
                        ReportRow(report = report, onClick = { onReportClick(report) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportRow(report: DailyUsageReport, onClick: () -> Unit) {
    NeumorphicCard(onClick = onClick, padding = 16.dp, corner = 22.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Date badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Nm.primary.copy(alpha = 0.1f))
            ) {
                Spacer(Modifier.height(9.dp))
                Text(
                    text = report.date.substringAfterLast("-"),
                    color = Nm.primary,
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = report.date.substringBeforeLast("-").substringAfter("-"),
                    color = Nm.onSurfaceMuted,
                    fontFamily = MonoFontFamily,
                    fontSize = 10.sp
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = report.deviceName,
                    color = Nm.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = report.date,
                    color = Nm.onSurfaceMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = formatAuraDuration(report.totalScreenTimeMs),
                color = Nm.onSurface,
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Nm.onSurfaceMuted, modifier = Modifier.size(18.dp))
        }
    }
}