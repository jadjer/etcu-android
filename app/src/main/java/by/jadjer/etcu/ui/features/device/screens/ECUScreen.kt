package by.jadjer.etcu.ui.features.device.screens

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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.telemetry.ECUTelemetry
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.telemetry.TelemetryGraphDialog
import by.jadjer.etcu.ui.component.telemetry.TelemetryRow
import by.jadjer.etcu.ui.component.telemetry.SelectedTelemetry
import by.jadjer.etcu.ui.features.device.DeviceViewModel

@Composable
fun EcuScreen(viewModel: DeviceViewModel) {
    val telemetry by viewModel.telemetry.collectAsState()
    val history by viewModel.telemetryHistory.collectAsState()

    var selectedTelemetry by remember { mutableStateOf<SelectedTelemetry?>(null) }

    EcuScreenContent(
        telemetry = telemetry.ecu,
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
fun EcuScreenContent(
    telemetry: ECUTelemetry,
    onValueClick: (String, String, (SystemTelemetry) -> Int) -> Unit = { _, _, _ -> }
) {
    val rpmLabel = stringResource(R.string.ecu_rpm)
    val rpmUnit = stringResource(R.string.unit_rpm)
    val speedLabel = stringResource(R.string.ecu_speed)
    val speedUnit = stringResource(R.string.unit_kmh)
    val tpsLabel = stringResource(R.string.ecu_tps)
    val tpsUnit = stringResource(R.string.unit_raw_1000)
    val batteryLabel = stringResource(R.string.ecu_battery)
    val batteryUnit = stringResource(R.string.unit_v)
    val mapLabel = stringResource(R.string.ecu_map)
    val mapUnit = stringResource(R.string.unit_kpa)
    val airTempLabel = stringResource(R.string.ecu_air_temp)
    val coolantTempLabel = stringResource(R.string.ecu_coolant_temp)
    val celsiusUnit = stringResource(R.string.unit_celsius)

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
            label = rpmLabel,
            value = telemetry.rpm.toString(),
            unit = rpmUnit,
            icon = Icons.Default.Timer,
            onClick = {
                onValueClick(
                    rpmLabel,
                    rpmUnit,
                    { it.ecu.rpm }
                )
            }
        )
        
        TelemetryRow(
            label = speedLabel,
            value = telemetry.speed.toString(),
            unit = speedUnit,
            icon = Icons.Default.Speed,
            onClick = {
                onValueClick(
                    speedLabel,
                    speedUnit,
                    { it.ecu.speed }
                )
            }
        )
        
        TelemetryRow(
            label = tpsLabel,
            value = telemetry.tps.toString(),
            unit = tpsUnit,
            icon = Icons.Default.TwoWheeler,
            onClick = {
                onValueClick(
                    tpsLabel,
                    tpsUnit,
                    { it.ecu.tps }
                )
            }
        )

        TelemetryRow(
            label = batteryLabel,
            value = telemetry.battery.toString(),
            unit = batteryUnit,
            icon = Icons.Default.FlashOn,
            onClick = {
                onValueClick(
                    batteryLabel,
                    batteryUnit,
                    { it.ecu.battery }
                )
            }
        )

        TelemetryRow(
            label = mapLabel,
            value = telemetry.map.toString(),
            unit = mapUnit,
            icon = Icons.Default.Cloud,
            onClick = {
                onValueClick(
                    mapLabel,
                    mapUnit,
                    { it.ecu.map }
                )
            }
        )

        TelemetryRow(
            label = airTempLabel,
            value = telemetry.airTemp.toString(),
            unit = celsiusUnit,
            icon = Icons.Default.Thermostat,
            onClick = {
                onValueClick(
                    airTempLabel,
                    celsiusUnit,
                    { it.ecu.airTemp }
                )
            }
        )

        TelemetryRow(
            label = coolantTempLabel,
            value = telemetry.coolantTemp.toString(),
            unit = celsiusUnit,
            icon = Icons.Default.Thermostat,
            onClick = {
                onValueClick(
                    coolantTempLabel,
                    celsiusUnit,
                    { it.ecu.coolantTemp }
                )
            }
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
