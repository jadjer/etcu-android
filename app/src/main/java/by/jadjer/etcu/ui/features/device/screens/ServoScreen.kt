package by.jadjer.etcu.ui.features.device.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.telemetry.ServoTelemetry
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.StatusRow
import by.jadjer.etcu.ui.component.history.HistoryGroup
import by.jadjer.etcu.ui.component.history.StatusGraphDialog
import by.jadjer.etcu.ui.features.device.DeviceViewModel

@Composable
fun ServoScreen(viewModel: DeviceViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    var showDialog by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    ServoScreenContent(
        telemetry = telemetry.servo,
        onValueClick = { label, unit, selector ->
            showDialog = {
                val group = HistoryGroup(
                    history = uiState.telemetryHistory.servo,
                    selector = selector,
                    currentValue = selector(telemetry.servo)
                )
                StatusGraphDialog(label, unit, group) { showDialog = null }
            }
        }
    )

    showDialog?.invoke()
}

@Composable
fun ServoScreenContent(
    telemetry: ServoTelemetry,
    onValueClick: (String, String, (ServoTelemetry) -> Float) -> Unit
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
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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

        HorizontalDivider()

        StatusRow(
            label = posLabel,
            value = telemetry.position.toString(),
            unit = posUnit,
            icon = Icons.Default.LocationSearching,
            onClick = { onValueClick(posLabel, posUnit) { it.position.toFloat() } }
        )

        StatusRow(
            label = curLabel,
            value = telemetry.current.toString(),
            unit = curUnit,
            icon = Icons.Default.Bolt,
            onClick = { onValueClick(curLabel, curUnit) { it.current.toFloat() } }
        )

        StatusRow(
            label = voltLabel,
            value = "%.1f".format(telemetry.voltage),
            unit = voltUnit,
            icon = Icons.Default.FlashOn,
            onClick = { onValueClick(voltLabel, voltUnit) { it.voltage } }
        )

        StatusRow(
            label = tempLabel,
            value = telemetry.temperature.toString(),
            unit = tempUnit,
            icon = Icons.Default.DeviceThermostat,
            onClick = { onValueClick(tempLabel, tempUnit) { it.temperature.toFloat() } }
        )

        HorizontalDivider()

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
                voltage = 12.2f,
                temperature = 38,
                isMoved = true
            ),
            onValueClick = { _, _, _ -> }
        )
    }
}
