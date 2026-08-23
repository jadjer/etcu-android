package by.jadjer.etcu.data.source.ble

import java.util.UUID

object BleConstants {
    // BLE UUIDs
    val SERVICE_UUID: UUID = UUID.fromString("019fa351-08ac-76bf-b925-fe3ae2f765fb")
    val TELEMETRY_UUID: UUID = UUID.fromString("019fa351-08ac-7940-a519-6ef5087c0329")
    val CONTROL_UUID: UUID = UUID.fromString("019fa351-08ac-7309-804b-ad328e7c1ef1")
    val SYSTEM_INFO_UUID: UUID = UUID.fromString("019fa351-08ac-7b10-8521-fe3ae2f765fb")
    val OTA_UUID: UUID = UUID.fromString("019fa351-08ac-7d45-8718-b4aa5af6756a")
    val DESCRIPTION_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // Connection parameters
    const val DEFAULT_MTU = 517

    // SharedPreferences
    const val PREFS_NAME = "ble_prefs"
    const val KEY_LAST_MAC = "last_mac"

    // Navigation Routes
    const val ROUTE_MAIN = "main"

    // GitHub OTA
    const val GITHUB_OWNER = "jadjer"
    const val GITHUB_REPO = "etcu" 
}
