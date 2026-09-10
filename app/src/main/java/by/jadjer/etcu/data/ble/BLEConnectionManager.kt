package by.jadjer.etcu.data.ble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import by.jadjer.etcu.domain.model.ble.ConnectionState
import com.welie.blessed.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("MissingPermission")
class BLEConnectionManager(
    app: Application
) {
    private val TAG = "BLEConnectionManager"
    
    private val bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter

    private val _connectionState = MutableStateFlow(ConnectionState.INITIALIZING)
    val connectionState = _connectionState.asStateFlow()

    private val _connectionDetail = MutableStateFlow<String?>(null)
    val connectionDetail = _connectionDetail.asStateFlow()

    private val _isManualForget = MutableStateFlow(false)
    val isManualForget = _isManualForget.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var reconnectJob: Job? = null

    val central: BluetoothCentralManager
    val scanner: BLEScanner
    var activePeripheral: BluetoothPeripheral? = null
        private set

    var peripheralCallback: BluetoothPeripheralCallback? = null

    private val centralCallback = object : BluetoothCentralManagerCallback() {
        override fun onDiscovered(peripheral: BluetoothPeripheral, scanResult: ScanResult) {
            scanner.handleDiscoveredPeripheral(peripheral, scanResult)
            
            if (!_isManualForget.value && _connectionState.value == ConnectionState.INITIALIZING) {
                if (peripheral.bondState == BondState.BONDED && isEtcuDevice(peripheral)) {
                    Log.i(TAG, "Auto-connecting during scan: ${peripheral.address}")
                    scanner.stopScan()
                    connect(peripheral.address)
                }
            }
        }

        override fun onConnected(peripheral: BluetoothPeripheral) {
            Log.i(TAG, "Connected: ${peripheral.address}")
            activePeripheral = peripheral
            _connectionState.value = ConnectionState.CONNECTED_DISCOVERING
        }

        override fun onConnectionFailed(peripheral: BluetoothPeripheral, status: HciStatus) {
            Log.e(TAG, "Connection failed: ${peripheral.address} ($status)")
            activePeripheral = null
            _connectionState.value = ConnectionState.ERROR_CONNECTION
            startAutoReconnectIfNeeded()
        }

        override fun onDisconnected(peripheral: BluetoothPeripheral, status: HciStatus) {
            Log.i(TAG, "Disconnected: ${peripheral.address} ($status)")
            activePeripheral = null
            _connectionState.value = ConnectionState.DISCONNECTED
            startAutoReconnectIfNeeded()
        }

        override fun onBluetoothAdapterStateChanged(state: Int) {
            if (state == BluetoothAdapter.STATE_OFF) {
                _connectionState.value = ConnectionState.BLUETOOTH_OFF
                reconnectJob?.cancel()
            }
        }
    }

    init {
        central = BluetoothCentralManager(app, centralCallback, Handler(Looper.getMainLooper()))
        scanner = BLEScanner(central)
    }

    fun connect(macAddress: String) {
        if (!central.isBluetoothEnabled) return
        
        reconnectJob?.cancel()
        _isManualForget.value = false
        _connectionState.value = ConnectionState.CONNECTING

        try {
            val peripheral = central.getPeripheral(macAddress)
            val callback = peripheralCallback ?: throw IllegalStateException("Callback not set")
            central.connect(peripheral, callback)
        } catch (e: Exception) {
            Log.e(TAG, "Connect failed", e)
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
            Log.i(TAG, "Using existing connection: ${connected.address}")
            connect(connected.address)
            return
        }

        scanner.startScan(timeout = 5000L)
        
        scope.launch {
            delay(5500.milliseconds)
            if (_connectionState.value == ConnectionState.INITIALIZING) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        }
    }

    fun forgetDevice() {
        _isManualForget.value = true
        reconnectJob?.cancel()
        
        activePeripheral?.let { peripheral ->
            val address = peripheral.address
            central.cancelConnection(peripheral)
            central.removeBond(address)
            Log.i(TAG, "Bond removed for $address")
        } ?: run {
            adapter?.bondedDevices
                ?.filter { it.name?.contains("ETCU", ignoreCase = true) == true }
                ?.forEach { device ->
                    central.removeBond(device.address)
                    Log.i(TAG, "Bond removed for ${device.address}")
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
            reconnectJob?.cancel()
            reconnectJob = scope.launch {
                delay(3000.milliseconds)
                if (!_connectionState.value.isActive) autoConnect()
            }
        }
    }
}
