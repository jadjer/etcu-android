package by.jadjer.etcu.domain.model

data class ECUTelemetry(
    val isConnected: Boolean = false,
    val isStarted: Boolean = false,
    val isClutchEnabled: Boolean = false,

    val rpm: Int = 0,
    val speed: Int = 0,
    val tps: Int = 0,
)
