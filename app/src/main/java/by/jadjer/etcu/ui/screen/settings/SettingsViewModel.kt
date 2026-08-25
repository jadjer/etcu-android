package by.jadjer.etcu.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.domain.model.BleControlData
import by.jadjer.etcu.domain.model.SystemInfo
import by.jadjer.etcu.domain.repository.BleRepository
import kotlinx.coroutines.flow.*

class SettingsViewModel(private val repository: BleRepository) : ViewModel() {

    private val _controlData = MutableStateFlow(BleControlData())
    val controlData: StateFlow<BleControlData> = _controlData

    val systemInfo: StateFlow<SystemInfo> = repository.systemInfo
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SystemInfo()
        )
    
    fun disconnect() {
        repository.disconnect()
    }

    fun forgetDevice() {
        repository.clearLastMac()
        repository.disconnect()
    }

    fun updateSyncEnabled(enabled: Boolean) {
        _controlData.value = _controlData.value.copy(syncEnabled = enabled)
        repository.sendControlData(_controlData.value)
    }

    fun updateAcceleratorOffset(offset: Int) {
        _controlData.value = _controlData.value.copy(acceleratorOffset = offset)
        repository.sendControlData(_controlData.value)
    }

    class Factory(private val app: ETCUApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(app.container.bleRepository) as T
        }
    }
}
