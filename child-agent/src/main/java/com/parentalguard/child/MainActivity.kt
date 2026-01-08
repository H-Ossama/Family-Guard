package com.parentalguard.child

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.parentalguard.child.ui.OnboardingActivity
import com.parentalguard.child.ui.screens.MainScreen
import com.parentalguard.child.ui.theme.ParentalGuardTheme
import com.parentalguard.child.service.MonitorService

import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val connectionString = mutableStateOf("")
    private val status = mutableStateOf("")
    private val qrBitmap = mutableStateOf<Bitmap?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check permissions and redirect to Onboarding if needed
        if (!hasUsageStatsPermission() || !hasOverlayPermission()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }
        
        status.value = getString(R.string.status_initializing)

        setContent {
            ParentalGuardTheme {
                MainScreen(
                    connectionString = connectionString.value,
                    status = status.value,
                    qrBitmap = qrBitmap.value,
                    onRequestUnlock = { requestTemporaryUnlock() },
                    onHideIcon = { hideLauncherIcon() }
                )
            }
        }
        
        // Start service explicitly on open
        val serviceIntent = Intent(this, MonitorService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        
        updateConnectionInfo()
    }

    private fun updateConnectionInfo() {
        // Get IP Address
        val ip = getLocalIpAddress()
        val port = 8080 // Default Ktor port
        
        if (ip != null) {
            val connStr = "$ip:$port"
            connectionString.value = connStr
            status.value = getString(R.string.status_running) // Or formatted string resource
            
            // Generate QR
            // Note: QRCodeGenerator must be accessible
            val bitmap = com.parentalguard.child.ui.QRCodeGenerator.generateQRCode(connStr, 512, 512)
            qrBitmap.value = bitmap
        } else {
            connectionString.value = getString(R.string.address_unavailable)
            status.value = getString(R.string.status_no_network)
        }
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
    
    private fun hasOverlayPermission(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this)
        }
        return true
    }

    private fun hideLauncherIcon() {
        val p = packageManager
        val componentName = android.content.ComponentName(this, MainActivity::class.java)
        p.setComponentEnabledSetting(
            componentName,
            android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            0 // Don't use DONT_KILL_APP to ensure the launcher refreshes
        )
        Toast.makeText(this, getString(R.string.toast_icon_hidden), Toast.LENGTH_LONG).show()
        
        // Kill the app completely so the launcher updates
        finishAffinity()
        System.exit(0)
    }
    
    private fun requestTemporaryUnlock() {
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.request_unlock))
            .setMessage(getString(R.string.request_unlock_message))
            .setPositiveButton(getString(R.string.approve)) { _, _ ->
                com.parentalguard.child.utils.EventHelper.sendUnlockRequest(
                    context = this,
                    requestType = "DEVICE"
                )
                Toast.makeText(this, getString(R.string.request_sent), Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
