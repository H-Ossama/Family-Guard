package com.parentalguard.child.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parentalguard.child.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakeSystemSettings(
    onTitleClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.fake_system_title),
                        modifier = Modifier.clickable { onTitleClick() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2), // Professional Blue
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Professional System Info Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD) // Light Blue background
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.fake_warning_critical),
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = "الإعدادات المفعلة",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SettingItem(
                title = stringResource(R.string.fake_kernel_title),
                description = stringResource(R.string.fake_kernel_desc),
                icon = Icons.Default.SettingsSuggest,
                onClick = {
                    dialogTitle = "تحسين الأداء"
                    dialogMessage = "هذا الجزء من النظام يعمل تلقائياً لتحسين استهلاك الذاكرة. لا يمكن تغييره يدوياً لضمان استقرار التطبيقات."
                    showDialog = true
                }
            )

            SettingItem(
                title = stringResource(R.string.fake_battery_title),
                description = stringResource(R.string.fake_battery_desc),
                icon = Icons.Default.BatteryChargingFull,
                onClick = {
                    dialogTitle = "صحة البطارية"
                    dialogMessage = "إدارة البطارية قيد العمل. تقوم هذه الخدمة بمراقبة درجة حرارة الجهاز وتقليل استهلاك الطاقة في الخلفية."
                    showDialog = true
                }
            )

            SettingItem(
                title = stringResource(R.string.fake_storage_title),
                description = stringResource(R.string.fake_storage_desc),
                icon = Icons.Default.VerifiedUser,
                onClick = {
                    dialogTitle = "حماية ملفات النظام"
                    dialogMessage = "يتم حماية ملفات النظام ضد التغييرات غير المصرح بها. الخدمة تعمل بشكل آمن ومشفر."
                    showDialog = true
                }
            )

            SettingItem(
                title = stringResource(R.string.fake_security_title),
                description = stringResource(R.string.fake_security_desc),
                icon = Icons.Default.Shield,
                onClick = {
                    dialogTitle = "أمان الشبكة"
                    dialogMessage = "بروتوكولات الأمان مفعلة لحماية اتصالك من التهديدات. يتم التحديث تلقائياً عند الضرورة."
                    showDialog = true
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "نسخة النظام: Framework v4.2.0-stable\nالحالة: جميع الخدمات تعمل بشكل طبيعي",
                color = Color.Gray.copy(alpha = 0.6f),
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = dialogTitle, color = Color(0xFF1976D2), fontWeight = FontWeight.Bold) },
            text = { Text(text = dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("إغلاق", color = Color(0xFF1976D2))
                }
            }
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.4f)
        )
    }
    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
}
