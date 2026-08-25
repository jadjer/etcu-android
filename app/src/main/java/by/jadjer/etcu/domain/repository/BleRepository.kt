package by.jadjer.etcu.domain.repository

import android.bluetooth.BluetoothDevice
import by.jadjer.etcu.data.model.*
import kotlinx.coroutines.flow.StateFlow

interface BleRepository {
    val connectionState: StateFlow<String>
    val isConnected: StateFlow<Boolean>
    val telemetry: StateFlow<SystemTelemetry>
    val systemInfo: StateFlow<SystemInfo>
    val discoveredDevices: StateFlow<List<Pair<BluetoothDevice, Int>>>
    val isScanning: StateFlow<Boolean>
    val otaFeedback: StateFlow<Int?>
    val savedMac: StateFlow<String?>

    fun startScan()
    fun stopScan()
    fun connect(macAddress: String)
    fun disconnect()
    fun autoConnect()
    fun clearLastMac()
    fun getPairedDevices(): List<BluetoothDevice>
    fun sendControlData(data: BleControlData)
    fun sendOtaChunk(chunk: OtaChunk)
}
