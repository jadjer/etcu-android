package by.jadjer.etcu.ui.features.device.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.telemetry.ServoTelemetry
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.telemetry.TelemetryGraphDialog
import by.jadjer.etcu.ui.component.telemetry.TelemetryRow
import by.jadjer.etcu.ui.component.telemetry.SelectedTelemetry
import by.jadjer.etcu.ui.features.device.DeviceViewModel

@Composable
fun ServoScreen(viewModel: DeviceViewModel) {
    val telemetry by viewModel.telemetry.collectAsState()
    val history by viewModel.telemetryHistory.collectAsState()

    var selectedTelemetry by remember { mutableStateOf<SelectedTelemetry?>(null) }

    ServoScreenContent(
        telemetry = telemetry.servo,
        onValueClick = { label, unit, selector ->
            selectedTelemetry = SelectedTelemetry(
                label = label,
                unit = unit,
                selector = selector
            )
        }
    )

    selectedTelemetry?.let { selected ->
        TelemetryGraphDialog(
            title = selected.label,
            value = selected.selector(telemetry).toString(),
            unit = selected.unit,
            history = history.map(selected.selector),
            onDismiss = { selectedTelemetry = null }
        )
    }
}

@Composable
fun ServoScreenContent(
    telemetry: ServoTelemetry,
    onValueClick: (String, String, (SystemTelemetry) -> Int) -> Unit = { _, _, _ -> }
) {
    val posLabel = stringResource(R.string.servo_position)
    val posUnit = stringResource(R.string.unit_raw_4095)
    val curLabel = stringResource(R.string.servo_current)
    val curUnit = stringResource(R.string.unit_ma)
    val voltLabel = stringResource(R.string.servo_voltage)
    val voltUnit = stringResource(R.string.unit_v)
    val tempLabel = stringResource(R.string.servo_temp)
    val tempUnit = stringResource(R.string.unit_celsius)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        StatusIndicator(
            label = stringResource(R.string.servo_conn_status),
            isActive = telemetry.isConnected,
            activeText = stringResource(R.string.servo_connected),
            inactiveText = stringResource(R.string.servo_disconnected)
        )

        StatusIndicator(
            label = stringResource(R.string.servo_enable_status),
            isActive = telemetry.isEnabled,
            activeText = stringResource(R.string.servo_enabled),
            inactiveText = stringResource(R.string.servo_disabled)
        )

        TelemetryRow(
            label = posLabel,
            value = telemetry.position.toString(),
            unit = posUnit,
            icon = Icons.Default.LocationSearching,
            onClick = {
                onValueClick(
                    posLabel,
                    posUnit,
                    { it.servo.position }
                )
            }
        )

        TelemetryRow(
            label = curLabel,
            value = telemetry.current.toString(),
            unit = curUnit,
            icon = Icons.Default.Bolt,
            onClick = {
                onValueClick(
                    curLabel,
                    curUnit,
                    { it.servo.current }
                )
            }
        )
        
        TelemetryRow(
            label = voltLabel,
            value = telemetry.voltage.toString(),
            unit = voltUnit,
            icon = Icons.Default.FlashOn,
            onClick = {
                onValueClick(
                    voltLabel,
                    voltUnit,
                    { it.servo.voltage }
                )
            }
        )
        
        TelemetryRow(
            label = tempLabel,
            value = telemetry.temperature.toString(),
            unit = tempUnit,
            icon = Icons.Default.DeviceThermostat,
            onClick = {
                onValueClick(
                    tempLabel,
                    tempUnit,
                    { it.servo.temperature }
                )
            }
        )

        StatusIndicator(
            label = stringResource(R.string.servo_motion_status),
            isActive = telemetry.isMoved,
            activeText = stringResource(R.string.servo_moving),
            inactiveText = stringResource(R.string.servo_static)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ServoScreenPreview() {
    MaterialTheme {
        ServoScreenContent(
            telemetry = ServoTelemetry(
                isConnected = true,
                position = 512,
                current = 450,
                voltage = 12,
                temperature = 38,
                isMoved = true
            )
        )
    }
}
