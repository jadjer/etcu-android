package by.jadjer.etcu.data.model

data class EcuTelemetry(
    val isConnected: Boolean = false,
    val isStarted: Boolean = false,
    val isClutchEnabled: Boolean = false,

    val rpm: Int = 0,
    val speed: Int = 0,
    val tps: Int = 0,
)
