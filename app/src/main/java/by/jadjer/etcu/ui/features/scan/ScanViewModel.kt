package by.jadjer.etcu.ui.features.scan

import androidx.lifecycle.ViewModel
import by.jadjer.etcu.domain.model.ConnectionState
import by.jadjer.etcu.domain.model.DiscoveredDevice
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.flow.StateFlow

class ScanViewModel(private val repository: BLERepository) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = repository.connectionState
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
}
