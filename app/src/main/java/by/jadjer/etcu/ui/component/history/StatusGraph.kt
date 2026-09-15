package by.jadjer.etcu.ui.component.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.telemetry.HistoryRecord
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.ui.theme.ETCUTheme
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun <T> StatusGraph(
    history: List<HistoryRecord<T>>,
    selector: (HistoryRecord<T>) -> Float,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    valueRange: ClosedFloatingPointRange<Float>? = null
) {
    if (history.size < 2) {
        EmptyGraphMessage(modifier)
        return
    }

    val data = remember(history, selector) { history.map(selector) }
    val minVal = remember(data, valueRange) { valueRange?.start ?: (data.minOrNull() ?: 0f) }
    val maxVal = remember(data, valueRange) { valueRange?.endInclusive ?: (data.maxOrNull() ?: 1f) }
    val valRange = remember(maxVal, minVal) { if (maxVal == minVal) 1f else maxVal - minVal }

    val startTime = history.first().timestamp
    val endTime = history.last().timestamp
    val totalDurationMs = endTime - startTime
    val graphWidth = (totalDurationMs * (10.dp.value / 1000f)).dp
    
    val scrollState = rememberScrollState()
    AutoScrollToEnd(history.size, scrollState)

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 8.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            YAxisLabels(minVal, maxVal, labelStyle)

            Spacer(Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(scrollState)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(graphWidth)
                        .fillMaxHeight()
                ) {
                    val width = size.width
                    val height = size.height - 20.dp.toPx()

                    drawGraphGrid(startTime, endTime, totalDurationMs, width, height, textMeasurer, labelStyle)
                    drawGraphPath(data, history, startTime, totalDurationMs, minVal, valRange, width, height, lineColor)
                }
            }
        }
    }
}

@Composable
private fun EmptyGraphMessage(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp), 
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.telemetry_graph_no_data),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AutoScrollToEnd(historySize: Int, scrollState: ScrollState) {
    var isInitial by remember { mutableStateOf(true) }
    LaunchedEffect(historySize) {
        if (isInitial) {
            snapshotFlow { scrollState.maxValue }.first { it > 0 }
            scrollState.scrollTo(scrollState.maxValue)
            isInitial = false
        } else if (scrollState.value >= scrollState.maxValue - 100) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
}

@Composable
private fun YAxisLabels(minVal: Float, maxVal: Float, style: TextStyle) {
    Column(
        modifier = Modifier
            .width(40.dp)
            .fillMaxHeight()
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        Text("%.1f".format(maxVal), style = style, textAlign = TextAlign.End)
        Text("%.1f".format((maxVal + minVal) / 2), style = style, textAlign = TextAlign.End)
        Text("%.1f".format(minVal), style = style, textAlign = TextAlign.End)
    }
}

private fun DrawScope.drawGraphGrid(
    startTime: Long,
    endTime: Long,
    totalDurationMs: Long,
    width: Float,
    height: Float,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle
) {
    val tenSecondsMs = 10000L
    val labelFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    var currentGridTime = (startTime / tenSecondsMs + 1) * tenSecondsMs

    while (currentGridTime < endTime) {
        val gridX = ((currentGridTime - startTime).toFloat() / totalDurationMs) * width
        drawLine(
            color = Color.LightGray.copy(alpha = 0.3f),
            start = Offset(gridX, 0f),
            end = Offset(gridX, height),
            strokeWidth = 1.dp.toPx()
        )

        if (currentGridTime % 30000L == 0L) {
            val timeStr = labelFormat.format(Date(currentGridTime))
            val textLayoutResult = textMeasurer.measure(timeStr, labelStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(gridX - textLayoutResult.size.width / 2, height + 4.dp.toPx())
            )
        }
        currentGridTime += tenSecondsMs
    }
}

private fun DrawScope.drawGraphPath(
    data: List<Float>,
    history: List<HistoryRecord<*>>,
    startTime: Long,
    totalDurationMs: Long,
    minVal: Float,
    valRange: Float,
    width: Float,
    height: Float,
    lineColor: Color
) {
    val path = Path().apply {
        history.forEachIndexed { index, record ->
            val value = data[index]
            val x = ((record.timestamp - startTime).toFloat() / totalDurationMs) * width
            val y = height - ((value - minVal) / valRange * height)
            if (index == 0) moveTo(x, y) else lineTo(x, y)
        }
    }
    drawPath(path = path, color = lineColor, style = Stroke(width = 2.dp.toPx()))
}

@Preview(showBackground = true)
@Composable
private fun StatusGraphPreview() {
    val now = System.currentTimeMillis()
    ETCUTheme {
        StatusGraph(
            history = listOf(
                HistoryRecord(SystemTelemetry(), now - 5000),
                HistoryRecord(SystemTelemetry(), now - 4000),
                HistoryRecord(SystemTelemetry(), now - 3000),
                HistoryRecord(SystemTelemetry(), now - 2000),
                HistoryRecord(SystemTelemetry(), now - 1000),
                HistoryRecord(SystemTelemetry(), now),
            ),
            selector = { 10f },
            valueRange = 0f..100f,
            modifier = Modifier.padding(16.dp)
        )
    }
}
