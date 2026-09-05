package by.jadjer.etcu.domain.model.telemetry

data class ECUTelemetry(
    val isConnected: Boolean = false,
    val isStarted: Boolean = false,
    val isClutchEnabled: Boolean = false,

    val rpm: Int = 0,
    val battery: Int = 0,
    val speed: Int = 0,
    val map: Int = 0,
    val tps: Int = 0,
    val airTemp: Int = 0,
    val coolantTemp: Int = 0,
)
