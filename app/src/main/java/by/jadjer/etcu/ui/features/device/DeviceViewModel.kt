package by.jadjer.etcu.ui.features.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.control.OperatingMode
import by.jadjer.etcu.domain.model.system.SystemInfo
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class DeviceViewModel(private val repository: BLERepository) : ViewModel() {

    val telemetry: StateFlow<SystemTelemetry> = repository.telemetry
    val systemInfo: StateFlow<SystemInfo> = repository.systemInfo

    private val _telemetryHistory = MutableStateFlow<List<SystemTelemetry>>(emptyList())
    val telemetryHistory: StateFlow<List<SystemTelemetry>> = _telemetryHistory.asStateFlow()

    private val _controlData = MutableStateFlow(ControlData())
    val controlData: StateFlow<ControlData> = _controlData.asStateFlow()

    val operatingMode: StateFlow<OperatingMode> = controlData
        .map { OperatingMode.fromServoMax(it.servoMax) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OperatingMode.CUSTOM)

    private var updateJob: Job? = null

    init {
        repository.controlData
            .onEach { _controlData.value = it }
            .launchIn(viewModelScope)

        telemetry
            .onEach { t ->
                _telemetryHistory.update { history ->
                    (history + t).takeLast(10000)
                }
            }
            .launchIn(viewModelScope)
    }

    fun disconnect() = repository.disconnect()

    fun forgetDevice() {
        repository.clearLastMac()
        repository.disconnect()
    }

    fun updateServoRange(min: Int, max: Int) {
        val updated = _controlData.value.copy(servoMin = min, servoMax = max)
        _controlData.value = updated
        scheduleUpdate(updated)
    }

    fun updateOperatingMode(mode: OperatingMode) {
        mode.servoMax?.let { max ->
            val updated = _controlData.value.copy(servoMax = max)
            _controlData.value = updated
            scheduleUpdate(updated)
        }
    }

    fun updateAccRange(min: Int, max: Int) {
        val updated = _controlData.value.copy(
            acceleratorMin = min,
            acceleratorMax = max
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
}
