package by.jadjer.etcu.domain.model.telemetry

data class SystemTelemetry(
    val status: SystemStatusTelemetry = SystemStatusTelemetry(),
    val ecu: EcuTelemetry = EcuTelemetry(),
    val servo: ServoTelemetry = ServoTelemetry(),
    val cruise: CruiseTelemetry = CruiseTelemetry(),
    val accelerator: AcceleratorTelemetry = AcceleratorTelemetry()
)
