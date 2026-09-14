package by.jadjer.etcu.ui.features.scan

import androidx.lifecycle.ViewModel
import by.jadjer.etcu.domain.model.ble.ConnectionState
import by.jadjer.etcu.domain.model.ble.DiscoveredDevice
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.flow.StateFlow

class ScanViewModel(private val _repository: BLERepository) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = _repository.connectionState
    val isScanning: StateFlow<Boolean> = _repository.isScanning

    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _repository.discoveredDevices

    fun startScanning() = _repository.startScan()

    fun stopScanning() = _repository.stopScan()

    fun connect(device: DiscoveredDevice) {
        stopScanning()
        _repository.connect(device.macAddress)
    }

    override fun onCleared() {
        stopScanning()
    }
}
