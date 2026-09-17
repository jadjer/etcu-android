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
            status = updateList(currentHistory.status, newTelemetry.status, timestamp, cutoff),
            ecu = updateList(currentHistory.ecu, newTelemetry.ecu, timestamp, cutoff),
            servo = updateList(currentHistory.servo, newTelemetry.servo, timestamp, cutoff),
            cruise = updateList(currentHistory.cruise, newTelemetry.cruise, timestamp, cutoff),
            accelerator = updateList(currentHistory.accelerator, newTelemetry.accelerator, timestamp, cutoff)
        )
    }

    private fun <T> updateList(
        current: List<HistoryRecord<T>>,
        newData: T,
        timestamp: Long,
        cutoff: Long
    ): List<HistoryRecord<T>> {
        val result = ArrayList<HistoryRecord<T>>(current.size + 1)

        // Поскольку записи добавляются последовательно, мы можем найти индекс первой валидной записи
        val firstValidIndex = current.indexOfFirst { it.timestamp > cutoff }

        if (firstValidIndex != -1) {
            for (i in firstValidIndex until current.size) {
                result.add(current[i])
            }
        }

        result.add(HistoryRecord(newData, timestamp))
        return result
    }
}
