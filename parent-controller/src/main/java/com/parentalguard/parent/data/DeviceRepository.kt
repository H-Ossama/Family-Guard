package com.parentalguard.parent.data

import android.content.Context
import android.content.SharedPreferences
import com.parentalguard.parent.viewmodel.ChildDevice
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.InetAddress

@Serializable
data class SavedDevice(
    val name: String,
    val ipAddress: String,
    val port: Int,
    val customName: String
)

class DeviceRepository(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    
    companion object {
        private const val KEY_DEVICES = "saved_devices"
    }
    
    /**
     * Save a list of devices
     */
    fun saveDevices(devices: List<ChildDevice>) {
        val savedDevices = devices.map { device ->
            SavedDevice(
                name = device.name,
                ipAddress = device.ip.hostAddress ?: "",
                port = device.port,
                customName = device.customName
            )
        }
        
        val jsonString = json.encodeToString(savedDevices)
        prefs.edit().putString(KEY_DEVICES, jsonString).apply()
    }
    
    /**
     * Load saved devices
     */
    fun loadDevices(): List<ChildDevice> {
        val jsonString = prefs.getString(KEY_DEVICES, null) ?: return emptyList()
        
        return try {
            val savedDevices = json.decodeFromString<List<SavedDevice>>(jsonString)
            savedDevices.map { saved ->
                ChildDevice(
                    name = saved.name,
                    ip = InetAddress.getByName(saved.ipAddress),
                    port = saved.port,
                    customName = saved.customName
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Update custom name for a device
     */
    fun updateDeviceName(device: ChildDevice, newName: String) {
        val devices = loadDevices().toMutableList()
        val index = devices.indexOfFirst { it.ip == device.ip && it.port == device.port }
        
        if (index != -1) {
            devices[index].customName = newName
            saveDevices(devices)
        }
    }
    
    /**
     * Add a device
     */
    fun addDevice(device: ChildDevice) {
        val devices = loadDevices().toMutableList()
        
        // Check if device already exists
        if (devices.none { it.ip == device.ip && it.port == device.port }) {
            devices.add(device)
            saveDevices(devices)
        }
    }
    
    /**
     * Remove a device
     */
    fun removeDevice(device: ChildDevice) {
        val devices = loadDevices().toMutableList()
        devices.removeAll { it.ip == device.ip && it.port == device.port }
        saveDevices(devices)
    }

    /**
     * Get device name by IP address
     */
    fun getDeviceName(ipAddress: String): String? {
        val devices = loadDevices()
        return devices.find { it.ip.hostAddress == ipAddress }?.customName
    }
}
