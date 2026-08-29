package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import by.jadjer.etcu.data.local.BLEPreferenceManager
import by.jadjer.etcu.domain.model.ControlData
import by.jadjer.etcu.domain.model.OTAChunk
import by.jadjer.etcu.domain.model.OTAStatus
import by.jadjer.etcu.domain.model.SystemInfo
import by.jadjer.etcu.domain.model.SystemTelemetry
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothCentralManagerCallback
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.BluetoothPeripheralCallback
import com.welie.blessed.GattStatus
import com.welie.blessed.HciStatus
import com.welie.blessed.WriteType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds
import by.jadjer.etcu.domain.model.ConnectionState as AppConnectionState

@SuppressLint("MissingPermission")
class BLEManager(
    context: Context,
    private val preferenceManager: BLEPreferenceManager,
) {
    private val appContext = context.applicationContext
    private val dataParser = BLEDataParser()

    private val _connectionState = MutableStateFlow(AppConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _controlData = MutableStateFlow(ControlData())
    val controlData = _controlData.asStateFlow()

    private val _telemetry = MutableStateFlow(SystemTelemetry())
    val telemetry = _telemetry.asStateFlow()

    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo = _systemInfo.asStateFlow()

    private val _otaFeedback = MutableSharedFlow<OTAStatus>(extraBufferCapacity = 1)
    val otaFeedback = _otaFeedback.asSharedFlow()

    private val _savedMac = MutableStateFlow(preferenceManager.getLastMac())
    val savedMac = _savedMac.asStateFlow()

    private val managerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isManualDisconnect = false
    private var reconnectJob: Job? = null
    private var autoConnectJob: Job? = null
    private var negotiatedMTU = BLEConstants.DEFAULT_MTU

    private val central: BluetoothCentralManager
    val scanner: BLEScanner
    private var activePeripheral: BluetoothPeripheral? = null

    private val centralCallback = object : BluetoothCentralManagerCallback() {
        override fun onDiscoveredPeripheral(peripheral: BluetoothPeripheral, scanResult: ScanResult) {
            scanner.handleDiscoveredPeripheral(peripheral, scanResult)
        }

        override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {
            activePeripheral = peripheral
            _connectionState.value = AppConnectionState.CONNECTED_DISCOVERING
        }

        override fun onConnectionFailed(peripheral: BluetoothPeripheral, status: HciStatus) {
            activePeripheral = null
            updateConnectionState(AppConnectionState.ERROR_CONNECTION)
        }

        override fun onDisconnectedPeripheral(peripheral: BluetoothPeripheral, status: HciStatus) {
            activePeripheral = null
            updateConnectionState(AppConnectionState.DISCONNECTED)
        }

        override fun onBluetoothAdapterStateChanged(state: Int) {
            if (state == BluetoothAdapter.STATE_OFF) {
                _connectionState.value = AppConnectionState.BLUETOOTH_OFF
                stopAllJobs()
            }
        }
    }

    private val peripheralCallback = object : BluetoothPeripheralCallback() {
        override fun onServicesDiscovered(peripheral: BluetoothPeripheral) {
            _connectionState.value = AppConnectionState.SERVICES_DISCOVERED
            preferenceManager.saveLastMac(peripheral.address)
            _savedMac.value = peripheral.address
            peripheral.requestMtu(BLEConstants.REQUESTED_MTU)
        }

        override fun onMtuChanged(peripheral: BluetoothPeripheral, mtu: Int, status: GattStatus) {
            if (status == GattStatus.SUCCESS) {
                negotiatedMTU = mtu
                _connectionState.value = AppConnectionState.OTA_SETUP
                peripheral.setNotify(BLEConstants.SERVICE_UUID, BLEConstants.TELEMETRY_UUID, true)
                peripheral.setNotify(BLEConstants.SERVICE_UUID, BLEConstants.OTA_UUID, true)
            } else {
                _connectionState.value = AppConnectionState.ERROR_MTU
            }
        }

        override fun onNotificationStateUpdate(
            peripheral: BluetoothPeripheral,
            characteristic: BluetoothGattCharacteristic,
            status: GattStatus
        ) {
            if (status == GattStatus.SUCCESS && characteristic.uuid == BLEConstants.OTA_UUID) {
                _connectionState.value = AppConnectionState.READING_INFO
                peripheral.readCharacteristic(BLEConstants.SERVICE_UUID, BLEConstants.SYSTEM_INFO_UUID)
            } else if (status != GattStatus.SUCCESS) {
                _connectionState.value = AppConnectionState.ERROR_DESCRIPTOR_WRITE
            }
        }

        override fun onCharacteristicUpdate(
            peripheral: BluetoothPeripheral,
            value: ByteArray,
            characteristic: BluetoothGattCharacteristic,
            status: GattStatus
        ) {
            if (status != GattStatus.SUCCESS) {
                _connectionState.value = AppConnectionState.ERROR_READ_CHAR
                return
            }

            when (characteristic.uuid) {
                BLEConstants.SYSTEM_INFO_UUID -> {
                    _systemInfo.value = dataParser.parseSystemInfo(value)
                    _connectionState.value = AppConnectionState.READING_SETTINGS
                    peripheral.readCharacteristic(BLEConstants.SERVICE_UUID, BLEConstants.CONTROL_UUID)
                }
                BLEConstants.CONTROL_UUID -> {
                    _controlData.value = dataParser.parseControlData(value)
                    _connectionState.value = AppConnectionState.READY
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
                _connectionState.value = AppConnectionState.ERROR_WRITE_CHAR
            } else if (_connectionState.value != AppConnectionState.READY) {
                _connectionState.value = AppConnectionState.READY
            }
        }
    }

    init {
        central = BluetoothCentralManager(appContext, centralCallback, Handler(Looper.getMainLooper()))
        scanner = BLEScanner(central)
    }

    private fun updateConnectionState(state: AppConnectionState) {
        _connectionState.value = state
        if (!state.isActive) {
            activePeripheral = null
            if (!isManualDisconnect) startAutoReconnect()
        }
    }

    fun connect(macAddress: String) {
        if (!central.isBluetoothEnabled) {
            _connectionState.value = AppConnectionState.BLUETOOTH_OFF
            return
        }

        stopAllJobs()
        isManualDisconnect = false
        _connectionState.value = AppConnectionState.CONNECTING
        
        runCatching {
            val peripheral = central.getPeripheral(macAddress)
            central.connectPeripheral(peripheral, peripheralCallback)
        }.onFailure {
            _connectionState.value = AppConnectionState.INVALID_MAC
        }
    }

    fun disconnect() {
        isManualDisconnect = true
        stopAllJobs()
        activePeripheral?.let { central.cancelConnection(it) }
    }

    private fun startAutoReconnect() {
        preferenceManager.getLastMac() ?: return
        reconnectJob?.cancel()
        reconnectJob = managerScope.launch {
            delay(3000.milliseconds)
            if (!_connectionState.value.isActive) autoConnect()
        }
    }

    fun autoConnect() {
        val mac = preferenceManager.getLastMac() ?: return
        if (!central.isBluetoothEnabled) {
            _connectionState.value = AppConnectionState.BLUETOOTH_OFF
            return
        }

        if (_connectionState.value.isActive || _connectionState.value == AppConnectionState.CONNECTING) return

        autoConnectJob?.cancel()
        autoConnectJob = managerScope.launch {
            _connectionState.value = AppConnectionState.SCANNING
            scanner.startScan()

            runCatching {
                withTimeout(15000.milliseconds) {
                    scanner.discoveredDevices
                        .firstOrNull { list -> list.any { it.macAddress == mac } }
                        ?.let {
                            scanner.stopScan()
                            connect(mac)
                        }
                }
            }.onFailure {
                scanner.stopScan()
                if (_connectionState.value == AppConnectionState.SCANNING) {
                    _connectionState.value = AppConnectionState.DISCONNECTED
                    if (!isManualDisconnect) startAutoReconnect()
                }
            }
        }
    }

    fun writeControlData(data: ControlData) {
        activePeripheral?.writeCharacteristic(
            BLEConstants.SERVICE_UUID,
            BLEConstants.CONTROL_UUID,
            dataParser.serializeControlData(data),
            WriteType.WITH_RESPONSE
        )
    }

    fun writeOtaChunk(chunk: OTAChunk) {
        val peripheral = activePeripheral ?: return
        
        if (BLEConstants.OTA_PACKAGE_SIZE > (negotiatedMTU - BLEConstants.BLE_HEADER_SIZE)) {
            _connectionState.value = AppConnectionState.ERROR_MTU
            return
        }

        peripheral.writeCharacteristic(
            BLEConstants.SERVICE_UUID,
            BLEConstants.OTA_UUID,
            dataParser.serializeOtaChunk(chunk),
            WriteType.WITH_RESPONSE
        )
    }

    fun clearLastMac() {
        isManualDisconnect = true
        stopAllJobs()
        activePeripheral?.let { central.cancelConnection(it) }
        preferenceManager.clearLastMac()
        _savedMac.value = null
    }

    private fun stopAllJobs() {
        reconnectJob?.cancel()
        autoConnectJob?.cancel()
        scanner.stopScan()
    }
}
