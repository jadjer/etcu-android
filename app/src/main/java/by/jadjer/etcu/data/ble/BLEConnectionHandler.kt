package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import by.jadjer.etcu.domain.model.ConnectionState
import by.jadjer.etcu.domain.model.ControlData
import by.jadjer.etcu.domain.model.OTAStatus
import by.jadjer.etcu.domain.model.SystemInfo
import by.jadjer.etcu.domain.model.SystemTelemetry
import java.util.UUID

@SuppressLint("MissingPermission")
class BLEConnectionHandler(
    private val dataParser: BLEDataParser,
    private val onConnectionStateChange: (ConnectionState, Boolean) -> Unit,
    private val onControlDataUpdate: (ControlData) -> Unit,
    private val onTelemetryUpdate: (SystemTelemetry) -> Unit,
    private val onSystemInfoUpdate: (SystemInfo) -> Unit,
    private val onOtaFeedback: (OTAStatus?) -> Unit,
    private val onServicesDiscovered: (BluetoothGatt) -> Unit,
    private val onMtuChanged: (BluetoothGatt) -> Unit
) : BluetoothGattCallback() {

    private val notificationQueue = ArrayDeque<UUID>()

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        when {
            status != BluetoothGatt.GATT_SUCCESS -> onConnectionStateChange(ConnectionState.ERROR_CONNECTION, false)
            newState == BluetoothProfile.STATE_CONNECTED -> {
                onConnectionStateChange(ConnectionState.CONNECTED_DISCOVERING, true)
                gatt.discoverServices()
            }
            newState == BluetoothProfile.STATE_DISCONNECTED -> onConnectionStateChange(ConnectionState.DISCONNECTED, false)
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onServicesDiscovered(gatt)
        } else {
            onConnectionStateChange(ConnectionState.ERROR_SERVICES, false)
        }
    }

    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onMtuChanged(gatt)
            notificationQueue.clear()
            notificationQueue.addAll(listOf(BLEConstants.TELEMETRY_UUID, BLEConstants.OTA_UUID))
            subscribeNext(gatt)
        } else {
            onConnectionStateChange(ConnectionState.ERROR_MTU, false)
        }
    }

    private fun subscribeNext(gatt: BluetoothGatt) {
        val uuid = notificationQueue.removeFirstOrNull() ?: return readSystemInfo(gatt)
        val characteristic = gatt.getChar(uuid)
        val descriptor = characteristic?.getDescriptor(BLEConstants.DESCRIPTION_UUID)

        if (characteristic != null && descriptor != null) {
            if (uuid == BLEConstants.OTA_UUID) onConnectionStateChange(ConnectionState.OTA_SETUP, true)
            gatt.setCharacteristicNotification(characteristic, true)
            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            subscribeNext(gatt)
        }
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            onConnectionStateChange(ConnectionState.ERROR_DESCRIPTOR_WRITE, false)
        } else {
            subscribeNext(gatt)
        }
    }

    private fun readSystemInfo(gatt: BluetoothGatt) {
        onConnectionStateChange(ConnectionState.READING_INFO, true)
        gatt.getChar(BLEConstants.SYSTEM_INFO_UUID)?.let {
            gatt.readCharacteristic(it)
        } ?: onConnectionStateChange(ConnectionState.ERROR_INFO_NOT_FOUND, false)
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            onConnectionStateChange(ConnectionState.ERROR_READ_CHAR, true)
            return
        }

        when (characteristic.uuid) {
            BLEConstants.SYSTEM_INFO_UUID -> {
                onSystemInfoUpdate(dataParser.parseSystemInfo(value))
                gatt.getChar(BLEConstants.CONTROL_UUID)?.let {
                    onConnectionStateChange(ConnectionState.READING_SETTINGS, true)
                    gatt.readCharacteristic(it)
                } ?: onConnectionStateChange(ConnectionState.READY, true)
            }
            BLEConstants.CONTROL_UUID -> {
                onControlDataUpdate(dataParser.parseControlData(value))
                onConnectionStateChange(ConnectionState.READY, true)
            }
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        when (characteristic.uuid) {
            BLEConstants.TELEMETRY_UUID -> onTelemetryUpdate(dataParser.parseSystemTelemetry(value))
            BLEConstants.OTA_UUID -> onOtaFeedback(dataParser.parseOtaFeedback(value))
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            onConnectionStateChange(ConnectionState.ERROR_WRITE_CHAR, true)
        } else if (characteristic.uuid == BLEConstants.CONTROL_UUID) {
            onConnectionStateChange(ConnectionState.READY, true)
        }
    }

    private fun BluetoothGatt.getChar(uuid: UUID): BluetoothGattCharacteristic? =
        getService(BLEConstants.SERVICE_UUID)?.getCharacteristic(uuid)
            ?: services.firstNotNullOfOrNull { it.getCharacteristic(uuid) }
}
