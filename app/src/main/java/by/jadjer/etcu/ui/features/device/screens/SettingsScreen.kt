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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import by.jadjer.etcu.domain.model.control.PID
import by.jadjer.etcu.domain.model.system.SystemInfo
import by.jadjer.etcu.domain.model.telemetry.TelemetryConstants
import by.jadjer.etcu.ui.component.ControlRangeSlider
import by.jadjer.etcu.ui.component.StatusRow
import by.jadjer.etcu.ui.features.device.DeviceViewModel
import by.jadjer.etcu.ui.util.labelResId

@Composable
fun SettingsScreen(
    viewModel: DeviceViewModel, onOtaClick: () -> Unit, onCalibrateClick: () -> Unit
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
        onCruiseAccPidChange = { p, i, d -> viewModel.updateCruiseAccPID(p, i, d) },
        onCruiseDecPidChange = { p, i, d -> viewModel.updateCruiseDecPID(p, i, d) },

        onCruiseRpmChange = { min, max ->
            viewModel.updateCruiseRPMRange(
                min.toInt(), max.toInt()
            )
        },
        onCruiseSpeedChange = { min, max ->
            viewModel.updateCruiseSpeedRange(
                min.toInt(), max.toInt()
            )
        },
        onCruiseLimiterUpChange = { value ->
            viewModel.updateCruiseLimiterUp(value.toInt())
        },
        onCruiseLimiterDownChange = { value ->
            viewModel.updateCruiseLimiterDown(value.toInt())
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
    onCruiseAccPidChange: (Float, Float, Float) -> Unit,
    onCruiseDecPidChange: (Float, Float, Float) -> Unit,
    onCruiseRpmChange: (Float, Float) -> Unit,
    onCruiseSpeedChange: (Float, Float) -> Unit,
    onCruiseLimiterUpChange: (Float) -> Unit,
    onCruiseLimiterDownChange: (Float) -> Unit,
    onForgetClick: () -> Unit,
    onOtaClick: () -> Unit,
    onCalibrateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DeviceInfoSection(systemInfo = systemInfo)

        HorizontalDivider()

        Text(stringResource(R.string.settings_control), style = MaterialTheme.typography.titleLarge)

        AcceleratorSettingsSection(
            acceleratorMin = controlData.acceleratorMin,
            acceleratorMax = controlData.acceleratorMax,
            onRangeChange = onAccRangeChange
        )

        HorizontalDivider()

        ServoSettingsSection(
            servoMin = controlData.servoMin,
            servoMax = controlData.servoMax,
            operatingMode = operatingMode,
            onModeChange = onModeChange,
            onRangeChange = onServoRangeChange
        )

        HorizontalDivider()

        CruiseSettingsSection(
            cruise = controlData.cruise,
            onAccPidChange = onCruiseAccPidChange,
            onDecPidChange = onCruiseDecPidChange,
            onRpmChange = onCruiseRpmChange,
            onSpeedChange = onCruiseSpeedChange,
            onLimiterUpChange = onCruiseLimiterUpChange,
            onLimiterDownChange = onCruiseLimiterDownChange
        )

        HorizontalDivider()

        DeviceActionsSection(
            onCalibrateClick = onCalibrateClick,
            onForgetClick = onForgetClick,
            onOtaClick = onOtaClick
        )
    }
}

@Composable
private fun DeviceInfoSection(systemInfo: SystemInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.settings_device_info),
            style = MaterialTheme.typography.titleLarge
        )
        StatusRow(
            label = stringResource(R.string.settings_board_version),
            value = systemInfo.boardVersion.ifEmpty { stringResource(R.string.unknown) })
        StatusRow(
            label = stringResource(R.string.settings_build_date),
            value = systemInfo.buildDate.ifEmpty { stringResource(R.string.unknown) })
        StatusRow(
            label = stringResource(R.string.settings_firmware_version),
            value = systemInfo.firmwareVersion.ifEmpty { "0.0.0" })
    }
}

@Composable
private fun AcceleratorSettingsSection(
    acceleratorMin: Int, acceleratorMax: Int, onRangeChange: (Float, Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.settings_accel_settings),
            style = MaterialTheme.typography.titleMedium
        )
        ControlRangeSlider(
            label = stringResource(
                R.string.settings_accel_range, acceleratorMin, acceleratorMax
            ),
            currentMin = acceleratorMin,
            currentMax = acceleratorMax,
            onRangeChange = onRangeChange,
            valueRange = ControlConstants.MIN_VALUE..ControlConstants.MAX_VALUE,
            steps = ControlConstants.STEPS
        )
    }
}

@Composable
private fun ServoSettingsSection(
    servoMin: Int,
    servoMax: Int,
    operatingMode: OperatingMode,
    onModeChange: (OperatingMode) -> Unit,
    onRangeChange: (Float, Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.settings_servo_settings),
            style = MaterialTheme.typography.titleMedium
        )
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
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OperatingMode.entries.filter { it != OperatingMode.CUSTOM }.forEach { mode ->
                FilterChip(
                    selected = operatingMode == mode,
                    onClick = { onModeChange(mode) },
                    label = { Text(stringResource(mode.labelResId)) })
            }
        }

        ControlRangeSlider(
            label = stringResource(
                R.string.settings_servo_range, servoMin, servoMax
            ),
            currentMin = servoMin,
            currentMax = servoMax,
            onRangeChange = onRangeChange,
            valueRange = ControlConstants.MIN_VALUE..ControlConstants.MAX_VALUE,
            steps = ControlConstants.STEPS
        )
    }
}

