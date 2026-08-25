package by.jadjer.etcu.ui.screen.ecu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.data.model.EcuTelemetry
import by.jadjer.etcu.domain.repository.BleRepository
import kotlinx.coroutines.flow.*

class EcuViewModel(repository: BleRepository) : ViewModel() {
    val ecuTelemetry: StateFlow<EcuTelemetry> = repository.telemetry
        .map { it.ecu }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EcuTelemetry()
        )

    class Factory(private val app: ETCUApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EcuViewModel(app.container.bleRepository) as T
        }
    }
}
