package by.jadjer.etcu.domain.model.telemetry

data class SystemTelemetry(
    val status: SystemStatusTelemetry = SystemStatusTelemetry(),
    val ecu: ECUTelemetry = ECUTelemetry(),
    val servo: ServoTelemetry = ServoTelemetry(),
    val cruise: CruiseTelemetry = CruiseTelemetry(),
    val accelerator: AcceleratorTelemetry = AcceleratorTelemetry()
)
