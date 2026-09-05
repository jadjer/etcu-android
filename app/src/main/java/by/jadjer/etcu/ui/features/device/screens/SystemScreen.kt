package by.jadjer.etcu.ui.features.device.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.system.SystemState
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.telemetry.TelemetryGraphDialog
import by.jadjer.etcu.ui.component.telemetry.TelemetryRow
import by.jadjer.etcu.ui.component.telemetry.SelectedTelemetry
import by.jadjer.etcu.ui.features.device.DeviceViewModel

@Composable
fun SystemScreen(viewModel: DeviceViewModel) {
    val telemetry by viewModel.telemetry.collectAsState()
    val history by viewModel.telemetryHistory.collectAsState()

    var selectedTelemetry by remember { mutableStateOf<SelectedTelemetry?>(null) }

    SystemScreenContent(
        telemetry = telemetry,
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
fun SystemScreenContent(
    telemetry: SystemTelemetry,
    onValueClick: (String, String, (SystemTelemetry) -> Int) -> Unit = { _, _, _ -> }
) {
    val accLabel = stringResource(R.string.system_accel)
    val thrLabel = stringResource(R.string.system_throttle_target)
    val rawUnit = stringResource(R.string.unit_raw_1000)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(stringResource(R.string.system_telemetry_title), style = MaterialTheme.typography.titleMedium)

        TelemetryRow(
            label = stringResource(R.string.system_state),
            value = stringResource(telemetry.systemState.resId),
            icon = Icons.Default.Info
        )
        
        TelemetryRow(
            label = accLabel,
            value = telemetry.acceleratorPosition.toString(),
            unit = rawUnit,
            icon = Icons.Default.TwoWheeler,
            onClick = {
                onValueClick(
                    accLabel,
                    rawUnit,
                    { it.acceleratorPosition }
                )
            }
        )
        
        TelemetryRow(
            label = thrLabel,
            value = telemetry.throttlePosition.toString(),
            unit = rawUnit,
            onClick = {
                onValueClick(
                    thrLabel,
                    rawUnit,
                    { it.throttlePosition }
                )
            }
        )
        
        StatusIndicator(
            label = stringResource(R.string.system_guard),
            isActive = telemetry.isGuardActive,
            icon = Icons.Default.Lock
        )
        
        StatusIndicator(
            label = stringResource(R.string.system_brake),
            isActive = telemetry.isBrakeEnabled
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SystemScreenPreview() {
    MaterialTheme {
        SystemScreenContent(
            telemetry = SystemTelemetry(
                isGuardActive = false,
                isBrakeEnabled = true,
                systemState = SystemState.NORMAL,
                acceleratorPosition = 300,
                throttlePosition = 280,
            )
        )
    }
}
