package by.jadjer.etcu.data.repository

import android.bluetooth.BluetoothDevice
import by.jadjer.etcu.data.ble.BleManager
import by.jadjer.etcu.data.model.*
import by.jadjer.etcu.domain.repository.BleRepository
import kotlinx.coroutines.flow.StateFlow

class BleRepositoryImpl(
    private val bleManager: BleManager
) : BleRepository {
    override val connectionState: StateFlow<String> = bleManager.connectionState
    override val isConnected: StateFlow<Boolean> = bleManager.isConnected
    override val telemetry: StateFlow<SystemTelemetry> = bleManager.telemetry
    override val systemInfo: StateFlow<SystemInfo> = bleManager.systemInfo
    override val discoveredDevices: StateFlow<List<Pair<BluetoothDevice, Int>>> = bleManager.scanner.discoveredDevices
    override val isScanning: StateFlow<Boolean> = bleManager.scanner.isScanning
    override val otaFeedback: StateFlow<Int?> = bleManager.otaFeedback
    override val savedMac: StateFlow<String?> = bleManager.savedMac

    override fun startScan() = bleManager.scanner.startScan()
    override fun stopScan() = bleManager.scanner.stopScan()
    override fun connect(macAddress: String) = bleManager.connect(macAddress)
    override fun disconnect() = bleManager.disconnect()
    override fun autoConnect() = bleManager.autoConnect()
    override fun clearLastMac() = bleManager.clearLastMac()
    override fun getPairedDevices() = bleManager.scanner.getPairedDevices()
    override fun sendControlData(data: BleControlData) = bleManager.writeControlData(data)
    override fun sendOtaChunk(chunk: OtaChunk) = bleManager.writeOtaChunk(chunk)
}
