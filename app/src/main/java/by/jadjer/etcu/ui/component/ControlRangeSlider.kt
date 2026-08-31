package by.jadjer.etcu.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import by.jadjer.etcu.ui.features.main.LocalPagerScrollEnabled
import by.jadjer.etcu.ui.theme.ETCUTheme

@Composable
fun ControlRangeSlider(
    label: String,
    currentMin: Int,
    currentMax: Int,
    onRangeChange: (Float, Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1000f,
    steps: Int = 100
) {
    val pagerScrollEnabled = LocalPagerScrollEnabled.current
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        RangeSlider(
            value = currentMin.toFloat()..currentMax.toFloat(),
            onValueChange = { range ->
                pagerScrollEnabled.value = false
                onRangeChange(range.start, range.endInclusive)
            },
            onValueChangeFinished = { pagerScrollEnabled.value = true },
            valueRange = valueRange,
            steps = steps
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ControlRangeSliderPreview() {
    ETCUTheme {
        ControlRangeSlider(
            label = "Range",
            currentMin = 200,
            currentMax = 800,
            onRangeChange = { _, _ -> }
        )
    }
}
