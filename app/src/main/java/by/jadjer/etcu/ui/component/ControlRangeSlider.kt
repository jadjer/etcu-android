package by.jadjer.etcu.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.ui.features.main.LocalPagerScrollEnabled
import by.jadjer.etcu.ui.theme.ETCUTheme

@Composable
fun ControlRangeSlider(
    label: String,
    currentMin: Int,
    currentMax: Int,
    onRangeChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1000f,
    steps: Int = 0
) {
    val pagerScrollEnabled = LocalPagerScrollEnabled.current

    // Оптимизация: используем remember(currentMin, currentMax), чтобы не плодить LaunchedEffect
    var sliderValue by remember(currentMin, currentMax) {
        mutableStateOf(currentMin.toFloat()..currentMax.toFloat())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // Внешний отступ для всего компонента
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp) // Отступ под заголовком
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp), // Отступ между текстовыми полями и слайдером
            horizontalArrangement = Arrangement.spacedBy(12.dp) // Увеличен зазор между полями
        ) {
            ValueField(
                value = currentMin.toFloat(),
                label = stringResource(R.string.label_min),
                onValueChange = { onRangeChange(it, currentMax.toFloat()) },
                modifier = Modifier.weight(1f)
            )
            ValueField(
                value = currentMax.toFloat(),
                label = stringResource(R.string.label_max),
                onValueChange = { onRangeChange(currentMin.toFloat(), it) },
                modifier = Modifier.weight(1f)
            )
        }

        RangeSlider(
            value = sliderValue,
            onValueChange = { range ->
                pagerScrollEnabled.value = false
                sliderValue = range
                onRangeChange(range.start, range.endInclusive)
            },
            onValueChangeFinished = {
                pagerScrollEnabled.value = true
                sliderValue = currentMin.toFloat()..currentMax.toFloat()
            },
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.padding(horizontal = 4.dp) // Небольшой отступ по бокам для краев слайдера
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
