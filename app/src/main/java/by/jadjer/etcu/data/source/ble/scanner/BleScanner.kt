package by.jadjer.etcu.data.source.ble.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@SuppressLint("MissingPermission")
class BleScanner(private val bluetoothAdapter: BluetoothAdapter?) {

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.name != null) {
                _discoveredDevices.update { currentList ->
                    if (currentList.any { it.address == device.address }) {
                        currentList
                    } else {
                        currentList + device
                    }
                }
            }
        }
    }

    fun startScan() {
        if (bluetoothAdapter?.isEnabled == true) {
            _discoveredDevices.value = emptyList()
            bluetoothAdapter.bluetoothLeScanner?.startScan(scanCallback)
        }
    }

    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            if (bluetoothAdapter?.isEnabled != true) return emptyList()
            bluetoothAdapter.bondedDevices.toList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }
}
