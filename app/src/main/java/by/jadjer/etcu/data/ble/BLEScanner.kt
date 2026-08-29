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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("MissingPermission")
class BLEScanner(
    private val bluetoothAdapter: BluetoothAdapter?
) {
    private val scannerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var scanJob: Job? = null

    private val _discoveredDevices = MutableStateFlow<Map<String, DiscoveredDevice>>(emptyMap())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices
        .map { it.values.sortedByDescending { device -> device.rssi }.toList() }
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
            val name = device.name ?: return

            _discoveredDevices.update { currentMap ->
                val existing = currentMap[device.address]
                if (existing != null && existing.rssi == result.rssi) return@update currentMap
                
                currentMap + (device.address to DiscoveredDevice(
                    name = name,
                    macAddress = device.address,
                    rssi = result.rssi,
                    distance = calculateDistance(result.rssi),
                    isPaired = device.bondState == BluetoothDevice.BOND_BONDED
                ))
            }
        }

        override fun onScanFailed(errorCode: Int) {
            stopScan()
        }
    }

    private fun calculateDistance(rssi: Int): Double {
        if (rssi == 0) return -1.0
        return 10.0.pow((-59 - rssi) / 20.0)
    }

    fun startScan(timeout: Long = 15000L) {
        if (bluetoothAdapter?.isEnabled != true || _isScanning.value) return

        _discoveredDevices.value = emptyMap()
        
        val scanner = bluetoothAdapter.bluetoothLeScanner ?: return
        
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BLEConstants.SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        _isScanning.value = true
        scanner.startScan(listOf(filter), settings, scanCallback)

        scanJob?.cancel()
        scanJob = scannerScope.launch {
            delay(timeout.milliseconds)
            stopScan()
        }
    }

    fun stopScan() {
        if (_isScanning.value) {
            _isScanning.value = false
            scanJob?.cancel()
            runCatching {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            }
        }
    }
}
