package by.jadjer.etcu.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import by.jadjer.etcu.ui.features.main.LocalPagerScrollEnabled
import by.jadjer.etcu.ui.theme.ETCUTheme

@Composable
fun ControlSlider(
    label: String,
    value: Int,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    steps: Int = 0
) {
    val pagerScrollEnabled = LocalPagerScrollEnabled.current
    
    var sliderValue by remember { mutableFloatStateOf(value.toFloat()) }

    LaunchedEffect(value) {
        sliderValue = value.toFloat()
    }

    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = sliderValue,
            onValueChange = {
                pagerScrollEnabled.value = false
                sliderValue = it
                onValueChange(it)
            },
            onValueChangeFinished = {
                pagerScrollEnabled.value = true
                sliderValue = value.toFloat()
            },
            valueRange = valueRange,
            steps = steps
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ControlSliderPreview() {
    ETCUTheme {
        ControlSlider(
            label = "Value",
            value = 50,
            onValueChange = {}
        )
    }
}
