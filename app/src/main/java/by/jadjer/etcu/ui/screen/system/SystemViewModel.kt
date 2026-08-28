package by.jadjer.etcu.ui.screen.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.domain.model.SystemTelemetry
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SystemViewModel(repository: BLERepository) : ViewModel() {
    val telemetry: StateFlow<SystemTelemetry> = repository.telemetry
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SystemTelemetry()
        )

    companion object {
        fun Factory(app: ETCUApplication) = viewModelFactory {
            initializer {
                SystemViewModel(app.container.bleRepository)
            }
        }
    }
}
