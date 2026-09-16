package by.jadjer.etcu.ui.features.device.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
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
import by.jadjer.etcu.domain.model.telemetry.CruiseTelemetry
import by.jadjer.etcu.domain.model.telemetry.TelemetryConstants
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.StatusRow
import by.jadjer.etcu.ui.component.history.rememberTelemetryHistoryState
import by.jadjer.etcu.ui.features.device.DeviceViewModel

@Composable
fun CruiseScreen(viewModel: DeviceViewModel) {
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val history by viewModel.telemetryHistory.collectAsStateWithLifecycle()

    val targetSpeedLabel = stringResource(R.string.cruise_target_speed)
    val currentSpeedLabel = stringResource(R.string.cruise_current_speed)
    val errorLabel = stringResource(R.string.cruise_error)
    val correctionLabel = stringResource(R.string.cruise_correction)
    val basePositionLabel = stringResource(R.string.cruise_base_pos)
    val targetPositionLabel = stringResource(R.string.cruise_target_pos)
    val currentPositionLabel = stringResource(R.string.cruise_current_pos)

    val historyState = rememberTelemetryHistoryState<CruiseTelemetry>(
        rangeProvider = remember(
            targetSpeedLabel, currentSpeedLabel, errorLabel, correctionLabel,
            basePositionLabel, targetPositionLabel, currentPositionLabel
        ) {
            { label: String ->
                when (label) {
                    targetSpeedLabel, currentSpeedLabel -> TelemetryConstants.SPEED_RANGE
                    errorLabel, correctionLabel -> TelemetryConstants.CRUISE_ERROR_RANGE
                    basePositionLabel, targetPositionLabel, currentPositionLabel -> TelemetryConstants.POSITION_RANGE
                    else -> null
                }
            }
        }
    )

    val targetSpeedProvider = remember { { t: CruiseTelemetry -> t.targetSpeed.toFloat() } }
    val currentSpeedProvider = remember { { t: CruiseTelemetry -> t.currentSpeed.toFloat() } }
    val errorProvider = remember { { t: CruiseTelemetry -> t.error } }
    val correctionProvider = remember { { t: CruiseTelemetry -> t.correction } }
    val basePositionProvider = remember { { t: CruiseTelemetry -> t.basePosition.toFloat() } }
    val currentPositionProvider = remember { { t: CruiseTelemetry -> t.currentPosition.toFloat() } }

    val onValueClick = remember(historyState) {
        { label: String, unit: String, provider: (CruiseTelemetry) -> Float ->
            historyState.onValueClick(label, unit, provider)
        }
    }

    CruiseScreenContent(
        telemetry = telemetry.cruise,
        targetSpeedProvider,
        currentSpeedProvider,
        errorProvider,
        correctionProvider,
        basePositionProvider,
        currentPositionProvider,
        onValueClick = onValueClick,
    )

    historyState.ShowDialog(
        historyProvider = { history.cruise },
        currentTelemetryProvider = { telemetry.cruise })
}

@Composable
fun CruiseScreenContent(
    telemetry: CruiseTelemetry,
    targetSpeedProvider: (CruiseTelemetry) -> Float,
    currentSpeedProvider: (CruiseTelemetry) -> Float,
    errorProvider: (CruiseTelemetry) -> Float,
    correctionProvider: (CruiseTelemetry) -> Float,
    basePositionProvider: (CruiseTelemetry) -> Float,
    currentPositionProvider: (CruiseTelemetry) -> Float,
    onValueClick: (String, String, (CruiseTelemetry) -> Float) -> Unit,
) {
    val errorLabel = stringResource(R.string.cruise_error)
    val correctionLabel = stringResource(R.string.cruise_correction)
    val targetSpeedLabel = stringResource(R.string.cruise_target_speed)
    val currentSpeedLabel = stringResource(R.string.cruise_current_speed)
    val basePositionLabel = stringResource(R.string.cruise_base_pos)
    val currentPositionLabel = stringResource(R.string.cruise_current_pos)

    val speedUnit = stringResource(R.string.unit_kmh)
    val positionUnit = stringResource(R.string.unit_raw_1000)
    val floatUnit = ""

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "cruise_status") {
            Spacer(Modifier.height(16.dp))
            StatusIndicator(
                label = stringResource(R.string.cruise_status_title),
                isActive = telemetry.isEnabled,
                activeText = stringResource(R.string.cruise_enabled),
                inactiveText = stringResource(R.string.cruise_disabled)
            )
        }

        item(key = "cruise_activated") {
            StatusIndicator(
                label = stringResource(R.string.cruise_activated),
                isActive = telemetry.isActivated,
                activeText = stringResource(R.string.cruise_activated),
                inactiveText = stringResource(R.string.cruise_inactive)
            )
        }

        item(key = "divider_1") { HorizontalDivider() }

        item(key = "target_speed") {
            StatusRow(
                label = targetSpeedLabel,
                value = telemetry.targetSpeed.toString(),
                unit = speedUnit,
                icon = Icons.Default.Speed,
                onClick = { onValueClick(targetSpeedLabel, speedUnit, targetSpeedProvider) }
            )
        }

        item(key = "current_speed") {
            StatusRow(
                label = currentSpeedLabel,
                value = telemetry.currentSpeed.toString(),
                unit = speedUnit,
                icon = Icons.Default.Speed,
                onClick = { onValueClick(currentSpeedLabel, speedUnit, currentSpeedProvider) }
            )
        }

        item(key = "divider_2") { HorizontalDivider() }

        item(key = "error") {
            StatusRow(
                label = errorLabel,
                value = "%.2f".format(telemetry.error),
                unit = floatUnit,
                icon = Icons.Default.Tune,
                onClick = { onValueClick(errorLabel, floatUnit, errorProvider) }
            )
        }

        item(key = "correction") {
            StatusRow(
                label = correctionLabel,
                value = "%.2f".format(telemetry.correction),
                unit = floatUnit,
                icon = Icons.AutoMirrored.Filled.ShowChart,
                onClick = { onValueClick(correctionLabel, floatUnit, correctionProvider) }
            )
        }

        item(key = "divider_3") { HorizontalDivider() }

        item(key = "base_position") {
            StatusRow(
                label = basePositionLabel,
                value = telemetry.basePosition.toString(),
                unit = positionUnit,
                icon = Icons.Default.LocationSearching,
                onClick = { onValueClick(basePositionLabel, positionUnit, basePositionProvider) }
            )
        }

        item(key = "current_position") {
            StatusRow(
                label = currentPositionLabel,
                value = telemetry.currentPosition.toString(),
                unit = positionUnit,
                icon = Icons.Default.Timeline,
                onClick = {
                    onValueClick(
                        currentPositionLabel,
                        positionUnit,
                        currentPositionProvider
                    )
                }
            )
            Spacer(Modifier.height(16.dp))
        }
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
            targetSpeedProvider = { it.targetSpeed.toFloat() },
            currentSpeedProvider = { it.currentSpeed.toFloat() },
            errorProvider = { it.error },
            correctionProvider = { it.correction },
            basePositionProvider = { it.basePosition.toFloat() },
            currentPositionProvider = { it.currentPosition.toFloat() },
            onValueClick = { _, _, _ -> }
        )
    }
}
