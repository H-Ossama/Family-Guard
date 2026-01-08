package com.parentalguard.parent.network

import com.parentalguard.common.model.*
import com.parentalguard.common.network.CommandType
import com.parentalguard.common.network.Packet
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceClient {
    private val json = Json { 
        prettyPrint = true 
        ignoreUnknownKeys = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(WebSockets)
    }

    fun observeEvents(ip: String, port: Int): Flow<Packet.Event> = flow {
        client.webSocket(method = HttpMethod.Get, host = ip, port = port, path = "/events") {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try {
                        val packet = json.decodeFromString<Packet>(text)
                        if (packet is Packet.Event) {
                            emit(packet)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    suspend fun getStats(ip: String, port: Int, includeIcons: Boolean = false): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            val response: Packet.Response = client.get("http://$ip:$port/stats") {
                if (includeIcons) {
                    parameter("includeIcons", "true")
                }
            }.body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateRules(ip: String, port: Int, rules: List<BlockingRule>): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            val command = Packet.Command(
                commandType = CommandType.UPDATE_RULES,
                ruleSet = RuleSet(rules)
            )
            val response: Packet.Response = client.post("http://$ip:$port/rules") {
                setBody(command)
                header("Content-Type", "application/json")
            }.body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun updateCategoryLimits(ip: String, port: Int, categoryLimits: List<CategoryLimit>): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            val command = Packet.Command(
                commandType = CommandType.UPDATE_RULES,
                ruleSet = RuleSet(emptyList(), categoryLimits)
            )
            val response: Packet.Response = client.post("http://$ip:$port/rules") {
                setBody(command)
                header("Content-Type", "application/json")
            }.body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun getDailyReport(ip: String, port: Int): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            val response: Packet.Response = client.get("http://$ip:$port/daily-report").body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun approveUnlock(ip: String, port: Int, durationMs: Long, packageName: String? = null): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            val command = Packet.Command(
                commandType = CommandType.APPROVE_UNLOCK,
                unlockDurationMs = durationMs,
                packageName = packageName
            )
            val response: Packet.Response = client.post("http://$ip:$port/unlock-response") {
                setBody(command)
                header("Content-Type", "application/json")
            }.body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun denyUnlock(ip: String, port: Int): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            val command = Packet.Command(commandType = CommandType.DENY_UNLOCK)
            val response: Packet.Response = client.post("http://$ip:$port/unlock-response") {
                setBody(command)
                header("Content-Type", "application/json")
            }.body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun resetPin(ip: String, port: Int): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            // For now, let's assume APPROVE_UNLOCK with a special flag or just a new CommandType if I added it.
            // Wait, did I add RESET_PIN to CommandType? Let's check Packet.kt.
            val response: Packet.Response = client.post("http://$ip:$port/reset-pin") {
                header("Content-Type", "application/json")
            }.body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateDeviceName(ip: String, port: Int, newName: String): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            val command = Packet.Command(
                commandType = CommandType.UPDATE_DEVICE_NAME,
                deviceName = newName
            )
            val response: Packet.Response = client.post("http://$ip:$port/device-name") {
                setBody(command)
                header("Content-Type", "application/json")
            }.body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun setLock(ip: String, port: Int, locked: Boolean): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            val type = if (locked) CommandType.LOCK_DEVICE else CommandType.UNLOCK_DEVICE
            val command = Packet.Command(commandType = type)
            
            val response: Packet.Response = client.post("http://$ip:$port/lock") {
                setBody(command)
                header("Content-Type", "application/json")
            }.body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun setAppIconVisibility(ip: String, port: Int, visible: Boolean): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            val type = if (visible) CommandType.UNHIDE_APP else CommandType.HIDE_APP
            val command = Packet.Command(commandType = type)
            val path = if (visible) "/unhide" else "/hide"
            val response: Packet.Response = client.post("http://$ip:$port$path") {
                setBody(command)
                header("Content-Type", "application/json")
            }.body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun setAppCategory(ip: String, port: Int, packageName: String, category: AppCategory): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            val command = Packet.Command(
                commandType = CommandType.SET_APP_CATEGORY,
                packageName = packageName,
                category = category
            )
            val response: Packet.Response = client.post("http://$ip:$port/device-name") { // Using existing endpoint or creating new one?
                // CommandServer handles SET_APP_CATEGORY in the same routing block as /device-name if I modified it that way?
                // Wait, I added it to /device-name block or similar? 
                // Let's check CommandServer.kt again. I added it as `else if` in the `/device-name` block.
                // It's cleaner to use a generic command endpoint or the same one if generic.
                setBody(command)
                header("Content-Type", "application/json")
            }.body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun setAppTimer(ip: String, port: Int, packageName: String, durationMs: Long): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            val command = Packet.Command(
                commandType = CommandType.SET_APP_TIMER,
                packageName = packageName,
                timerDurationMs = durationMs
            )
            val response: Packet.Response = client.post("http://$ip:$port/device-name") { // Reusing endpoint as it handles polymorphic commands now
                setBody(command)
                header("Content-Type", "application/json")
            }.body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    suspend fun setCategoryTimer(ip: String, port: Int, category: AppCategory, durationMs: Long): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            val command = Packet.Command(
                commandType = CommandType.SET_CATEGORY_TIMER,
                category = category,
                timerDurationMs = durationMs
            )
            val response: Packet.Response = client.post("http://$ip:$port/device-name") {
                setBody(command)
                header("Content-Type", "application/json")
            }.body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun setLanguage(ip: String, port: Int, languageCode: String): Packet.Response? = withContext(Dispatchers.IO) {
        try {
            val command = Packet.Command(
                commandType = CommandType.SET_LANGUAGE,
                languageCode = languageCode
            )
            val response: Packet.Response = client.post("http://$ip:$port/device-name") {
                setBody(command)
                header("Content-Type", "application/json")
            }.body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

