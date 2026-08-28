package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import by.jadjer.etcu.data.local.BLEPreferenceManager
import by.jadjer.etcu.domain.model.ConnectionState
import by.jadjer.etcu.domain.model.ControlData
import by.jadjer.etcu.domain.model.OTAChunk
import by.jadjer.etcu.domain.model.OTAStatus
import by.jadjer.etcu.domain.model.SystemInfo
import by.jadjer.etcu.domain.model.SystemTelemetry
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

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

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

    private val gattCallback = BLEConnectionHandler(
        dataParser = dataParser,
        onConnectionStateChange = { state, connected ->
            _connectionState.value = state
            _isConnected.value = connected
            if (!connected) {
                closeGatt()
                if (state == ConnectionState.DISCONNECTED && !isManualDisconnect) startAutoReconnect()
            }
        },
        onControlDataUpdate = { _controlData.value = it },
        onTelemetryUpdate = { _telemetry.value = it },
        onSystemInfoUpdate = { _systemInfo.value = it },
        onOtaFeedback = { status -> status?.let { _otaFeedback.tryEmit(it) } },
        onServicesDiscovered = { gatt ->
            bluetoothGatt = gatt
            handleServicesDiscovered(gatt)
        },
        onMtuChanged = { gatt -> bluetoothGatt = gatt }
    )

    private fun handleServicesDiscovered(gatt: BluetoothGatt) {
        _connectionState.value = ConnectionState.SERVICES_DISCOVERED
        val mac = gatt.device.address
        preferenceManager.saveLastMac(mac)
        _savedMac.value = mac
        gatt.requestMtu(BLEConstants.REQUESTED_MTU)
    }

    fun connect(macAddress: String) {
        if (bluetoothAdapter?.isEnabled != true) {
            _connectionState.value = ConnectionState.BLUETOOTH_OFF
            return
        }
        reconnectJob?.cancel()
        isManualDisconnect = false
        closeGatt()

        runCatching {
            _connectionState.value = ConnectionState.CONNECTING

            bluetoothGatt = bluetoothAdapter?.getRemoteDevice(macAddress)
                ?.connectGatt(appContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        }.onFailure {
            _connectionState.value = ConnectionState.INVALID_MAC
        }
    }

    fun disconnect() {
        isManualDisconnect = true
        reconnectJob?.cancel()
        bluetoothGatt?.disconnect()
    }

    private fun startAutoReconnect() {
        preferenceManager.getLastMac() ?: return
        reconnectJob?.cancel()
        reconnectJob = managerScope.launch {
            delay(3000.milliseconds)
            if (!_isConnected.value) autoConnect()
        }
    }

    fun autoConnect() {
        val mac = preferenceManager.getLastMac() ?: return
        if (bluetoothAdapter?.isEnabled != true) {
            _connectionState.value = ConnectionState.BLUETOOTH_OFF
            return
        }

        _connectionState.value = ConnectionState.SCANNING
        managerScope.launch {
            runCatching {
                withTimeout(7000.milliseconds) {
                    scanner.discoveredDevices
                        .firstOrNull { list -> list.any { it.macAddress == mac } }
                        ?.let {
                            scanner.stopScan()
                            connect(mac)
                        }
                }
            }.onFailure {
                scanner.stopScan()
                _connectionState.value = ConnectionState.DISCONNECTED
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

        gatt.writeCharacteristic(
            characteristic,
            bytes,
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
    }

    fun writeOtaChunk(chunk: OTAChunk) {
        val gatt = bluetoothGatt ?: return
        val characteristic = getCharacteristic(BLEConstants.OTA_UUID) ?: return

        managerScope.launch {
            runCatching {
                val bytes = ByteBuffer.allocate(8 + chunk.data.size).order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(chunk.firmwareSize.toInt())
                    .putShort(chunk.totalChunks.toShort())
                    .putShort(chunk.chunkNumber.toShort())
                    .put(chunk.data)
                    .array()

                gatt.writeCharacteristic(
                    characteristic,
                    bytes,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            }.onFailure {
                _connectionState.value = ConnectionState.ERROR_WRITE_CHAR
            }
        }
    }

    private fun getCharacteristic(characteristicUUID: UUID): BluetoothGattCharacteristic? =
        bluetoothGatt?.services?.firstNotNullOfOrNull { it.getCharacteristic(characteristicUUID) }

    fun clearLastMac() {
        preferenceManager.clearLastMac()
        _savedMac.value = null
    }

    private fun closeGatt() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
