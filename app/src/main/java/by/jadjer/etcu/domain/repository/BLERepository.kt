package by.jadjer.etcu.domain.repository

import by.jadjer.etcu.domain.model.ble.*
import by.jadjer.etcu.domain.model.control.*
import by.jadjer.etcu.domain.model.telemetry.*
import by.jadjer.etcu.domain.model.system.*
import by.jadjer.etcu.domain.model.ota.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BLERepository {
    val connectionState: StateFlow<ConnectionState>
    val controlData: StateFlow<ControlData>
    val telemetry: StateFlow<SystemTelemetry>
    val systemInfo: StateFlow<SystemInfo>
    val discoveredDevices: StateFlow<List<DiscoveredDevice>>
    val isScanning: StateFlow<Boolean>
    val otaFeedback: SharedFlow<OTAStatus>
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
