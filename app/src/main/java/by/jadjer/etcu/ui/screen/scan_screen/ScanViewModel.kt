package by.jadjer.etcu.ui.screen.scan_screen

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.data.model.DiscoveredDevice
import by.jadjer.etcu.data.repository.BleRepository
import kotlinx.coroutines.flow.*

class ScanViewModel(private val repository: BleRepository) : ViewModel() {
    
    val connectionState: StateFlow<String> = repository.connectionState
    
    private val _pairedDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val pairedDevices: StateFlow<List<DiscoveredDevice>> = _pairedDevices

    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = repository.discoveredDevices
        .map { devices ->
            devices.map { it.toDiscoveredDevice(isPaired = false) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        updatePairedDevices()
    }

    fun startScanning() = repository.startScan()

    fun stopScanning() = repository.stopScan()

    fun updatePairedDevices() {
        _pairedDevices.value = repository.getPairedDevices().map { 
            it.toDiscoveredDevice(isPaired = true)
        }
    }

    fun connect(device: DiscoveredDevice) {
        stopScanning()
        repository.connect(device.macAddress)
    }

    override fun onCleared() {
        stopScanning()
    }

    @SuppressLint("MissingPermission")
    private fun BluetoothDevice.toDiscoveredDevice(isPaired: Boolean): DiscoveredDevice {
        return DiscoveredDevice(
            name = name ?: "Unknown Device",
            macAddress = address,
            isPaired = isPaired
        )
    }

    class Factory(private val app: ETCUApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScanViewModel(app.container.bleRepository) as T
        }
    }
}
