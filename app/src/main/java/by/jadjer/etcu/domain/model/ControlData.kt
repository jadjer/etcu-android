package by.jadjer.etcu.domain.model

data class ControlData(
    val servoMin: Int = 0,
    val servoMax: Int = 0,
    val acceleratorMin: Int = 0,
    val acceleratorMax: Int = 0,
)
