package com.parentalguard.child.network

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.parentalguard.common.network.BluetoothConfig
import com.parentalguard.common.network.Packet
import com.parentalguard.child.utils.DeviceUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.security.MessageDigest

/**
 * Classic Bluetooth RFCOMM (SPP) server. Tunnels the exact same [Packet] JSON
 * protocol used over HTTP/WebSocket, so the parent can control the child even
 * when they are not on the same WiFi network.
 *
 * Framing: 4-byte big-endian length prefix followed by the JSON bytes
 * (JSON may contain newlines, so line-based framing is unsafe).
 */
class BluetoothCommandServer(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val connections = ConcurrentHashMap<BluetoothSocket, Job>()

    private var adapter: BluetoothAdapter? = null
    private var serverSocket: BluetoothServerSocket? = null
    private var acceptJob: Job? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    fun start() {
        if (acceptJob?.isActive == true) return

        val a = BluetoothAdapter.getDefaultAdapter()
        if (a == null) {
            Log.w(TAG, "Device has no Bluetooth adapter")
            return
        }
        if (!a.isEnabled) {
            Log.w(TAG, "Bluetooth is disabled; RFCOMM server not started")
            return
        }
        adapter = a

        // Best-effort: advertise a recognizable adapter name for parent discovery.
        val btName = BluetoothConfig.bluetoothNameFor(
            DeviceUtils.getDeviceId(context),
            DeviceUtils.getDeviceName(context)
        )
        try {
            if (a.name != btName) a.name = btName
            Log.i(TAG, "Bluetooth RFCOMM identity: $btName")
        } catch (e: Exception) {
            Log.d(TAG, "Could not set Bluetooth name: ${e.message}")
        }

        acceptJob = scope.launch {
            try {
                serverSocket = a.listenUsingRfcommWithServiceRecord(
                    SERVICE_RECORD_NAME,
                    BluetoothConfig.SPP_UUID
                )
                Log.i(TAG, "RFCOMM server listening on ${BluetoothConfig.SPP_UUID}")
                _isRunning.value = true

                while (isActive) {
                    val socket = try {
                        serverSocket?.accept()
                    } catch (e: Exception) {
                        if (isActive) Log.e(TAG, "Accept failed", e)
                        null
                    }
                    if (socket != null && isActive) {
                        handleConnection(socket)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "RFCOMM server error", e)
            } finally {
                _isRunning.value = false
                try { serverSocket?.close() } catch (_: Exception) {}
                serverSocket = null
            }
        }
    }

    private fun handleConnection(socket: BluetoothSocket) {
        val job = scope.launch {
            try {
                Log.i(TAG, "Client connected: ${socket.remoteDevice.name}")
                val input = socket.inputStream
                val output = socket.outputStream
                while (isActive) {
                    val command = readCommand(input) ?: break
                    val response = if (isAuthorized(command)) {
                        CommandDispatcher.dispatch(context, command.copy(authToken = null))
                    } else {
                        Packet.Response(false, "Unauthorized", requestId = command.requestId)
                    }
                    writeResponse(output, response)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Connection handling ended: ${e.message}")
            } finally {
                try { socket.close() } catch (_: Exception) {}
                connections.remove(socket)
                Log.i(TAG, "Client disconnected")
            }
        }
        connections[socket] = job
    }

    private fun isAuthorized(command: Packet.Command): Boolean {
        // Stats/reports/ping are allowed for discovery; every mutation needs the QR token.
        if (command.commandType == com.parentalguard.common.network.CommandType.GET_STATS ||
            command.commandType == com.parentalguard.common.network.CommandType.SEND_DAILY_REPORT ||
            command.commandType == com.parentalguard.common.network.CommandType.PING
        ) return true

        val expected = PairingManager.getToken(context) ?: return false
        val provided = command.authToken ?: return false
        return MessageDigest.isEqual(
            expected.toByteArray(Charsets.UTF_8),
            provided.toByteArray(Charsets.UTF_8)
        )
    }

    private suspend fun readCommand(input: InputStream): Packet.Command? = withContext(Dispatchers.IO) {
        try {
            val lenBytes = readFully(input, 4) ?: return@withContext null
            val len = ByteBuffer.wrap(lenBytes).int
            if (len <= 0 || len > BluetoothConfig.MAX_PACKET_SIZE) {
                Log.w(TAG, "Invalid frame length $len")
                return@withContext null
            }
            val body = readFully(input, len) ?: return@withContext null
            val packet = json.decodeFromString<Packet>(String(body, Charsets.UTF_8))
            packet as? Packet.Command
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read command", e)
            null
        }
    }

    private suspend fun writeResponse(output: OutputStream, response: Packet.Response) = withContext(Dispatchers.IO) {
        try {
            val bytes = json.encodeToString<Packet>(response).toByteArray(Charsets.UTF_8)
            output.write(ByteBuffer.allocate(4).putInt(bytes.size).array())
            output.write(bytes)
            output.flush()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write response", e)
        }
    }

    private fun readFully(input: InputStream, count: Int): ByteArray? {
        val buffer = ByteArray(count)
        var offset = 0
        while (offset < count) {
            val read = input.read(buffer, offset, count - offset)
            if (read < 0) return null // EOF
            offset += read
        }
        return buffer
    }

    fun stop() {
        acceptJob?.cancel()
        acceptJob = null
        connections.values.forEach { it.cancel() }
        connections.clear()
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        _isRunning.value = false
    }

    companion object {
        private const val TAG = "BluetoothCmdServer"
        private const val SERVICE_RECORD_NAME = "ParentalGuard"
    }
}
