package by.jadjer.etcu.ui.features.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.domain.model.control.ControlConstants
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class DeviceUiState(
    val controlData: ControlData = ControlData(),
    val systemInfo: SystemInfo = SystemInfo(),
    val operatingMode: OperatingMode = OperatingMode.CUSTOM,
    val telemetryHistory: List<SystemTelemetry> = emptyList()
)

class DeviceViewModel(private val repository: BLERepository) : ViewModel() {

    val telemetry: StateFlow<SystemTelemetry> = repository.telemetry

    private val _telemetryHistory = MutableStateFlow<List<SystemTelemetry>>(emptyList())
    private val _controlData = MutableStateFlow(ControlData())

    val uiState: StateFlow<DeviceUiState> = combine(
        _controlData,
        repository.systemInfo,
        _telemetryHistory
    ) { controlData, systemInfo, history ->
        DeviceUiState(
            controlData = controlData,
            systemInfo = systemInfo,
            operatingMode = OperatingMode.fromServoMax(controlData.servo.max),
            telemetryHistory = history
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DeviceUiState()
    )

    private var updateJob: Job? = null

    init {
        repository.controlData
            .onEach { _controlData.value = it }
            .launchIn(viewModelScope)

        telemetry
            .onEach { t ->
                _telemetryHistory.update { history ->
                    (history + t).takeLast(1000)
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
        val updated = _controlData.value.copy(servo = _controlData.value.servo.copy(min = min, max = max))
        _controlData.value = updated
        scheduleUpdate(updated)
    }

    fun updateOperatingMode(mode: OperatingMode) {
        mode.servoMax?.let { max ->
            val updated = _controlData.value.copy(servo = _controlData.value.servo.copy(max = max))
            _controlData.value = updated
            scheduleUpdate(updated)
        }
    }

    fun updateAccRange(min: Int, max: Int) {
        val updated = _controlData.value.copy(
            accelerator = _controlData.value.accelerator.copy(min = min, max = max)
        )
        _controlData.value = updated
        scheduleUpdate(updated)
    }

    private fun scheduleUpdate(data: ControlData) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            delay(ControlConstants.UPDATE_DELAY_MS.milliseconds)
            repository.sendControlData(data)
        }
    }
}
