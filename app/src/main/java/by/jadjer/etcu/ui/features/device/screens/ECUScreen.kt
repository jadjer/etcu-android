package by.jadjer.etcu.ui.features.device.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.telemetry.ECUTelemetry
import by.jadjer.etcu.domain.model.telemetry.TelemetryConstants
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.StatusRow
import by.jadjer.etcu.ui.component.history.rememberTelemetryHistoryState
import by.jadjer.etcu.ui.features.device.DeviceViewModel

@Composable
fun EcuScreen(viewModel: DeviceViewModel) {
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val history by viewModel.telemetryHistory.collectAsStateWithLifecycle()

    val rpmLabel = stringResource(R.string.ecu_rpm)
    val speedLabel = stringResource(R.string.ecu_speed)
    val tpsLabel = stringResource(R.string.ecu_tps)
    val batteryLabel = stringResource(R.string.ecu_battery)
    val mapLabel = stringResource(R.string.ecu_map)
    val airTemperatureLabel = stringResource(R.string.ecu_air_temp)
    val coolantTemperatureLabel = stringResource(R.string.ecu_coolant_temp)

    val historyState = rememberTelemetryHistoryState<ECUTelemetry>(
        rangeProvider = remember(
            rpmLabel,
            speedLabel,
            tpsLabel,
            batteryLabel,
            mapLabel,
            airTemperatureLabel,
            coolantTemperatureLabel
        ) {
            { label: String ->
                when (label) {
                    rpmLabel -> TelemetryConstants.RPM_RANGE
                    speedLabel -> TelemetryConstants.SPEED_RANGE
                    tpsLabel -> TelemetryConstants.TPS_RANGE
                    batteryLabel -> TelemetryConstants.VOLTAGE_RANGE
                    mapLabel -> TelemetryConstants.MAP_RANGE
                    airTemperatureLabel, coolantTemperatureLabel -> TelemetryConstants.TEMPERATURE_RANGE
                    else -> null
                }
            }
        }
    )

    val rpmProvider = remember { { t: ECUTelemetry -> t.rpm.toFloat() } }
    val speedProvider = remember { { t: ECUTelemetry -> t.speed.toFloat() } }
    val tpsProvider = remember { { t: ECUTelemetry -> t.tps.toFloat() } }
    val batteryProvider = remember { { t: ECUTelemetry -> t.battery } }
    val mapProvider = remember { { t: ECUTelemetry -> t.map.toFloat() } }
    val airTemperatureProvider = remember { { t: ECUTelemetry -> t.airTemp.toFloat() } }
    val coolantTemperatureProvider = remember { { t: ECUTelemetry -> t.coolantTemp.toFloat() } }

    val onValueClick = remember(historyState) {
        { label: String, unit: String, provider: (ECUTelemetry) -> Float ->
            historyState.onValueClick(label, unit, provider)
        }
    }

    EcuScreenContent(
        telemetry = telemetry.ecu,
        rpmProvider = rpmProvider,
        speedProvider = speedProvider,
        tpsProvider = tpsProvider,
        batteryProvider = batteryProvider,
        mapProvider = mapProvider,
        airTemperatureProvider = airTemperatureProvider,
        coolantTemperatureProvider = coolantTemperatureProvider,
        onValueClick = onValueClick,
    )

    historyState.ShowDialog(
        historyProvider = { history.ecu },
        currentTelemetryProvider = { telemetry.ecu })
}

