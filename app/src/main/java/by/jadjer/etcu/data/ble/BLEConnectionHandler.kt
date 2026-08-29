package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.Handler
import android.os.Looper
import by.jadjer.etcu.domain.model.*
import java.util.UUID

@SuppressLint("MissingPermission")
class BLEConnectionHandler(
    private val dataParser: BLEDataParser,
    private val onConnectionStateChange: (ConnectionState) -> Unit,
    private val onControlDataUpdate: (ControlData) -> Unit,
    private val onTelemetryUpdate: (SystemTelemetry) -> Unit,
    private val onSystemInfoUpdate: (SystemInfo) -> Unit,
    private val onOtaFeedback: (OTAStatus?) -> Unit,
    private val onServicesDiscovered: (BluetoothGatt) -> Unit,
    private val onMtuUpdate: (BluetoothGatt, Int) -> Unit
) : BluetoothGattCallback() {

    private val notificationQueue = ArrayDeque<UUID>()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        when {
            status != BluetoothGatt.GATT_SUCCESS -> {
                onConnectionStateChange(ConnectionState.ERROR_CONNECTION)
            }
            newState == BluetoothProfile.STATE_CONNECTED -> {
                onConnectionStateChange(ConnectionState.CONNECTED_DISCOVERING)
                // Small delay before discovery to ensure stability
                mainHandler.postDelayed({ gatt.discoverServices() }, 600)
            }
            newState == BluetoothProfile.STATE_DISCONNECTED -> {
                onConnectionStateChange(ConnectionState.DISCONNECTED)
            }
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onServicesDiscovered(gatt)
        } else {
            onConnectionStateChange(ConnectionState.ERROR_SERVICES)
        }
    }

    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onMtuUpdate(gatt, mtu)
            notificationQueue.clear()
            notificationQueue.addAll(listOf(BLEConstants.TELEMETRY_UUID, BLEConstants.OTA_UUID))
            subscribeNext(gatt)
        } else {
            onConnectionStateChange(ConnectionState.ERROR_MTU)
        }
    }

    private fun subscribeNext(gatt: BluetoothGatt) {
        val uuid = notificationQueue.removeFirstOrNull() ?: return readSystemInfo(gatt)
        
        val characteristic = gatt.findChar(uuid)
        val descriptor = characteristic?.getDescriptor(BLEConstants.DESCRIPTION_UUID)

        if (characteristic != null && descriptor != null) {
            if (uuid == BLEConstants.OTA_UUID) onConnectionStateChange(ConnectionState.OTA_SETUP)
            gatt.setCharacteristicNotification(characteristic, true)
            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            subscribeNext(gatt)
        }
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            onConnectionStateChange(ConnectionState.ERROR_DESCRIPTOR_WRITE)
        } else {
            mainHandler.postDelayed({ subscribeNext(gatt) }, 100)
        }
    }

    private fun readSystemInfo(gatt: BluetoothGatt) {
        onConnectionStateChange(ConnectionState.READING_INFO)
        gatt.findChar(BLEConstants.SYSTEM_INFO_UUID)?.let {
            gatt.readCharacteristic(it)
        } ?: onConnectionStateChange(ConnectionState.ERROR_INFO_NOT_FOUND)
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            onConnectionStateChange(ConnectionState.ERROR_READ_CHAR)
            return
        }

        when (characteristic.uuid) {
            BLEConstants.SYSTEM_INFO_UUID -> {
                onSystemInfoUpdate(dataParser.parseSystemInfo(value))
                gatt.findChar(BLEConstants.CONTROL_UUID)?.let {
                    onConnectionStateChange(ConnectionState.READING_SETTINGS)
                    gatt.readCharacteristic(it)
                } ?: onConnectionStateChange(ConnectionState.READY)
            }
            BLEConstants.CONTROL_UUID -> {
                onControlDataUpdate(dataParser.parseControlData(value))
                onConnectionStateChange(ConnectionState.READY)
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
            onConnectionStateChange(ConnectionState.ERROR_WRITE_CHAR)
        } else if (characteristic.uuid == BLEConstants.CONTROL_UUID) {
            onConnectionStateChange(ConnectionState.READY)
        }
    }

    // --- Helper Extensions ---

    private fun BluetoothGatt.findChar(uuid: UUID): BluetoothGattCharacteristic? =
        getService(BLEConstants.SERVICE_UUID)?.getCharacteristic(uuid)
            ?: services.firstNotNullOfOrNull { it.getCharacteristic(uuid) }
}
