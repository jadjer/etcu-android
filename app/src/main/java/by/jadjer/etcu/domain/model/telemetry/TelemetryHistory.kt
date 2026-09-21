 package by.jadjer.etcu.domain.model.telemetry

import by.jadjer.etcu.domain.util.CircularHistoryBuffer

data class TelemetryHistory(
    val status: CircularHistoryBuffer<SystemStatusTelemetry> = CircularHistoryBuffer(),
    val ecu: CircularHistoryBuffer<ECUTelemetry> = CircularHistoryBuffer(),
    val servo: CircularHistoryBuffer<ServoTelemetry> = CircularHistoryBuffer(),
    val cruise: CircularHistoryBuffer<CruiseTelemetry> = CircularHistoryBuffer(),
    val accelerator: CircularHistoryBuffer<AcceleratorTelemetry> = CircularHistoryBuffer(),
    val lastUpdate: Long = 0L
)
