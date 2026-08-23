package by.jadjer.etcu.ui.screen.system

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.data.model.SystemState
import by.jadjer.etcu.data.model.SystemTelemetry
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.TelemetryRow

@Composable
fun SystemScreen(viewModel: SystemViewModel) {
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
        Text("Текущая телеметрия", style = MaterialTheme.typography.titleMedium)

        TelemetryRow(
            label = "Режим системы",
            value = telemetry.systemState.name,
            icon = Icons.Default.Info
        )
        
        TelemetryRow(
            label = "Акселератор",
            value = telemetry.acceleratorPosition.toString(),
            unit = "/ 1000",
            icon = Icons.Default.PedalBike
        )
        
        TelemetryRow(
            label = "Дроссель (цель)",
            value = telemetry.throttlePosition.toString(),
            unit = "/ 1000"
        )
        
        StatusIndicator(
            label = "Защита (Guard)",
            isActive = telemetry.guardActive,
            icon = Icons.Default.Lock
        )
        
        StatusIndicator(
            label = "Тормоз",
            isActive = telemetry.brakeEnabled
        )
        
        StatusIndicator(
            label = "Сцепление",
            isActive = telemetry.clutchEnabled
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SystemScreenPreview() {
    MaterialTheme {
        SystemScreenContent(
            telemetry = SystemTelemetry(
                systemState = SystemState.NORMAL,
                acceleratorPosition = 300,
                throttlePosition = 280,
                guardActive = false,
                brakeEnabled = true,
                clutchEnabled = false
            )
        )
    }
}
