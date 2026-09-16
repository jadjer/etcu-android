package by.jadjer.etcu.domain.util

import by.jadjer.etcu.domain.model.telemetry.HistoryRecord
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.domain.model.telemetry.TelemetryConstants
import by.jadjer.etcu.domain.model.telemetry.TelemetryHistory

class TelemetryHistoryManager {

    fun updateHistory(
        currentHistory: TelemetryHistory,
        newTelemetry: SystemTelemetry
    ): TelemetryHistory {
        val timestamp = System.currentTimeMillis()
        val cutoff = timestamp - TelemetryConstants.HISTORY_DURATION_MS

        return TelemetryHistory(
            status = (currentHistory.status + HistoryRecord(
                newTelemetry.status,
                timestamp
            )).filter { it.timestamp > cutoff },
            ecu = (currentHistory.ecu + HistoryRecord(
                newTelemetry.ecu,
                timestamp
            )).filter { it.timestamp > cutoff },
            servo = (currentHistory.servo + HistoryRecord(
                newTelemetry.servo,
                timestamp
            )).filter { it.timestamp > cutoff },
            cruise = (currentHistory.cruise + HistoryRecord(
                newTelemetry.cruise,
                timestamp
            )).filter { it.timestamp > cutoff },
            accelerator = (currentHistory.accelerator + HistoryRecord(
                newTelemetry.accelerator,
                timestamp
            )).filter { it.timestamp > cutoff }
        )
    }
}
