package by.jadjer.etcu.ui.features.calibration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.domain.model.calibration.CalibrationData
import by.jadjer.etcu.domain.model.calibration.CalibrationRange
import by.jadjer.etcu.domain.model.control.ControlConstants
import by.jadjer.etcu.domain.model.control.PositionRange
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class CalibrationUiState(
    val currentCalibration: CalibrationData = CalibrationData(),
    val editingCalibration: CalibrationData = CalibrationData(),
    val detectedHallA: CalibrationRange = CalibrationRange(min = 4095, max = 0),
    val detectedHallB: CalibrationRange = CalibrationRange(min = 4095, max = 0),
    val hallA: Int = 0,
    val hallB: Int = 0,
    val servoPosition: Int = 0
)

class CalibrationViewModel(private val repository: BLERepository) : ViewModel() {

    private val _detectedHallA = MutableStateFlow(CalibrationRange(min = 4095, max = 0))
    private val _detectedHallB = MutableStateFlow(CalibrationRange(min = 4095, max = 0))
    private val _editingCalibration = MutableStateFlow(CalibrationData())

    private val originalControlData = repository.controlData.value

    val uiState: StateFlow<CalibrationUiState> = combine(
        repository.calibrationData,
        _editingCalibration,
        _detectedHallA,
        _detectedHallB,
        repository.telemetry
    ) { current, editing, detA, detB, telemetry ->
        CalibrationUiState(
            currentCalibration = current,
            editingCalibration = editing,
            detectedHallA = detA,
            detectedHallB = detB,
            hallA = telemetry.accelerator.hallA,
            hallB = telemetry.accelerator.hallB,
            servoPosition = telemetry.servo.position
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalibrationUiState()
    )

    init {
        repository.calibrationData
            .onEach { _editingCalibration.value = it }
            .launchIn(viewModelScope)
        repository.telemetry
            .onEach { t ->
                val a = t.accelerator.hallA
                val b = t.accelerator.hallB

                if (a > 0) {
                    _detectedHallA.update { current ->
                        CalibrationRange(
                            min = if (a < current.min) a else current.min,
                            max = if (a > current.max) a else current.max
                        )
                    }
                }

                if (b > 0) {
                    _detectedHallB.update { current ->
                        CalibrationRange(
                            min = if (b < current.min) b else current.min,
                            max = if (b > current.max) b else current.max
                        )
                    }
                }
            }
            .launchIn(viewModelScope)

        // Set servo to full range for calibration
        repository.sendControlData(
            originalControlData.copy(
                servo = PositionRange(
                    min = ControlConstants.MIN_VALUE.toInt(),
                    max = ControlConstants.MAX_VALUE.toInt()
                )
            )
        )
    }

    override fun onCleared() {
        repository.sendControlData(originalControlData)
    }

    fun resetDetection() {
        _detectedHallA.value = CalibrationRange(min = 4095, max = 0)
        _detectedHallB.value = CalibrationRange(min = 4095, max = 0)
    }

    fun applyDetected() {
        val detA = _detectedHallA.value
        val detB = _detectedHallB.value
        val margin = 50

        val finalA = CalibrationRange(
            min = if (detA.max > detA.min) detA.min + margin else detA.min,
            max = if (detA.max > detA.min) detA.max - margin else detA.max
        )

        val finalB = CalibrationRange(
            min = if (detB.max > detB.min) detB.min + margin else detB.min,
            max = if (detB.max > detB.min) detB.max - margin else detB.max
        )

        _editingCalibration.update {
            it.copy(hallA = finalA, hallB = finalB)
        }
    }

    fun updateHallA(min: Int, max: Int) {
        _editingCalibration.update { it.copy(hallA = CalibrationRange(min, max)) }
    }

    fun updateHallB(min: Int, max: Int) {
        _editingCalibration.update { it.copy(hallB = CalibrationRange(min, max)) }
    }

    fun updateServo(min: Int, max: Int) {
        _editingCalibration.update { it.copy(servo = CalibrationRange(min, max)) }
    }

    fun saveCalibration() {
        repository.sendCalibrationData(_editingCalibration.value)
    }
}
