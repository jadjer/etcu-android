package by.jadjer.etcu.domain.model.control

data class PID(
    val p: Float = 0.0f,
    val i: Float = 0.0f,
    val d: Float = 0.0f,
)

data class Cruise(
    val pid: PID = PID(),
    val rpmMin: Int = 0,
    val rpmMax: Int = 0,
    val speedMin: Int = 0,
    val speedMax: Int = 0,
    val limiterUp: Int = 0,
    val limiterDown: Int = 0,
)

data class ControlData(
    val cruise: Cruise = Cruise(),
    val servoMin: Int = 0,
    val servoMax: Int = 0,
    val acceleratorMin: Int = 0,
    val acceleratorMax: Int = 0,
)
