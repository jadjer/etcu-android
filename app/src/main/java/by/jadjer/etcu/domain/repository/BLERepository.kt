package by.jadjer.etcu.domain.repository

import by.jadjer.etcu.domain.model.ble.ConnectionState
import by.jadjer.etcu.domain.model.ble.DiscoveredDevice
import by.jadjer.etcu.domain.model.calibration.CalibrationData
import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.ota.OTAChunk
import by.jadjer.etcu.domain.model.ota.OTAStatus
import by.jadjer.etcu.domain.model.system.SystemInfo
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.domain.model.telemetry.TelemetryHistory
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BLERepository {
    val connectionState: StateFlow<ConnectionState>
    val connectionDetail: StateFlow<String?>
    val isManualForget: StateFlow<Boolean>
    val controlData: StateFlow<ControlData>
    val calibrationData: StateFlow<CalibrationData>
    val telemetry: StateFlow<SystemTelemetry>
    val history: StateFlow<TelemetryHistory>
    val systemInfo: StateFlow<SystemInfo>
    val discoveredDevices: StateFlow<List<DiscoveredDevice>>
    val isScanning: StateFlow<Boolean>
    val otaFeedback: SharedFlow<OTAStatus>

    fun startScan()
    fun stopScan()
    fun connect(macAddress: String)
    fun autoConnect()
    fun forgetDevice()
    fun isBonded(): Boolean
    fun sendControlData(data: ControlData)
    fun sendCalibrationData(data: CalibrationData)
    fun sendOtaChunk(chunk: OTAChunk)
}
