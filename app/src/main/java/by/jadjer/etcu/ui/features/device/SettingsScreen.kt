package by.jadjer.etcu.ui.features.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import by.jadjer.etcu.domain.model.OperatingMode
import by.jadjer.etcu.domain.model.SystemInfo
import by.jadjer.etcu.ui.component.ControlRangeSlider
import by.jadjer.etcu.ui.component.TelemetryRow

@Composable
fun SettingsScreen(
    viewModel: DeviceViewModel,
    onOtaClick: () -> Unit
) {
    val controlData by viewModel.controlData.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()
    val operatingMode by viewModel.operatingMode.collectAsState()

    SettingsScreenContent(
        controlData = controlData,
        systemInfo = systemInfo,
        operatingMode = operatingMode,
        onModeChange = { viewModel.updateOperatingMode(it) },
        onAccRangeChange = { min, max ->
            viewModel.updateAccRange(
                min = min.toInt(),
                max = max.toInt(),
            )
        },
        onServoRangeChange = { min, max ->
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
    operatingMode: OperatingMode,
    onModeChange: (OperatingMode) -> Unit,
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
        Text(
            stringResource(R.string.settings_device_info),
            style = MaterialTheme.typography.titleLarge
        )

        TelemetryRow(
            label = stringResource(R.string.settings_board_version),
            value = systemInfo.boardVersion.ifEmpty { stringResource(R.string.unknown) }
        )

        TelemetryRow(
            label = stringResource(R.string.settings_build_date),
            value = systemInfo.buildDate.ifEmpty { stringResource(R.string.unknown) }
        )

        TelemetryRow(
            label = stringResource(R.string.settings_firmware_version),
            value = systemInfo.firmwareVersion.ifEmpty { "0.0.0" }
        )

        HorizontalDivider()

        Text(stringResource(R.string.settings_control), style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.settings_operating_mode), style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OperatingMode.entries.filter { it != OperatingMode.CUSTOM }.forEach { mode ->
                        FilterChip(
                            selected = operatingMode == mode,
                            onClick = { onModeChange(mode) },
                            label = { Text(stringResource(mode.resId)) }
                        )
                    }
                }
                if (operatingMode == OperatingMode.CUSTOM) {
                    Text(
                        stringResource(OperatingMode.CUSTOM.resId),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                ControlRangeSlider(
                    label = stringResource(
                        R.string.settings_accel_range,
                        controlData.acceleratorMin,
                        controlData.acceleratorMax
                    ),
                    currentMin = controlData.acceleratorMin,
                    currentMax = controlData.acceleratorMax,
                    onRangeChange = onAccRangeChange,
                    valueRange = 0f..1000f,
                    steps = 999
                )

                ControlRangeSlider(
                    label = stringResource(
                        R.string.settings_servo_range,
                        controlData.servoMin,
                        controlData.servoMax
                    ),
                    currentMin = controlData.servoMin,
                    currentMax = controlData.servoMax,
                    onRangeChange = onServoRangeChange,
                    valueRange = 0f..1000f,
                    steps = 999
                )
            }
        }

        HorizontalDivider()

        Text(
            stringResource(R.string.settings_device_settings),
            style = MaterialTheme.typography.titleLarge
        )

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
                servoMin = 0,
                servoMax = 600,
                acceleratorMin = 150,
                acceleratorMax = 850,
            ),
            systemInfo = SystemInfo(
                boardVersion = "v2.1",
                buildDate = "2023-08-20",
                firmwareVersion = "1.2.3"
            ),
            operatingMode = OperatingMode.NORMAL,
            onModeChange = {},
            onAccRangeChange = { _, _ -> },
            onServoRangeChange = { _, _ -> },
            onDisconnectClick = {},
            onForgetClick = {},
            onOtaClick = {}
        )
    }
}
