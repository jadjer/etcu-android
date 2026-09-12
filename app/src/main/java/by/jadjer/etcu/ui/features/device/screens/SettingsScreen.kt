package by.jadjer.etcu.ui.features.device.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import by.jadjer.etcu.domain.model.control.ControlConstants
import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.control.Cruise
import by.jadjer.etcu.domain.model.control.OperatingMode
import by.jadjer.etcu.domain.model.system.SystemInfo
import by.jadjer.etcu.ui.component.ControlRangeSlider
import by.jadjer.etcu.ui.component.ControlSlider
import by.jadjer.etcu.ui.component.SettingsGroup
import by.jadjer.etcu.ui.component.telemetry.TelemetryRow
import by.jadjer.etcu.ui.features.device.DeviceViewModel
import by.jadjer.etcu.ui.util.labelResId

@Composable
fun SettingsScreen(
    viewModel: DeviceViewModel,
    onOtaClick: () -> Unit,
    onCalibrateClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreenContent(
        controlData = uiState.controlData,
        systemInfo = uiState.systemInfo,
        operatingMode = uiState.operatingMode,
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
        onCruisePidChange = { p, i, d -> viewModel.updateCruisePID(p, i, d) },

        onCruiseRpmChange = { min, max ->
            viewModel.updateCruiseRPMRange(
                min.toInt(),
                max.toInt()
            )
        },
        onCruiseSpeedChange = { min, max ->
            viewModel.updateCruiseSpeedRange(
                min.toInt(),
                max.toInt()
            )
        },
        onCruiseLimiterLeftChange = { value ->
            viewModel.updateCruiseLimiterLeft(value.toInt())
        },
        onCruiseLimiterRightChange = { value ->
            viewModel.updateCruiseLimiterRight(value.toInt())
        },
        onCruiseIntegralChange = { left, right ->
            viewModel.updateCruiseIntegralLimits(left, right)
        },
        onCruiseFilterAlphaChange = { filterAlpha ->
            viewModel.updateCruiseFilterAlpha(filterAlpha)
        },
        onCruiseFadeDurationChange = { fadeDuration ->
            viewModel.updateCruiseFadeDuration(fadeDuration)
        },
        onForgetClick = { viewModel.forgetDevice() },
        onOtaClick = onOtaClick,
        onCalibrateClick = onCalibrateClick
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

    onCruisePidChange: (Float, Float, Float) -> Unit,
    onCruiseRpmChange: (Float, Float) -> Unit,
    onCruiseSpeedChange: (Float, Float) -> Unit,
    onCruiseLimiterLeftChange: (Float) -> Unit,
    onCruiseLimiterRightChange: (Float) -> Unit,
    onCruiseIntegralChange: (Float, Float) -> Unit,
    onCruiseFilterAlphaChange: (Float) -> Unit,
    onCruiseFadeDurationChange: (Float) -> Unit,

    onForgetClick: () -> Unit,
    onOtaClick: () -> Unit,
    onCalibrateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column {
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
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(stringResource(R.string.settings_control), style = MaterialTheme.typography.titleLarge)

        SettingsGroup(title = stringResource(R.string.settings_accel_settings)) {
            ControlRangeSlider(
                label = stringResource(
                    R.string.settings_accel_range,
                    controlData.accelerator_min,
                    controlData.accelerator_max
                ),
                currentMin = controlData.accelerator_min,
                currentMax = controlData.accelerator_max,
                onRangeChange = onAccRangeChange,
                valueRange = ControlConstants.MIN_VALUE..ControlConstants.MAX_VALUE,
                steps = ControlConstants.STEPS
            )
        }

        SettingsGroup(title = stringResource(R.string.settings_servo_settings)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.settings_operating_mode),
                    style = MaterialTheme.typography.labelLarge
                )
                if (operatingMode == OperatingMode.CUSTOM) {
                    Text(
                        stringResource(operatingMode.labelResId),
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
                        label = { Text(stringResource(mode.labelResId)) }
                    )
                }
            }

            ControlRangeSlider(
                label = stringResource(
                    R.string.settings_servo_range,
                    controlData.servo_min,
                    controlData.servo_max
                ),
                currentMin = controlData.servo_min,
                currentMax = controlData.servo_max,
                onRangeChange = onServoRangeChange,
                valueRange = ControlConstants.MIN_VALUE..ControlConstants.MAX_VALUE,
                steps = ControlConstants.STEPS
            )
        }

        SettingsGroup(title = "Настройки круиз-контроля") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = controlData.cruise.p.toString(),
                    onValueChange = { newValue ->
                        newValue.toFloatOrNull()?.let {
                            onCruisePidChange(
                                it,
                                controlData.cruise.i,
                                controlData.cruise.d
                            )
                        }
                    },
                    label = { Text("P") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = controlData.cruise.i.toString(),
                    onValueChange = { newValue ->
                        newValue.toFloatOrNull()?.let {
                            onCruisePidChange(
                                controlData.cruise.p,
                                it,
                                controlData.cruise.d
                            )
                        }
                    },
                    label = { Text("I") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = controlData.cruise.d.toString(),
                    onValueChange = { newValue ->
                        newValue.toFloatOrNull()?.let {
                            onCruisePidChange(
                                controlData.cruise.p,
                                controlData.cruise.i,
                                it
                            )
                        }
                    },
                    label = { Text("D") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = controlData.cruise.integralMin.toString(),
                    onValueChange = { newValue ->
                        newValue.toFloatOrNull()
                            ?.let { onCruiseIntegralChange(it, controlData.cruise.integralMax) }
                    },
                    label = { Text("Integral Min") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = controlData.cruise.integralMax.toString(),
                    onValueChange = { newValue ->
                        newValue.toFloatOrNull()
                            ?.let { onCruiseIntegralChange(controlData.cruise.integralMin, it) }
                    },
                    label = { Text("Integral Max") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = controlData.cruise.filterAlpha.toString(),
                    onValueChange = { newValue ->
                        newValue.toFloatOrNull()?.let { onCruiseFilterAlphaChange(it) }
                    },
                    label = { Text("Speed filter alpha") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = controlData.cruise.fadeDuration.toString(),
                    onValueChange = { newValue ->
                        newValue.toFloatOrNull()?.let { onCruiseFadeDurationChange(it) }
                    },
                    label = { Text("Fade duration (sec)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            HorizontalDivider()

            ControlRangeSlider(
                label = "Ограничение RPM: ${controlData.cruise.rpm_min} - ${controlData.cruise.rpm_max}",
                currentMin = controlData.cruise.rpm_min,
                currentMax = controlData.cruise.rpm_max,
                onRangeChange = onCruiseRpmChange,
                valueRange = 1000f..10000f,
                steps = 899
            )
            ControlRangeSlider(
                label = "Рабочая скорость: ${controlData.cruise.speed_min} - ${controlData.cruise.speed_max} км/ч",
                currentMin = controlData.cruise.speed_min,
                currentMax = controlData.cruise.speed_max,
                onRangeChange = onCruiseSpeedChange,
                valueRange = 40f..160f,
                steps = 119
            )
            ControlSlider(
                label = "Лимит торможения:  ${controlData.cruise.limiter_left}",
                value = controlData.cruise.limiter_left,
                onValueChange = onCruiseLimiterLeftChange,
                valueRange = 0f..1000f,
                steps = 999
            )
            ControlSlider(
                label = "Лимит ускорения: ${controlData.cruise.limiter_right}",
                value = controlData.cruise.limiter_right,
                onValueChange = onCruiseLimiterRightChange,
                valueRange = 0f..1000f,
                steps = 999
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            stringResource(R.string.settings_device_settings),
            style = MaterialTheme.typography.titleLarge
        )

        Column {
            Button(
                onClick = onCalibrateClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.btn_calibrate))
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
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreenContent(
            controlData = ControlData(
                cruise = Cruise(
                    p = 10.0f,
                    i = 0.5f,
                    d = 2.0f,
                    rpm_min = 2500,
                    rpm_max = 7000,
                    speed_min = 40,
                    speed_max = 160,
                    limiter_left = -250,
                    limiter_right = 250,
                ),
                servo_min = 0,
                servo_max = 600,
                accelerator_min = 150,
                accelerator_max = 850,
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
            onCruisePidChange = { _, _, _ -> },
            onCruiseRpmChange = { _, _ -> },
            onCruiseSpeedChange = { _, _ -> },
            onCruiseFilterAlphaChange = { _ -> },
            onCruiseFadeDurationChange = { _ -> },
            onCruiseIntegralChange = { _, _ -> },
            onCruiseLimiterLeftChange = { _ -> },
            onCruiseLimiterRightChange = { _ -> },
            onForgetClick = {},
            onOtaClick = {},
            onCalibrateClick = {}
        )
    }
}
