package by.jadjer.etcu.ui.component.history

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.telemetry.HistoryRecord
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.ui.theme.ETCUTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun <T> StatusGraphDialog(
    title: String,
    unit: String,
    group: HistoryGroup<T>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        val selector = remember(group.selector) { 
            { record: HistoryRecord<T> -> group.selector(record.data) }
        }
        
        StatusGraphDialogContent(
            title = title,
            value = "%.1f".format(group.currentValue),
            unit = unit,
            history = group.history,
            selector = selector,
            valueRange = group.valueRange,
            startTime = group.history.firstOrNull()?.timestamp ?: 0L,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun <T> StatusGraphDialogContent(
    title: String,
    value: String,
    unit: String,
    history: List<HistoryRecord<T>>,
    selector: (HistoryRecord<T>) -> Float,
    valueRange: ClosedFloatingPointRange<Float>? = null,
    startTime: Long = 0,
    onDismiss: () -> Unit
) {
    val data = remember(history, selector) { history.map(selector) }
    val minVal = remember(data) { data.minOrNull() ?: 0f }
    val maxVal = remember(data) { data.maxOrNull() ?: 0f }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val sessionStartStr = remember(startTime) {
        if (startTime > 0) timeFormat.format(Date(startTime)) else "-"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (startTime > 0) {
                Text(
                    text = "Session started at: $sessionStartStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Min
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.label_min),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%.1f".format(minVal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Current
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (unit.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.unit_format, unit),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Max
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.label_max),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%.1f".format(maxVal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            StatusGraph(
                history = history,
                selector = selector,
                valueRange = valueRange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(android.R.string.ok))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusGraphDialogPreview() {
    val now = System.currentTimeMillis()
    ETCUTheme {
        StatusGraphDialogContent(
            title = "Engine RPM",
            value = "2500",
            unit = "RPM",
            history = listOf(
                HistoryRecord(SystemTelemetry(), now),
                HistoryRecord(SystemTelemetry(), now + 1000),
                HistoryRecord(SystemTelemetry(), now + 2000),
            ),
            selector = { 2500f },
            onDismiss = {}
        )
    }
}
