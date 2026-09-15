package by.jadjer.etcu.data.repository

import by.jadjer.etcu.data.ble.BLEManager
import by.jadjer.etcu.domain.model.ble.ConnectionState
import by.jadjer.etcu.domain.model.ble.DiscoveredDevice
import by.jadjer.etcu.domain.model.calibration.CalibrationData
import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.ota.OTAChunk
import by.jadjer.etcu.domain.model.ota.OTAStatus
import by.jadjer.etcu.domain.model.system.SystemInfo
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.domain.model.telemetry.TelemetryHistory
import by.jadjer.etcu.domain.repository.BLERepository
import by.jadjer.etcu.domain.util.TelemetryHistoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class BLERepositoryImpl(
    private val _bleManager: BLEManager,
    scope: CoroutineScope
) : BLERepository {
    private val _historyManager = TelemetryHistoryManager()
    override val connectionState: StateFlow<ConnectionState> = _bleManager.connectionState
    override val connectionDetail: StateFlow<String?> = _bleManager.connectionDetail
    override val isManualForget: StateFlow<Boolean> = _bleManager.isManualForget
    override val controlData: StateFlow<ControlData> = _bleManager.controlData
    override val calibrationData: StateFlow<CalibrationData> = _bleManager.calibrationData
    override val telemetry: StateFlow<SystemTelemetry> = _bleManager.telemetry
    override val systemInfo: StateFlow<SystemInfo> = _bleManager.systemInfo

    private val _history = MutableStateFlow(TelemetryHistory())
    override val history: StateFlow<TelemetryHistory> = _history.asStateFlow()

    override val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _bleManager.scanner.discoveredDevices
    override val isScanning: StateFlow<Boolean> = _bleManager.scanner.isScanning
    override val otaFeedback: SharedFlow<OTAStatus> = _bleManager.otaFeedback

    init {
        telemetry.onEach { t ->
            _history.update { h -> _historyManager.updateHistory(h, t) }
        }.launchIn(scope)
    }

    override fun startScan() = _bleManager.scanner.startScan()
    override fun stopScan() = _bleManager.scanner.stopScan()
    override fun connect(macAddress: String) = _bleManager.connect(macAddress)
    override fun autoConnect() = _bleManager.autoConnect()
    override fun forgetDevice() = _bleManager.forgetDevice()
    override fun isBonded(): Boolean = _bleManager.isBonded()

    override fun sendControlData(data: ControlData) = _bleManager.writeControlData(data)
    override fun sendCalibrationData(data: CalibrationData) = _bleManager.writeCalibrationData(data)
    override fun sendOtaChunk(chunk: OTAChunk) = _bleManager.writeOtaChunk(chunk)
}
