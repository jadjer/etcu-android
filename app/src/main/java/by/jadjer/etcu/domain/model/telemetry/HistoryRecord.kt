package by.jadjer.etcu.domain.model.telemetry

data class HistoryRecord<T>(
    val data: T,
    val timestamp: Long = System.currentTimeMillis()
)
