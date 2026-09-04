package by.jadjer.etcu.data.repository

import by.jadjer.etcu.data.ble.BLEManager
import by.jadjer.etcu.domain.model.ble.*
import by.jadjer.etcu.domain.model.control.*
import by.jadjer.etcu.domain.model.telemetry.*
import by.jadjer.etcu.domain.model.system.*
import by.jadjer.etcu.domain.model.ota.*
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class BLERepositoryImpl(
    private val bleManager: BLEManager
) : BLERepository {
    override val connectionState: StateFlow<ConnectionState> = bleManager.connectionState
    override val controlData: StateFlow<ControlData> = bleManager.controlData
    override val telemetry: StateFlow<SystemTelemetry> = bleManager.telemetry
    override val systemInfo: StateFlow<SystemInfo> = bleManager.systemInfo
    override val discoveredDevices: StateFlow<List<DiscoveredDevice>> = bleManager.scanner.discoveredDevices
    override val isScanning: StateFlow<Boolean> = bleManager.scanner.isScanning
    override val otaFeedback: SharedFlow<OTAStatus> = bleManager.otaFeedback
    override val savedMac: StateFlow<String?> = bleManager.savedMac

    override fun startScan() = bleManager.scanner.startScan()
    override fun stopScan() = bleManager.scanner.stopScan()
    override fun connect(macAddress: String) = bleManager.connect(macAddress)
    override fun disconnect() = bleManager.disconnect()
    override fun autoConnect() = bleManager.autoConnect()
    override fun clearLastMac() = bleManager.clearLastMac()

    override fun sendControlData(data: ControlData) = bleManager.writeControlData(data)
    override fun sendOtaChunk(chunk: OTAChunk) = bleManager.writeOtaChunk(chunk)
}
