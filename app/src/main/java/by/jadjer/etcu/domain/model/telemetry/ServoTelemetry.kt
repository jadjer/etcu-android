package by.jadjer.etcu.domain.model.telemetry

data class ServoTelemetry(
    val isConnected: Boolean = false,
    val isEnabled: Boolean = false,
    val isMoved: Boolean = false,

    val current: Int = 0,
    val voltage: Int = 0,
    val position: Int = 0,
    val temperature: Int = 0
)
