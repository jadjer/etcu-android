package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.os.Looper
import by.jadjer.etcu.data.local.BLEPreferenceManager
import by.jadjer.etcu.domain.model.ConnectionState
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothCentralManagerCallback
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.BluetoothPeripheralCallback
import com.welie.blessed.HciStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("MissingPermission")
class BLEConnectionManager(
    app: Application,
    private val preferenceManager: BLEPreferenceManager
) {
    var peripheralCallback: BluetoothPeripheralCallback? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _savedMac = MutableStateFlow(preferenceManager.getLastMac())
    val savedMac = _savedMac.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isManualDisconnect = false
    private var reconnectJob: Job? = null
    private var autoConnectJob: Job? = null

    val central: BluetoothCentralManager
    val scanner: BLEScanner
    var activePeripheral: BluetoothPeripheral? = null
        private set

    private val centralCallback = object : BluetoothCentralManagerCallback() {
        override fun onDiscoveredPeripheral(peripheral: BluetoothPeripheral, scanResult: android.bluetooth.le.ScanResult) {
            scanner.handleDiscoveredPeripheral(peripheral, scanResult)
        }

        override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {
            activePeripheral = peripheral
            _connectionState.value = ConnectionState.CONNECTED_DISCOVERING
        }

        override fun onConnectionFailed(peripheral: BluetoothPeripheral, status: HciStatus) {
            activePeripheral = null
            _connectionState.value = ConnectionState.ERROR_CONNECTION
            startAutoReconnectIfNeeded()
        }

        override fun onDisconnectedPeripheral(peripheral: BluetoothPeripheral, status: HciStatus) {
            activePeripheral = null
            _connectionState.value = ConnectionState.DISCONNECTED
            startAutoReconnectIfNeeded()
        }

        override fun onBluetoothAdapterStateChanged(state: Int) {
            if (state == BluetoothAdapter.STATE_OFF) {
                _connectionState.value = ConnectionState.BLUETOOTH_OFF
                stopAllJobs()
            }
        }
    }

    init {
        central = BluetoothCentralManager(app, centralCallback, Handler(Looper.getMainLooper()))
        scanner = BLEScanner(central)
    }

    fun connect(macAddress: String) {
        if (!central.isBluetoothEnabled) {
            _connectionState.value = ConnectionState.BLUETOOTH_OFF
            return
        }

        stopAllJobs()
        isManualDisconnect = false
        _connectionState.value = ConnectionState.CONNECTING

        runCatching {
            val peripheral = central.getPeripheral(macAddress)
            val callback = peripheralCallback ?: throw IllegalStateException("Peripheral callback not set")
            central.connectPeripheral(peripheral, callback)
        }.onFailure { e ->
            _connectionState.value = if (e is SecurityException) ConnectionState.BLUETOOTH_OFF else ConnectionState.INVALID_MAC
        }
    }

    fun disconnect() {
        isManualDisconnect = true
        stopAllJobs()
        activePeripheral?.let { central.cancelConnection(it) }
    }

    fun autoConnect() {
        val mac = preferenceManager.getLastMac() ?: return
        if (!central.isBluetoothEnabled || _connectionState.value.isActive) return

        autoConnectJob?.cancel()
        autoConnectJob = scope.launch {
            _connectionState.value = ConnectionState.SCANNING
            scanner.startScan()

            runCatching {
                withTimeout(15000.milliseconds) {
                    scanner.discoveredDevices
                        .firstOrNull { list -> list.any { it.macAddress == mac } }
                        ?.let {
                            scanner.stopScan()
                            connect(mac)
                        }
                }
            }.onFailure {
                scanner.stopScan()
                if (_connectionState.value == ConnectionState.SCANNING) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    startAutoReconnectIfNeeded()
                }
            }
        }
    }

    fun clearLastMac() {
        isManualDisconnect = true
        stopAllJobs()
        activePeripheral?.let { central.cancelConnection(it) }
        preferenceManager.clearLastMac()
        _savedMac.value = null
    }

    fun updateState(state: ConnectionState) {
        _connectionState.value = state
    }

    fun updateSavedMac(mac: String) {
        preferenceManager.saveLastMac(mac)
        _savedMac.value = mac
    }

    private fun startAutoReconnectIfNeeded() {
        if (!isManualDisconnect && preferenceManager.getLastMac() != null) {
            reconnectJob?.cancel()
            reconnectJob = scope.launch {
                delay(3000.milliseconds)
                if (!_connectionState.value.isActive) autoConnect()
            }
        }
    }

    private fun stopAllJobs() {
        reconnectJob?.cancel()
        autoConnectJob?.cancel()
        scanner.stopScan()
    }
}
