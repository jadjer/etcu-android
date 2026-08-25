package by.jadjer.etcu.ui.screen.servo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.data.model.ServoTelemetry
import by.jadjer.etcu.domain.repository.BleRepository
import kotlinx.coroutines.flow.*

class ServoViewModel(repository: BleRepository) : ViewModel() {
    val servoTelemetry: StateFlow<ServoTelemetry> = repository.telemetry
        .map { it.servo }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ServoTelemetry()
        )

    class Factory(private val app: ETCUApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ServoViewModel(app.container.bleRepository) as T
        }
    }
}
