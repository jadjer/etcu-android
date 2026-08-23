package by.jadjer.etcu.data.repository

import android.bluetooth.BluetoothDevice
import by.jadjer.etcu.data.source.ble.BleManager
import by.jadjer.etcu.data.model.*
import kotlinx.coroutines.flow.StateFlow

class BleRepository(
    private val bleManager: BleManager
) {
    val connectionState: StateFlow<String> = bleManager.connectionState
    val isConnected: StateFlow<Boolean> = bleManager.isConnected
    val telemetry: StateFlow<SystemTelemetry> = bleManager.telemetry
    val systemInfo: StateFlow<SystemInfo> = bleManager.systemInfo
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = bleManager.scanner.discoveredDevices
    val otaFeedback: StateFlow<Int?> = bleManager.otaFeedback

    fun startScan() = bleManager.scanner.startScan()
    fun stopScan() = bleManager.scanner.stopScan()
    fun connect(macAddress: String) = bleManager.connect(macAddress)
    fun disconnect() = bleManager.disconnect()
    fun autoConnect() = bleManager.autoConnect()
    fun clearLastMac() = bleManager.clearLastMac()
    fun getPairedDevices() = bleManager.scanner.getPairedDevices()
    fun sendControlData(data: BleControlData) = bleManager.writeControlData(data)
    fun sendOtaChunk(chunk: OtaChunk) = bleManager.writeOtaChunk(chunk)
}
