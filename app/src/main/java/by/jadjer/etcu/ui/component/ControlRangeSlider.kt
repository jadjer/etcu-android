package by.jadjer.etcu.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.ui.features.main.LocalPagerScrollEnabled
import by.jadjer.etcu.ui.theme.ETCUTheme

@OptIn(ExperimentalMaterial3Api::class)
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

    // Internal state for smooth sliding
    var sliderValue by remember {
        mutableStateOf(currentMin.toFloat()..currentMax.toFloat())
    }

    // Sync internal state with external updates when not interacting
    val startInteractionSource = remember { MutableInteractionSource() }
    val endInteractionSource = remember { MutableInteractionSource() }
    val isStartPressed by startInteractionSource.collectIsPressedAsState()
    val isEndPressed by endInteractionSource.collectIsPressedAsState()

    LaunchedEffect(currentMin, currentMax) {
        if (!isStartPressed && !isEndPressed) {
            sliderValue = currentMin.toFloat()..currentMax.toFloat()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
            },
            valueRange = valueRange,
            steps = steps,
            startInteractionSource = startInteractionSource,
            endInteractionSource = endInteractionSource,
            modifier = Modifier.padding(horizontal = 4.dp)
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
