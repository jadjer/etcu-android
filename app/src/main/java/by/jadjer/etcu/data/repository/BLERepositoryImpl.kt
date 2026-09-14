package by.jadjer.etcu.data.repository

import by.jadjer.etcu.data.ble.BLEManager
import by.jadjer.etcu.domain.model.ble.*
import by.jadjer.etcu.domain.model.control.*
import by.jadjer.etcu.domain.model.calibration.*
import by.jadjer.etcu.domain.model.telemetry.*
import by.jadjer.etcu.domain.model.system.*
import by.jadjer.etcu.domain.model.ota.*
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class BLERepositoryImpl(
    private val bleManager: BLEManager,
    private val scope: CoroutineScope
) : BLERepository {
    override val connectionState: StateFlow<ConnectionState> = bleManager.connectionState
    override val connectionDetail: StateFlow<String?> = bleManager.connectionDetail
    override val isManualForget: StateFlow<Boolean> = bleManager.isManualForget
    override val controlData: StateFlow<ControlData> = bleManager.controlData
    override val calibrationData: StateFlow<CalibrationData> = bleManager.calibrationData
    override val telemetry: StateFlow<SystemTelemetry> = bleManager.telemetry
    override val systemInfo: StateFlow<SystemInfo> = bleManager.systemInfo

    private val _history = MutableStateFlow(TelemetryHistory())
    override val history: StateFlow<TelemetryHistory> = _history.asStateFlow()

    override val discoveredDevices: StateFlow<List<DiscoveredDevice>> = bleManager.scanner.discoveredDevices
    override val isScanning: StateFlow<Boolean> = bleManager.scanner.isScanning
    override val otaFeedback: SharedFlow<OTAStatus> = bleManager.otaFeedback

    init {
        telemetry.onEach { t ->
            val timestamp = System.currentTimeMillis()
            _history.update { h ->
                h.copy(
                    status = (h.status + HistoryRecord(t.status, timestamp)).takeLast(1000),
                    ecu = (h.ecu + HistoryRecord(t.ecu, timestamp)).takeLast(1000),
                    servo = (h.servo + HistoryRecord(t.servo, timestamp)).takeLast(1000),
                    cruise = (h.cruise + HistoryRecord(t.cruise, timestamp)).takeLast(1000),
                    accelerator = (h.accelerator + HistoryRecord(t.accelerator, timestamp)).takeLast(1000)
                )
            }
        }.launchIn(scope)
    }

    override fun startScan() = bleManager.scanner.startScan()
    override fun stopScan() = bleManager.scanner.stopScan()
    override fun connect(macAddress: String) = bleManager.connect(macAddress)
    override fun autoConnect() = bleManager.autoConnect()
    override fun forgetDevice() = bleManager.forgetDevice()
    override fun isBonded(): Boolean = bleManager.isBonded()

    override fun sendControlData(data: ControlData) = bleManager.writeControlData(data)
    override fun sendCalibrationData(data: CalibrationData) = bleManager.writeCalibrationData(data)
    override fun sendOtaChunk(chunk: OTAChunk) = bleManager.writeOtaChunk(chunk)
}
