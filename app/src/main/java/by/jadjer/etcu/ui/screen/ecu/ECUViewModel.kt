package by.jadjer.etcu.ui.screen.ecu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.domain.model.ECUTelemetry
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ECUViewModel(repository: BLERepository) : ViewModel() {
    val ecuTelemetry: StateFlow<ECUTelemetry> = repository.telemetry
        .map { it.ecu }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ECUTelemetry()
        )

    companion object {
        fun Factory(app: ETCUApplication) = viewModelFactory {
            initializer {
                ECUViewModel(app.container.bleRepository)
            }
        }
    }
}
