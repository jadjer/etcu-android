package by.jadjer.etcu.ui.features.calibration.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.calibration.CalibrationData
import by.jadjer.etcu.domain.model.calibration.CalibrationRange
import by.jadjer.etcu.ui.component.SettingsGroup
import by.jadjer.etcu.ui.features.calibration.CalibrationUiState
import by.jadjer.etcu.ui.features.calibration.CalibrationViewModel

@Composable
fun CalibrationScreen(
    viewModel: CalibrationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    CalibrationScreenContent(
        uiState = uiState,
        onResetClick = viewModel::resetDetection,
        onSaveClick = viewModel::saveCalibration,
        onApplyDetected = viewModel::applyDetected,
        onHallAChange = viewModel::updateHallA,
        onHallBChange = viewModel::updateHallB,
        onServoChange = viewModel::updateServo
    )
}

@Composable
fun CalibrationScreenContent(
    uiState: CalibrationUiState,
    onResetClick: () -> Unit,
    onSaveClick: () -> Unit,
    onApplyDetected: () -> Unit,
    onHallAChange: (Int, Int) -> Unit,
    onHallBChange: (Int, Int) -> Unit,
    onServoChange: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.cal_title),
                style = MaterialTheme.typography.headlineMedium
            )

            TextButton(onClick = onApplyDetected) {
                Text(stringResource(R.string.cal_btn_apply_auto))
            }
        }

        CalibrationGroup(
            title = stringResource(R.string.cal_hall_a),
            currentValue = uiState.hallA,
            detectedRange = uiState.detectedHallA,
            editingRange = uiState.editingCalibration.hallA,
            onRangeChange = onHallAChange
        )

        CalibrationGroup(
            title = stringResource(R.string.cal_hall_b),
            currentValue = uiState.hallB,
            detectedRange = uiState.detectedHallB,
            editingRange = uiState.editingCalibration.hallB,
            onRangeChange = onHallBChange
        )

        CalibrationGroup(
            title = stringResource(R.string.cal_servo),
            currentValue = uiState.servoPosition,
            detectedRange = null,
            editingRange = uiState.editingCalibration.servo,
            onRangeChange = onServoChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onResetClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cal_btn_reset))
            }

            Button(
                onClick = onSaveClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cal_btn_save))
            }
        }
    }
}

@Composable
private fun CalibrationGroup(
    title: String,
    currentValue: Int,
    detectedRange: CalibrationRange?,
    editingRange: CalibrationRange,
    onRangeChange: (Int, Int) -> Unit
) {
    SettingsGroup(title = title) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.servo_position) + ": $currentValue",
                style = MaterialTheme.typography.bodyLarge
            )
            
            detectedRange?.let {
                Text(
                    text = stringResource(R.string.cal_detected) + ": " + 
                        stringResource(R.string.cal_hall_format, it.min, it.max),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editingRange.min.toString(),
                    onValueChange = { newValue ->
                        newValue.toIntOrNull()?.let { onRangeChange(it, editingRange.max) }
                    },
                    label = { Text(stringResource(R.string.cal_min)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = editingRange.max.toString(),
                    onValueChange = { newValue ->
                        newValue.toIntOrNull()?.let { onRangeChange(editingRange.min, it) }
                    },
                    label = { Text(stringResource(R.string.cal_max)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalibrationScreenPreview() {
    MaterialTheme {
        CalibrationScreenContent(
            uiState = CalibrationUiState(
                currentCalibration = CalibrationData(
                    hallA = CalibrationRange(150, 850),
                    hallB = CalibrationRange(160, 840),
                    servo = CalibrationRange(0, 1000)
                ),
                editingCalibration = CalibrationData(
                    hallA = CalibrationRange(150, 850),
                    hallB = CalibrationRange(160, 840),
                    servo = CalibrationRange(0, 1000)
                ),
                detectedHallA = CalibrationRange(145, 855),
                detectedHallB = CalibrationRange(155, 845),
                hallA = 500,
                hallB = 510,
                servoPosition = 505
            ),
            onResetClick = {},
            onSaveClick = {},
            onApplyDetected = {},
            onHallAChange = { _, _ -> },
            onHallBChange = { _, _ -> },
            onServoChange = { _, _ -> }
        )
    }
}
