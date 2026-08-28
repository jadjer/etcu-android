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
class BLEScanner(
    private val bluetoothAdapter: BluetoothAdapter?
) {
    private val scannerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _discoveredDevices = MutableStateFlow<Map<String, DiscoveredDevice>>(emptyMap())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices
        .map { it.values.toList() }
        .stateIn(
            scope = scannerScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: return // Пропускаем устройства без имени атомарно

            _discoveredDevices.update { currentMap ->
                currentMap.toMutableMap().apply {
                    put(
                        device.address, DiscoveredDevice(
                            name = name,
                            macAddress = device.address,
                            rssi = result.rssi,
                            distance = calculateDistance(result.rssi),
                            isPaired = device.bondState == BluetoothDevice.BOND_BONDED
                        )
                    )
                }
            }
        }
    }

    private fun calculateDistance(rssi: Int): Double {
        if (rssi == 0) return -1.0
        return 10.0.pow((-59 - rssi) / 20.0)
    }

    fun startScan() {
        if (bluetoothAdapter?.isEnabled != true || _isScanning.value) return

        _discoveredDevices.value = emptyMap()
        _isScanning.value = true

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BLEConstants.SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothAdapter.bluetoothLeScanner?.startScan(listOf(filter), settings, scanCallback)
    }

    fun stopScan() {
        if (_isScanning.value) {
            _isScanning.value = false
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        }
    }
}
