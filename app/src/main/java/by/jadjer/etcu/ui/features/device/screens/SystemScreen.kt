package by.jadjer.etcu.ui.features.device.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.system.SystemState
import by.jadjer.etcu.domain.model.telemetry.AcceleratorTelemetry
import by.jadjer.etcu.domain.model.telemetry.SystemStatusTelemetry
import by.jadjer.etcu.domain.model.telemetry.TelemetryConstants
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.StatusIndicatorPlaceholder
import by.jadjer.etcu.ui.component.StatusRow
import by.jadjer.etcu.ui.component.StatusRowPlaceholder
import by.jadjer.etcu.ui.component.history.rememberTelemetryHistoryState
import by.jadjer.etcu.ui.features.device.DeviceViewModel
import by.jadjer.etcu.ui.util.labelResId

@Composable
fun SystemScreen(viewModel: DeviceViewModel) {
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val history by viewModel.telemetryHistory.collectAsStateWithLifecycle()

    val acceleratorHistoryState = rememberTelemetryHistoryState<AcceleratorTelemetry>(
        rangeProvider = remember { { TelemetryConstants.POSITION_RANGE } }
    )

    val throttleHistoryState = rememberTelemetryHistoryState<SystemStatusTelemetry>(
        rangeProvider = remember { { TelemetryConstants.POSITION_RANGE } }
    )

    val acceleratorProvider = remember { { t: AcceleratorTelemetry -> t.position.toFloat() } }
    val throttleProvider = remember { { t: SystemStatusTelemetry -> t.throttlePosition.toFloat() } }

    val onAcceleratorClick = remember(acceleratorHistoryState) {
        { label: String, unit: String, provider: (AcceleratorTelemetry) -> Float ->
            acceleratorHistoryState.onValueClick(label, unit, provider)
        }
    }

    val onThrottleClick = remember(throttleHistoryState) {
        { label: String, unit: String, provider: (SystemStatusTelemetry) -> Float ->
            throttleHistoryState.onValueClick(label, unit, provider)
        }
    }

    SystemScreenContent(
        statusTelemetry = telemetry.status,
        acceleratorTelemetry = telemetry.accelerator,
        isLoading = telemetry.status.systemState == SystemState.UNKNOWN,
        acceleratorProvider = acceleratorProvider,
        onAcceleratorClick = onAcceleratorClick,
        throttleProvider = throttleProvider,
        onThrottleClick = onThrottleClick
    )

    acceleratorHistoryState.ShowDialog(
        historyProvider = { history.accelerator },
        currentTelemetryProvider = { telemetry.accelerator },
        lastUpdateProvider = { history.lastUpdate }
    )
    throttleHistoryState.ShowDialog(
        historyProvider = { history.status },
        currentTelemetryProvider = { telemetry.status },
        lastUpdateProvider = { history.lastUpdate }
    )
}

@Composable
fun SystemScreenContent(
    statusTelemetry: SystemStatusTelemetry,
    acceleratorTelemetry: AcceleratorTelemetry,
    isLoading: Boolean = false,
    acceleratorProvider: (AcceleratorTelemetry) -> Float,
    onAcceleratorClick: (String, String, (AcceleratorTelemetry) -> Float) -> Unit,
    throttleProvider: (SystemStatusTelemetry) -> Float,
    onThrottleClick: (String, String, (SystemStatusTelemetry) -> Float) -> Unit
) {
    val acceleratorLabel = stringResource(R.string.system_accel)
    val throttleLabel = stringResource(R.string.system_throttle_target)
    val rawUnit = stringResource(R.string.unit_raw_1000)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "header") {
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.system_status_title),
                style = MaterialTheme.typography.titleLarge
            )
        }

        item(key = "system_state") {
            if (isLoading) StatusRowPlaceholder()
            else StatusRow(
                label = stringResource(R.string.system_state),
                value = stringResource(statusTelemetry.systemState.labelResId),
                icon = Icons.Default.Info
            )
        }

        item(key = "divider_1") { HorizontalDivider() }

        item(key = "accelerator") {
            if (isLoading) StatusRowPlaceholder()
            else StatusRow(
                label = acceleratorLabel,
                value = acceleratorTelemetry.position.toString(),
                unit = rawUnit,
                icon = Icons.Default.TwoWheeler,
                onClick = { onAcceleratorClick(acceleratorLabel, rawUnit, acceleratorProvider) }
            )
        }

        item(key = "throttle") {
            if (isLoading) StatusRowPlaceholder()
            else StatusRow(
                label = throttleLabel,
                value = statusTelemetry.throttlePosition.toString(),
                unit = rawUnit,
                icon = Icons.Default.Timeline,
                onClick = { onThrottleClick(throttleLabel, rawUnit, throttleProvider) }
            )
        }

        item(key = "divider_2") { HorizontalDivider() }

        item(key = "system_guard") {
            if (isLoading) StatusIndicatorPlaceholder()
            else StatusIndicator(
                label = stringResource(R.string.system_guard),
                isActive = statusTelemetry.isGuardActive,
                icon = Icons.Default.Lock
            )
        }

        item(key = "system_brake") {
            if (isLoading) StatusIndicatorPlaceholder()
            else StatusIndicator(
                label = stringResource(R.string.system_brake),
                isActive = statusTelemetry.isBrakeEnabled
            )
            Spacer(Modifier.height(16.dp))
        }
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
            isLoading = false,
            acceleratorProvider = { it.position.toFloat() },
            onAcceleratorClick = { _, _, _ -> },
            throttleProvider = { it.throttlePosition.toFloat() },
            onThrottleClick = { _, _, _ -> },
        )
    }
}
