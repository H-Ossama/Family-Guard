package com.parentalguard.parent.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.parentalguard.common.network.EventType
import com.parentalguard.common.network.Packet
import com.parentalguard.parent.R
import com.parentalguard.parent.network.DeviceClient
import com.parentalguard.parent.ui.components.NotificationHelper
import com.parentalguard.parent.data.DeviceRepository
import com.parentalguard.parent.viewmodel.ChildDevice
import kotlinx.coroutines.*
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

class NotificationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val deviceClient = DeviceClient()
    private val observationJobs = ConcurrentHashMap<String, Job>()
    
    // In-memory cache of known devices (IP -> Device)
    private val knownDevices = ConcurrentHashMap<String, ChildDevice>()
    private lateinit var deviceRepository: DeviceRepository

    companion object {
        private const val CHANNEL_ID = "parent_monitor_service"
        private const val ACTION_START_MONITORING = "com.parentalguard.parent.action.START_MONITORING"
        private const val EXTRA_IP = "extra_ip"
        private const val EXTRA_PORT = "extra_port"
        private const val EXTRA_NAME = "extra_name"

        fun startMonitoring(context: Context, device: ChildDevice) {
            val intent = Intent(context, NotificationService::class.java).apply {
                action = ACTION_START_MONITORING
                putExtra(EXTRA_IP, device.ip.hostAddress)
                putExtra(EXTRA_PORT, device.port)
                putExtra(EXTRA_NAME, device.customName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        deviceRepository = DeviceRepository(this)
        loadSavedDevices()
        createNotificationChannel()
        startForeground(1, createNotification())
    }

    private fun loadSavedDevices() {
        val saved = deviceRepository.loadDevices()
        saved.forEach { device ->
            knownDevices[device.ip.hostAddress ?: ""] = device
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START_MONITORING) {
            val ip = intent.getStringExtra(EXTRA_IP)
            val port = intent.getIntExtra(EXTRA_PORT, 0)
            val name = intent.getStringExtra(EXTRA_NAME) ?: "Unknown"

            if (ip != null && port != 0) {
                 try {
                     val device = ChildDevice(name, InetAddress.getByName(ip), port, name)
                     knownDevices[ip] = device // Update or add
                     observeDeviceEvents(device)
                 } catch (e: Exception) {
                     Log.e("NotificationService", "Error adding device", e)
                 }
            }
        }
        return START_STICKY
    }

    private fun observeDeviceEvents(device: ChildDevice) {
        val deviceId = device.ip.hostAddress ?: return
        if (observationJobs.containsKey(deviceId)) return

        Log.i("NotificationService", "Starting observation for ${device.customName} ($deviceId)")
        
        observationJobs[deviceId] = serviceScope.launch {
            var retryDelay = 5000L
            while (isActive) {
                try {
                    deviceClient.observeEvents(deviceId, device.port).collect { event ->
                        handleEvent(device, event)
                        retryDelay = 5000L
                    }
                } catch (e: java.net.ConnectException) {
                    Log.d("NotificationService", "Connection failed to $deviceId: ${e.message}. Retrying...")
                    delay(retryDelay)
                    retryDelay = (retryDelay * 1.5).toLong().coerceAtMost(60000L)
                } catch (e: Exception) {
                    Log.e("NotificationService", "Error observing $deviceId", e)
                    delay(5000)
                }
            }
        }
    }

    private fun handleEvent(device: ChildDevice, event: Packet.Event) {
        // Resolve the latest name from the repository if possible
        val deviceIp = device.ip.hostAddress ?: ""
        val customName = deviceRepository.getDeviceName(deviceIp) ?: device.customName
        
        Log.i("NotificationService", "Received event: ${event.eventType} from $customName")
        if (event.eventType == EventType.UNLOCK_REQUESTED) {
            NotificationHelper.showUnlockRequestNotification(
                context = this,
                deviceId = deviceIp,
                deviceName = customName, 
                requestType = event.requestType ?: "DEVICE",
                appPackageName = event.appPackageName,
                appName = event.appName
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Parental Monitor Service",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Parental Guard Active")
            .setContentText("Listening for child device events...")
            .setSmallIcon(R.drawable.ic_parent_logo) // Ensure this exists, or use default
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
