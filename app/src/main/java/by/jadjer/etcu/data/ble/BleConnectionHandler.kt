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
    private val onMtuChanged: (BluetoothGatt) -> Unit
) : BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                onConnectionStateChange(context.getString(R.string.ble_state_connected_discovering), false)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                onConnectionStateChange(context.getString(R.string.ble_state_disconnected), false)
                gatt.close()
            }
        } else {
            onConnectionStateChange(context.getString(R.string.ble_error_connection, status), false)
            gatt.close()
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onServicesDiscovered(gatt)
        } else {
            onConnectionStateChange(context.getString(R.string.ble_error_services, status), false)
        }
    }

    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onMtuChanged(gatt)
        } else {
            onConnectionStateChange(context.getString(R.string.ble_error_mtu, status), false)
        }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onConnectionStateChange(context.getString(R.string.ble_state_subscribed_reading_info), false)

            val service = gatt.getService(BleConstants.SERVICE_UUID)
            val systemInfoChar = service?.getCharacteristic(BleConstants.SYSTEM_INFO_UUID)

            if (systemInfoChar != null) {
                val success = gatt.readCharacteristic(systemInfoChar)
                if (!success) {
                    onConnectionStateChange(
                        context.getString(R.string.ble_error_read_info),
                        false
                    )
                }
            } else {
                onConnectionStateChange(context.getString(R.string.ble_error_info_not_found), false)
            }
        } else {
            onConnectionStateChange(context.getString(R.string.ble_error_descriptor_write, status), false)
        }
    }

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

                    val service = gatt.getService(BleConstants.SERVICE_UUID)
                    val controlChar = service?.getCharacteristic(BleConstants.CONTROL_UUID)
                    if (controlChar != null) {
                        onConnectionStateChange(context.getString(R.string.ble_state_reading_settings), false)

                        gatt.readCharacteristic(controlChar)
                    } else {
                        onConnectionStateChange(context.getString(R.string.ble_state_ready), true)
                    }
                }

                BleConstants.CONTROL_UUID -> {
                    onControlDataUpdate(dataParser.parseControlData(value))
                    onConnectionStateChange(context.getString(R.string.ble_state_ready), true)
                }
            }
        } else {
            onConnectionStateChange(
                context.getString(R.string.ble_error_read_char, characteristic.uuid.toString(), status),
                false
            )
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        when (characteristic.uuid) {
            BleConstants.TELEMETRY_UUID -> onTelemetryUpdate(dataParser.parseSystemTelemetry(value))
            BleConstants.OTA_UUID -> dataParser.parseOtaFeedback(value)?.let(onOtaFeedback)
        }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onConnectionStateChange(context.getString(R.string.ble_state_ready), true)
        } else {
            onConnectionStateChange(context.getString(R.string.ble_error_read_char, "Write ${characteristic.uuid}", status), false)
        }
    }
}
