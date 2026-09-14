package by.jadjer.etcu.ui.features.device.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.system.SystemState
import by.jadjer.etcu.domain.model.telemetry.AcceleratorTelemetry
import by.jadjer.etcu.domain.model.telemetry.HistoryRecord
import by.jadjer.etcu.domain.model.telemetry.SystemStatusTelemetry
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.StatusRow
import by.jadjer.etcu.ui.component.history.HistoryGroup
import by.jadjer.etcu.ui.component.history.StatusGraphDialog
import by.jadjer.etcu.ui.features.device.DeviceUiState
import by.jadjer.etcu.ui.features.device.DeviceViewModel
import by.jadjer.etcu.ui.util.labelResId

@Composable
fun SystemScreen(viewModel: DeviceViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    var showDialog by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    SystemScreenContent(
        uiState = uiState,
        onValueClick = { label, unit, group ->
            showDialog = {
                SystemStatusGraphDialog(label, unit, group) { showDialog = null }
            }
        }
    )

    showDialog?.invoke()
}

@Composable
private fun <T> SystemStatusGraphDialog(
    label: String,
    unit: String,
    group: HistoryGroup<T>,
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
fun SystemScreenContent(
    uiState: DeviceUiState,
    onValueClick: (String, String, HistoryGroup<*>) -> Unit = { _, _, _ -> }
) {
    val telemetry = uiState.telemetry
    val accLabel = stringResource(R.string.system_accel)
    val thrLabel = stringResource(R.string.system_throttle_target)
    val targetSpeedLabel = stringResource(R.string.system_target_speed)
    val rawUnit = stringResource(R.string.unit_raw_1000)
    val kmhUnit = stringResource(R.string.unit_kmh)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            stringResource(R.string.system_status_title),
            style = MaterialTheme.typography.titleLarge
        )

        StatusRow(
            label = stringResource(R.string.system_state),
            value = stringResource(telemetry.status.systemState.labelResId),
            icon = Icons.Default.Info
        )

        HorizontalDivider()

        StatusRow(
            label = accLabel,
            value = telemetry.accelerator.position.toString(),
            unit = rawUnit,
            icon = Icons.Default.TwoWheeler,
            onClick = {
                onValueClick(
                    accLabel,
                    rawUnit,
                    HistoryGroup(
                        history = uiState.telemetryHistory.accelerator,
                        selector = { it.position.toFloat() },
                        currentValue = telemetry.accelerator.position.toFloat()
                    )
                )
            }
        )

        StatusRow(
            label = thrLabel,
            value = telemetry.status.throttlePosition.toString(),
            unit = rawUnit,
            icon = Icons.Default.Timeline,
            onClick = {
                onValueClick(
                    thrLabel,
                    rawUnit,
                    HistoryGroup(
                        history = uiState.telemetryHistory.status,
                        selector = { it.throttlePosition.toFloat() },
                        currentValue = telemetry.status.throttlePosition.toFloat()
                    )
                )
            }
        )

        HorizontalDivider()

        StatusIndicator(
            label = stringResource(R.string.system_guard),
            isActive = telemetry.status.isGuardActive,
            icon = Icons.Default.Lock
        )
        StatusIndicator(
            label = stringResource(R.string.system_brake),
            isActive = telemetry.status.isBrakeEnabled
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SystemScreenPreview() {
    MaterialTheme {
        SystemScreenContent(
            uiState = DeviceUiState(
                telemetry = SystemTelemetry(
                    status = SystemStatusTelemetry(
                        isGuardActive = false,
                        isBrakeEnabled = true,
                        systemState = SystemState.NORMAL,
                        throttlePosition = 280,
                    ),
                    accelerator = AcceleratorTelemetry(
                        hallA = 500,
                        hallB = 510,
                        position = 300,
                    )
                )
            )
        )
    }
}
