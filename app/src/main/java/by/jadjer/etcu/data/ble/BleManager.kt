package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattConnectionSettings
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import by.jadjer.etcu.R
import by.jadjer.etcu.data.local.BlePreferenceManager
import by.jadjer.etcu.domain.model.ControlData
import by.jadjer.etcu.domain.model.OtaChunk
import by.jadjer.etcu.domain.model.SystemInfo
import by.jadjer.etcu.domain.model.SystemTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("MissingPermission")
class BleManager(
    private val context: Context,
    private val preferenceManager: BlePreferenceManager,
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val dataParser = BleDataParser()
    val scanner = BleScanner(bluetoothAdapter)

    private var bluetoothGatt: BluetoothGatt? = null

    private val _connectionState =
        MutableStateFlow(context.getString(R.string.ble_state_disconnected))
    val connectionState: StateFlow<String> = _connectionState

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _controlData = MutableStateFlow(ControlData())
    val controlData: StateFlow<ControlData> = _controlData

    private val _telemetry = MutableStateFlow(SystemTelemetry())
    val telemetry: StateFlow<SystemTelemetry> = _telemetry

    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo: StateFlow<SystemInfo> = _systemInfo

    private val _otaFeedback = MutableStateFlow<Int?>(null)
    val otaFeedback: StateFlow<Int?> = _otaFeedback

    private val _savedMac = MutableStateFlow<String?>(preferenceManager.getLastMac())
    val savedMac: StateFlow<String?> = _savedMac

    private val managerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isManualDisconnect = false
    private var reconnectJob: Job? = null

    private val gattCallback = BleConnectionHandler(
        context = context,
        dataParser = dataParser,
        onConnectionStateChange = { state, connected ->
            _connectionState.value = state
            _isConnected.value = connected
            if (!connected) {
                bluetoothGatt = null
                if (!isManualDisconnect) {
                    startAutoReconnect()
                }
            }
        },
        onControlDataUpdate = { _controlData.value = it },
        onTelemetryUpdate = { _telemetry.value = it },
        onSystemInfoUpdate = { _systemInfo.value = it },
        onOtaFeedback = { _otaFeedback.value = it },
        onServicesDiscovered = { handleServicesDiscovered(it) },
        onMtuChanged = { gatt ->
            if (!subscribeToCharacteristics(gatt)) {
                _connectionState.value = context.getString(R.string.ble_state_error_protocol)
                disconnect()
            }
        }
    )

    private fun handleServicesDiscovered(gatt: BluetoothGatt) {
        _connectionState.value = context.getString(R.string.ble_state_services_discovered)

        val mac = gatt.device.address
        preferenceManager.saveLastMac(mac)
        _savedMac.value = mac

        // Запрос MTU (никаких других BLE вызовов тут быть не должно!)
        gatt.requestMtu(BleConstants.DEFAULT_MTU)
    }

    fun connect(macAddress: String) {
        if (bluetoothAdapter?.isEnabled != true) {
            _connectionState.value = context.getString(R.string.ble_state_bluetooth_off)
            return
        }

        reconnectJob?.cancel()
        isManualDisconnect = false

        bluetoothGatt?.close()
        bluetoothGatt = null

        try {
            val device = bluetoothAdapter.getRemoteDevice(macAddress)
            _connectionState.value = context.getString(R.string.ble_state_connecting, macAddress)

            bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                val settings = BluetoothGattConnectionSettings.Builder()
                    .setTransport(BluetoothDevice.TRANSPORT_LE)
                    .setAutoConnectEnabled(false)
                    .build()

                val executor = ContextCompat.getMainExecutor(context)

                device.connectGatt(settings, executor, gattCallback)
            } else {
                @Suppress("DEPRECATION")
                device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            }

        } catch (_: IllegalArgumentException) {
            _connectionState.value = context.getString(R.string.ble_state_invalid_mac)
        }
    }

    fun disconnect() {
        isManualDisconnect = true
        reconnectJob?.cancel()
        bluetoothGatt?.disconnect()
    }

    private fun startAutoReconnect() {
        val mac = preferenceManager.getLastMac() ?: return
        reconnectJob?.cancel()
        reconnectJob = managerScope.launch {
            delay(3000.milliseconds)
            if (!_isConnected.value) {
                connect(mac)
            }
        }
    }

    fun writeControlData(data: ControlData) {
        val characteristic = getCharacteristic(BleConstants.CONTROL_UUID) ?: return

        val buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(data.accMin.toShort())
        buffer.putShort(data.accMax.toShort())
        buffer.putShort(data.servoMin.toShort())
        buffer.putShort(data.servoMax.toShort())

        bluetoothGatt?.writeCharacteristic(
            characteristic,
            buffer.array(),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
    }

    fun writeOtaChunk(otaChunk: OtaChunk) {
        val characteristic = getCharacteristic(BleConstants.OTA_UUID) ?: return
        val packageSize = otaChunk.data.size
        val buffer = ByteBuffer.allocate(packageSize + 10).order(ByteOrder.LITTLE_ENDIAN)

        // New structure order: firmware_size (4), chunk_total (2), chunk_number (2), chunk_size (2), then chunk data
        buffer.putInt(otaChunk.firmwareSize.toInt())
        buffer.putShort(otaChunk.totalChunks.toShort())
        buffer.putShort(otaChunk.chunkNumber.toShort())
        buffer.putShort(packageSize.toShort())
        buffer.put(otaChunk.data)

        bluetoothGatt?.writeCharacteristic(
            characteristic,
            buffer.array(),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
    }

    private fun getCharacteristic(uuid: java.util.UUID): BluetoothGattCharacteristic? {
        return bluetoothGatt?.getService(BleConstants.SERVICE_UUID)?.getCharacteristic(uuid)
    }

    private fun subscribeToCharacteristics(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(BleConstants.SERVICE_UUID)
        val telemetryChar = service?.getCharacteristic(BleConstants.TELEMETRY_UUID) ?: return false

        // Локальное разрешение на прием уведомлений в Android
        gatt.setCharacteristicNotification(telemetryChar, true)

        val descriptor = telemetryChar.getDescriptor(BleConstants.DESCRIPTION_UUID) ?: return false

        // На API 35 пишем дескриптор напрямую новым методом
        val statusCode = gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        return statusCode == BluetoothGatt.GATT_SUCCESS
    }

    fun autoConnect() {
        preferenceManager.getLastMac()?.let { connect(it) }
    }

    fun clearLastMac() {
        preferenceManager.clearLastMac()
        _savedMac.value = null
    }
}
