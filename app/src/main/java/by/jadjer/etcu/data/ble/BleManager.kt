package by.jadjer.etcu.data.ble

import android.bluetooth.BluetoothDevice
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bluetoothGatt: BluetoothGatt? = null

    // UUID вашей службы и характеристики (замените на свои)
    private val _serviceUUID =
        UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    private val _characteristicUUID =
        UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    private val _clientConfigDescriptionUUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val _connectionState = MutableStateFlow("Отключено")
    val connectionState: StateFlow<String> = _connectionState

    private val _receivedData = MutableStateFlow("Нет данных")
    val receivedData: StateFlow<String> = _receivedData

    val telemetryState = MutableStateFlow(SystemTelemetryState())

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    _connectionState.value = "Подключено. Запрос MTU..."
                    // ШАГ 1: Запрашиваем MTU 517 после успешного подключения
                    gatt.requestMtu(517)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    _connectionState.value = "Отключено"
                    gatt.close()
                    bluetoothGatt = null
                }
            } else {
                _connectionState.value = "Ошибка подключения: $status"
                gatt.close()
                bluetoothGatt = null
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = "MTU изменен на $mtu. Поиск служб..."
                // ШАГ 2: Обнаруживаем службы после изменения MTU
                gatt.discoverServices()
            } else {
                _connectionState.value = "Не удалось изменить MTU. Поиск служб..."
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = "Службы найдены. Подписка..."
                // ШАГ 3: Подписываемся на уведомления характеристики
                subscribeToCharacteristic(gatt)
            } else {
                _connectionState.value = "Ошибка поиска служб: $status"
            }
        }

        // Для новых версий Android (API 33+) раскомментируйте этот метод:
         override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
             if (characteristic.uuid == _characteristicUUID) {
                 val hexString = value.joinToString(separator = " ") { String.format("%02X", it) }
                 _receivedData.value = "Данные (HEX): $hexString"

                 telemetryState.value = parseTelemetry(value)
             }
         }
    }

    fun connect(macAddress: String) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = "Bluetooth выключен или недоступен"
            return
        }

        try {
            val device = bluetoothAdapter.getRemoteDevice(macAddress)
            _connectionState.value = "Соединение с $macAddress..."
            // Используем TRANSPORT_LE для BLE-устройств
            bluetoothGatt =
                device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } catch (e: IllegalArgumentException) {
            _connectionState.value = "Неверный MAC-адрес"
        }
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    private fun subscribeToCharacteristic(gatt: BluetoothGatt) {
        val service = gatt.getService(_serviceUUID)
        val characteristic = service?.getCharacteristic(_characteristicUUID)

        if (characteristic != null) {
            // Включаем уведомления локально в стеке Android
            gatt.setCharacteristicNotification(characteristic, true)

            // Записываем дескриптор на удаленное устройство, чтобы оно начало слать пакеты
            val descriptor = characteristic.getDescriptor(_clientConfigDescriptionUUID)
            if (descriptor != null) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
                _connectionState.value = "Подписка успешна. Ожидание данных..."
            } else {
                _connectionState.value = "Дескриптор не найден"
            }
        } else {
            _connectionState.value = "Характеристика не найдена"
        }
    }

    private fun parseTelemetry(bytes: ByteArray): SystemTelemetryState {
        if (bytes.size < 35) return SystemTelemetryState() // Защита от коротких пакетов

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        // 1. Парсим ServoTelemetry
        val servoConnected = buffer.get().toInt() != 0
        val servoMoved = buffer.get().toInt() != 0
        val servoLoad = buffer.short.toInt() and 0xFFFF
        val servoSpeed = buffer.short.toInt() and 0xFFFF
        val servoCurrent = buffer.short.toInt() and 0xFFFF
        val servoVoltage = buffer.get().toInt() and 0xFF // uint8_t
        val servoPosition = buffer.short.toInt() and 0xFFFF
        val servoTemperature = buffer.short.toInt() and 0xFFFF

        val servo = ServoTelemetryState(
            isConnected = servoConnected, isMoved = servoMoved, load = servoLoad,
            speed = servoSpeed, current = servoCurrent, voltage = servoVoltage,
            position = servoPosition, temperature = servoTemperature
        )

        // 2. Парсим ECUTelemetry
        val ecuConnected = buffer.get().toInt() != 0
        val ecuRpm = buffer.short.toInt() and 0xFFFF
        val ecuSpeed = buffer.short.toInt() and 0xFFFF
        val ecuTps = buffer.short.toInt() and 0xFFFF
        val ecuStarted = buffer.get().toInt() != 0
        val ecuClutch = buffer.get().toInt() != 0

        val ecu = EcuTelemetryState(
            isConnected = ecuConnected, rpm = ecuRpm, speed = ecuSpeed,
            tps = ecuTps, started = ecuStarted, clutchEnabled = ecuClutch
        )

        // 3. Парсим оставшуюся часть SystemTelemetry
        val accelPos = buffer.short.toInt() and 0xFFFF
        val accelOffset = buffer.short.toInt() and 0xFFFF
        val throttlePos = buffer.short.toInt() and 0xFFFF
        val targetSpeed = buffer.short.toInt() and 0xFFFF

        val guardActive = buffer.get().toInt() != 0
        val brakeEnabled = buffer.get().toInt() != 0
        val sysClutchEnabled = buffer.get().toInt() != 0

        val systemState = SystemState.fromByte(buffer.get())

        val rawErrorsMask = buffer.int.toLong() and 0xFFFFFFFFL
        val activeErrorsList = SystemError.parseErrors(rawErrorsMask)

        return SystemTelemetryState(
            servo = servo,
            ecu = ecu,
            acceleratorPosition = accelPos,
            acceleratorOffset = accelOffset,
            throttlePosition = throttlePos,
            targetSpeed = targetSpeed,
            guardActive = guardActive,
            brakeEnabled = brakeEnabled,
            clutchEnabled = sysClutchEnabled,
            systemState = systemState,
            activeErrors = activeErrorsList
        )
    }
}
