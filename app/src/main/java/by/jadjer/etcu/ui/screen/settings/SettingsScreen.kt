package by.jadjer.etcu.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.ControlData
import by.jadjer.etcu.domain.model.SystemInfo
import by.jadjer.etcu.ui.component.TelemetryRow

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOtaClick: () -> Unit
) {
    val controlData by viewModel.controlData.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()

    SettingsScreenContent(
        controlData = controlData,
        systemInfo = systemInfo,
        onAccRangeChange = { min, max ->
            viewModel.updateAccRange(
                min = min.toInt(),
                max = max.toInt(),
            )
        },
        onServoRangeChange = {min, max ->
            viewModel.updateServoRange(
                min = min.toInt(),
                max = max.toInt(),
            )
        },
        onDisconnectClick = { viewModel.disconnect() },
        onForgetClick = { viewModel.forgetDevice() },
        onOtaClick = onOtaClick
    )
}

@Composable
fun SettingsScreenContent(
    controlData: ControlData,
    systemInfo: SystemInfo,
    onAccRangeChange: (Float, Float) -> Unit,
    onServoRangeChange: (Float, Float) -> Unit,
    onDisconnectClick: () -> Unit,
    onForgetClick: () -> Unit,
    onOtaClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.settings_device_info), style = MaterialTheme.typography.titleLarge)

        TelemetryRow(
            label = stringResource(R.string.settings_board_version),
            value = systemInfo.boardVersion
        )

        TelemetryRow(
            label = stringResource(R.string.settings_build_date),
            value = systemInfo.buildDate
        )

        TelemetryRow(
            label = stringResource(R.string.settings_firmware_version),
            value = systemInfo.firmwareVersion
        )

        HorizontalDivider()

        Text(stringResource(R.string.settings_control), style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.settings_accel_range, controlData.accMin, controlData.accMax))

                RangeSlider(
                    value = controlData.accMin.toFloat()..controlData.accMax.toFloat(),
                    onValueChange = { range ->
                        onAccRangeChange(range.start, range.endInclusive)
                    },
                    valueRange = 0f..1000f,
                    steps = 100
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.settings_servo_range, controlData.servoMin, controlData.servoMax))

                RangeSlider(
                    value = controlData.servoMin.toFloat()..controlData.servoMax.toFloat(),
                    onValueChange = { range ->
                        onServoRangeChange(range.start, range.endInclusive)
                    },
                    valueRange = 0f..1000f,
                    steps = 100
                )
            }
        }

        HorizontalDivider()

        Text(stringResource(R.string.settings_device_settings), style = MaterialTheme.typography.titleLarge)

        Button(
            onClick = onDisconnectClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(stringResource(R.string.btn_disconnect))
        }

        Button(
            onClick = onForgetClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(stringResource(R.string.btn_forget))
        }

        OutlinedButton(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_calibrate))
        }

        OutlinedButton(
            onClick = onOtaClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_check_updates))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreenContent(
            controlData = ControlData(
                accMin = 150,
                accMax = 900,
                servoMin = 0,
                servoMax = 1000,
            ),
            systemInfo = SystemInfo(
                boardVersion = "v2.1",
                buildDate = "2023-08-20",
                firmwareVersion = "1.2.3"
            ),
            onAccRangeChange = { _, _ -> },
            onServoRangeChange = { _, _ -> },
            onDisconnectClick = {},
            onForgetClick = {},
            onOtaClick = {}
        )
    }
}
