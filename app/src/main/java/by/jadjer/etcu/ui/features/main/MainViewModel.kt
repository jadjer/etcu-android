package by.jadjer.etcu.ui.features.main

import androidx.lifecycle.ViewModel
import by.jadjer.etcu.domain.model.ble.ConnectionState
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(
    private val bleRepository: BLERepository
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = bleRepository.connectionState
    val connectionDetail: StateFlow<String?> = bleRepository.connectionDetail
    val isManualForget: StateFlow<Boolean> = bleRepository.isManualForget
    val telemetry: StateFlow<SystemTelemetry> = bleRepository.telemetry

    fun retryConnection() {
        bleRepository.autoConnect()
    }
    
    fun forgetDevice() {
        bleRepository.forgetDevice()
    }

    fun isBonded(): Boolean {
        return bleRepository.isBonded() && !bleRepository.isManualForget.value
    }
}
