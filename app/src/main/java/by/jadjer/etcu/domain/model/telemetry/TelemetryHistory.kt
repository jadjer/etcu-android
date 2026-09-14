package by.jadjer.etcu.domain.model.telemetry

data class TelemetryHistory(
    val status: List<HistoryRecord<SystemStatusTelemetry>> = emptyList(),
    val ecu: List<HistoryRecord<EcuTelemetry>> = emptyList(),
    val servo: List<HistoryRecord<ServoTelemetry>> = emptyList(),
    val cruise: List<HistoryRecord<CruiseTelemetry>> = emptyList(),
    val accelerator: List<HistoryRecord<AcceleratorTelemetry>> = emptyList()
)
