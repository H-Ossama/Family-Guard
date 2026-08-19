package com.parentalguard.parent.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parentalguard.common.model.DailyUsageReport
import com.parentalguard.parent.R
import com.parentalguard.parent.ui.aura.auraEnter
import com.parentalguard.parent.ui.aura.formatAuraDuration
import com.parentalguard.parent.ui.neumorphic.NeumorphicBackground
import com.parentalguard.parent.ui.neumorphic.NeumorphicCard
import com.parentalguard.parent.ui.neumorphic.NeumorphicDonut
import com.parentalguard.parent.ui.neumorphic.NeumorphicDuration
import com.parentalguard.parent.ui.neumorphic.NeumorphicIconTile
import com.parentalguard.parent.ui.neumorphic.NeumorphicProgressRing
import com.parentalguard.parent.ui.neumorphic.NeumorphicShimmer
import com.parentalguard.parent.ui.neumorphic.NeumorphicUsageBar
import com.parentalguard.parent.ui.neumorphic.Nm
import com.parentalguard.parent.ui.neumorphic.NmDatum
import com.parentalguard.parent.ui.theme.MonoFontFamily
import com.parentalguard.parent.ui.theme.getCategoryColor

private const val GOAL_MS = 8L * 60 * 60 * 1000

@Composable
fun ReportDetailScreen(
    report: DailyUsageReport?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeumorphicBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 12.dp, end = 20.dp, top = 14.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeumorphicIconTile(icon = Icons.Default.ArrowBack, onClick = onBack, contentDescription = stringResource(R.string.back))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = report?.deviceName ?: "",
                        color = Nm.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = report?.date ?: "",
                        color = Nm.onSurfaceMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (report == null) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    NeumorphicShimmer(Modifier.fillMaxWidth().height(160.dp), corner = 30.dp)
                    NeumorphicShimmer(Modifier.fillMaxWidth().height(240.dp), corner = 24.dp)
                }
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    NeumorphicCard(modifier = Modifier.fillMaxWidth().auraEnter(0), padding = 24.dp, corner = 30.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.report_total_label),
                                    color = Nm.onSurfaceMuted,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(Modifier.height(6.dp))
                                NeumorphicDuration(targetMs = report.totalScreenTimeMs, fontSize = 36)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    stringResource(R.string.pulse_goal),
                                    color = Nm.onSurfaceMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            NeumorphicProgressRing(
                                progress = (report.totalScreenTimeMs / GOAL_MS.toFloat()).coerceIn(0f, 1f),
                                ringSize = 112.dp,
                                strokeWidth = 12.dp
                            ) {
                                Text(
                                    "${((report.totalScreenTimeMs / GOAL_MS.toFloat()) * 100).toInt()}%",
                                    color = Nm.onSurface,
                                    fontFamily = MonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }

                item {
                    NeumorphicCard(modifier = Modifier.fillMaxWidth().auraEnter(1), corner = 24.dp) {
                        Text(
                            stringResource(R.string.category_usage),
                            color = Nm.onSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NeumorphicDonut(
                                data = report.categoryUsages.map { usage ->
                                    NmDatum(
                                        label = stringResource(getCategoryNameResId(usage.category)),
                                        value = usage.totalTimeMs.toFloat(),
                                        color = getCategoryColor(usage.category)
                                    )
                                },
                                size = 132.dp,
                                strokeWidth = 22.dp
                            )
                        }
                        Spacer(Modifier.height(18.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val maxCat = report.categoryUsages.maxOfOrNull { it.totalTimeMs } ?: 1L
                            report.categoryUsages.sortedByDescending { it.totalTimeMs }.forEach { usage ->
                                NeumorphicUsageBar(
                                    label = stringResource(getCategoryNameResId(usage.category)),
                                    valueLabel = formatAuraDuration(usage.totalTimeMs),
                                    fraction = usage.totalTimeMs / maxCat.toFloat(),
                                    color = getCategoryColor(usage.category)
                                )
                            }
                        }
                    }
                }

                item {
                    NeumorphicCard(modifier = Modifier.fillMaxWidth().auraEnter(2), corner = 24.dp) {
                        Text(
                            stringResource(R.string.most_used_apps),
                            color = Nm.onSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        val maxApp = report.mostUsedApps.maxOfOrNull { it.totalTimeInForeground } ?: 1L
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            report.mostUsedApps.take(8).forEach { app ->
                                NeumorphicUsageBar(
                                    label = app.packageName.substringAfterLast("."),
                                    valueLabel = formatAuraDuration(app.totalTimeInForeground),
                                    fraction = app.totalTimeInForeground / maxApp.toFloat(),
                                    color = getCategoryColor(app.category)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}