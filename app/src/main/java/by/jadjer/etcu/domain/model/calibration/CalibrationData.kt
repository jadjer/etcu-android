package by.jadjer.etcu.domain.model.calibration

data class CalibrationData(
    val hallA: CalibrationRange = CalibrationRange(),
    val hallB: CalibrationRange = CalibrationRange(),
    val servo: CalibrationRange = CalibrationRange()
)

data class CalibrationRange(
    val min: Int = 0,
    val max: Int = 0
)
