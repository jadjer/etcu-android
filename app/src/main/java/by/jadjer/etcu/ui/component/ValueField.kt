package by.jadjer.etcu.ui.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import by.jadjer.etcu.ui.theme.ETCUTheme

@Composable
fun ValueField(
    value: Float,
    label: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            // Filter input to only allow numbers, dots, and minus sign
            val filtered = newValue.filter { it.isDigit() || it == '.' || it == '-' }
            textValue = filtered

            // Try to parse, but don't notify if it's an intermediate state
            if (filtered.isNotEmpty() && filtered != "-" && filtered != ".") {
                filtered.toFloatOrNull()?.let { onValueChange(it) }
            }
        },
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            autoCorrectEnabled = false
        ),
        singleLine = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun ControlSliderPreview() {
    ETCUTheme {
        ValueField(
            value = 12.4f,
            label = "Value",
            onValueChange = {}
        )
    }
}
