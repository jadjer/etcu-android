package by.jadjer.etcu.domain.model

data class ServoTelemetry(
    val isConnected: Boolean = false,
    val isMoved: Boolean = false,

    val load: Int = 0,
    val speed: Int = 0,
    val current: Int = 0,
    val voltage: Int = 0,
    val position: Int = 0,
    val temperature: Int = 0
)
