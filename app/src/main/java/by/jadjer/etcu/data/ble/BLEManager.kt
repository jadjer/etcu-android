package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothGattCharacteristic
import by.jadjer.etcu.data.local.BLEPreferenceManager
import by.jadjer.etcu.domain.model.*
import com.welie.blessed.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import by.jadjer.etcu.domain.model.ConnectionState as AppConnectionState

@SuppressLint("MissingPermission")
class BLEManager(
    app: Application,
    preferenceManager: BLEPreferenceManager,
) {
    private val dataParser = BLEDataParser()
    private var negotiatedMTU = BLEConstants.DEFAULT_MTU

    private val _controlData = MutableStateFlow(ControlData())
    val controlData = _controlData.asStateFlow()

    private val _telemetry = MutableStateFlow(SystemTelemetry())
    val telemetry = _telemetry.asStateFlow()

    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo = _systemInfo.asStateFlow()

    private val _otaFeedback = MutableSharedFlow<OTAStatus>(extraBufferCapacity = 1)
    val otaFeedback = _otaFeedback.asSharedFlow()

    private val connectionManager = BLEConnectionManager(app, preferenceManager)

    private val peripheralCallback = object : BluetoothPeripheralCallback() {
        override fun onServicesDiscovered(peripheral: BluetoothPeripheral) {
            connectionManager.updateState(AppConnectionState.SERVICES_DISCOVERED)
            connectionManager.updateSavedMac(peripheral.address)
            peripheral.requestMtu(BLEConstants.REQUESTED_MTU)
        }

        override fun onMtuChanged(peripheral: BluetoothPeripheral, mtu: Int, status: GattStatus) {
            if (status == GattStatus.SUCCESS) {
                negotiatedMTU = mtu
                connectionManager.updateState(AppConnectionState.OTA_SETUP)
                peripheral.setNotify(BLEConstants.SERVICE_UUID, BLEConstants.TELEMETRY_UUID, true)
                peripheral.setNotify(BLEConstants.SERVICE_UUID, BLEConstants.OTA_UUID, true)
            } else {
                connectionManager.updateState(AppConnectionState.ERROR_MTU)
            }
        }

        override fun onNotificationStateUpdate(
            peripheral: BluetoothPeripheral,
            characteristic: BluetoothGattCharacteristic,
            status: GattStatus
        ) {
            if (status == GattStatus.SUCCESS && characteristic.uuid == BLEConstants.OTA_UUID) {
                connectionManager.updateState(AppConnectionState.READING_INFO)
                peripheral.readCharacteristic(BLEConstants.SERVICE_UUID, BLEConstants.SYSTEM_INFO_UUID)
            } else if (status != GattStatus.SUCCESS) {
                connectionManager.updateState(AppConnectionState.ERROR_DESCRIPTOR_WRITE)
            }
        }

        override fun onCharacteristicUpdate(
            peripheral: BluetoothPeripheral,
            value: ByteArray,
            characteristic: BluetoothGattCharacteristic,
            status: GattStatus
        ) {
            if (status != GattStatus.SUCCESS) {
                connectionManager.updateState(AppConnectionState.ERROR_READ_CHAR)
                return
            }

            when (characteristic.uuid) {
                BLEConstants.SYSTEM_INFO_UUID -> {
                    _systemInfo.value = dataParser.parseSystemInfo(value)
                    connectionManager.updateState(AppConnectionState.READING_SETTINGS)
                    peripheral.readCharacteristic(BLEConstants.SERVICE_UUID, BLEConstants.CONTROL_UUID)
                }
                BLEConstants.CONTROL_UUID -> {
                    _controlData.value = dataParser.parseControlData(value)
                    connectionManager.updateState(AppConnectionState.READY)
                }
                BLEConstants.TELEMETRY_UUID -> _telemetry.value = dataParser.parseSystemTelemetry(value)
                BLEConstants.OTA_UUID -> _otaFeedback.tryEmit(dataParser.parseOtaFeedback(value))
            }
        }

        override fun onCharacteristicWrite(
            peripheral: BluetoothPeripheral,
            value: ByteArray,
            characteristic: BluetoothGattCharacteristic,
            status: GattStatus
        ) {
            if (status != GattStatus.SUCCESS) {
                connectionManager.updateState(AppConnectionState.ERROR_WRITE_CHAR)
            } else if (connectionManager.connectionState.value != AppConnectionState.READY) {
                connectionManager.updateState(AppConnectionState.READY)
            }
        }
    }

    init {
        connectionManager.peripheralCallback = peripheralCallback
    }

    val connectionState: kotlinx.coroutines.flow.StateFlow<AppConnectionState> = connectionManager.connectionState
    val scanner: BLEScanner = connectionManager.scanner
    val savedMac: kotlinx.coroutines.flow.StateFlow<String?> = connectionManager.savedMac

    fun connect(macAddress: String) = connectionManager.connect(macAddress)
    fun disconnect() = connectionManager.disconnect()
    fun autoConnect() = connectionManager.autoConnect()
    fun clearLastMac() = connectionManager.clearLastMac()

    fun writeControlData(data: ControlData) {
        val peripheral = connectionManager.activePeripheral ?: return

        peripheral.writeCharacteristic(
            BLEConstants.SERVICE_UUID,
            BLEConstants.CONTROL_UUID,
            dataParser.serializeControlData(data),
            WriteType.WITH_RESPONSE
        )
    }

    fun writeOtaChunk(chunk: OTAChunk) {
        val peripheral = connectionManager.activePeripheral ?: return
        
        if (BLEConstants.OTA_PACKAGE_SIZE > (negotiatedMTU - BLEConstants.BLE_HEADER_SIZE)) {
            connectionManager.updateState(AppConnectionState.ERROR_MTU)
            return
        }

        peripheral.writeCharacteristic(
            BLEConstants.SERVICE_UUID,
            BLEConstants.OTA_UUID,
            dataParser.serializeOtaChunk(chunk),
            WriteType.WITH_RESPONSE
        )
    }
}
