package by.jadjer.etcu.ui.features.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.domain.model.*
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class DeviceViewModel(private val repository: BLERepository) : ViewModel() {

    // Telemetry and Info
    val telemetry: StateFlow<SystemTelemetry> = repository.telemetry
    val systemInfo: StateFlow<SystemInfo> = repository.systemInfo

    // Control Data (local copy for editing)
    private val _controlData = MutableStateFlow(ControlData())
    val controlData: StateFlow<ControlData> = _controlData.asStateFlow()

    private var updateJob: Job? = null

    init {
        repository.controlData
            .onEach { _controlData.value = it }
            .launchIn(viewModelScope)
    }

    // Actions
    fun disconnect() {
        repository.disconnect()
    }

    fun forgetDevice() {
        repository.clearLastMac()
        repository.disconnect()
    }

    fun updateAccRange(min: Int, max: Int) {
        val updated = _controlData.value.copy(accMin = min, accMax = max)
        _controlData.value = updated
        scheduleUpdate(updated)
    }

    fun updateServoRange(min: Int, max: Int) {
        val updated = _controlData.value.copy(servoMin = min, servoMax = max)
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
}
