package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile

import android.content.Context
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.ControlData
import by.jadjer.etcu.domain.model.SystemInfo
import by.jadjer.etcu.domain.model.SystemTelemetry

@SuppressLint("MissingPermission")
class BleConnectionHandler(
    private val context: Context,
    private val dataParser: BleDataParser,
    private val onConnectionStateChange: (String, Boolean) -> Unit,
    private val onControlDataUpdate: (ControlData) -> Unit,
    private val onTelemetryUpdate: (SystemTelemetry) -> Unit,
    private val onSystemInfoUpdate: (SystemInfo) -> Unit,
    private val onOtaFeedback: (Int) -> Unit,
    private val onServicesDiscovered: (BluetoothGatt) -> Unit,
    private val onMtuChanged: (BluetoothGatt) -> Unit // Новый колбек для API 35
) : BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                onConnectionStateChange("Подключено. Поиск служб...", false)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                onConnectionStateChange(context.getString(R.string.ble_state_disconnected), false)
                gatt.close()
            }
        } else {
            onConnectionStateChange("Ошибка подключения: $status", false)
            gatt.close()
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onServicesDiscovered(gatt)
        } else {
            onConnectionStateChange("Ошибка поиска служб: $status", false)
        }
    }

    // Шаг 2: Вызывается операционной системой, когда MTU успешно изменен
    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onMtuChanged(gatt)
        } else {
            onConnectionStateChange("Ошибка согласования MTU: $status", false)
        }
    }

    // Шаг 4: Вызывается, когда дескриптор подписки записан. Линия СВОБОДНА.
    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onConnectionStateChange("Подписка подтверждена. Чтение SystemInfo...", false)

            val service = gatt.getService(BleConstants.SERVICE_UUID)
            val systemInfoChar = service?.getCharacteristic(BleConstants.SYSTEM_INFO_UUID)

            if (systemInfoChar != null) {
                // Прямой вызов без проверок Build.VERSION — мы на API 35!
                val success = gatt.readCharacteristic(systemInfoChar)
                if (!success) {
                    onConnectionStateChange("Ошибка: Стек Bluetooth отклонил чтение SystemInfo", false)
                }
            } else {
                onConnectionStateChange("Характеристика SystemInfo не найдена", false)
            }
        } else {
            onConnectionStateChange("Ошибка записи дескриптора: $status", false)
        }
    }

    // Шаг 5: Финал. Данные SystemInfo получены физически.
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            when (characteristic.uuid) {
                BleConstants.SYSTEM_INFO_UUID -> {
                    onSystemInfoUpdate(dataParser.parseSystemInfo(value))

                    // Шаг 6: Читаем ControlData
                    val service = gatt.getService(BleConstants.SERVICE_UUID)
                    val controlChar = service?.getCharacteristic(BleConstants.CONTROL_UUID)
                    if (controlChar != null) {
                        onConnectionStateChange("Чтение настроек...", false)
                        val success = gatt.readCharacteristic(controlChar)
                        if (!success) {
                            onConnectionStateChange("Устройство готово к работе", true)
                        }
                    } else {
                        onConnectionStateChange("Устройство готово к работе", true)
                    }
                }
                BleConstants.CONTROL_UUID -> {
                    onControlDataUpdate(dataParser.parseControlData(value))
                    onConnectionStateChange("Устройство готово к работе", true)
                }
            }
        } else {
            onConnectionStateChange("Ошибка чтения ${characteristic.uuid}: $status", false)
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        when (characteristic.uuid) {
            BleConstants.CONTROL_UUID -> onControlDataUpdate(dataParser.parseControlData(value))
            BleConstants.TELEMETRY_UUID -> onTelemetryUpdate(dataParser.parseSystemTelemetry(value))
            BleConstants.OTA_UUID -> dataParser.parseOtaFeedback(value)?.let(onOtaFeedback)
        }
    }
}
