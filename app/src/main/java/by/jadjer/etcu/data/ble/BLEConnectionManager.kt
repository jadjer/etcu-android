package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import by.jadjer.etcu.domain.model.ble.ConnectionState
import by.jadjer.etcu.util.AppLogger
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothCentralManagerCallback
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.BluetoothPeripheralCallback
import com.welie.blessed.BondState
import com.welie.blessed.HciStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("MissingPermission")
class BLEConnectionManager(
    app: Application
) {
    private val _tag = "BLEConnectionManager"

    private val _bluetoothManager =
        app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val _adapter = _bluetoothManager.adapter

    private val _connectionState = MutableStateFlow(ConnectionState.INITIALIZING)
    val connectionState = _connectionState.asStateFlow()

    private val _connectionDetail = MutableStateFlow<String?>(null)
    val connectionDetail = _connectionDetail.asStateFlow()

    private val _isManualForget = MutableStateFlow(false)
    val isManualForget = _isManualForget.asStateFlow()

    private val _scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var _reconnectJob: Job? = null

    private var _currentReconnectDelayMs = 3000L
    private val _minReconnectDelayMs = 3000L
    private val _maxReconnectDelayMs = 30000L

    private val _bleHandlerThread = HandlerThread("BLEConnectionManagerThread").apply { start() }
    private val _bleHandler = Handler(_bleHandlerThread.looper)

    val central: BluetoothCentralManager
    val scanner: BLEScanner
    var activePeripheral: BluetoothPeripheral? = null
        private set

    var peripheralCallback: BluetoothPeripheralCallback? = null

    private val _centralCallback = object : BluetoothCentralManagerCallback() {
        override fun onDiscovered(peripheral: BluetoothPeripheral, scanResult: ScanResult) {
            scanner.handleDiscoveredPeripheral(peripheral, scanResult)

            if (!_isManualForget.value && _connectionState.value == ConnectionState.INITIALIZING) {
                if (peripheral.bondState == BondState.BONDED && isEtcuDevice(peripheral)) {
                    AppLogger.i(_tag) { "Auto-connecting during scan: ${peripheral.address}" }
                    scanner.stopScan()
                    connect(peripheral.address)
                }
            }
        }

        override fun onConnected(peripheral: BluetoothPeripheral) {
            AppLogger.i(_tag) { "Connected: ${peripheral.address}" }
            activePeripheral = peripheral
            _connectionState.value = ConnectionState.CONNECTED_DISCOVERING
            _currentReconnectDelayMs = _minReconnectDelayMs
        }

        override fun onConnectionFailed(peripheral: BluetoothPeripheral, status: HciStatus) {
            AppLogger.e(_tag, message = { "Connection failed: ${peripheral.address} ($status)" })
            activePeripheral = null
            _connectionState.value = ConnectionState.ERROR_CONNECTION
            startAutoReconnectIfNeeded()
        }

        override fun onDisconnected(peripheral: BluetoothPeripheral, status: HciStatus) {
            AppLogger.i(_tag) { "Disconnected: ${peripheral.address} ($status)" }
            activePeripheral = null
            _connectionState.value = ConnectionState.DISCONNECTED
            startAutoReconnectIfNeeded()
        }

        override fun onBluetoothAdapterStateChanged(state: Int) {
            if (state == BluetoothAdapter.STATE_OFF) {
                _connectionState.value = ConnectionState.BLUETOOTH_OFF
                _reconnectJob?.cancel()
            }
        }
    }

    init {
        central = BluetoothCentralManager(app, _centralCallback, _bleHandler)
        scanner = BLEScanner(central)
    }

    fun connect(macAddress: String) {
        if (!central.isBluetoothEnabled) return

        _reconnectJob?.cancel()
        _currentReconnectDelayMs = _minReconnectDelayMs
        _isManualForget.value = false
        _connectionState.value = ConnectionState.CONNECTING

        try {
            val peripheral = central.getPeripheral(macAddress)
            val callback = peripheralCallback ?: throw IllegalStateException("Callback not set")
            central.connect(peripheral, callback)
        } catch (e: Exception) {
            AppLogger.e(_tag, message = { "Connect failed" }, throwable = e)
            _connectionState.value = ConnectionState.INVALID_MAC
        }
    }

    fun autoConnect() {
        if (!central.isBluetoothEnabled || (_connectionState.value.isActive && _connectionState.value != ConnectionState.INITIALIZING)) return

        _isManualForget.value = false
        _connectionState.value = ConnectionState.INITIALIZING

        val connected = central.getConnectedPeripherals()
            .firstOrNull { isEtcuDevice(it) }

        if (connected != null) {
            AppLogger.i(_tag) { "Using existing connection: ${connected.address}" }
            connect(connected.address)
            return
        }

        scanner.startScan(timeout = 5000L)

        _scope.launch {
            delay(5500.milliseconds)
            if (_connectionState.value == ConnectionState.INITIALIZING) {
                _connectionState.value = ConnectionState.DISCONNECTED
                startAutoReconnectIfNeeded()
            }
        }
    }

    fun forgetDevice() {
        _isManualForget.value = true
        _reconnectJob?.cancel()

        activePeripheral?.let { peripheral ->
            val address = peripheral.address
            central.cancelConnection(peripheral)
            central.removeBond(address)
            AppLogger.i(_tag) { "Bond removed for $address" }
        } ?: run {
            _adapter?.bondedDevices
                ?.filter { it.name?.contains("ETCU", ignoreCase = true) == true }
                ?.forEach { device ->
                    central.removeBond(device.address)
                    AppLogger.i(_tag) { "Bond removed for ${device.address}" }
                }
        }

        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun updateState(state: ConnectionState, detail: String? = null) {
        _connectionDetail.value = detail
        _connectionState.value = state
    }

    private fun isEtcuDevice(peripheral: BluetoothPeripheral): Boolean {
        return peripheral.name.contains("ETCU", ignoreCase = true)
    }

    private fun startAutoReconnectIfNeeded() {
        if (!_isManualForget.value) {
            _reconnectJob?.cancel()
            _reconnectJob = _scope.launch {
                delay(_currentReconnectDelayMs.milliseconds)
                if (!_connectionState.value.isActive) {
                    _currentReconnectDelayMs = (_currentReconnectDelayMs * 2).coerceAtMost(_maxReconnectDelayMs)
                    autoConnect()
                }
            }
        }
    }
}
