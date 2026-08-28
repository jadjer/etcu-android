package by.jadjer.etcu.data.ble

import java.util.UUID

object BLEConstants {
    // BLE UUIDs
    val SERVICE_UUID: UUID = UUID.fromString("019fa351-08ac-76bf-b925-fe3ae2f765fb")
    val TELEMETRY_UUID: UUID = UUID.fromString("019fa351-08ac-7940-a519-6ef5087c0329")
    val CONTROL_UUID: UUID = UUID.fromString("019fa351-08ac-7309-804b-ad328e7c1ef1")
    val SYSTEM_INFO_UUID: UUID = UUID.fromString("01a044f2-cf05-7494-aef5-a5298c878532")
    val OTA_UUID: UUID = UUID.fromString("019fa351-08ac-7d45-8718-b4aa5af6756a")
    val DESCRIPTION_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // Connection & OTA parameters
    const val REQUESTED_MTU = 517
    const val MAX_OTA_PAYLOAD_SIZE = 244

    // SharedPreferences
    const val PREFS_NAME = "ble_prefs"
    const val KEY_LAST_MAC = "last_mac"
}
