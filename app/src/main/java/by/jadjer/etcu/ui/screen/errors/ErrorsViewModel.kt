package by.jadjer.etcu.ui.screen.errors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.data.model.SystemError
import by.jadjer.etcu.data.repository.BleRepository
import kotlinx.coroutines.flow.*

class ErrorsViewModel(repository: BleRepository) : ViewModel() {
    val activeErrors: StateFlow<List<SystemError>> = repository.telemetry
        .map { it.activeErrors }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    class Factory(private val app: ETCUApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ErrorsViewModel(app.container.bleRepository) as T
        }
    }
}
