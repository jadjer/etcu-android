package by.jadjer.etcu.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.domain.model.SystemTelemetry
import by.jadjer.etcu.domain.repository.BleRepository
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(
    private val bleRepository: BleRepository
) : ViewModel() {

    val isConnected: StateFlow<Boolean> = bleRepository.isConnected
    val connectionState: StateFlow<String> = bleRepository.connectionState
    val savedMac: StateFlow<String?> = bleRepository.savedMac
    val telemetry: StateFlow<SystemTelemetry> = bleRepository.telemetry

    fun clearLastMac() {
        bleRepository.clearLastMac()
    }

    fun retryConnection() {
        bleRepository.autoConnect()
    }

    class Factory(private val app: ETCUApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(app.container.bleRepository) as T
        }
    }
}
