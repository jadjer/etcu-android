package by.jadjer.etcu.ui.features.device.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.control.AutoSet
import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.control.OperatingMode
import by.jadjer.etcu.domain.model.control.Range
import by.jadjer.etcu.domain.model.system.SystemInfo
import by.jadjer.etcu.ui.component.ControlRangeSlider
import by.jadjer.etcu.ui.component.telemetry.TelemetryRow
import by.jadjer.etcu.ui.features.device.DeviceViewModel

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
        onAutoSetChange = { enabled, delay, threshold, tolerance ->
            viewModel.updateAutoSet(enabled, delay, threshold, tolerance)
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
    onAutoSetChange: (Boolean?, Int?, Int?, Int?) -> Unit,
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
                Text(stringResource(R.string.settings_accel_settings), style = MaterialTheme.typography.titleMedium)
                ControlRangeSlider(
                    label = stringResource(
                        R.string.settings_accel_range,
                        controlData.accelerator.min,
                        controlData.accelerator.max
                    ),
                    currentMin = controlData.accelerator.min,
                    currentMax = controlData.accelerator.max,
                    onRangeChange = onAccRangeChange,
                    valueRange = 0f..1000f,
                    steps = 999
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.settings_autoset_settings), style = MaterialTheme.typography.titleMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.settings_autoset_enabled), style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = controlData.autoSet.enabled,
                        onCheckedChange = { onAutoSetChange(it, null, null, null) }
                    )
                }

                Column {
                    Text(
                        stringResource(R.string.settings_autoset_delay, controlData.autoSet.delay),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Slider(
                        value = controlData.autoSet.delay.toFloat(),
                        onValueChange = { onAutoSetChange(null, it.toInt(), null, null) },
                        valueRange = 0f..5000f,
                        steps = 49
                    )
                }

                Column {
                    Text(
                        stringResource(R.string.settings_autoset_threshold, controlData.autoSet.threshold),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Slider(
                        value = controlData.autoSet.threshold.toFloat(),
                        onValueChange = { onAutoSetChange(null, null, it.toInt(), null) },
                        valueRange = 0f..255f,
                        steps = 254
                    )
                }

                Column {
                    Text(
                        stringResource(R.string.settings_autoset_tolerance, controlData.autoSet.tolerance),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Slider(
                        value = controlData.autoSet.tolerance.toFloat(),
                        onValueChange = { onAutoSetChange(null, null, null, it.toInt()) },
                        valueRange = 0f..255f,
                        steps = 254
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.settings_servo_settings), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.settings_operating_mode), style = MaterialTheme.typography.labelLarge)
                    if (operatingMode == OperatingMode.CUSTOM) {
                        Text(
                            stringResource(OperatingMode.CUSTOM.resId),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                FlowRow(
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

                ControlRangeSlider(
                    label = stringResource(
                        R.string.settings_servo_range,
                        controlData.servo.min,
                        controlData.servo.max
                    ),
                    currentMin = controlData.servo.min,
                    currentMax = controlData.servo.max,
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
                autoSet = AutoSet(enabled = true, delay = 500, threshold = 10, tolerance = 2),
                servo = Range(min = 0, max = 600),
                accelerator = Range(min = 150, max = 850),
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
            onAutoSetChange = { _, _, _, _ -> },
            onDisconnectClick = {},
            onForgetClick = {},
            onOtaClick = {}
        )
    }
}
