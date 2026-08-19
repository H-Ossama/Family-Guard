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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parentalguard.parent.R
import com.parentalguard.parent.ui.aura.auraEnter
import com.parentalguard.parent.ui.neumorphic.NeumorphicBackground
import com.parentalguard.parent.ui.neumorphic.NeumorphicCard
import com.parentalguard.parent.ui.neumorphic.NeumorphicIconTile
import com.parentalguard.parent.ui.neumorphic.Nm
import com.parentalguard.parent.ui.theme.MonoFontFamily

@Composable
fun DeviceOwnerGuideScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeumorphicBackground {
        LazyColumn(
            modifier = modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().auraEnter(0),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeumorphicIconTile(
                        icon = Icons.Default.ArrowBack,
                        onClick = onBack,
                        contentDescription = stringResource(R.string.back)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.device_owner_guide_title),
                            color = Nm.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.device_owner_guide_subtitle),
                            color = Nm.onSurfaceMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    NeumorphicIconTile(
                        icon = Icons.Default.Shield,
                        tint = Nm.primary,
                        contentDescription = stringResource(R.string.device_owner_guide_title)
                    )
                }
            }

            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth().auraEnter(1),
                    padding = 20.dp,
                    corner = 24.dp,
                    backgroundColor = Nm.primary.copy(alpha = 0.06f)
                ) {
                    Text(
                        text = stringResource(R.string.device_owner_guide_intro),
                        color = Nm.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth().auraEnter(2),
                    padding = 16.dp,
                    corner = 20.dp
                ) {
                    Text(
                        text = stringResource(R.string.device_owner_guide_adb_label),
                        color = Nm.onSurfaceMuted,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.device_owner_guide_adb_command),
                        color = Nm.onSurface,
                        fontFamily = MonoFontFamily,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                GuideStep(number = 1, text = stringResource(R.string.device_owner_guide_step_1))
            }
            item {
                GuideStep(number = 2, text = stringResource(R.string.device_owner_guide_step_2))
            }
            item {
                GuideStep(number = 3, text = stringResource(R.string.device_owner_guide_step_3))
            }
            item {
                GuideStep(number = 4, text = stringResource(R.string.device_owner_guide_step_4))
            }
            item {
                GuideStep(number = 5, text = stringResource(R.string.device_owner_guide_step_5))
            }

            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth().auraEnter(7),
                    padding = 16.dp,
                    corner = 20.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, null, tint = Nm.warning)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.device_owner_guide_note),
                            color = Nm.onSurfaceMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideStep(number: Int, text: String) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth().auraEnter(number + 1),
        padding = 16.dp,
        corner = 20.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NeumorphicIconTile(
                icon = Icons.Default.Shield,
                tint = Nm.primary,
                size = 38.dp,
                iconSize = 18.dp,
                contentDescription = null
            )
            Spacer(Modifier.width(12.dp))
            Text(text = text, color = Nm.onSurface, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
