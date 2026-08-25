package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import by.jadjer.etcu.domain.model.*
import by.jadjer.etcu.data.local.BlePreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

    private val _connectionState = MutableStateFlow("Отключено")
    val connectionState: StateFlow<String> = _connectionState

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _telemetry = MutableStateFlow(SystemTelemetry())
    val telemetry: StateFlow<SystemTelemetry> = _telemetry

    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo: StateFlow<SystemInfo> = _systemInfo

    private val _otaFeedback = MutableStateFlow<Int?>(null)
    val otaFeedback: StateFlow<Int?> = _otaFeedback

    private val _savedMac = MutableStateFlow<String?>(preferenceManager.getLastMac())
    val savedMac: StateFlow<String?> = _savedMac

    private val gattCallback = BleConnectionHandler(
        dataParser = dataParser,
        onConnectionStateChange = { state, connected ->
            _connectionState.value = state
            _isConnected.value = connected
            if (!connected && state == "Отключено") bluetoothGatt = null
        },
        onTelemetryUpdate = { _telemetry.value = it },
        onSystemInfoUpdate = { _systemInfo.value = it },
        onOtaFeedback = { _otaFeedback.value = it },
        onServicesDiscovered = { handleServicesDiscovered(it) }
    )

    private fun handleServicesDiscovered(gatt: BluetoothGatt) {
        _connectionState.value = "Службы найдены. Проверка характеристик..."
        if (subscribeToCharacteristics(gatt)) {
            val mac = gatt.device.address
            preferenceManager.saveLastMac(mac)
            _savedMac.value = mac
            _isConnected.value = true
            gatt.requestMtu(BleConstants.DEFAULT_MTU)
        } else {
            _connectionState.value = "Ошибка: Устройство не поддерживает ETCU протокол"
            disconnect()
        }
    }

    fun connect(macAddress: String) {
        if (bluetoothAdapter?.isEnabled != true) {
            _connectionState.value = "Bluetooth выключен или недоступен"
            return
        }

        try {
            val device = bluetoothAdapter.getRemoteDevice(macAddress)
            _connectionState.value = "Соединение с $macAddress..."
            bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } catch (_: IllegalArgumentException) {
            _connectionState.value = "Неверный MAC-адрес"
        }
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    fun writeControlData(data: BleControlData) {
        val characteristic = getCharacteristic(BleConstants.CONTROL_UUID) ?: return
        val buffer = ByteBuffer.allocate(3).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(if (data.syncEnabled) 1.toByte() else 0.toByte())
        buffer.putShort(data.acceleratorOffset.toShort())

        bluetoothGatt?.writeCharacteristic(characteristic, buffer.array(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
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

        bluetoothGatt?.writeCharacteristic(characteristic, buffer.array(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
    }

    private fun getCharacteristic(uuid: java.util.UUID): BluetoothGattCharacteristic? {
        return bluetoothGatt?.getService(BleConstants.SERVICE_UUID)?.getCharacteristic(uuid)
    }

    private fun subscribeToCharacteristics(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(BleConstants.SERVICE_UUID)
        val telemetryChar = service?.getCharacteristic(BleConstants.TELEMETRY_UUID)
        val systemInfoChar = service?.getCharacteristic(BleConstants.SYSTEM_INFO_UUID)

        if (telemetryChar == null) return false

        gatt.setCharacteristicNotification(telemetryChar, true)
        telemetryChar.getDescriptor(BleConstants.DESCRIPTION_UUID)?.let {
            gatt.writeDescriptor(it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        }

        if (systemInfoChar != null) {
            gatt.readCharacteristic(systemInfoChar)
        }

        return true
    }

    fun autoConnect() {
        preferenceManager.getLastMac()?.let { connect(it) }
    }

    fun clearLastMac() {
        preferenceManager.clearLastMac()
        _savedMac.value = null
    }
}
