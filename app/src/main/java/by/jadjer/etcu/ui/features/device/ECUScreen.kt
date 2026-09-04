package by.jadjer.etcu.ui.features.device

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.ECUTelemetry
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.TelemetryRow

@Composable
fun EcuScreen(viewModel: DeviceViewModel) {
    val telemetry by viewModel.telemetry.collectAsState()
    EcuScreenContent(telemetry.ecu)
}

@Composable
fun EcuScreenContent(telemetry: ECUTelemetry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        StatusIndicator(
            label = stringResource(R.string.ecu_conn_status),
            isActive = telemetry.isConnected,
            activeText = stringResource(R.string.ecu_connected),
            inactiveText = stringResource(R.string.ecu_disconnected)
        )

        StatusIndicator(
            label = stringResource(R.string.ecu_engine_status),
            isActive = telemetry.isStarted,
            activeText = stringResource(R.string.ecu_engine_started),
            inactiveText = stringResource(R.string.ecu_engine_stopped)
        )

        TelemetryRow(
            label = stringResource(R.string.ecu_rpm),
            value = telemetry.rpm.toString(),
            unit = stringResource(R.string.unit_rpm),
            icon = Icons.Default.Timer
        )
        
        TelemetryRow(
            label = stringResource(R.string.ecu_speed),
            value = telemetry.speed.toString(),
            unit = stringResource(R.string.unit_kmh),
            icon = Icons.Default.Speed
        )
        
        TelemetryRow(
            label = stringResource(R.string.ecu_tps),
            value = telemetry.tps.toString(),
            unit = stringResource(R.string.unit_raw_1000),
            icon = Icons.Default.TwoWheeler
        )

        TelemetryRow(
            label = stringResource(R.string.ecu_battery),
            value = telemetry.battery.toString(),
            unit = stringResource(R.string.unit_v),
            icon = Icons.Default.FlashOn
        )

        TelemetryRow(
            label = stringResource(R.string.ecu_map),
            value = telemetry.map.toString(),
            unit = stringResource(R.string.unit_kpa),
            icon = Icons.Default.Cloud
        )

        TelemetryRow(
            label = stringResource(R.string.ecu_air_temp),
            value = telemetry.airTemp.toString(),
            unit = stringResource(R.string.unit_celsius),
            icon = Icons.Default.Thermostat
        )

        TelemetryRow(
            label = stringResource(R.string.ecu_coolant_temp),
            value = telemetry.coolantTemp.toString(),
            unit = stringResource(R.string.unit_celsius),
            icon = Icons.Default.Thermostat
        )
        

        
        StatusIndicator(
            label = stringResource(R.string.ecu_clutch_status),
            isActive = telemetry.isClutchEnabled,
            activeText = stringResource(R.string.ecu_clutch_pressed),
            inactiveText = stringResource(R.string.ecu_clutch_released)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EcuScreenPreview() {
    MaterialTheme {
        EcuScreenContent(
            telemetry = ECUTelemetry(
                isConnected = true,
                isStarted = true,
                isClutchEnabled = false,
                rpm = 2500,
                battery = 13,
                speed = 60,
                map = 56,
                tps = 150,
                airTemp = 30,
                coolantTemp = 78,
            )
        )
    }
}
