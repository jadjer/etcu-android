package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import by.jadjer.etcu.domain.model.control.*
import by.jadjer.etcu.domain.model.calibration.*
import by.jadjer.etcu.domain.model.telemetry.*
import by.jadjer.etcu.domain.model.ota.*
import by.jadjer.etcu.domain.model.system.*
import by.jadjer.etcu.domain.model.ble.*
import com.welie.blessed.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import by.jadjer.etcu.domain.model.ble.ConnectionState as AppConnectionState

@SuppressLint("MissingPermission")
class BLEManager(
    app: Application
) {
    private val _tag = "BLEManager"
    private val _dataParser = BLEDataParser()
    private var _negotiatedMTU = BLEConstants.DEFAULT_MTU
    
    private val _bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val _adapter = _bluetoothManager.adapter

    private val _controlData = MutableStateFlow(ControlData())
    val controlData = _controlData.asStateFlow()

    private val _calibrationData = MutableStateFlow(CalibrationData())
    val calibrationData = _calibrationData.asStateFlow()

    private val _telemetry = MutableStateFlow(SystemTelemetry())
    val telemetry = _telemetry.asStateFlow()

    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo = _systemInfo.asStateFlow()

    private val _otaFeedback = MutableSharedFlow<OTAStatus>(extraBufferCapacity = 1)
    val otaFeedback = _otaFeedback.asSharedFlow()

    private val connectionManager = BLEConnectionManager(app)

    private val peripheralCallback = object : BluetoothPeripheralCallback() {
        override fun onServicesDiscovered(peripheral: BluetoothPeripheral) {
            Log.d(_tag, "Services discovered for ${peripheral.address}")
            connectionManager.updateState(AppConnectionState.SERVICES_DISCOVERED)
            peripheral.requestMtu(BLEConstants.REQUESTED_MTU)
        }

        override fun onMtuChanged(peripheral: BluetoothPeripheral, mtu: Int, status: GattStatus) {
            if (status == GattStatus.SUCCESS) {
                Log.d(_tag, "MTU changed to $mtu")
                _negotiatedMTU = mtu
                connectionManager.updateState(AppConnectionState.OTA_SETUP)
                peripheral.startNotify(BLEConstants.SERVICE_UUID, BLEConstants.TELEMETRY_UUID, false)
            } else {
                Log.e(_tag, "MTU change failed with status $status")
                connectionManager.updateState(AppConnectionState.ERROR_MTU, status.value.toString())
            }
        }

        override fun onNotificationStateUpdate(
            peripheral: BluetoothPeripheral,
            characteristic: BluetoothGattCharacteristic,
            status: GattStatus
        ) {
            if (status != GattStatus.SUCCESS) {
                Log.e(_tag, "Notification failed for ${characteristic.uuid}: $status")
                connectionManager.updateState(AppConnectionState.ERROR_DESCRIPTOR_WRITE, status.value.toString())
                return
            }

            when (characteristic.uuid) {
                BLEConstants.TELEMETRY_UUID -> {
                    peripheral.startNotify(BLEConstants.SERVICE_UUID, BLEConstants.OTA_UUID, false)
                }
                BLEConstants.OTA_UUID -> {
                    peripheral.startNotify(BLEConstants.SERVICE_UUID, BLEConstants.CONTROL_UUID, false)
                }
                BLEConstants.CONTROL_UUID -> {
                    connectionManager.updateState(AppConnectionState.READING_INFO)
                    peripheral.readCharacteristic(BLEConstants.SERVICE_UUID, BLEConstants.SYSTEM_INFO_UUID)
                }
            }
        }

        override fun onCharacteristicUpdate(
            peripheral: BluetoothPeripheral,
            value: ByteArray,
            characteristic: BluetoothGattCharacteristic,
            status: GattStatus
        ) {
            if (status != GattStatus.SUCCESS) {
                Log.e(_tag, "Read failed for ${characteristic.uuid}: $status")
                connectionManager.updateState(AppConnectionState.ERROR_READ_CHAR, status.value.toString())
                return
            }

            when (characteristic.uuid) {
                BLEConstants.SYSTEM_INFO_UUID -> {
                    _systemInfo.value = _dataParser.parseSystemInfo(value)
                    connectionManager.updateState(AppConnectionState.READING_SETTINGS)
                    peripheral.readCharacteristic(BLEConstants.SERVICE_UUID, BLEConstants.CONTROL_UUID)
                }
                BLEConstants.CONTROL_UUID -> {
                    _controlData.value = _dataParser.parseControlData(value)
                    peripheral.readCharacteristic(BLEConstants.SERVICE_UUID, BLEConstants.CALIBRATION_UUID)
                }
                BLEConstants.CALIBRATION_UUID -> {
                    _calibrationData.value = _dataParser.parseCalibrationData(value)
                    connectionManager.updateState(AppConnectionState.READY)
                }
                BLEConstants.TELEMETRY_UUID -> _telemetry.value = _dataParser.parseSystemTelemetry(value)
                BLEConstants.OTA_UUID -> _otaFeedback.tryEmit(_dataParser.parseOtaFeedback(value))
            }
        }

        override fun onCharacteristicWrite(
            peripheral: BluetoothPeripheral,
            value: ByteArray,
            characteristic: BluetoothGattCharacteristic,
            status: GattStatus
        ) {
            if (status != GattStatus.SUCCESS) {
                Log.e(_tag, "Write failed for ${characteristic.uuid}: $status")
                connectionManager.updateState(AppConnectionState.ERROR_WRITE_CHAR, status.value.toString())
            } else {
                when (characteristic.uuid) {
                    BLEConstants.CONTROL_UUID -> _controlData.value = _dataParser.parseControlData(value)
                    BLEConstants.CALIBRATION_UUID -> _calibrationData.value = _dataParser.parseCalibrationData(value)
                }
            }
        }
    }

    init {
        connectionManager.peripheralCallback = peripheralCallback
    }

    val connectionState: StateFlow<AppConnectionState> = connectionManager.connectionState
    val connectionDetail: StateFlow<String?> = connectionManager.connectionDetail
    val isManualForget: StateFlow<Boolean> = connectionManager.isManualForget
    val scanner: BLEScanner = connectionManager.scanner

    fun connect(macAddress: String) = connectionManager.connect(macAddress)
    fun autoConnect() = connectionManager.autoConnect()
    fun forgetDevice() = connectionManager.forgetDevice()

    fun isBonded(): Boolean {
        return _adapter?.bondedDevices?.any { it.name?.contains("ETCU", ignoreCase = true) == true } ?: false
    }

    fun writeControlData(data: ControlData) {
        val peripheral = connectionManager.activePeripheral ?: return
        peripheral.writeCharacteristic(
            BLEConstants.SERVICE_UUID,
            BLEConstants.CONTROL_UUID,
            _dataParser.serializeControlData(data),
            WriteType.WITH_RESPONSE
        )
    }

    fun writeCalibrationData(data: CalibrationData) {
        val peripheral = connectionManager.activePeripheral ?: return
        peripheral.writeCharacteristic(
            BLEConstants.SERVICE_UUID,
            BLEConstants.CALIBRATION_UUID,
            _dataParser.serializeCalibrationData(data),
            WriteType.WITH_RESPONSE
        )
    }

    fun writeOtaChunk(chunk: OTAChunk) {
        val peripheral = connectionManager.activePeripheral ?: return
        if (BLEConstants.OTA_PACKAGE_SIZE > (_negotiatedMTU - BLEConstants.BLE_HEADER_SIZE)) {
            connectionManager.updateState(AppConnectionState.ERROR_MTU)
            return
        }
        peripheral.writeCharacteristic(
            BLEConstants.SERVICE_UUID,
            BLEConstants.OTA_UUID,
            _dataParser.serializeOtaChunk(chunk),
            WriteType.WITH_RESPONSE
        )
    }
}
