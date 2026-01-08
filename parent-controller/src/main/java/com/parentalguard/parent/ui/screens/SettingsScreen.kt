package com.parentalguard.parent.ui.screens



import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.parentalguard.parent.R
import com.parentalguard.parent.ui.components.*
import com.parentalguard.parent.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onLanguageChanged: (String) -> Unit = {}
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showAboutDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("parent_prefs", android.content.Context.MODE_PRIVATE) }
    var biometricEnabled by remember { mutableStateOf(prefs.getBoolean("biometric_enabled", false)) }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Primary, Secondary)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.settings_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security Section
            item {
                SettingsSection(title = stringResource(R.string.security_title)) {
                    SettingsItem(
                        icon = Icons.Outlined.Lock,
                        title = stringResource(R.string.pin_protection_title),
                        subtitle = stringResource(R.string.pin_protection_desc),
                        onClick = { showPinDialog = true },
                        trailing = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    
                    SettingsDivider(modifier = Modifier.padding(start = 56.dp))
                    
                    SettingsItem(
                        icon = Icons.Outlined.Fingerprint,
                        title = stringResource(R.string.biometric_title),
                        subtitle = stringResource(R.string.biometric_desc),
                        trailing = {
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { 
                                    biometricEnabled = it 
                                    prefs.edit().putBoolean("biometric_enabled", it).apply()
                                },
                                enabled = true
                            )
                        }
                    )
                }
            }
            
            // Notifications Section
            item {
                SettingsSection(title = stringResource(R.string.notifications_title)) {
                    SettingsItem(
                        icon = Icons.Outlined.Notifications,
                        title = stringResource(R.string.push_notif_title),
                        subtitle = stringResource(R.string.push_notif_desc),
                        trailing = {
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it }
                            )
                        }
                    )
                    
                    SettingsDivider(modifier = Modifier.padding(start = 56.dp))
                    
                    SettingsItem(
                        icon = Icons.Outlined.NotificationsActive,
                        title = stringResource(R.string.unlock_req_title),
                        subtitle = stringResource(R.string.unlock_req_desc),
                        trailing = {
                            Switch(
                                checked = true,
                                onCheckedChange = { }
                            )
                        }
                    )
                    
                    SettingsDivider(modifier = Modifier.padding(start = 56.dp))
                    
                    SettingsItem(
                        icon = Icons.Outlined.ReportProblem,
                        title = stringResource(R.string.usage_alerts_title),
                        subtitle = stringResource(R.string.usage_alerts_desc),
                        trailing = {
                            Switch(
                                checked = true,
                                onCheckedChange = { }
                            )
                        }
                    )
                }
            }
            
            // Appearance Section
            item {
                var currentThemeMode by remember { 
                    mutableStateOf(ThemePreferences.getThemeMode(context)) 
                }
                var showThemeDialog by remember { mutableStateOf(false) }
                
                SettingsSection(title = stringResource(R.string.appearance_title)) {
                    SettingsItem(
                        icon = Icons.Outlined.DarkMode,
                        title = stringResource(R.string.dark_mode_title),
                        subtitle = ThemePreferences.getThemeModeDisplayName(currentThemeMode),
                        onClick = { showThemeDialog = true },
                        trailing = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    
                    SettingsDivider(modifier = Modifier.padding(start = 56.dp))
                    
                    SettingsItem(
                        icon = Icons.Outlined.Language,
                        title = stringResource(R.string.settings_language),
                        subtitle = getCurrentLanguageName(),
                        onClick = { showLanguageDialog = true },
                        trailing = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
                
                // Theme Selection Dialog
                if (showThemeDialog) {
                    ThemeSelectionDialog(
                        currentMode = currentThemeMode,
                        onDismiss = { showThemeDialog = false },
                        onThemeSelected = { mode ->
                            currentThemeMode = mode
                            ThemePreferences.saveThemeMode(context, mode)
                            showThemeDialog = false
                            // Force recomposition by recreating activity
                            (context as? android.app.Activity)?.recreate()
                        }
                    )
                }
            }
            
            // About Section
            item {
                SettingsSection(title = stringResource(R.string.about_section_title)) {
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        title = stringResource(R.string.about_title),
                        subtitle = stringResource(R.string.app_version, "1.0.0"),
                        onClick = { showAboutDialog = true },
                        trailing = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    
                    SettingsDivider(modifier = Modifier.padding(start = 56.dp))
                    
                    SettingsItem(
                        icon = Icons.Outlined.Help,
                        title = stringResource(R.string.help_title),
                        subtitle = stringResource(R.string.help_desc),
                        onClick = { },
                        trailing = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    
                    SettingsDivider(modifier = Modifier.padding(start = 56.dp))
                    
                    SettingsItem(
                        icon = Icons.Outlined.Policy,
                        title = stringResource(R.string.privacy_title),
                        subtitle = stringResource(R.string.privacy_desc),
                        onClick = { },
                        trailing = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
            
            // Footer
            item {
                Spacer(Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.about_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // PIN Setup Dialog
    if (showPinDialog) {
        PinSetupDialog(
            onDismiss = { showPinDialog = false },
            onPinSet = { pin ->
                com.parentalguard.parent.security.PinManager.setPin(context, pin)
                showPinDialog = false
            }
        )
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { languageCode ->
                setAppLocale(languageCode)
                onLanguageChanged(languageCode)
                showLanguageDialog = false
            }
        )
    }
    
    // About Dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun getCurrentLanguageName(): String {
    val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
    return when (locale.language) {
        "ar" -> stringResource(R.string.language_darija)
        else -> stringResource(R.string.language_english)
    }
}

fun setAppLocale(languageCode: String) {
    val appLocale = LocaleListCompat.forLanguageTags(languageCode)
    AppCompatDelegate.setApplicationLocales(appLocale)
}

@Composable
private fun LanguageSelectionDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings_language)) },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected("en") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.language_english))
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected("ar") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.language_darija))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ThemeSelectionDialog(
    currentMode: ThemePreferences.ThemeMode,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemePreferences.ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(R.string.dark_mode_title),
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column {
                ThemePreferences.ThemeMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(mode) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = ThemePreferences.getThemeModeDisplayName(mode),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (mode == currentMode) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = when (mode) {
                                    ThemePreferences.ThemeMode.SYSTEM -> "Follow device settings"
                                    ThemePreferences.ThemeMode.LIGHT -> "Always use light theme"
                                    ThemePreferences.ThemeMode.DARK -> "Always use dark theme"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (mode == currentMode) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Primary
                            )
                        }
                    }
                    if (mode != ThemePreferences.ThemeMode.values().last()) {
                        Divider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() } 
                else Modifier
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PinSetupDialog(
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.pin_dialog_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.pin_dialog_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text(stringResource(R.string.enter_pin)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6) confirmPin = it },
                    label = { Text(stringResource(R.string.confirm_pin)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null
                )
                
                if (error != null) {
                    Text(
                        text = error!!,
                        color = Error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            GradientButton(
                text = stringResource(R.string.set_pin_btn),
                onClick = {
                    when {
                        pin.length < 4 -> error = context.getString(R.string.pin_error_short)
                        pin != confirmPin -> error = context.getString(R.string.pin_error_mismatch)
                        else -> onPinSet(pin)
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Primary, Secondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.app_version, "1.0.0"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.about_dialog_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                SettingsDivider()
                
                Text(
                    text = stringResource(R.string.label_features),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        stringResource(R.string.feature_view_apps),
                        stringResource(R.string.feature_monitor_usage),
                        stringResource(R.string.feature_time_limits),
                        stringResource(R.string.feature_block_apps),
                        stringResource(R.string.daily_report)
                    ).forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = Primary)
            }
        }
    )
}

@Composable
private fun SettingsDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
