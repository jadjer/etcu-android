package by.jadjer.etcu.ui.component.telemetry

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.ui.theme.ETCUTheme

@Composable
fun TelemetryGraph(
    data: List<Int>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.size < 2) {
        Box(modifier = modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("Not enough data", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxVal = data.maxOrNull()?.toFloat() ?: 1f
    val minVal = data.minOrNull()?.toFloat() ?: 0f
    val range = if (maxVal == minVal) 1f else maxVal - minVal

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1)

        val path = Path().apply {
            data.forEachIndexed { index, value ->
                val x = index * stepX
                val y = height - ((value - minVal) / range * height)
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
private fun TelemetryGraphPreview() {
    ETCUTheme {
        TelemetryGraph(
            data = listOf(10, 50, 20, 80, 40, 90, 30, 100, 60, 70),
            modifier = Modifier.padding(16.dp)
        )
    }
}
