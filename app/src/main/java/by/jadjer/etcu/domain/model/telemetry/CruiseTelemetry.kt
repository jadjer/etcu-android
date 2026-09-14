package by.jadjer.etcu.domain.model.telemetry

data class CruiseTelemetry(
    val isEnabled: Boolean = false,
    val isActivated: Boolean = false,

    val error: Float = 0.0f,
    val correction: Float = 0.0f,
    val derivative: Float = 0.0f,

    val targetSpeed: Int = 0,
    val currentSpeed: Int = 0,

    val lastPosition: Int = 0,
    val currentPosition: Int = 0,
)