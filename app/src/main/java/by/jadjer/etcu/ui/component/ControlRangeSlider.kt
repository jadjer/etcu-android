package by.jadjer.etcu.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
    steps: Int = 0
) {
    val pagerScrollEnabled = LocalPagerScrollEnabled.current
    
    var sliderValue by remember {
        mutableStateOf(currentMin.toFloat()..currentMax.toFloat())
    }

    // Принудительно синхронизируем состояние, когда данные приходят от устройства (ViewModel)
    LaunchedEffect(currentMin, currentMax) {
        sliderValue = currentMin.toFloat()..currentMax.toFloat()
    }

    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        RangeSlider(
            value = sliderValue,
            onValueChange = { range ->
                pagerScrollEnabled.value = false
                sliderValue = range
                onRangeChange(range.start, range.endInclusive)
            },
            onValueChangeFinished = {
                pagerScrollEnabled.value = true
                // После завершения жеста гарантируем, что ползунок стоит в точном значении из ViewModel
                sliderValue = currentMin.toFloat()..currentMax.toFloat()
            },
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
