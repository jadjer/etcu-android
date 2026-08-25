package by.jadjer.etcu.domain.repository

import by.jadjer.etcu.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface BleRepository {
    val connectionState: StateFlow<String>
    val isConnected: StateFlow<Boolean>
    val telemetry: StateFlow<SystemTelemetry>
    val systemInfo: StateFlow<SystemInfo>
    val discoveredDevices: StateFlow<List<DiscoveredDevice>>
    val isScanning: StateFlow<Boolean>
    val otaFeedback: StateFlow<Int?>
    val savedMac: StateFlow<String?>

    fun startScan()
    fun stopScan()
    fun connect(macAddress: String)
    fun disconnect()
    fun autoConnect()
    fun clearLastMac()
    fun getPairedDevices(): List<DiscoveredDevice>
    fun sendControlData(data: BleControlData)
    fun sendOtaChunk(chunk: OtaChunk)
}