@Composable
fun EcuScreenContent(
    telemetry: ECUTelemetry,
    rpmProvider: (ECUTelemetry) -> Float,
    speedProvider: (ECUTelemetry) -> Float,
    tpsProvider: (ECUTelemetry) -> Float,
    batteryProvider: (ECUTelemetry) -> Float,
    mapProvider: (ECUTelemetry) -> Float,
    airTemperatureProvider: (ECUTelemetry) -> Float,
    coolantTemperatureProvider: (ECUTelemetry) -> Float,
    onValueClick: (String, String, (ECUTelemetry) -> Float) -> Unit,
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "ecu_conn_status") {
            Spacer(Modifier.height(16.dp))
            StatusIndicator(
                label = stringResource(R.string.ecu_conn_status),
                isActive = telemetry.isConnected,
                activeText = stringResource(R.string.ecu_connected),
                inactiveText = stringResource(R.string.ecu_disconnected)
            )
        }

        item(key = "ecu_engine_status") {
            StatusIndicator(
                label = stringResource(R.string.ecu_engine_status),
                isActive = telemetry.isStarted,
                activeText = stringResource(R.string.ecu_engine_started),
                inactiveText = stringResource(R.string.ecu_engine_stopped)
            )
        }

        item(key = "divider_1") { HorizontalDivider() }

        item(key = "rpm") {
            StatusRow(
                label = rpmLabel,
                value = telemetry.rpm.toString(),
                unit = rpmUnit,
                icon = Icons.Default.Timer,
                onClick = { onValueClick(rpmLabel, rpmUnit, rpmProvider) }
            )
        }

        item(key = "speed") {
            StatusRow(
                label = speedLabel,
                value = telemetry.speed.toString(),
                unit = speedUnit,
                icon = Icons.Default.Speed,
                onClick = { onValueClick(speedLabel, speedUnit, speedProvider) }
            )
        }

        item(key = "tps") {
            StatusRow(
                label = tpsLabel,
                value = telemetry.tps.toString(),
                unit = tpsUnit,
                icon = Icons.Default.TwoWheeler,
                onClick = { onValueClick(tpsLabel, tpsUnit, tpsProvider) }
            )
        }

        item(key = "battery") {
            StatusRow(
                label = batteryLabel,
                value = "%.1f".format(telemetry.battery),
                unit = batteryUnit,
                icon = Icons.Default.FlashOn,
                onClick = { onValueClick(batteryLabel, batteryUnit, batteryProvider) }
            )
        }

        item(key = "map") {
            StatusRow(
                label = mapLabel,
                value = telemetry.map.toString(),
                unit = mapUnit,
                icon = Icons.Default.Cloud,
                onClick = { onValueClick(mapLabel, mapUnit, mapProvider) }
            )
        }

        item(key = "air_temp") {
            StatusRow(
                label = airTempLabel,
                value = telemetry.airTemp.toString(),
                unit = celsiusUnit,
                icon = Icons.Default.Thermostat,
                onClick = { onValueClick(airTempLabel, celsiusUnit, airTemperatureProvider) }
            )
        }

        item(key = "coolant_temp") {
            StatusRow(
                label = coolantTempLabel,
                value = telemetry.coolantTemp.toString(),
                unit = celsiusUnit,
                icon = Icons.Default.Thermostat,
                onClick = {
                    onValueClick(
                        coolantTempLabel,
                        celsiusUnit,
                        coolantTemperatureProvider
                    )
                }
            )
        }

        item(key = "divider_2") { HorizontalDivider() }

        item(key = "ecu_neutral_status") {
            StatusIndicator(
                label = stringResource(R.string.ecu_neutral_status),
                isActive = telemetry.isNeutral,
                activeText = stringResource(R.string.ecu_neutral_on),
                inactiveText = stringResource(R.string.ecu_neutral_off)
            )
            Spacer(Modifier.height(16.dp))
        }
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
                isNeutral = false,
                rpm = 2500,
                battery = 13.0f,
                speed = 60,
                map = 56,
                tps = 150,
                airTemp = 30,
                coolantTemp = 78,
            ),
            rpmProvider = { it.rpm.toFloat() },
            speedProvider = { it.speed.toFloat() },
            tpsProvider = { it.tps.toFloat() },
            batteryProvider = { it.battery },
            mapProvider = { it.map.toFloat() },
            airTemperatureProvider = { it.airTemp.toFloat() },
            coolantTemperatureProvider = { it.coolantTemp.toFloat() },
            onValueClick = { _, _, _ -> }
        )
    }
}