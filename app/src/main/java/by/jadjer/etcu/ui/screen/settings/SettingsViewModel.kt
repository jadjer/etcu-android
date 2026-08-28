package by.jadjer.etcu.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.domain.model.ControlData
import by.jadjer.etcu.domain.model.SystemInfo
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SettingsViewModel(private val repository: BLERepository) : ViewModel() {

    private val _controlData = MutableStateFlow(ControlData())
    val controlData: StateFlow<ControlData> = _controlData.asStateFlow()

    val systemInfo: StateFlow<SystemInfo> = repository.systemInfo
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SystemInfo()
        )

    init {
        repository.controlData
            .onEach { _controlData.value = it }
            .launchIn(viewModelScope)
    }
    
    fun disconnect() {
        repository.disconnect()
    }

    fun forgetDevice() {
        repository.clearLastMac()
        repository.disconnect()
    }

    private var updateJob: Job? = null

    fun updateAccRange(min: Int, max: Int) {
        val updated = _controlData.value.copy(
            accMin = min,
            accMax = max,
        )
        _controlData.value = updated
        scheduleUpdate(updated)
    }

    fun updateServoRange(min: Int, max: Int) {
        val updated = _controlData.value.copy(
            servoMin = min,
            servoMax = max,
        )
        _controlData.value = updated
        scheduleUpdate(updated)
    }

    private fun scheduleUpdate(data: ControlData) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            delay(250.milliseconds)
            repository.sendControlData(data)
        }
    }

    companion object {
        fun Factory(app: ETCUApplication) = viewModelFactory {
            initializer {
                SettingsViewModel(app.container.bleRepository)
            }
        }
    }
}
