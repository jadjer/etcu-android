package by.jadjer.etcu.ui.component.history

import androidx.compose.runtime.*
import by.jadjer.etcu.domain.model.telemetry.HistoryRecord

/**
 * A wrapper to handle generic history data in a type-safe way for the UI.
 */
data class HistoryGroup<T>(
    val history: List<HistoryRecord<T>>,
    val selector: (T) -> Float,
    val currentValue: Float,
    val valueRange: ClosedFloatingPointRange<Float>? = null
)

/**
 * State holder to manage telemetry history dialogs across different screens.
 */
@Stable
class TelemetryHistoryState<T>(
    private val rangeProvider: (String) -> ClosedFloatingPointRange<Float>?
) {
    var dialogInfo by mutableStateOf<DialogInfo<T>?>(null)
        private set

    fun onValueClick(label: String, unit: String, selector: (T) -> Float) {
        dialogInfo = DialogInfo(label, unit, selector)
    }

    fun dismissDialog() {
        dialogInfo = null
    }

    @Composable
    fun ShowDialog(history: List<HistoryRecord<T>>, currentTelemetry: T) {
        dialogInfo?.let { info ->
            StatusGraphDialog(
                title = info.label,
                unit = info.unit,
                group = HistoryGroup(
                    history = history,
                    selector = info.selector,
                    currentValue = info.selector(currentTelemetry),
                    valueRange = rangeProvider(info.label)
                ),
                onDismiss = { dismissDialog() }
            )
        }
    }
}

data class DialogInfo<T>(
    val label: String,
    val unit: String,
    val selector: (T) -> Float
)

@Composable
fun <T> rememberTelemetryHistoryState(
    rangeProvider: (String) -> ClosedFloatingPointRange<Float>?
): TelemetryHistoryState<T> {
    return remember {
        TelemetryHistoryState(rangeProvider)
    }
}
