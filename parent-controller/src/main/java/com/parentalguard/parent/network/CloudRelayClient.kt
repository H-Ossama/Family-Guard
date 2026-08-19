package com.parentalguard.parent.network

import android.content.Context
import android.util.Log
import com.parentalguard.common.network.CloudConfig
import com.parentalguard.common.network.Packet
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.util.UUID

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
        install(WebSockets) {
            pingInterval = 20000
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
        }
    }

    private var session: DefaultClientWebSocketSession? = null
    private var job: Job? = null
    val parentId: String by lazy {
        val prefs = context.getSharedPreferences("relay_prefs", Context.MODE_PRIVATE)
        var id = prefs.getString("parent_id", null)
        if (id == null) {
            id = "parent_" + java.util.UUID.randomUUID().toString().take(8)
            prefs.edit().putString("parent_id", id).apply()
        }
        id!!
    }

    private val _responses = MutableSharedFlow<Packet.Response>(replay = 0, extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val responses: SharedFlow<Packet.Response> = _responses.asSharedFlow()

    private val _events = MutableSharedFlow<Packet.Event>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<Packet.Event> = _events.asSharedFlow()

    fun start() {
        if (job?.isActive == true) return
        job = CoroutineScope(Dispatchers.IO).launch {
            var currentDelay = 5000L
            while (isActive) {
                try {
                    connect()
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
        Log.i("CloudRelayClient", "Registering with Cloud Relay as $parentId...")
        // 1. Get Token
        // This will throw if it fails, allowing start() to handle backoff
        val response = client.post(CloudConfig.BASE_URL + CloudConfig.ENDPOINT_REGISTER) {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(parentId, "parent"))
        }.body<RegisterResponse>()

        val token = response.token
        Log.i("CloudRelayClient", "Connected! WebSocket starting with token: ${token.take(10)}...")

        // 2. Connect WebSocket
        client.webSocket(CloudConfig.WS_URL + "?token=$token") {
            session = this
            Log.i("CloudRelayClient", "WebSocket Session Active")
            
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    handleIncomingMessage(frame.readText())
                }
            }
        }
        Log.i("CloudRelayClient", "Cloud Relay Session disconnected")
    }

    private suspend fun handleIncomingMessage(text: String) {
        try {
            val relayWrapper = json.decodeFromString<RelayMessage>(text)
            if (relayWrapper.targetDeviceId == parentId || relayWrapper.targetDeviceId == null) {
                when (relayWrapper.type) {
                    "RESPONSE" -> {
                        if (relayWrapper.payload != null) {
                            val packet = json.decodeFromString<Packet>(relayWrapper.payload)
                            if (packet is Packet.Response) {
                                _responses.emit(packet)
                            }
                        }
                    }
                    "EVENT" -> {
                        if (relayWrapper.payload != null) {
                            val packet = json.decodeFromString<Packet>(relayWrapper.payload)
                            if (packet is Packet.Event) {
                                // Populate deviceId from the relay wrapper if it's missing in the packet
                                val eventWithId = if (packet.deviceId == null) {
                                    packet.copy(deviceId = relayWrapper.fromDeviceId)
                                } else {
                                    packet
                                }
                                _events.emit(eventWithId)
                            }
                        }
                    }
                    "ERROR" -> {
                        Log.w("CloudRelayClient", "Relay ERROR: ${relayWrapper.payload}")
                    }
                    else -> {
                        Log.d("CloudRelayClient", "Received relay message: type=${relayWrapper.type}, hasPayload=${relayWrapper.payload != null}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CloudRelayClient", "Error handling message: $text", e)
        }
    }

    suspend fun sendCommand(targetDeviceId: String, command: Packet.Command): Packet.Response? = withContext(Dispatchers.IO) {
        if (session == null) {
            Log.e("CloudRelayClient", "Cannot send command: No active session")
            return@withContext null
        }

        val requestId = java.util.UUID.randomUUID().toString()
        val payload = json.encodeToString<Packet>(command.copy(requestId = requestId))
        val message = RelayMessage(
            targetDeviceId = targetDeviceId,
            type = "COMMAND",
            payload = payload
        )

        try {
            // Subscribe before sending so we never miss a fast relayed response.
            return@withContext withTimeoutOrNull(10000) {
                val responseDeferred = async { responses.first { it.requestId == requestId } }
                session?.send(Frame.Text(json.encodeToString(message)))
                responseDeferred.await()
            }
        } catch (e: Exception) {
            Log.e("CloudRelayClient", "Error sending command", e)
            null
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        session = null
    }
}
