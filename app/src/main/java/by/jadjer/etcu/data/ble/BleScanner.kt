package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*

@SuppressLint("MissingPermission")
class BleScanner(private val bluetoothAdapter: BluetoothAdapter?) {

    private val scannerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _discoveredDevices = MutableStateFlow<Map<String, Pair<BluetoothDevice, Int>>>(emptyMap())
    val discoveredDevices: StateFlow<List<Pair<BluetoothDevice, Int>>> = _discoveredDevices
        .map { it.values.toList() }
        .stateIn(
            scope = scannerScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.name != null) {
                _discoveredDevices.update { currentMap ->
                    currentMap + (device.address to (device to result.rssi))
                }
            }
        }
    }

    fun startScan() {
        if (bluetoothAdapter?.isEnabled == true && !_isScanning.value) {
            _discoveredDevices.value = emptyMap()
            _isScanning.value = true

            val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(BleConstants.SERVICE_UUID))
                .build()

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            bluetoothAdapter.bluetoothLeScanner?.startScan(listOf(filter), settings, scanCallback)
        }
    }

    fun stopScan() {
        if (_isScanning.value) {
            _isScanning.value = false
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            if (bluetoothAdapter?.isEnabled != true) return emptyList()
            bluetoothAdapter.bondedDevices.toList()
        } catch (_: SecurityException) {
            emptyList()
        }
    }
}