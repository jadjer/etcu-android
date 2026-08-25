package by.jadjer.etcu.ui.screen.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.data.model.SystemTelemetry
import by.jadjer.etcu.domain.repository.BleRepository
import kotlinx.coroutines.flow.*

class SystemViewModel(repository: BleRepository) : ViewModel() {
    val telemetry: StateFlow<SystemTelemetry> = repository.telemetry
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SystemTelemetry()
        )

    class Factory(private val app: ETCUApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SystemViewModel(app.container.bleRepository) as T
        }
    }
}
