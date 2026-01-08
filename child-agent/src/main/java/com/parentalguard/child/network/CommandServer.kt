package com.parentalguard.child.network

import com.parentalguard.child.data.RuleRepository
import com.parentalguard.child.monitor.UsageMonitor
import com.parentalguard.child.service.MonitorService
import com.parentalguard.common.model.DeviceStats
import com.parentalguard.common.network.CommandType
import com.parentalguard.common.network.Packet
import android.content.Intent
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import android.content.Context
import android.util.Log
import java.time.Duration
import java.util.Collections
import kotlinx.coroutines.launch

class CommandServer(private val context: Context) {
    
    private var server: NettyApplicationEngine? = null
    private val sessions = Collections.synchronizedList(ArrayList<DefaultWebSocketServerSession>())
    private val json = Json { 
        prettyPrint = true 
        ignoreUnknownKeys = true
    }
    
    // Store parent IP for sending unlock requests
    var parentIp: String? = null

    fun start() {
        if (server != null) return

        server = embeddedServer(Netty, port = 8080) {
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }
            install(ContentNegotiation) {
                json(json)
            }

            routing {
                // ... Existing HTTP routes ...

                webSocket("/events") {
                    Log.i("CommandServer", "New WebSocket connection")
                    sessions.add(this)
                    try {
                        for (frame in incoming) {
                            // Keep alive
                        }
                    } catch (e: Exception) {
                        Log.e("CommandServer", "WebSocket error", e)
                    } finally {
                        Log.i("CommandServer", "WebSocket disconnected")
                        sessions.remove(this)
                    }
                }
                
                get("/stats") {
                    val includeIcons = call.request.queryParameters["includeIcons"]?.toBoolean() ?: false
                    // ...
                    val monitor = UsageMonitor(this@CommandServer.context)
                    val logs = monitor.getTodayUsage()
                    
                    val batteryLevel = com.parentalguard.child.utils.DeviceUtils.getBatteryLevel(this@CommandServer.context)
                    val installedApps = com.parentalguard.child.utils.DeviceUtils.getInstalledApps(this@CommandServer.context, includeIcons)
                    val hourlyBreakdown = monitor.getHourlyBreakdown()
                    val activeRules = RuleRepository.rules.value
                    val appTimers = RuleRepository.appTimers.value
                    
                    val isLocked = RuleRepository.globalLock.value
                    
                    val p = this@CommandServer.context.packageManager
                    val componentName = android.content.ComponentName(this@CommandServer.context, "com.parentalguard.child.MainActivity")
                    val isIconHidden = try {
                        p.getComponentEnabledSetting(componentName) == android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    } catch (e: Exception) {
                        false
                    }
                    
                    val stats = DeviceStats(
                        batteryLevel = batteryLevel,
                        lastSeenTimestamp = System.currentTimeMillis(),
                        usageLogs = logs,
                        installedApps = installedApps,
                        hourlyBreakdown = hourlyBreakdown,
                        activeRules = activeRules,
                        isLocked = isLocked,
                        isIconHidden = isIconHidden,
                        appTimers = appTimers,
                        categoryTimers = RuleRepository.categoryTimers.value
                    )
                    call.respond(Packet.Response(true, stats = stats))
                }

                post("/unhide") {
                    try {
                        val packet = call.receive<Packet.Command>()
                        if (packet.commandType == CommandType.UNHIDE_APP) {
                            val p = this@CommandServer.context.packageManager
                            val componentName = android.content.ComponentName(this@CommandServer.context, "com.parentalguard.child.MainActivity")
                            p.setComponentEnabledSetting(
                                componentName,
                                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                android.content.pm.PackageManager.DONT_KILL_APP
                            )
                            Log.i("CommandServer", "Unhide app command executed")
                            call.respond(Packet.Response(true, "App unhidden"))
                        } else {
                            call.respond(Packet.Response(false, "Invalid unhide command"))
                        }
                    } catch (e: Exception) {
                        Log.e("CommandServer", "Error unhiding app", e)
                        call.respond(Packet.Response(false, e.message))
                    }
                }

                post("/hide") {
                    try {
                        val packet = call.receive<Packet.Command>()
                        if (packet.commandType == CommandType.HIDE_APP) {
                            val p = this@CommandServer.context.packageManager
                            val componentName = android.content.ComponentName(this@CommandServer.context, "com.parentalguard.child.MainActivity")
                            p.setComponentEnabledSetting(
                                componentName,
                                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                android.content.pm.PackageManager.DONT_KILL_APP
                            )
                            Log.i("CommandServer", "Hide app command executed")
                            call.respond(Packet.Response(true, "App hidden"))
                        } else {
                            call.respond(Packet.Response(false, "Invalid hide command"))
                        }
                    } catch (e: Exception) {
                        Log.e("CommandServer", "Error hiding app", e)
                        call.respond(Packet.Response(false, e.message))
                    }
                }

                post("/rules") {
                    try {
                        val packet = call.receive<Packet.Command>()
                        if (packet.commandType == CommandType.UPDATE_RULES && packet.ruleSet != null) {
                            RuleRepository.updateRules(packet.ruleSet!!.rules)
                            RuleRepository.updateCategoryLimits(packet.ruleSet!!.categoryLimits)
                            RuleRepository.setTemporaryUnlock(packet.ruleSet!!.temporaryUnlockUntil)
                            RuleRepository.setGlobalLockUntil(packet.ruleSet!!.globalLockUntil)
                            Log.i("CommandServer", "Rules updated: ${packet.ruleSet!!.rules.size} rules, ${packet.ruleSet!!.categoryLimits.size} category limits")
                            call.respond(Packet.Response(true, "Rules updated"))
                        } else {
                            call.respond(Packet.Response(false, "Invalid command"))
                        }
                    } catch (e: Exception) {
                        Log.e("CommandServer", "Error updating rules", e)
                        call.respond(Packet.Response(false, e.message))
                    }
                }

                post("/device-name") {
                    try {
                        val packet = call.receive<Packet.Command>()
                        if (packet.commandType == CommandType.UPDATE_DEVICE_NAME && packet.deviceName != null) {
                            Log.i("CommandServer", "Renaming device to: ${packet.deviceName}")
                            // In a real implementation, we would persist this name.
                             call.respond(Packet.Response(true, "Device renamed"))
                        } else if (packet.commandType == CommandType.SET_APP_CATEGORY && packet.packageName != null && packet.category != null) {
                            val pkgName = packet.packageName!!
                            val cat = packet.category!!
                            RuleRepository.setCustomCategory(pkgName, cat)
                            Log.i("CommandServer", "Set category for $pkgName to $cat")
                            call.respond(Packet.Response(true, "Category updated"))
                        } else if (packet.commandType == CommandType.SET_APP_TIMER && packet.packageName != null && packet.timerDurationMs != null) {
                            val pkgName = packet.packageName!!
                            val duration = packet.timerDurationMs!!
                            RuleRepository.setAppTimer(pkgName, duration)
                            Log.i("CommandServer", "Set timer for $pkgName to ${duration}ms")
                            call.respond(Packet.Response(true, "Timer set"))
                        } else if (packet.commandType == CommandType.SET_CATEGORY_TIMER && packet.category != null && packet.timerDurationMs != null) {
                            val cat = packet.category!!
                            val duration = packet.timerDurationMs!!
                            RuleRepository.setCategoryTimer(cat, duration)
                            Log.i("CommandServer", "Set category timer for $cat to ${duration}ms")
                            call.respond(Packet.Response(true, "Category timer set"))
                        } else if (packet.commandType == CommandType.SET_LANGUAGE && packet.languageCode != null) {
                            Log.i("CommandServer", "Language sync received: ${packet.languageCode}")
                            // We can use this to update localized strings if needed, or just ACK
                            call.respond(Packet.Response(true, "Language synced"))
                        } else {
                             call.respond(Packet.Response(false, "Invalid command"))
                        }
                    } catch (e: Exception) {
                        Log.e("CommandServer", "Error renaming device", e)
                        call.respond(Packet.Response(false, e.message))
                    }
                }
                
                post("/lock") {
                     try {
                         val packet = call.receive<Packet.Command>()
                         if (packet.commandType == CommandType.LOCK_DEVICE) {
                             RuleRepository.setGlobalLock(true)
                             call.respond(Packet.Response(true, "Device Locked"))
                         } else if (packet.commandType == CommandType.UNLOCK_DEVICE) {
                             RuleRepository.setGlobalLock(false)
                             call.respond(Packet.Response(true, "Device Unlocked"))
                         } else if (packet.commandType == CommandType.SET_LANGUAGE && packet.languageCode != null) {
                             // Handle Language Set
                             val languageCode = packet.languageCode!!
                             Log.i("CommandServer", "Setting language to $languageCode")
                             
                             // Persist language
                             val prefs = this@CommandServer.context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                             prefs.edit().putString("language_code", languageCode).apply()
                             
                             // Update Locale
                             val locale = java.util.Locale(languageCode)
                             java.util.Locale.setDefault(locale)
                             val config = android.content.res.Configuration()
                             config.setLocale(locale)
                             this@CommandServer.context.resources.updateConfiguration(config, this@CommandServer.context.resources.displayMetrics)
                             
                             // Restart App to apply changes
                             val i = this@CommandServer.context.packageManager.getLaunchIntentForPackage(this@CommandServer.context.packageName)
                             if (i != null) {
                                 i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                 i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                 this@CommandServer.context.startActivity(i)
                             }
                             
                             call.respond(Packet.Response(true, "Language updated"))
                         } else {
                             call.respond(Packet.Response(false, "Invalid lock/command"))
                         }
                     } catch (e: Exception) {
                         call.respond(Packet.Response(false, e.message))
                     }
                }
                
                // Handle unlock approval/denial from parent
                post("/unlock-response") {
                    try {
                        val packet = call.receive<Packet.Command>()
                        when (packet.commandType) {
                            CommandType.APPROVE_UNLOCK -> {
                                val duration = packet.unlockDurationMs ?: (10 * 60 * 1000L) // Default 10 minutes
                                if (packet.packageName != null) {
                                    RuleRepository.setAppTimer(packet.packageName!!, duration)
                                    Log.i("CommandServer", "App ${packet.packageName} unlock approved for ${duration / 60000} minutes")
                                    call.respond(Packet.Response(true, "App unlock approved"))
                                } else {
                                    val unlockUntil = System.currentTimeMillis() + duration
                                    RuleRepository.setTemporaryUnlock(unlockUntil)
                                    Log.i("CommandServer", "Device unlock approved for ${duration / 60000} minutes")
                                    call.respond(Packet.Response(true, "Device unlock approved"))
                                }
                            }
                            CommandType.DENY_UNLOCK -> {
                                Log.i("CommandServer", "Unlock request denied")
                                call.respond(Packet.Response(true, "Unlock denied"))
                            }
                            else -> {
                                call.respond(Packet.Response(false, "Invalid unlock response"))
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CommandServer", "Error handling unlock response", e)
                        call.respond(Packet.Response(false, e.message))
                    }
                }
                
                // Get daily report
                get("/daily-report") {
                    try {
                        val monitor = UsageMonitor(this@CommandServer.context)
                        val report = monitor.generateDailyReport()
                        call.respond(Packet.Response(true, dailyReport = report))
                    } catch (e: Exception) {
                        Log.e("CommandServer", "Error generating report", e)
                        call.respond(Packet.Response(false, e.message))
                    }
                }
                
                
                // Reset PIN from parent
                post("/reset-pin") {
                    try {
                        com.parentalguard.child.security.PinManager.resetPin(this@CommandServer.context)
                        Log.i("CommandServer", "PIN reset requested by parent")
                        call.respond(Packet.Response(true, "PIN reset successfully"))
                    } catch (e: Exception) {
                        Log.e("CommandServer", "Error resetting PIN", e)
                        call.respond(Packet.Response(false, e.message))
                    }
                }
                
                get("/ping") {
                    call.respond(Packet.Response(true, "Pong"))
                }
            }
        }.start(wait = false)
        Log.i("CommandServer", "Server started on port 8080")
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
    }
    
    /**
     * Send unlock request to parent app
     * This would be called from MainActivity when child taps "Request Unlock"
     */
    suspend fun sendUnlockRequest(message: String): Boolean {
        // This is a simplified version. In production, you'd want to:
        // 1. Queue the request if parent is not currently connected
        // 2. Use a proper HTTP client like Ktor client
        // 3. Handle retries and timeouts
        return try {
            // Parent app would need to be listening for incoming requests
            // For now, this is a placeholder
            Log.i("CommandServer", "Unlock request: $message")
            true
        } catch (e: Exception) {
            Log.e("CommandServer", "Failed to send unlock request", e)
            false
        }
    }

    fun broadcast(event: Packet.Event) {
        val message = json.encodeToString<Packet>(event)
        server?.application?.launch {
            synchronized(sessions) {
                sessions.forEach { session ->
                    launch {
                        try {
                            session.send(Frame.Text(message))
                        } catch (e: Exception) {
                            Log.e("CommandServer", "Broadcast failed", e)
                        }
                    }
                }
            }
        }
    }
}

