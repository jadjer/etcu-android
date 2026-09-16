package by.jadjer.etcu.ui.features.device.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import by.jadjer.etcu.ui.component.ValueField
import by.jadjer.etcu.ui.features.device.DeviceViewModel
import by.jadjer.etcu.ui.util.labelResId

@Composable
fun SettingsScreen(
    viewModel: DeviceViewModel,
    onOtaClick: () -> Unit,
    onCalibrateClick: () -> Unit
) {
    val controlData by viewModel.controlData.collectAsStateWithLifecycle()
    val systemInfo by viewModel.systemInfo.collectAsStateWithLifecycle()
    val operatingMode by viewModel.operatingMode.collectAsStateWithLifecycle()

    val onModeChange =
        remember(viewModel) { { mode: OperatingMode -> viewModel.updateOperatingMode(mode) } }
    val onAccRangeChange = remember(viewModel) {
        { min: Float, max: Float ->
            viewModel.updateAccRange(min = min.toInt(), max = max.toInt())
        }
    }
    val onServoRangeChange = remember(viewModel) {
        { min: Float, max: Float ->
            viewModel.updateServoRange(min = min.toInt(), max = max.toInt())
        }
    }
    val onCruisePidChange = remember(viewModel) {
        { p: Float, i: Float, d: Float -> viewModel.updateCruisePID(p, i, d) }
    }
    val onCruiseRpmChange = remember(viewModel) {
        { min: Float, max: Float -> viewModel.updateCruiseRPMRange(min.toInt(), max.toInt()) }
    }
    val onCruiseSpeedChange = remember(viewModel) {
        { min: Float, max: Float -> viewModel.updateCruiseSpeedRange(min.toInt(), max.toInt()) }
    }
    val onCruiseLimiterUpChange = remember(viewModel) {
        { value: Float -> viewModel.updateCruiseLimiterUp(value.toInt()) }
    }
    val onCruiseLimiterDownChange = remember(viewModel) {
        { value: Float -> viewModel.updateCruiseLimiterDown(value.toInt()) }
    }
    val onForgetClick = remember(viewModel) { { viewModel.forgetDevice() } }

    SettingsScreenContent(
        controlData = controlData,
        systemInfo = systemInfo,
        operatingMode = operatingMode,
        onModeChange = onModeChange,
        onAccRangeChange = onAccRangeChange,
        onServoRangeChange = onServoRangeChange,
        onCruisePidChange = onCruisePidChange,
        onCruiseRpmChange = onCruiseRpmChange,
        onCruiseSpeedChange = onCruiseSpeedChange,
        onCruiseLimiterUpChange = onCruiseLimiterUpChange,
        onCruiseLimiterDownChange = onCruiseLimiterDownChange,
        onForgetClick = onForgetClick,
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
    onCruiseLimiterUpChange: (Float) -> Unit,
    onCruiseLimiterDownChange: (Float) -> Unit,
    onForgetClick: () -> Unit,
    onOtaClick: () -> Unit,
    onCalibrateClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "device_info") {
            Spacer(Modifier.height(16.dp))
            DeviceInfoSection(systemInfo = systemInfo)
        }

        item(key = "divider_1") { HorizontalDivider() }

        item(key = "header_control") {
            Text(
                stringResource(R.string.settings_control),
                style = MaterialTheme.typography.titleLarge
            )
        }

        item(key = "accel_settings") {
            AcceleratorSettingsSection(
                acceleratorMin = controlData.acceleratorMin,
                acceleratorMax = controlData.acceleratorMax,
                onRangeChange = onAccRangeChange
            )
        }

        item(key = "divider_2") { HorizontalDivider() }

        item(key = "servo_settings") {
            ServoSettingsSection(
                servoMin = controlData.servoMin,
                servoMax = controlData.servoMax,
                operatingMode = operatingMode,
                onModeChange = onModeChange,
                onRangeChange = onServoRangeChange
            )
        }

        item(key = "divider_3") { HorizontalDivider() }

        item(key = "cruise_settings") {
            CruiseSettingsSection(
                cruise = controlData.cruise,
                onPidChange = onCruisePidChange,
                onRpmChange = onCruiseRpmChange,
                onSpeedChange = onCruiseSpeedChange,
                onLimiterUpChange = onCruiseLimiterUpChange,
                onLimiterDownChange = onCruiseLimiterDownChange
            )
        }

        item(key = "divider_4") { HorizontalDivider() }

        item(key = "device_actions") {
            DeviceActionsSection(
                onCalibrateClick = onCalibrateClick,
                onForgetClick = onForgetClick,
                onOtaClick = onOtaClick
            )
            Spacer(Modifier.height(16.dp))
        }
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
            label = stringResource(R.string.settings_accel_range),
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
            label = stringResource(R.string.settings_servo_range),
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
    onPidChange: (Float, Float, Float) -> Unit,
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
            ValueField(
                value = cruise.pid.p,
                label = stringResource(R.string.cruise_p),
                onValueChange = { onPidChange(it, cruise.pid.i, cruise.pid.d) },
                modifier = Modifier.weight(1f)
            )
            ValueField(
                value = cruise.pid.i,
                label = stringResource(R.string.cruise_i),
                onValueChange = { onPidChange(cruise.pid.p, it, cruise.pid.d) },
                modifier = Modifier.weight(1f)
            )
            ValueField(
                value = cruise.pid.d,
                label = stringResource(R.string.cruise_d),
                onValueChange = { onPidChange(cruise.pid.p, cruise.pid.i, it) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ValueField(
                value = cruise.limiterDown.toFloat(),
                label = stringResource(R.string.cruise_limiter_brake),
                onValueChange = { onLimiterDownChange(it) },
                modifier = Modifier.weight(1f)
            )
            ValueField(
                value = cruise.limiterUp.toFloat(),
                label = stringResource(R.string.cruise_limiter_accel),
                onValueChange = { onLimiterUpChange(it) },
                modifier = Modifier.weight(1f)
            )
        }

        ControlRangeSlider(
            label = stringResource(R.string.cruise_rpm_limit),
            currentMin = cruise.rpmMin,
            currentMax = cruise.rpmMax,
            onRangeChange = onRpmChange,
            valueRange = TelemetryConstants.RPM_RANGE,
            steps = 9999
        )
        ControlRangeSlider(
            label = stringResource(R.string.cruise_speed_range),
            currentMin = cruise.speedMin,
            currentMax = cruise.speedMax,
            onRangeChange = onSpeedChange,
            valueRange = TelemetryConstants.SPEED_RANGE,
            steps = 249
        )
    }
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
                    pid = PID(p = 10.0f, i = 0.5f, d = 2.0f),
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
            onCruisePidChange = { _, _, _ -> },
            onCruiseRpmChange = { _, _ -> },
            onCruiseSpeedChange = { _, _ -> },
            onCruiseLimiterUpChange = { _ -> },
            onCruiseLimiterDownChange = { _ -> },
            onForgetClick = {},
            onOtaClick = {},
            onCalibrateClick = {})
    }
}
