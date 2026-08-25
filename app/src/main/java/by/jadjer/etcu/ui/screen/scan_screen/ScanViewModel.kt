package by.jadjer.etcu.ui.screen.scan_screen

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.data.model.DiscoveredDevice
import by.jadjer.etcu.domain.repository.BleRepository
import by.jadjer.etcu.util.BleUtils
import kotlinx.coroutines.flow.*

class ScanViewModel(private val repository: BleRepository) : ViewModel() {
    
    val connectionState: StateFlow<String> = repository.connectionState
    val isScanning: StateFlow<Boolean> = repository.isScanning
    
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = repository.discoveredDevices
        .map { devices ->
            devices.map { (device, rssi) ->
                device.toDiscoveredDevice(isPaired = false, rssi = rssi)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun startScanning() = repository.startScan()

    fun stopScanning() = repository.stopScan()

    fun connect(device: DiscoveredDevice) {
        stopScanning()
        repository.connect(device.macAddress)
    }

    override fun onCleared() {
        stopScanning()
    }

    @SuppressLint("MissingPermission")
    private fun BluetoothDevice.toDiscoveredDevice(isPaired: Boolean, rssi: Int = 0): DiscoveredDevice {
        return DiscoveredDevice(
            isPaired = isPaired,
            name = name ?: "Unknown Device",
            macAddress = address,
            rssi = rssi,
            distance = BleUtils.calculateDistance(rssi)
        )
    }

    class Factory(private val app: ETCUApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScanViewModel(app.container.bleRepository) as T
        }
    }
}
