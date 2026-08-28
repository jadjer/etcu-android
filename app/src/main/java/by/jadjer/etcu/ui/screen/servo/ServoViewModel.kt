package by.jadjer.etcu.ui.screen.servo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.domain.model.ServoTelemetry
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ServoViewModel(repository: BLERepository) : ViewModel() {
    val servoTelemetry: StateFlow<ServoTelemetry> = repository.telemetry
        .map { it.servo }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ServoTelemetry()
        )

    companion object {
        fun Factory(app: ETCUApplication) = viewModelFactory {
            initializer {
                ServoViewModel(app.container.bleRepository)
            }
        }
    }
}
