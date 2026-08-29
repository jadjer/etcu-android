package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import by.jadjer.etcu.data.local.BLEPreferenceManager
import by.jadjer.etcu.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("MissingPermission")
class BLEManager(
    context: Context,
    private val preferenceManager: BLEPreferenceManager,
) {
    private val appContext = context.applicationContext
    private val bluetoothAdapter: BluetoothAdapter? =
        (appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    private val dataParser = BLEDataParser()
    val scanner = BLEScanner(bluetoothAdapter)
    private var bluetoothGatt: BluetoothGatt? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
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
    private var negotiatedMtu = 23

    private val gattCallback = BLEConnectionHandler(
        dataParser = dataParser,
        onConnectionStateChange = { state ->
            updateConnectionState(state)
        },
        onControlDataUpdate = { _controlData.value = it },
        onTelemetryUpdate = { _telemetry.value = it },
        onSystemInfoUpdate = { _systemInfo.value = it },
        onOtaFeedback = { status -> status?.let { _otaFeedback.tryEmit(it) } },
        onServicesDiscovered = { gatt -> handleServicesDiscovered(gatt) },
        onMtuUpdate = { gatt, mtu ->
            bluetoothGatt = gatt
            negotiatedMtu = mtu
        }
    )

    private fun updateConnectionState(state: ConnectionState) {
        _connectionState.value = state
        
        if (!state.isActive) {
            closeGatt()
            if (!isManualDisconnect) startAutoReconnect()
        }
    }

    private fun handleServicesDiscovered(gatt: BluetoothGatt) {
        bluetoothGatt = gatt
        _connectionState.value = ConnectionState.SERVICES_DISCOVERED
        val mac = gatt.device.address
        preferenceManager.saveLastMac(mac)
        _savedMac.value = mac
        gatt.requestMtu(BLEConstants.REQUESTED_MTU)
    }

    @Suppress("Deprecation")
    fun connect(macAddress: String) {
        if (bluetoothAdapter?.isEnabled != true) {
            _connectionState.value = ConnectionState.BLUETOOTH_OFF
            return
        }
        
        stopAllJobs()
        isManualDisconnect = false
        closeGatt()

        runCatching {
            _connectionState.value = ConnectionState.CONNECTING
            val device = bluetoothAdapter?.getRemoteDevice(macAddress)
            bluetoothGatt = device?.connectGatt(appContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        }.onFailure {
            _connectionState.value = ConnectionState.INVALID_MAC
        }
    }

    fun disconnect() {
        isManualDisconnect = true
        stopAllJobs()
        bluetoothGatt?.disconnect()
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
        if (bluetoothAdapter?.isEnabled != true) {
            _connectionState.value = ConnectionState.BLUETOOTH_OFF
            return
        }

        if (_connectionState.value.isActive || _connectionState.value == ConnectionState.CONNECTING) return

        autoConnectJob?.cancel()
        autoConnectJob = managerScope.launch {
            _connectionState.value = ConnectionState.SCANNING
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
                if (_connectionState.value == ConnectionState.SCANNING) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    if (!isManualDisconnect) startAutoReconnect()
                }
            }
        }
    }

    fun writeControlData(data: ControlData) {
        val gatt = bluetoothGatt ?: return
        val characteristic = getCharacteristic(BLEConstants.CONTROL_UUID) ?: return

        val bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(data.accMin.toShort()).putShort(data.accMax.toShort())
            .putShort(data.servoMin.toShort()).putShort(data.servoMax.toShort())
            .array()

        writeCharacteristic(gatt, characteristic, bytes)
    }

    fun writeOtaChunk(chunk: OTAChunk) {
        val gatt = bluetoothGatt ?: return
        val characteristic = getCharacteristic(BLEConstants.OTA_UUID) ?: return

        val packetSize = BLEConstants.OTA_HEADER_SIZE + chunk.data.size
        if (packetSize > negotiatedMtu - 3) {
            _connectionState.value = ConnectionState.ERROR_MTU
            return
        }

        val bytes = ByteBuffer.allocate(packetSize).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(chunk.firmwareSize.toInt())
            .putShort(chunk.totalChunks.toShort())
            .putShort(chunk.chunkNumber.toShort())
            .put(chunk.data)
            .array()

        writeCharacteristic(gatt, characteristic, bytes)
    }

    private fun writeCharacteristic(gatt: BluetoothGatt, char: BluetoothGattCharacteristic, data: ByteArray) {
        runCatching {
            val status = gatt.writeCharacteristic(char, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            if (status != BluetoothStatusCodes.SUCCESS) {
                _connectionState.value = ConnectionState.ERROR_WRITE_CHAR
            }
        }.onFailure {
            _connectionState.value = ConnectionState.ERROR_WRITE_CHAR
        }
    }

    private fun getCharacteristic(uuid: UUID): BluetoothGattCharacteristic? =
        bluetoothGatt?.services?.firstNotNullOfOrNull { it.getCharacteristic(uuid) }

    fun clearLastMac() {
        isManualDisconnect = true
        stopAllJobs()
        bluetoothGatt?.disconnect()
        preferenceManager.clearLastMac()
        _savedMac.value = null
    }

    private fun stopAllJobs() {
        reconnectJob?.cancel()
        autoConnectJob?.cancel()
        scanner.stopScan()
    }

    private fun closeGatt() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
