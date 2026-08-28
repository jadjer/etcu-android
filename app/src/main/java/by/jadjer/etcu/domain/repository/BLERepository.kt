package by.jadjer.etcu.domain.repository

import by.jadjer.etcu.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface BLERepository {
    val connectionState: StateFlow<String>
    val isConnected: StateFlow<Boolean>
    val controlData: StateFlow<ControlData>
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
    fun sendControlData(data: ControlData)
    fun sendOtaChunk(chunk: OTAChunk)
}
