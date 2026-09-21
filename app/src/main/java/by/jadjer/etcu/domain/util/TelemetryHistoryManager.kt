package by.jadjer.etcu.domain.util

import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.domain.model.telemetry.TelemetryHistory

class TelemetryHistoryManager {

    fun updateHistory(
        currentHistory: TelemetryHistory,
        newTelemetry: SystemTelemetry
    ) {
        val timestamp = System.currentTimeMillis()

        currentHistory.status.add(newTelemetry.status, timestamp)
        currentHistory.ecu.add(newTelemetry.ecu, timestamp)
        currentHistory.servo.add(newTelemetry.servo, timestamp)
        currentHistory.cruise.add(newTelemetry.cruise, timestamp)
        currentHistory.accelerator.add(newTelemetry.accelerator, timestamp)
    }
}
