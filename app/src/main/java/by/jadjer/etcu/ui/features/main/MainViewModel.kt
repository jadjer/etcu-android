package by.jadjer.etcu.ui.features.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.domain.model.ConnectionState
import by.jadjer.etcu.domain.model.SystemTelemetry
import by.jadjer.etcu.domain.repository.BLERepository
import by.jadjer.etcu.ui.features.device.DeviceViewModel
import by.jadjer.etcu.ui.features.ota.OtaViewModel
import by.jadjer.etcu.ui.features.scan.ScanViewModel
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

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as ETCUApplication
                val bleRepository = application.container.bleRepository
                val otaRepository = application.container.otaRepository
                
                return when {
                    modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(bleRepository) as T
                    modelClass.isAssignableFrom(DeviceViewModel::class.java) -> DeviceViewModel(bleRepository) as T
                    modelClass.isAssignableFrom(ScanViewModel::class.java) -> ScanViewModel(bleRepository) as T
                    modelClass.isAssignableFrom(OtaViewModel::class.java) -> OtaViewModel(bleRepository, otaRepository, application.applicationContext) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
