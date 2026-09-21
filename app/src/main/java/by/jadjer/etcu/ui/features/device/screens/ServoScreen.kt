package by.jadjer.etcu.ui.features.device.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationSearching
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
import by.jadjer.etcu.domain.model.telemetry.ServoTelemetry
import by.jadjer.etcu.domain.model.telemetry.TelemetryConstants
import by.jadjer.etcu.domain.model.system.SystemState
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.StatusIndicatorPlaceholder
import by.jadjer.etcu.ui.component.StatusRow
import by.jadjer.etcu.ui.component.StatusRowPlaceholder
import by.jadjer.etcu.ui.component.history.rememberTelemetryHistoryState
import by.jadjer.etcu.ui.features.device.DeviceViewModel

@Composable
fun ServoScreen(viewModel: DeviceViewModel) {
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val history by viewModel.telemetryHistory.collectAsStateWithLifecycle()

    val positionLabel = stringResource(R.string.servo_position)
    val currentLabel = stringResource(R.string.servo_current)
    val voltageLabel = stringResource(R.string.servo_voltage)
    val temperatureLabel = stringResource(R.string.servo_temp)

    val historyState = rememberTelemetryHistoryState<ServoTelemetry>(
        rangeProvider = remember(positionLabel, currentLabel, voltageLabel, temperatureLabel) {
            { label: String ->
                when (label) {
                    positionLabel -> TelemetryConstants.SERVO_POSITION_RANGE
                    currentLabel -> TelemetryConstants.SERVO_CURRENT_RANGE
                    voltageLabel -> TelemetryConstants.VOLTAGE_RANGE
                    temperatureLabel -> TelemetryConstants.TEMPERATURE_RANGE
                    else -> null
                }
            }
        }
    )

    val positionProvider = remember { { t: ServoTelemetry -> t.position.toFloat() } }
    val currentProvider = remember { { t: ServoTelemetry -> t.current.toFloat() } }
    val voltageProvider = remember { { t: ServoTelemetry -> t.voltage } }
    val temperatureProvider = remember { { t: ServoTelemetry -> t.temperature.toFloat() } }

    val onValueClick = remember(historyState) {
        { label: String, unit: String, provider: (ServoTelemetry) -> Float ->
            historyState.onValueClick(label, unit, provider)
        }
    }

    ServoScreenContent(
        telemetry = telemetry.servo,
        isLoading = telemetry.status.systemState == SystemState.UNKNOWN,
        positionProvider = positionProvider,
        currentProvider = currentProvider,
        voltageProvider = voltageProvider,
        temperatureProvider = temperatureProvider,
        onValueClick = onValueClick
    )

    historyState.ShowDialog(
        historyProvider = { history.servo },
        currentTelemetryProvider = { telemetry.servo },
        lastUpdateProvider = { history.lastUpdate }
    )
}

@Composable
fun ServoScreenContent(
    telemetry: ServoTelemetry,
    isLoading: Boolean = false,
    positionProvider: (ServoTelemetry) -> Float,
    currentProvider: (ServoTelemetry) -> Float,
    voltageProvider: (ServoTelemetry) -> Float,
    temperatureProvider: (ServoTelemetry) -> Float,
    onValueClick: (String, String, (ServoTelemetry) -> Float) -> Unit
) {
    val positionLabel = stringResource(R.string.servo_position)
    val positionUnit = stringResource(R.string.unit_raw_4095)
    val currentLabel = stringResource(R.string.servo_current)
    val currentUnit = stringResource(R.string.unit_ma)
    val voltageLabel = stringResource(R.string.servo_voltage)
    val voltageUnit = stringResource(R.string.unit_v)
    val temperatureLabel = stringResource(R.string.servo_temp)
    val temperatureUnit = stringResource(R.string.unit_celsius)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "servo_conn_status") {
            Spacer(Modifier.height(16.dp))
            if (isLoading) StatusIndicatorPlaceholder()
            else StatusIndicator(
                label = stringResource(R.string.servo_conn_status),
                isActive = telemetry.isConnected,
                activeText = stringResource(R.string.servo_connected),
                inactiveText = stringResource(R.string.servo_disconnected)
            )
        }

        item(key = "servo_enable_status") {
            if (isLoading) StatusIndicatorPlaceholder()
            else StatusIndicator(
                label = stringResource(R.string.servo_enable_status),
                isActive = telemetry.isEnabled,
                activeText = stringResource(R.string.servo_enabled),
                inactiveText = stringResource(R.string.servo_disabled)
            )
        }

        item(key = "divider_1") { HorizontalDivider() }

        item(key = "position") {
            if (isLoading) StatusRowPlaceholder()
            else StatusRow(
                label = positionLabel,
                value = telemetry.position.toString(),
                unit = positionUnit,
                icon = Icons.Default.LocationSearching,
                onClick = { onValueClick(positionLabel, positionUnit, positionProvider) }
            )
        }

        item(key = "current") {
            if (isLoading) StatusRowPlaceholder()
            else StatusRow(
                label = currentLabel,
                value = telemetry.current.toString(),
                unit = currentUnit,
                icon = Icons.Default.Bolt,
                onClick = { onValueClick(currentLabel, currentUnit, currentProvider) }
            )
        }

        item(key = "voltage") {
            if (isLoading) StatusRowPlaceholder()
            else StatusRow(
                label = voltageLabel,
                value = "%.1f".format(telemetry.voltage),
                unit = voltageUnit,
                icon = Icons.Default.FlashOn,
                onClick = { onValueClick(voltageLabel, voltageUnit, voltageProvider) }
            )
        }

        item(key = "temperature") {
            if (isLoading) StatusRowPlaceholder()
            else StatusRow(
                label = temperatureLabel,
                value = telemetry.temperature.toString(),
                unit = temperatureUnit,
                icon = Icons.Default.DeviceThermostat,
                onClick = { onValueClick(temperatureLabel, temperatureUnit, temperatureProvider) }
            )
        }

        item(key = "divider_2") { HorizontalDivider() }

        item(key = "servo_motion_status") {
            if (isLoading) StatusIndicatorPlaceholder()
            else StatusIndicator(
                label = stringResource(R.string.servo_motion_status),
                isActive = telemetry.isMoved,
                activeText = stringResource(R.string.servo_moving),
                inactiveText = stringResource(R.string.servo_static)
            )
            Spacer(Modifier.height(16.dp))
        }
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
            isLoading = false,
            positionProvider = { it.position.toFloat() },
            currentProvider = { it.current.toFloat() },
            voltageProvider = { it.voltage },
            temperatureProvider = { it.temperature.toFloat() },
            onValueClick = { _, _, _ -> }
        )
    }
}
