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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.abs

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

    var selectedPoint by remember { mutableStateOf<Pair<Long, Float>?>(null) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp), contentAlignment = Alignment.Center
        ) {
            if (selectedPoint != null) {
                Text(
                    text = "${timeFormat.format(Date(selectedPoint!!.first))}: ${
                        "%.2f".format(
                            selectedPoint!!.second
                        )
                    }",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
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
                        .pointerInput(history, totalDurationMs, startTime) {
                            val touchSlop = viewConfiguration.touchSlop

                            awaitPointerEventScope {
                                var startX = 0f
                                var isMoving = false

                                while (true) {
                                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull()

                                    if (change != null) {
                                        when (event.type) {
                                            PointerEventType.Press -> {
                                                startX = change.position.x
                                                isMoving = false
                                            }

                                            PointerEventType.Move -> {
                                                if (abs(change.position.x - startX) > touchSlop) {
                                                    isMoving = true
                                                }
                                            }

                                            PointerEventType.Release -> {
                                                if (!isMoving) {
                                                    val clickedTime =
                                                        startTime + (change.position.x / size.width * totalDurationMs).toLong()
                                                    val closest =
                                                        history.minByOrNull { abs(it.timestamp - clickedTime) }
                                                    closest?.let {
                                                        selectedPoint = it.timestamp to selector(it)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height - 20.dp.toPx()

                    val viewportStart = scrollState.value.toFloat()
                    val viewportEnd = viewportStart + width

                    drawGraphGrid(
                        startTime,
                        endTime,
                        totalDurationMs,
                        width,
                        height,
                        textMeasurer,
                        labelStyle,
                        viewportStart,
                        viewportEnd
                    )

                    drawGraphPath(
                        data,
                        history,
                        startTime,
                        totalDurationMs,
                        minVal,
                        valRange,
                        width,
                        height,
                        lineColor,
                        viewportStart,
                        viewportEnd
                    )

                    selectedPoint?.let { (time, value) ->
                        drawSelectionHighlight(
                            time,
                            value,
                            startTime,
                            totalDurationMs,
                            minVal,
                            valRange,
                            width,
                            height,
                            lineColor
                        )
                    }
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
        Text(
            "%.1f".format(maxVal),
            style = style,
            textAlign = TextAlign.End
        )
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
    labelStyle: TextStyle,
    viewportStart: Float,
    viewportEnd: Float
) {
    val tenSecondsMs = 10000L
    val labelFormat = SimpleDateFormat(
        "HH:mm:ss",
        Locale.getDefault()
    )
    val estimatedMsPerPixel = totalDurationMs.toFloat() / width
    val minVisibleTime = startTime + (viewportStart * estimatedMsPerPixel).toLong()
    var currentGridTime =
        ((minVisibleTime / tenSecondsMs) * tenSecondsMs).coerceAtLeast(startTime + tenSecondsMs)
    while (currentGridTime < endTime) {
        val gridX =
            ((currentGridTime - startTime).toFloat() / totalDurationMs) * width// Оптимизация: Выходим из цикла, если сетка ушла правее экрана
        if (gridX > viewportEnd) break
        if (gridX >= viewportStart) {
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
    lineColor: Color,
    viewportStart: Float,
    viewportEnd: Float
) {
    val path = Path()
    var isPathEmpty = true// Запас в пикселях, чтобы линии на стыке экрана не обрывались резко
    val padding = 50f
    val activeRange = (viewportStart - padding)..(viewportEnd + padding)
    history.forEachIndexed { index, record ->
        val x =
            ((record.timestamp - startTime).toFloat() / totalDurationMs) * width// Оптимизация: обрабатываем только точки в зоне видимости (+/- запас)
        if (x in activeRange) {
            val value = data[index]
            val y = height - ((value - minVal) / valRange * height)
            if (isPathEmpty) {
                path.moveTo(x, y)
                isPathEmpty = false
            } else {
                path.lineTo(x, y)
            }
        } else if (!isPathEmpty && x > viewportEnd + padding) {// Если мы уже вышли далеко за правый край экрана — прерываем цикл,// так как следующие точки истории гарантированно не видны
            return@forEachIndexed
        }
    }
    if (!isPathEmpty) {
        drawPath(path = path, color = lineColor, style = Stroke(width = 2.dp.toPx()))
    }
}

private fun DrawScope.drawSelectionHighlight(
    time: Long,
    value: Float,
    startTime: Long,
    totalDurationMs: Long,
    minVal: Float,
    valRange: Float,
    width: Float,
    height: Float,
    lineColor: Color
) {
    val x = ((time - startTime).toFloat() / totalDurationMs) * width
    val y = height - ((value - minVal) / valRange * height)
    drawLine(
        color = lineColor.copy(alpha = 0.5f),
        start = Offset(x, 0f),
        end = Offset(x, height),
        strokeWidth = 1.dp.toPx()
    )
    drawCircle(color = lineColor, radius = 5.dp.toPx(), center = Offset(x, y))
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
            ), selector = { 10f }, valueRange = 0f..100f, modifier = Modifier.padding(16.dp)
        )
    }
}
