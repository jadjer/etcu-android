package by.jadjer.etcu.domain.model.telemetry

data class ServoTelemetry(
    val isConnected: Boolean = false,
    val isEnabled: Boolean = false,
    val isMoved: Boolean = false,

    val current: Int = 0,
    val voltage: Float = 0.0f,
    val position: Int = 0,
    val temperature: Int = 0
)
