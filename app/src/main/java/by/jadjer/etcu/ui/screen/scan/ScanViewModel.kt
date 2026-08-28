package by.jadjer.etcu.ui.screen.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import by.jadjer.etcu.ETCUApplication
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

    companion object {
        fun Factory(app: ETCUApplication) = viewModelFactory {
            initializer {
                ScanViewModel(app.container.bleRepository)
            }
        }
    }
}
