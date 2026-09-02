package by.jadjer.etcu.ui.features.device

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.SystemState
import by.jadjer.etcu.domain.model.SystemTelemetry
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.TelemetryRow

@Composable
fun SystemScreen(viewModel: DeviceViewModel) {
    val telemetry by viewModel.telemetry.collectAsState()
    SystemScreenContent(telemetry)
}

@Composable
fun SystemScreenContent(
    telemetry: SystemTelemetry
) {
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
            label = stringResource(R.string.system_accel),
            value = telemetry.acceleratorPosition.toString(),
            unit = stringResource(R.string.unit_raw_1000),
            icon = Icons.Default.PedalBike
        )
        
        TelemetryRow(
            label = stringResource(R.string.system_throttle_target),
            value = telemetry.throttlePosition.toString(),
            unit = stringResource(R.string.unit_raw_1000)
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
