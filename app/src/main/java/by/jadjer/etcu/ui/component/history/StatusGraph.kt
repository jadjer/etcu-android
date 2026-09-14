package by.jadjer.etcu.ui.component.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.telemetry.HistoryRecord
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.ui.theme.ETCUTheme

@Composable
fun <T> StatusGraph(
    history: List<HistoryRecord<T>>,
    selector: (HistoryRecord<T>) -> Float,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (history.size < 2) {
        Box(modifier = modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.telemetry_graph_no_data), style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val data = remember(history, selector) { history.map(selector) }
    val maxVal = remember(data) { data.maxOrNull() ?: 1f }
    val minVal = remember(data) { data.minOrNull() ?: 0f }
    val valRange = remember(maxVal, minVal) { if (maxVal == minVal) 1f else maxVal - minVal }

    val startTime = history.first().timestamp
    val endTime = history.last().timestamp
    val timeRange = remember(startTime, endTime) { if (endTime == startTime) 1L else endTime - startTime }

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            history.forEachIndexed { index, record ->
                val value: Float = data[index]
                val x = ((record.timestamp - startTime).toFloat() / timeRange) * width
                val y = height - ((value - minVal) / valRange * height)
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusGraphPreview() {
    val now = System.currentTimeMillis()
    ETCUTheme {
        StatusGraph(
            history = listOf(
                HistoryRecord(SystemTelemetry(), now),
                HistoryRecord(SystemTelemetry(), now + 1000),
                HistoryRecord(SystemTelemetry(), now + 2000),
            ),
            selector = { 10f },
            modifier = Modifier.padding(16.dp)
        )
    }
}
