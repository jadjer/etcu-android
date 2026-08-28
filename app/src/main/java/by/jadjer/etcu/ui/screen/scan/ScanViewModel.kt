package by.jadjer.etcu.ui.screen.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.domain.model.DiscoveredDevice
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.flow.*

class ScanViewModel(private val repository: BLERepository) : ViewModel() {
    
    val connectionState: StateFlow<String> = repository.connectionState
    val isScanning: StateFlow<Boolean> = repository.isScanning
    
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = repository.discoveredDevices

    fun startScanning() = repository.startScan()

    fun stopScanning() = repository.stopScan()

    fun connect(device: DiscoveredDevice) {
        stopScanning()
        repository.connect(device.macAddress)
    }

    override fun onCleared() {
        stopScanning()
    }

    class Factory(private val app: ETCUApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScanViewModel(app.container.bleRepository) as T
        }
    }
}
