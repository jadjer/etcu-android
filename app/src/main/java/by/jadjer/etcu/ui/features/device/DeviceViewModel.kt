package by.jadjer.etcu.ui.features.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.domain.model.control.ControlConstants
import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.control.OperatingMode
import by.jadjer.etcu.domain.model.control.PID
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class DeviceViewModel(private val _repository: BLERepository) : ViewModel() {

    val telemetry: StateFlow<SystemTelemetry> = _repository.telemetry

    private val _controlData = MutableStateFlow(ControlData())
    val controlData = _controlData.asStateFlow()

    val systemInfo = _repository.systemInfo
    val telemetryHistory = _repository.history

    val operatingMode: StateFlow<OperatingMode> = _controlData
        .map { OperatingMode.fromServoMax(it.servoMax) }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OperatingMode.CUSTOM
        )

    val errorCount: StateFlow<Int> = telemetry
        .map { it.status.activeErrors.size }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private var _updateJob: Job? = null

    init {
        _repository.controlData.onEach { _controlData.value = it }.launchIn(viewModelScope)
    }

    fun forgetDevice() {
        _repository.forgetDevice()
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
        val updated = _controlData.value.copy(acceleratorMin = min, acceleratorMax = max)
        _controlData.value = updated
        scheduleUpdate(updated)
    }

    fun updateCruisePID(p: Float, i: Float, d: Float) {
        val updated = _controlData.value.copy(
            cruise = _controlData.value.cruise.copy(pid = PID(p = p, i = i, d = d))
        )
        _controlData.value = updated
        scheduleUpdate(updated)
    }

    fun updateCruiseRPMRange(min: Int, max: Int) {
        val updated = _controlData.value.copy(
            cruise = _controlData.value.cruise.copy(rpmMin = min, rpmMax = max)
        )
        _controlData.value = updated
        scheduleUpdate(updated)
    }

    fun updateCruiseSpeedRange(min: Int, max: Int) {
        val updated = _controlData.value.copy(
            cruise = _controlData.value.cruise.copy(speedMin = min, speedMax = max)
        )
        _controlData.value = updated
        scheduleUpdate(updated)
    }

    fun updateCruiseLimiterUp(left: Int) {
        val updated = _controlData.value.copy(
            cruise = _controlData.value.cruise.copy(limiterUp = left)
        )
        _controlData.value = updated
        scheduleUpdate(updated)
    }

    fun updateCruiseLimiterDown(right: Int) {
        val updated = _controlData.value.copy(
            cruise = _controlData.value.cruise.copy(limiterDown = right)
        )
        _controlData.value = updated
        scheduleUpdate(updated)
    }

    private fun scheduleUpdate(data: ControlData) {
        _updateJob?.cancel()
        _updateJob = viewModelScope.launch {
            delay(ControlConstants.UPDATE_DELAY_MS.milliseconds)
            _repository.sendControlData(data)
        }
    }
}
