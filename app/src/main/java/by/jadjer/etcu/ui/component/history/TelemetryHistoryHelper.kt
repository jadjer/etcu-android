package by.jadjer.etcu.ui.component.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import by.jadjer.etcu.domain.model.telemetry.HistoryRecord

data class HistoryGroup<T>(
    val history: List<HistoryRecord<T>>,
    val selector: (T) -> Float,
    val currentValue: Float,
    val valueRange: ClosedFloatingPointRange<Float>? = null
)

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
    fun ShowDialog(
        historyProvider: () -> List<HistoryRecord<T>>,
        currentTelemetryProvider: () -> T
    ) {
        val info = dialogInfo ?: return

        val historyGroup by remember(info) {
            derivedStateOf {
                val currentTelemetry = currentTelemetryProvider()
                HistoryGroup(
                    history = historyProvider(),
                    selector = info.selector,
                    currentValue = info.selector(currentTelemetry),
                    valueRange = rangeProvider(info.label)
                )
            }
        }

        StatusGraphDialog(
            title = info.label,
            unit = info.unit,
            group = historyGroup,
            onDismiss = { dismissDialog() }
        )
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
    val currentRangeProvider by rememberUpdatedState(rangeProvider)
    return remember {
        TelemetryHistoryState(currentRangeProvider)
    }
}
