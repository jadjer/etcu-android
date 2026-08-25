package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile

@SuppressLint("MissingPermission")
class BleConnectionHandler(
    private val dataParser: BleDataParser,
    private val onConnectionStateChange: (String, Boolean) -> Unit,
    private val onTelemetryUpdate: (by.jadjer.etcu.data.model.SystemTelemetry) -> Unit,
    private val onSystemInfoUpdate: (by.jadjer.etcu.data.model.SystemInfo) -> Unit,
    private val onOtaFeedback: (Int) -> Unit,
    private val onServicesDiscovered: (BluetoothGatt) -> Unit
) : BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                onConnectionStateChange("Подключено. Поиск служб...", false)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                onConnectionStateChange("Отключено", false)
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

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS && characteristic.uuid == BleConstants.SYSTEM_INFO_UUID) {
            onSystemInfoUpdate(dataParser.parseSystemInfo(value))
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        when (characteristic.uuid) {
            BleConstants.TELEMETRY_UUID -> onTelemetryUpdate(dataParser.parseTelemetry(value))
            BleConstants.OTA_UUID -> dataParser.parseOtaFeedback(value)?.let(onOtaFeedback)
        }
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onConnectionStateChange("Подписка подтверждена", true)
        }
    }
}
