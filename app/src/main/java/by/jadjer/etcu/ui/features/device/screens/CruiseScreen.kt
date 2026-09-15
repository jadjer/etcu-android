package by.jadjer.etcu.ui.features.device.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.telemetry.CruiseTelemetry
import by.jadjer.etcu.domain.model.telemetry.TelemetryConstants
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.StatusRow
import by.jadjer.etcu.ui.component.history.rememberTelemetryHistoryState
import by.jadjer.etcu.ui.features.device.DeviceViewModel

@Composable
fun CruiseScreen(viewModel: DeviceViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    val targetSpeedLabel = stringResource(R.string.cruise_target_speed)
    val currentSpeedLabel = stringResource(R.string.cruise_current_speed)
    val errorLabel = stringResource(R.string.cruise_error)
    val corrLabel = stringResource(R.string.cruise_correction)
    val basePosLabel = stringResource(R.string.cruise_base_pos)
    val targetPosLabel = stringResource(R.string.cruise_target_pos)
    val currentPosLabel = stringResource(R.string.cruise_current_pos)

    val historyState = rememberTelemetryHistoryState<CruiseTelemetry>(
        rangeProvider = { label ->
            when (label) {
                targetSpeedLabel, currentSpeedLabel -> TelemetryConstants.SPEED_RANGE
                errorLabel, corrLabel -> TelemetryConstants.CRUISE_ERROR_RANGE
                basePosLabel, targetPosLabel, currentPosLabel -> TelemetryConstants.POSITION_RANGE
                else -> null
            }
        }
    )

    CruiseScreenContent(
        telemetry = telemetry.cruise,
        onValueClick = historyState::onValueClick
    )

    historyState.ShowDialog(history = uiState.telemetryHistory.cruise, currentTelemetry = telemetry.cruise)
}

@Composable
fun CruiseScreenContent(
    telemetry: CruiseTelemetry,
    onValueClick: (String, String, (CruiseTelemetry) -> Float) -> Unit
) {
    val errorLabel = stringResource(R.string.cruise_error)
    val corrLabel = stringResource(R.string.cruise_correction)
    val targetSpeedLabel = stringResource(R.string.cruise_target_speed)
    val currentSpeedLabel = stringResource(R.string.cruise_current_speed)
    val basePosLabel = stringResource(R.string.cruise_base_pos)
    val currentPosLabel = stringResource(R.string.cruise_current_pos)

    val speedUnit = stringResource(R.string.unit_kmh)
    val posUnit = stringResource(R.string.unit_raw_1000)
    val floatUnit = ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusIndicator(
            label = stringResource(R.string.cruise_status_title),
            isActive = telemetry.isEnabled,
            activeText = stringResource(R.string.cruise_enabled),
            inactiveText = stringResource(R.string.cruise_disabled)
        )

        StatusIndicator(
            label = stringResource(R.string.cruise_activated),
            isActive = telemetry.isActivated,
            activeText = stringResource(R.string.cruise_activated),
            inactiveText = stringResource(R.string.cruise_inactive)
        )

        HorizontalDivider()

        StatusRow(
            label = targetSpeedLabel,
            value = telemetry.targetSpeed.toString(),
            unit = speedUnit,
            icon = Icons.Default.Speed,
            onClick = { onValueClick(targetSpeedLabel, speedUnit) { it.targetSpeed.toFloat() } }
        )

        StatusRow(
            label = currentSpeedLabel,
            value = telemetry.currentSpeed.toString(),
            unit = speedUnit,
            icon = Icons.Default.Speed,
            onClick = { onValueClick(currentSpeedLabel, speedUnit) { it.currentSpeed.toFloat() } }
        )

        HorizontalDivider()

        StatusRow(
            label = errorLabel,
            value = "%.2f".format(telemetry.error),
            unit = floatUnit,
            icon = Icons.Default.Tune,
            onClick = { onValueClick(errorLabel, floatUnit) { it.error } }
        )

        StatusRow(
            label = corrLabel,
            value = "%.2f".format(telemetry.correction),
            unit = floatUnit,
            icon = Icons.AutoMirrored.Filled.ShowChart,
            onClick = { onValueClick(corrLabel, floatUnit) { it.correction } }
        )

        HorizontalDivider()

        StatusRow(
            label = basePosLabel,
            value = telemetry.basePosition.toString(),
            unit = posUnit,
            icon = Icons.Default.LocationSearching,
            onClick = { onValueClick(basePosLabel, posUnit) { it.basePosition.toFloat() } }
        )

        StatusRow(
            label = currentPosLabel,
            value = telemetry.currentPosition.toString(),
            unit = posUnit,
            icon = Icons.Default.Timeline,
            onClick = { onValueClick(currentPosLabel, posUnit) { it.currentPosition.toFloat() } }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CruiseScreenPreview() {
    MaterialTheme {
        CruiseScreenContent(
            telemetry = CruiseTelemetry(
                isEnabled = true,
                isActivated = true,

                error = 1.5f,
                correction = 10.0f,

                targetSpeed = 60,
                currentSpeed = 58,

                basePosition = 285,
                currentPosition = 310
            ),
            onValueClick = { _, _, _ -> }
        )
    }
}
