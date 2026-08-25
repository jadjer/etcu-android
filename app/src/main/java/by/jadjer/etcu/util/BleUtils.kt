package by.jadjer.etcu.util

import kotlin.math.pow

object BleUtils {
    /**
     * Calculates approximate distance in meters based on RSSI.
     * Uses Log-distance path loss model.
     */
    fun calculateDistance(rssi: Int): Double {
        if (rssi == 0) return 0.0
        val txPower = -59 // RSSI at 1 meter for typical BLE devices
        return 10.0.pow((txPower - rssi) / (10.0 * 2.0))
    }
}
