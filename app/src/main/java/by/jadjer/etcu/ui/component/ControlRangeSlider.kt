package by.jadjer.etcu.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ControlRangeSlider(
    label: String,
    currentMin: Int,
    currentMax: Int,
    onRangeChange: (Float, Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1000f,
    steps: Int = 100
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        
        RangeSlider(
            value = currentMin.toFloat()..currentMax.toFloat(),
            onValueChange = { range ->
                onRangeChange(range.start, range.endInclusive)
            },
            valueRange = valueRange,
            steps = steps
        )
    }
}
