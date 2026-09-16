package by.jadjer.etcu.ui.features.main

import androidx.lifecycle.ViewModel
import by.jadjer.etcu.domain.model.ble.ConnectionState
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(
    private val _bleRepository: BLERepository
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = _bleRepository.connectionState
    val connectionDetail: StateFlow<String?> = _bleRepository.connectionDetail
    val isManualForget: StateFlow<Boolean> = _bleRepository.isManualForget
    val telemetry: StateFlow<SystemTelemetry> = _bleRepository.telemetry

    fun retryConnection() {
        _bleRepository.autoConnect()
    }

    fun forgetDevice() {
        _bleRepository.forgetDevice()
    }

    fun isBonded(): Boolean {
        return _bleRepository.isBonded() && !_bleRepository.isManualForget.value
    }
}
