package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import by.jadjer.etcu.domain.model.DiscoveredDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlin.math.pow

@SuppressLint("MissingPermission")
class BleScanner(private val bluetoothAdapter: BluetoothAdapter?) {
    private val _scannerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _discoveredDevices = MutableStateFlow<Map<String, DiscoveredDevice>>(emptyMap())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices
        .map { it.values.toList() }
        .stateIn(
            scope = _scannerScope,
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
                    val distance = calculateDistance(result.rssi)
                    currentMap + (device.address to DiscoveredDevice(
                        name = device.name ?: "Unknown",
                        macAddress = device.address,
                        rssi = result.rssi,
                        distance = distance,
                        isPaired = device.bondState == BluetoothDevice.BOND_BONDED
                    ))
                }
            }
        }
    }

    private fun calculateDistance(rssi: Int): Double {
        if (rssi == 0) return -1.0
        val txPower = -59
        return 10.0.pow((txPower - rssi) / (10.0 * 2.0))
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
}
