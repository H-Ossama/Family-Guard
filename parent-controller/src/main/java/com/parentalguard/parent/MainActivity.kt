package com.parentalguard.parent

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.parentalguard.parent.ui.ParentalControlApp
import com.parentalguard.parent.security.PinManager
import com.parentalguard.parent.ui.screens.PinLockScreen
import com.parentalguard.parent.ui.components.NotificationHelper
import com.parentalguard.parent.ui.theme.ParentalGuardTheme

class MainActivity : AppCompatActivity() {
    
    companion object {
        const val ACTION_UNLOCK_REQUEST = "com.parentalguard.parent.ACTION_UNLOCK_REQUEST"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
        
        setContent {
            ParentalGuardTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val context = LocalContext.current
                    val isPinSet = remember { PinManager.isPinSet(context) }
                    var isUnlocked by remember { mutableStateOf(!isPinSet) }
                    
                    // Request notification permission on Android 13+
                    var hasNotificationPermission by remember {
                        mutableStateOf(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            } else {
                                true // Permission not needed on older versions
                            }
                        )
                    }
                    
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        hasNotificationPermission = isGranted
                    }
                    
                    // Request permission on first launch if needed
                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    if (isUnlocked) {
                        // Extract intent data
                        val deviceId = intent.getStringExtra("deviceId")
                        val navigateTo = intent.getStringExtra("navigate_to")
                        val deviceName = intent.getStringExtra("deviceName")
                        val requestType = intent.getStringExtra("requestType")
                        val appPackageName = intent.getStringExtra("appPackageName")
                        val appName = intent.getStringExtra("appName")
                        
                        ParentalControlApp(
                            initialDeviceId = if (navigateTo == "device_control" || intent.action == ACTION_UNLOCK_REQUEST) deviceId else null,
                            initialUnlockRequest = if (intent.action == ACTION_UNLOCK_REQUEST) {
                                UnlockRequestData(
                                    deviceId = deviceId ?: "",
                                    deviceName = deviceName ?: "Unknown Device",
                                    requestType = requestType ?: "DEVICE",
                                    appPackageName = appPackageName,
                                    appName = appName
                                )
                            } else null
                        )
                    } else {
                        PinLockScreen(
                            onUnlocked = { isUnlocked = true }
                        )
                    }
                }
            }
        }
    }
}

data class UnlockRequestData(
    val deviceId: String,
    val deviceName: String,
    val requestType: String,
    val appPackageName: String?,
    val appName: String?
)
