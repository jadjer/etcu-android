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
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.system.SystemState
import by.jadjer.etcu.domain.model.telemetry.AcceleratorTelemetry
import by.jadjer.etcu.domain.model.telemetry.SystemStatusTelemetry
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.StatusRow
import by.jadjer.etcu.ui.component.history.HistoryGroup
import by.jadjer.etcu.ui.component.history.StatusGraphDialog
import by.jadjer.etcu.ui.features.device.DeviceViewModel
import by.jadjer.etcu.ui.util.labelResId

@Composable
fun SystemScreen(viewModel: DeviceViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    var showDialog by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    SystemScreenContent(
        statusTelemetry = telemetry.status,
        acceleratorTelemetry = telemetry.accelerator,
        onAcceleratorClick = { label, unit, selector ->
            showDialog = {
                val group = HistoryGroup(
                    history = uiState.telemetryHistory.accelerator,
                    selector = selector,
                    currentValue = selector(telemetry.accelerator)
                )
                StatusGraphDialog(label, unit, group) { showDialog = null }
            }
        },
        onThrottleClick = { label, unit, selector ->
            showDialog = {
                val group = HistoryGroup(
                    history = uiState.telemetryHistory.status,
                    selector = selector,
                    currentValue = selector(telemetry.status)
                )
                StatusGraphDialog(label, unit, group) { showDialog = null }
            }
        }
    )

    showDialog?.invoke()
}

@Composable
fun SystemScreenContent(
    statusTelemetry: SystemStatusTelemetry,
    acceleratorTelemetry: AcceleratorTelemetry,
    onAcceleratorClick: (String, String, (AcceleratorTelemetry) -> Float) -> Unit,
    onThrottleClick: (String, String, (SystemStatusTelemetry) -> Float) -> Unit
) {
    val accLabel = stringResource(R.string.system_accel)
    val thrLabel = stringResource(R.string.system_throttle_target)
    val rawUnit = stringResource(R.string.unit_raw_1000)

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
            value = stringResource(statusTelemetry.systemState.labelResId),
            icon = Icons.Default.Info
        )

        HorizontalDivider()

        StatusRow(
            label = accLabel,
            value = acceleratorTelemetry.position.toString(),
            unit = rawUnit,
            icon = Icons.Default.TwoWheeler,
            onClick = { onAcceleratorClick(accLabel, rawUnit) { it.position.toFloat() } }
        )

        StatusRow(
            label = thrLabel,
            value = statusTelemetry.throttlePosition.toString(),
            unit = rawUnit,
            icon = Icons.Default.Timeline,
            onClick = { onThrottleClick(thrLabel, rawUnit) { it.throttlePosition.toFloat() } }
        )

        HorizontalDivider()

        StatusIndicator(
            label = stringResource(R.string.system_guard),
            isActive = statusTelemetry.isGuardActive,
            icon = Icons.Default.Lock
        )
        StatusIndicator(
            label = stringResource(R.string.system_brake),
            isActive = statusTelemetry.isBrakeEnabled
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SystemScreenPreview() {
    MaterialTheme {
        SystemScreenContent(
            statusTelemetry = SystemStatusTelemetry(
                isGuardActive = false,
                isBrakeEnabled = true,
                systemState = SystemState.NORMAL,
                throttlePosition = 280,
            ),
            acceleratorTelemetry = AcceleratorTelemetry(
                hallA = 500,
                hallB = 510,
                position = 300,
            ),
            onThrottleClick = { _, _, _ -> },
            onAcceleratorClick = { _, _, _ -> }
        )
    }
}
