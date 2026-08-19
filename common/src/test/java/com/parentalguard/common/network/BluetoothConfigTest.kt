package com.parentalguard.common.network

import org.junit.Assert.assertEquals
import org.junit.Test

class BluetoothConfigTest {
    @Test
    fun humanNameKeepsStableDeviceIdDiscoverable() {
        val name = BluetoothConfig.bluetoothNameFor("1348G4", "Mohammed S24 Ultra")

        assertEquals("1348G4", BluetoothConfig.deviceIdFromBluetoothName(name))
    }

    @Test
    fun legacyPrefixStillParses() {
        assertEquals("1348G4", BluetoothConfig.deviceIdFromBluetoothName("PG_Child_1348G4"))
    }
}
