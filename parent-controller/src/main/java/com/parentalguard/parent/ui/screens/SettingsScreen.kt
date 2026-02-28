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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = PremiumGradient)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = LiquidGradientPrimary
                            )
                        )
                        .padding(top = 48.dp, bottom = 80.dp) // Adjusted for consistency
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "Control your parental preferences",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "PREMIUM VERSION 1.1.0",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
            
            // Content
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-30).dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Security Section
                    LiquidGlassSection(title = stringResource(R.string.security_title)) {
                        SettingsItemRow(
                            icon = Icons.Outlined.Lock,
                            title = stringResource(R.string.pin_protection_title),
                            subtitle = stringResource(R.string.pin_protection_desc),
                            onClick = { showPinDialog = true }
                        )
                        
                        LiquidGlassDivider()
                        
                        LiquidGlassSwitch(
                            checked = biometricEnabled,
                            onCheckedChange = { 
                                biometricEnabled = it 
                                prefs.edit().putBoolean("biometric_enabled", it).apply()
                            },
                            label = stringResource(R.string.biometric_title),
                            sublabel = stringResource(R.string.biometric_desc),
                            icon = Icons.Outlined.Fingerprint,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Appearance Section
                    var currentThemeMode by remember { 
                        mutableStateOf(ThemePreferences.getThemeMode(context)) 
                    }
                    var showThemeDialog by remember { mutableStateOf(false) }
                    
                    LiquidGlassSection(title = stringResource(R.string.appearance_title)) {
                        SettingsItemRow(
                            icon = Icons.Outlined.DarkMode,
                            title = stringResource(R.string.dark_mode_title),
                            subtitle = ThemePreferences.getThemeModeDisplayName(currentThemeMode),
                            onClick = { showThemeDialog = true }
                        )
                        
                        LiquidGlassDivider()
                        
                        SettingsItemRow(
                            icon = Icons.Outlined.Language,
                            title = stringResource(R.string.settings_language),
                            subtitle = getCurrentLanguageName(),
                            onClick = { showLanguageDialog = true }
                        )
                        
                        LiquidGlassDivider()

                        LiquidGlassSwitch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            label = "Push Notifications",
                            sublabel = "Get real-time alerts",
                            icon = Icons.Outlined.Notifications,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    if (showThemeDialog) {
                        ThemeSelectionDialog(
                            currentMode = currentThemeMode,
                            onDismiss = { showThemeDialog = false },
                            onThemeSelected = { mode ->
                                currentThemeMode = mode
                                ThemePreferences.saveThemeMode(context, mode)
                                showThemeDialog = false
                                (context as? android.app.Activity)?.recreate()
                            }
                        )
                    }

                    // About Section
                    LiquidGlassSection(title = stringResource(R.string.about_section_title)) {
                        SettingsItemRow(
                            icon = Icons.Outlined.Info,
                            title = stringResource(R.string.about_title),
                            subtitle = "Learn more about Family Guard",
                            onClick = { showAboutDialog = true }
                        )
                        
                        LiquidGlassDivider()
                        
                        SettingsItemRow(
                            icon = Icons.Outlined.Help,
                            title = stringResource(R.string.help_title),
                            subtitle = stringResource(R.string.help_desc),
                            onClick = { }
                        )
                    }
                    
                    Spacer(Modifier.height(40.dp))
                    
                    Text(
                        text = "FAMILY GUARD",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        letterSpacing = 6.sp
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
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LiquidBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LiquidBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
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
        title = { Text(text = stringResource(R.string.settings_language), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected("en") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.language_english), style = MaterialTheme.typography.bodyLarge)
                }
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected("ar") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.language_darija), style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = LiquidBlue)
            }
        },
        shape = RoundedCornerShape(24.dp)
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
                                tint = LiquidBlue
                            )
                        }
                    }
                    if (mode != ThemePreferences.ThemeMode.values().last()) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = LiquidBlue)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6) confirmPin = it },
                    label = { Text(stringResource(R.string.confirm_pin)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    shape = RoundedCornerShape(12.dp)
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
            LiquidGlassButton(
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
                Text(stringResource(R.string.cancel), color = LiquidBlue)
            }
        },
        shape = RoundedCornerShape(24.dp)
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
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(LiquidBlue, LiquidPurple)
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
                        text = stringResource(R.string.app_version, "1.1.0"),
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
                
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                
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
                Text(stringResource(R.string.close), color = LiquidBlue, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
