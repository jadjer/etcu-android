package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import by.jadjer.etcu.domain.model.ble.DiscoveredDevice
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.BondState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("MissingPermission")
class BLEScanner(private val _central: BluetoothCentralManager) {
    private val _scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var _scanJob: Job? = null

    private val _discoveredDevices = MutableStateFlow<Map<String, DiscoveredDevice>>(emptyMap())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices
        .map { map -> map.values.sortedByDescending { it.rssi } }
        .stateIn(
            scope = _scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    fun handleDiscoveredPeripheral(peripheral: BluetoothPeripheral, scanResult: ScanResult) {
        _discoveredDevices.update { currentMap ->
            val existing = currentMap[peripheral.address]
            if (existing != null && existing.rssi == scanResult.rssi) return@update currentMap

            currentMap + (peripheral.address to DiscoveredDevice(
                name = peripheral.name,
                macAddress = peripheral.address,
                rssi = scanResult.rssi,
                isPaired = peripheral.bondState == BondState.BONDED
            ))
        }
    }

    fun startScan(timeout: Long = 15000L) {
        if (!_central.isBluetoothEnabled || _isScanning.value) return

        _discoveredDevices.value = emptyMap()
        _isScanning.value = true

        try {
            _central.scanForPeripheralsWithServices(setOf(BLEConstants.SERVICE_UUID))
        } catch (_: Exception) {
            _isScanning.value = false
            return
        }

        _scanJob?.cancel()
        _scanJob = _scope.launch {
            delay(timeout.milliseconds)
            stopScan()
        }
    }

    fun stopScan() {
        if (_isScanning.value) {
            _isScanning.value = false
            _scanJob?.cancel()
            _central.stopScan()
        }
    }
}
