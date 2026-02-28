package com.parentalguard.child.network

import android.content.Context
import android.util.Log
import com.parentalguard.common.network.CloudConfig
import com.parentalguard.common.network.Packet
import com.parentalguard.child.utils.DeviceUtils
import com.parentalguard.child.data.RuleRepository
import com.parentalguard.child.monitor.UsageMonitor
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class RegisterRequest(
    val deviceId: String,
    val deviceType: String,
    val parentId: String? = null
)

@Serializable
data class RegisterResponse(
    val token: String
)

@Serializable
data class RelayMessage(
    val targetDeviceId: String? = null,
    val fromDeviceId: String? = null,
    val type: String,
    val payload: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class CloudRelayClient(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(WebSockets)
    }

    private var session: DefaultClientWebSocketSession? = null
    private var job: Job? = null
    private val deviceId = DeviceUtils.getDeviceId(context)
    private val parentId: String?
        get() = context.getSharedPreferences("relay_prefs", Context.MODE_PRIVATE).getString("parent_id", null)

    fun start() {
        if (job?.isActive == true) return
        job = CoroutineScope(Dispatchers.IO).launch {
            var currentDelay = 5000L
            while (isActive) {
                try {
                    connect()
                    // If we return here, it means connection closed cleanly or after a successful session
                    // Reset backoff only if we had a successful long-lived connection, 
                    // but for now, resetting to base delay is fine as we are reconnecting.
                    currentDelay = 5000L
                } catch (e: java.nio.channels.UnresolvedAddressException) {
                    Log.e("CloudRelayClient", "DNS Resolution failed. Retrying in ${currentDelay / 1000}s")
                    delay(currentDelay)
                    currentDelay = (currentDelay * 2).coerceAtMost(60000L)
                } catch (e: Exception) {
                    Log.e("CloudRelayClient", "Connection error: ${e.message}. Retrying in ${currentDelay / 1000}s", e)
                    delay(currentDelay)
                    currentDelay = (currentDelay * 2).coerceAtMost(60000L)
                }
            }
        }
    }

    private suspend fun connect() {
        // 1. Get Token
        // This will throw if it fails, which is what we want for the retry logic in start()
        val response = client.post(CloudConfig.BASE_URL + CloudConfig.ENDPOINT_REGISTER) {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(deviceId, "child"))
        }.body<RegisterResponse>()

        val token = response.token
        Log.i("CloudRelayClient", "Cloud Token obtained. Connecting to WebSocket...")

        // 2. Connect WebSocket
        client.webSocket(CloudConfig.WS_URL + "?token=$token") {
            session = this
            Log.i("CloudRelayClient", "Connected to Cloud Relay")
            
            // Listen for messages
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    handleIncomingMessage(frame.readText())
                }
            }
        }
        // WebSocket session closed
        Log.i("CloudRelayClient", "Cloud Relay Session disconnected")
    }
    private suspend fun handleIncomingMessage(text: String) {
        try {
            val relayWrapper = json.decodeFromString<RelayMessage>(text)
            if (relayWrapper.type == "COMMAND" && relayWrapper.payload != null) {
                val packet = json.decodeFromString<Packet>(relayWrapper.payload)
                if (packet is Packet.Command) {
                    Log.i("CloudRelayClient", "Received Command: ${packet.commandType}")
                    val response = dispatchCommand(packet)
                    
                    // Send response back to the sender
                    sendToRelay(RelayMessage(
                        targetDeviceId = relayWrapper.fromDeviceId,
                        type = "RESPONSE",
                        payload = json.encodeToString<Packet>(response)
                    ))
                }
            } else {
                Log.d("CloudRelayClient", "Received relay message: type=${relayWrapper.type}, hasPayload=${relayWrapper.payload != null}")
            }
        } catch (e: Exception) {
            Log.e("CloudRelayClient", "Error handling message", e)
        }
    }

    private suspend fun dispatchCommand(command: Packet.Command): Packet.Response {
        return withContext(Dispatchers.IO) { // Changed to IO
            try {
                when (command.commandType) {
                    com.parentalguard.common.network.CommandType.GET_STATS -> {
                        val monitor = UsageMonitor(context)
                        val battery = DeviceUtils.getBatteryLevel(context)
                        val includeIcons = command.includeIcons ?: false
                        val apps = DeviceUtils.getInstalledApps(context, includeIcons)
                        
                        Packet.Response(true, stats = com.parentalguard.common.model.DeviceStats(
                            batteryLevel = battery,
                            lastSeenTimestamp = System.currentTimeMillis(),
                            usageLogs = monitor.getTodayUsage(),
                            installedApps = apps,
                            hourlyBreakdown = monitor.getHourlyBreakdown(),
                            activeRules = RuleRepository.rules.value,
                            isLocked = RuleRepository.globalLock.value,
                            appTimers = RuleRepository.appTimers.value,
                            categoryTimers = RuleRepository.categoryTimers.value,
                            deviceName = DeviceUtils.getDeviceName(context)
                        ))
                    }
                    com.parentalguard.common.network.CommandType.LOCK_DEVICE -> {
                        RuleRepository.setGlobalLock(true)
                        Packet.Response(true, "Locked")
                    }
                    com.parentalguard.common.network.CommandType.UNLOCK_DEVICE -> {
                        RuleRepository.setGlobalLock(false)
                        Packet.Response(true, "Unlocked")
                    }
                    com.parentalguard.common.network.CommandType.UPDATE_RULES -> {
                        if (command.ruleSet != null) {
                            RuleRepository.updateRules(command.ruleSet!!.rules)
                            RuleRepository.updateCategoryLimits(command.ruleSet!!.categoryLimits)
                            RuleRepository.setTemporaryUnlock(command.ruleSet!!.temporaryUnlockUntil)
                            RuleRepository.setGlobalLockUntil(command.ruleSet!!.globalLockUntil)
                            Packet.Response(true, "Rules updated")
                        } else {
                            Packet.Response(false, "Invalid rule set")
                        }
                    }
                    com.parentalguard.common.network.CommandType.UPDATE_DEVICE_NAME -> {
                        // Persist custom name if needed, for now just ACK
                        Packet.Response(true, "Device name updated")
                    }
                    com.parentalguard.common.network.CommandType.SET_APP_CATEGORY -> {
                        if (command.packageName != null && command.category != null) {
                            RuleRepository.setCustomCategory(command.packageName!!, command.category!!)
                            Packet.Response(true, "Category updated")
                        } else {
                            Packet.Response(false, "Invalid category data")
                        }
                    }
                    com.parentalguard.common.network.CommandType.SET_APP_TIMER -> {
                        if (command.packageName != null && command.timerDurationMs != null) {
                            RuleRepository.setAppTimer(command.packageName!!, command.timerDurationMs!!)
                            Packet.Response(true, "App timer set")
                        } else {
                            Packet.Response(false, "Invalid timer data")
                        }
                    }
                    com.parentalguard.common.network.CommandType.SET_CATEGORY_TIMER -> {
                        if (command.category != null && command.timerDurationMs != null) {
                            RuleRepository.setCategoryTimer(command.category!!, command.timerDurationMs!!)
                            Packet.Response(true, "Category timer set")
                        } else {
                            Packet.Response(false, "Invalid timer data")
                        }
                    }
                    com.parentalguard.common.network.CommandType.HIDE_APP -> {
                        setAppVisibility(false)
                        Packet.Response(true, "App hidden")
                    }
                    com.parentalguard.common.network.CommandType.UNHIDE_APP -> {
                        setAppVisibility(true)
                        Packet.Response(true, "App unhidden")
                    }
                    com.parentalguard.common.network.CommandType.APPROVE_UNLOCK -> {
                        val duration = command.unlockDurationMs ?: (10 * 60 * 1000L)
                        if (command.packageName != null) {
                            RuleRepository.setAppTimer(command.packageName!!, duration)
                            Packet.Response(true, "App unlock approved")
                        } else {
                            val unlockUntil = System.currentTimeMillis() + duration
                            RuleRepository.setTemporaryUnlock(unlockUntil)
                            Packet.Response(true, "Device unlock approved")
                        }
                    }
                    com.parentalguard.common.network.CommandType.DENY_UNLOCK -> {
                        Packet.Response(true, "Unlock denied")
                    }
                    com.parentalguard.common.network.CommandType.RESET_PIN -> {
                        com.parentalguard.child.security.PinManager.resetPin(context)
                        Packet.Response(true, "PIN reset successfully")
                    }
                    com.parentalguard.common.network.CommandType.SET_LANGUAGE -> {
                        if (command.languageCode != null) {
                             updateLanguage(command.languageCode!!)
                             Packet.Response(true, "Language updating...")
                        } else {
                             Packet.Response(false, "Invalid language code")
                        }
                    }
                    com.parentalguard.common.network.CommandType.SET_RELAY_PARENT_ID -> {
                        if (command.relayParentId != null) {
                            val prefs = context.getSharedPreferences("relay_prefs", Context.MODE_PRIVATE)
                            prefs.edit().putString("parent_id", command.relayParentId).apply()
                            Packet.Response(true, "Relay Parent ID updated")
                        } else {
                            Packet.Response(false, "Invalid relay parent ID")
                        }
                    }
                    else -> Packet.Response(false, "Command not implemented: ${command.commandType}")
                }
            } catch (e: Exception) {
                Packet.Response(false, "Error: ${e.message}")
            }
        }
    }

    private fun setAppVisibility(visible: Boolean) {
        val p = context.packageManager
        val componentName = android.content.ComponentName(context, "com.parentalguard.child.MainActivity")
        p.setComponentEnabledSetting(
            componentName,
            if (visible) android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED 
            else android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            android.content.pm.PackageManager.DONT_KILL_APP
        )
    }

    private fun updateLanguage(languageCode: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("language_code", languageCode).apply()
        
        val locale = java.util.Locale(languageCode)
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        
        val i = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (i != null) {
            i.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
            i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }


    private suspend fun sendToRelay(message: RelayMessage) {
        try {
            session?.send(Frame.Text(json.encodeToString(message)))
        } catch (e: Exception) {
            Log.e("CloudRelayClient", "Failed to send to relay", e)
        }
    }

    suspend fun sendEvent(event: Packet.Event) {
        val payload = json.encodeToString<Packet>(event)
        val message = RelayMessage(
            targetDeviceId = parentId,
            type = "EVENT",
            payload = payload
        )
        sendToRelay(message)
    }

    fun stop() {
        job?.cancel()
        job = null
        session = null
    }
}
