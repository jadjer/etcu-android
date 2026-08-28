package by.jadjer.etcu.ui.screen.servo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.domain.model.ServoTelemetry
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.flow.*

class ServoViewModel(repository: BLERepository) : ViewModel() {
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
