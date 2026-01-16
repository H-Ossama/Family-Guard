package com.parentalguard.child.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.parentalguard.child.ChildApp
import com.parentalguard.child.R
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class MonitorService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    private val serviceScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.Job())

    private var commandServer: com.parentalguard.child.network.CommandServer? = null
    private var serviceRegistrar: com.parentalguard.child.network.ServiceRegistrar? = null
    private var lockManager: com.parentalguard.child.ui.LockManager? = null

    private val internalReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
             if (intent.action == "com.parentalguard.child.INTERNAL_EVENT") {
                 val type = intent.getStringExtra("type")
                 val payload = intent.getStringExtra("payload")
                 val deviceName = intent.getStringExtra("deviceName")
                 val requestType = intent.getStringExtra("requestType")
                 val appPackageName = intent.getStringExtra("appPackageName")
                 val appName = intent.getStringExtra("appName")
                 
                 if (type != null) {
                     try {
                         val eventType = com.parentalguard.common.network.EventType.valueOf(type)
                         val event = com.parentalguard.common.network.Packet.Event(
                             eventType = eventType,
                             payload = payload,
                             deviceName = deviceName,
                             requestType = requestType,
                             appPackageName = appPackageName,
                             appName = appName
                         )
                         commandServer?.broadcast(event)
                     } catch (e: Exception) {
                         Log.e("MonitorService", "Failed to broadcast internal event", e)
                     }
                 }
             }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        com.parentalguard.child.data.RuleRepository.initialize(this)
        startForeground()
        lockManager = com.parentalguard.child.ui.LockManager(applicationContext)
        startUsageMonitoring() // Coroutine
        startLockMonitoring() // New Coroutine for Lock State
        
        // Register Internal Receiver
        val filter = android.content.IntentFilter("com.parentalguard.child.INTERNAL_EVENT")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
             // For internal broadcasts, we can use RECEIVER_NOT_EXPORTED
             registerReceiver(internalReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
             // Or verify package in onReceive?
             // But for internal broadcasts, usually LocalBroadcastManager was preferred.
             // Since we use sendBroadcast(intent.setPackage(...)), it is explicit.
             registerReceiver(internalReceiver, filter)
        }
        
        if (commandServer == null) {
             commandServer = com.parentalguard.child.network.CommandServer(applicationContext)
             serviceRegistrar = com.parentalguard.child.network.ServiceRegistrar(applicationContext)
             
             serviceScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                 try {
                    commandServer?.start() // Starts on port 8080
                    serviceRegistrar?.registerService(8080)
                 } catch (e: Exception) {
                     Log.e("MonitorService", "Failed to start server/service", e)
                 }
             }
        }
        
        return START_STICKY
    }

    private fun startLockMonitoring() {
        // Monitor for explicit lock state changes
        serviceScope.launch {
            com.parentalguard.child.data.RuleRepository.globalLock.collect { isLocked ->
                // Must run on main thread to manipulate views
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (isLocked) {
                        Log.d("MonitorService", "Global Lock Enabled")
                        lockManager?.showLockScreen()
                    } else {
                        Log.d("MonitorService", "Global Lock Disabled")
                        lockManager?.hideLockScreen()
                    }
                }
            }
        }

        // Monitor for expiration
        serviceScope.launch {
            while (isActive) {
                val until = com.parentalguard.child.data.RuleRepository.globalLockUntil.value
                val isLocked = com.parentalguard.child.data.RuleRepository.globalLock.value
                
                if (isLocked && until > 0 && until <= System.currentTimeMillis()) {
                    Log.i("MonitorService", "Lock expired, releasing.")
                    com.parentalguard.child.data.RuleRepository.setGlobalLock(false)
                }
                delay(1000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(internalReceiver)
        serviceRegistrar?.unregisterService()
        commandServer?.stop()
        lockManager?.hideLockScreen()
        serviceScope.cancel()
    }

    private fun startUsageMonitoring() {
        serviceScope.launch {
            val monitor = com.parentalguard.child.monitor.UsageMonitor(applicationContext)
            var lastMonitoredPackage: String? = null

            while (isActive) {
                try {
                    val topPackage = getForegroundPackage()
                    
                    // If we see ourselves, it might be the lock screen or the main app.
                    // If we are showing the lock screen, we should keep monitoring the 'last' known package
                    // to see if it's still what's 'under' the lock screen.
                    val packageToEvaluate = if (topPackage == packageName) {
                        lastMonitoredPackage
                    } else {
                        topPackage
                    }
                    
                    if (topPackage != null && topPackage != packageName) {
                        lastMonitoredPackage = topPackage
                    }

                    if (packageToEvaluate != null && packageToEvaluate != packageName) {
                        val ruleRepo = com.parentalguard.child.data.RuleRepository
                        
                        // Check for App Timer (Temporary Allowance / Enforcement)
                        val timerActive = ruleRepo.isAppTimerActive(packageToEvaluate)
                        val timerSet = ruleRepo.appTimers.value.containsKey(packageToEvaluate)
                        
                        // Check for temporary global unlock
                        val isTemporarilyUnlocked = ruleRepo.isTemporarilyUnlocked()
                        
                        var shouldBlock = false
                        
                        if (isTemporarilyUnlocked) {
                            shouldBlock = false
                        } else if (timerSet) {
                            if (timerActive) {
                                shouldBlock = false // Explicitly allowed by timer
                            } else {
                                shouldBlock = true // Timer expired, force block
                            }
                        } else {
                            // Check for Category Timer
                            val category = ruleRepo.getCategory(packageToEvaluate)
                            val categoryTimerActive = ruleRepo.isCategoryTimerActive(category)
                            val categoryTimerSet = ruleRepo.categoryTimers.value.containsKey(category)
                            
                            if (categoryTimerSet) {
                                if (categoryTimerActive) {
                                    shouldBlock = false // Explicitly allowed by category timer
                                } else {
                                    shouldBlock = true // Category timer expired, force block
                                }
                            } else {
                                // Normal Rules
                                val rules = ruleRepo.rules.value
                                shouldBlock = rules.any { rule ->
                                    rule.packageName == packageToEvaluate && 
                                    (rule.isPermanentlyBlocked || rule.blockEndTime > System.currentTimeMillis())
                                }
                            }
                        }
                        
                        // Global Lock
                        val globalLock = ruleRepo.globalLock.value
                        
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            if (shouldBlock || globalLock) {
                                if (lockManager?.isShowing == false) {
                                    Log.i("MonitorService", "Blocking app: $packageToEvaluate (Reason: ${if(globalLock) "Global Lock" else "App Rule/Timer"})")
                                    lockManager?.showLockScreen()
                                }
                            } else {
                                if (lockManager?.isShowing == true) {
                                     Log.i("MonitorService", "Unblocking app: $packageToEvaluate")
                                     lockManager?.hideLockScreen()
                                }
                            }
                        }
                    } else if (packageToEvaluate == null) {
                        // If no package detected (e.g. permission missing), we might want to default to block if we are extra strict
                        // but for now let's just log it.
                        // Log.w("MonitorService", "Could not detect foreground package - verification needed")
                    }
                } catch (e: Exception) {
                    Log.e("MonitorService", "Error monitoring usage", e)
                }
                delay(1000)
            }
        }
    }

    private fun getForegroundPackage(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? android.app.usage.UsageStatsManager ?: return null
        val time = System.currentTimeMillis()
        // Query events for the last 2 minutes to capture recent activity
        val events = usm.queryEvents(time - 1000 * 120, time) 
        val event = android.app.usage.UsageEvents.Event()
        var lastPackage: String? = null
        var lastEventTime = 0L
        
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                if (event.timeStamp >= lastEventTime) {
                    lastEventTime = event.timeStamp
                    lastPackage = event.packageName
                }
            }
        }
        
        // Fallback to UsageStats if no recent event found (e.g. app open for > 2 mins)
        if (lastPackage == null) {
             val stats = usm.queryUsageStats(android.app.usage.UsageStatsManager.INTERVAL_DAILY, time - 1000 * 60, time)
             lastPackage = stats?.maxByOrNull { it.lastTimeUsed }?.packageName
        }
        
        return lastPackage
    }

    private fun startForeground() {
        val disguises = listOf(
            Pair(R.string.stealth_battery, R.string.stealth_battery_desc),
            Pair(R.string.stealth_update, R.string.stealth_update_desc),
            Pair(R.string.stealth_wifi, R.string.stealth_wifi_desc),
            Pair(R.string.stealth_sync, R.string.stealth_sync_desc)
        )
        val (titleId, descId) = disguises.random()

        val notification = NotificationCompat.Builder(this, ChildApp.CHANNEL_ID)
            .setContentTitle(getString(titleId))
            .setContentText(getString(descId))
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setPriority(NotificationCompat.PRIORITY_MIN) // Low priority for stealth
            .setShowWhen(false)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
    }
}
