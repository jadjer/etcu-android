package by.jadjer.etcu.ui.features.main

import androidx.lifecycle.ViewModel
import by.jadjer.etcu.domain.model.ConnectionState
import by.jadjer.etcu.domain.model.SystemTelemetry
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(
    private val bleRepository: BLERepository
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = bleRepository.connectionState
    val savedMac: StateFlow<String?> = bleRepository.savedMac
    val telemetry: StateFlow<SystemTelemetry> = bleRepository.telemetry

    fun clearLastMac() {
        bleRepository.clearLastMac()
    }

    fun retryConnection() {
        bleRepository.autoConnect()
    }
}
