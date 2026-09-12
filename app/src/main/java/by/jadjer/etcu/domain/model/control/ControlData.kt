package by.jadjer.etcu.domain.model.control

data class Cruise(
    val p: Float = 0.0f,
    val i: Float = 0.0f,
    val d: Float = 0.0f,
    val integralMin: Float = 0.0f,
    val integralMax: Float = 0.0f,
    val filterAlpha: Float = 0.0f,
    val fadeDuration: Float = 0.0f,
    val rpm_min: Int = 0,
    val rpm_max: Int = 0,
    val speed_min: Int = 0,
    val speed_max: Int = 0,
    val limiter_left: Int = 0,
    val limiter_right: Int = 0,
)

data class ControlData(

    val cruise: Cruise = Cruise(),
    val servo_min: Int = 0,
    val servo_max: Int = 0,
    val accelerator_min: Int = 0,
    val accelerator_max: Int = 0,
)