@Composable
private fun CruiseSettingsSection(
    cruise: Cruise,
    onAccPidChange: (Float, Float, Float) -> Unit,
    onDecPidChange: (Float, Float, Float) -> Unit,
    onRpmChange: (Float, Float) -> Unit,
    onSpeedChange: (Float, Float) -> Unit,
    onLimiterUpChange: (Float) -> Unit,
    onLimiterDownChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.cruise_settings_title),
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CruiseTextField(
                value = cruise.acc.p,
                label = stringResource(R.string.cruise_acc_p),
                onValueChange = { onAccPidChange(it, cruise.acc.i, cruise.acc.d) },
                modifier = Modifier.weight(1f)
            )
            CruiseTextField(
                value = cruise.acc.i,
                label = stringResource(R.string.cruise_acc_i),
                onValueChange = { onAccPidChange(cruise.acc.p, it, cruise.acc.d) },
                modifier = Modifier.weight(1f)
            )
            CruiseTextField(
                value = cruise.acc.d,
                label = stringResource(R.string.cruise_acc_d),
                onValueChange = { onAccPidChange(cruise.acc.p, cruise.acc.i, it) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CruiseTextField(
                value = cruise.dec.p,
                label = stringResource(R.string.cruise_dec_p),
                onValueChange = { onDecPidChange(it, cruise.dec.i, cruise.dec.d) },
                modifier = Modifier.weight(1f)
            )
            CruiseTextField(
                value = cruise.dec.i,
                label = stringResource(R.string.cruise_dec_i),
                onValueChange = { onDecPidChange(cruise.dec.p, it, cruise.dec.d) },
                modifier = Modifier.weight(1f)
            )
            CruiseTextField(
                value = cruise.dec.d,
                label = stringResource(R.string.cruise_dec_d),
                onValueChange = { onDecPidChange(cruise.dec.p, cruise.dec.i, it) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CruiseTextField(
                value = cruise.limiterDown.toFloat(),
                label = stringResource(R.string.cruise_limiter_brake),
                onValueChange = { onLimiterDownChange(it) },
                modifier = Modifier.weight(1f)
            )
            CruiseTextField(
                value = cruise.limiterUp.toFloat(),
                label = stringResource(R.string.cruise_limiter_accel),
                onValueChange = { onLimiterUpChange(it) },
                modifier = Modifier.weight(1f)
            )
        }

        ControlRangeSlider(
            label = stringResource(R.string.cruise_rpm_limit, cruise.rpmMin, cruise.rpmMax),
            currentMin = cruise.rpmMin,
            currentMax = cruise.rpmMax,
            onRangeChange = onRpmChange,
            valueRange = TelemetryConstants.RPM_RANGE,
            steps = 9999
        )
        ControlRangeSlider(
            label = stringResource(R.string.cruise_speed_range, cruise.speedMin, cruise.speedMax),
            currentMin = cruise.speedMin,
            currentMax = cruise.speedMax,
            onRangeChange = onSpeedChange,
            valueRange = TelemetryConstants.SPEED_RANGE,
            steps = 249
        )
    }
}

@Composable
private fun CruiseTextField(
    value: Float, label: String, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            textValue = newValue
            newValue.toFloatOrNull()?.let { onValueChange(it) }
        },
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
    )
}

@Composable
private fun DeviceActionsSection(
    onCalibrateClick: () -> Unit, onForgetClick: () -> Unit, onOtaClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.settings_device_settings),
            style = MaterialTheme.typography.titleLarge
        )
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
            onClick = onOtaClick, modifier = Modifier.fillMaxWidth()
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
                cruise = Cruise(
                    acc = PID(p = 10.0f, i = 0.5f, d = 2.0f),
                    dec = PID(p = 10.0f, i = 0.5f, d = 2.0f),
                    rpmMin = 2500,
                    rpmMax = 7000,
                    speedMin = 40,
                    speedMax = 160,
                    limiterUp = -250,
                    limiterDown = 250,
                ),
                servoMin = 0,
                servoMax = 600,
                acceleratorMin = 150,
                acceleratorMax = 850,
            ),
            systemInfo = SystemInfo(
                boardVersion = "v2.1", buildDate = "2023-08-20", firmwareVersion = "1.2.3"
            ),
            operatingMode = OperatingMode.NORMAL,
            onModeChange = {},
            onAccRangeChange = { _, _ -> },
            onServoRangeChange = { _, _ -> },
            onCruiseAccPidChange = { _, _, _ -> },
            onCruiseDecPidChange = { _, _, _ -> },
            onCruiseRpmChange = { _, _ -> },
            onCruiseSpeedChange = { _, _ -> },
            onCruiseLimiterUpChange = { _ -> },
            onCruiseLimiterDownChange = { _ -> },
            onForgetClick = {},
            onOtaClick = {},
            onCalibrateClick = {})
    }
}
