package by.jadjer.etcu.ui.component.history

import by.jadjer.etcu.domain.model.telemetry.HistoryRecord

/**
 * A wrapper to handle generic history data in a type-safe way for the UI.
 */
data class HistoryGroup<T>(
    val history: List<HistoryRecord<T>>,
    val selector: (T) -> Float,
    val currentValue: Float
)
