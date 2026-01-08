package com.parentalguard.child.ui

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.parentalguard.child.MainActivity
import com.parentalguard.child.ui.screens.OnboardingScreen
import com.parentalguard.child.ui.theme.ParentalGuardTheme

import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    private val currentStep = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ParentalGuardTheme {
                OnboardingScreen(
                    currentStep = currentStep.value,
                    onNext = { 
                        currentStep.value = currentStep.value + 1 
                    },
                    onRequestUsageAccess = { 
                        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) 
                    },
                    onRequestOverlayAccess = { 
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)) 
                    },
                    onFinish = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
        
        // Initial check in case permissions already granted
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }
    
    private fun checkPermissions() {
        // Auto-advance logic
        if (currentStep.value == 1 && hasUsageStatsPermission()) {
            currentStep.value = 2
        } 
        
        // Note: Overlay permission check might need a slight delay or specific check
        // But for now typical flow works. 
        if (currentStep.value == 2 && hasOverlayPermission()) {
            currentStep.value = 3
        }
        
        // Skip intro if everything granted? Maybe not, allow user to see flow.
        // Or if already granted, we assume user is re-opening onboarding?
        // Usually Onboarding is only shown if not granted (checked in MainActivity).
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
}
