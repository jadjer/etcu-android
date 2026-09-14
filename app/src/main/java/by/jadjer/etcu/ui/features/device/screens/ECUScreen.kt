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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.telemetry.EcuTelemetry
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.StatusRow
import by.jadjer.etcu.ui.component.history.HistoryGroup
import by.jadjer.etcu.ui.component.history.StatusGraphDialog
import by.jadjer.etcu.ui.features.device.DeviceViewModel

@Composable
fun EcuScreen(viewModel: DeviceViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    var showDialog by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    EcuScreenContent(
        telemetry = telemetry.ecu,
        onValueClick = { label, unit, selector ->
            showDialog = {
                val group = HistoryGroup(
                    history = uiState.telemetryHistory.ecu,
                    selector = selector,
                    currentValue = selector(telemetry.ecu)
                )
                EcuStatusGraphDialog(label, unit, group) { showDialog = null }
            }
        }
    )

    showDialog?.invoke()
}

@Composable
private fun EcuStatusGraphDialog(
    label: String,
    unit: String,
    group: HistoryGroup<EcuTelemetry>,
    onDismiss: () -> Unit
) {
    val startTime = group.history.firstOrNull()?.timestamp ?: 0L
    StatusGraphDialog(
        title = label,
        value = "%.1f".format(group.currentValue),
        unit = unit,
        history = group.history,
        selector = { record -> group.selector(record.data) },
        startTime = startTime,
        onDismiss = onDismiss
    )
}

@Composable
fun EcuScreenContent(
    telemetry: EcuTelemetry,
    onValueClick: (String, String, (EcuTelemetry) -> Float) -> Unit = { _, _, _ -> }
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
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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

        HorizontalDivider()

        StatusRow(
            label = rpmLabel,
            value = telemetry.rpm.toString(),
            unit = rpmUnit,
            icon = Icons.Default.Timer,
            onClick = { onValueClick(rpmLabel, rpmUnit) { it.rpm.toFloat() } }
        )
        
        StatusRow(
            label = speedLabel,
            value = telemetry.speed.toString(),
            unit = speedUnit,
            icon = Icons.Default.Speed,
            onClick = { onValueClick(speedLabel, speedUnit) { it.speed.toFloat() } }
        )
        
        StatusRow(
            label = tpsLabel,
            value = telemetry.tps.toString(),
            unit = tpsUnit,
            icon = Icons.Default.TwoWheeler,
            onClick = { onValueClick(tpsLabel, tpsUnit) { it.tps.toFloat() } }
        )

        StatusRow(
            label = batteryLabel,
            value = "%.1f".format(telemetry.battery),
            unit = batteryUnit,
            icon = Icons.Default.FlashOn,
            onClick = { onValueClick(batteryLabel, batteryUnit) { it.battery / 10.0f } }
        )

        StatusRow(
            label = mapLabel,
            value = telemetry.map.toString(),
            unit = mapUnit,
            icon = Icons.Default.Cloud,
            onClick = { onValueClick(mapLabel, mapUnit) { it.map.toFloat() } }
        )

        StatusRow(
            label = airTempLabel,
            value = telemetry.airTemp.toString(),
            unit = celsiusUnit,
            icon = Icons.Default.Thermostat,
            onClick = { onValueClick(airTempLabel, celsiusUnit) { it.airTemp.toFloat() } }
        )

        StatusRow(
            label = coolantTempLabel,
            value = telemetry.coolantTemp.toString(),
            unit = celsiusUnit,
            icon = Icons.Default.Thermostat,
            onClick = { onValueClick(coolantTempLabel, celsiusUnit) { it.coolantTemp.toFloat() } }
        )

        HorizontalDivider()

        StatusIndicator(
            label = stringResource(R.string.ecu_neutral_status),
            isActive = telemetry.isNeutral,
            activeText = stringResource(R.string.ecu_neutral_on),
            inactiveText = stringResource(R.string.ecu_neutral_off)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EcuScreenPreview() {
    MaterialTheme {
        EcuScreenContent(
            telemetry = EcuTelemetry(
                isConnected = true,
                isStarted = true,
                isNeutral = false,
                rpm = 2500,
                battery = 13.0f,
                speed = 60,
                map = 56,
                tps = 150,
                airTemp = 30,
                coolantTemp = 78,
            )
        )
    }
}
