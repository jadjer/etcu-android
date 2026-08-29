package by.jadjer.etcu.ui.features.device

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.ServoTelemetry
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.TelemetryRow

@Composable
fun ServoScreen(viewModel: DeviceViewModel) {
    val telemetry by viewModel.telemetry.collectAsState()
    ServoScreenContent(telemetry.servo)
}

@Composable
fun ServoScreenContent(telemetry: ServoTelemetry) {
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
        
        TelemetryRow(
            label = stringResource(R.string.servo_position),
            value = telemetry.position.toString(),
            unit = stringResource(R.string.unit_raw_1024)
        )
        
        TelemetryRow(
            label = stringResource(R.string.servo_current),
            value = telemetry.current.toString(),
            unit = stringResource(R.string.unit_ma),
            icon = Icons.Default.Bolt
        )
        
        TelemetryRow(
            label = stringResource(R.string.servo_voltage),
            value = telemetry.voltage.toString(),
            unit = stringResource(R.string.unit_v)
        )
        
        TelemetryRow(
            label = stringResource(R.string.servo_temp),
            value = telemetry.temperature.toString(),
            unit = stringResource(R.string.unit_celsius),
            icon = Icons.Default.DeviceThermostat
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
