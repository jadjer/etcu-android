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
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.StatusRow
import by.jadjer.etcu.ui.component.StatusRow
import by.jadjer.etcu.ui.component.history.HistoryGroup
import by.jadjer.etcu.ui.component.history.StatusGraphDialog
import by.jadjer.etcu.ui.features.device.DeviceViewModel

@Composable
fun CruiseScreen(viewModel: DeviceViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    var showDialog by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    CruiseScreenContent(
        telemetry = telemetry.cruise,
        onValueClick = { label, unit, selector ->
            showDialog = {
                val group = HistoryGroup(
                    history = uiState.telemetryHistory.cruise,
                    selector = selector,
                    currentValue = selector(telemetry.cruise)
                )
                CruiseStatusGraphDialog(label, unit, group) { showDialog = null }
            }
        }
    )

    showDialog?.invoke()
}

@Composable
private fun CruiseStatusGraphDialog(
    label: String,
    unit: String,
    group: HistoryGroup<CruiseTelemetry>,
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
fun CruiseScreenContent(
    telemetry: CruiseTelemetry,
    onValueClick: (String, String, (CruiseTelemetry) -> Float) -> Unit = { _, _, _ -> }
) {
    val errorLabel = stringResource(R.string.cruise_error)
    val corrLabel = stringResource(R.string.cruise_correction)
    val targetSpeedLabel = stringResource(R.string.cruise_target_speed)
    val basePosLabel = stringResource(R.string.cruise_base_pos)
    val targetPosLabel = stringResource(R.string.cruise_target_pos)
    
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
            label = targetPosLabel,
            value = telemetry.targetPosition.toString(),
            unit = posUnit,
            icon = Icons.Default.Timeline,
            onClick = { onValueClick(targetPosLabel, posUnit) { it.targetPosition.toFloat() } }
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
                targetSpeed = 60,
                error = 1.5f,
                correction = 10.0f,
                basePosition = 300,
                targetPosition = 310
            )
        )
    }
}
