package by.jadjer.etcu.ui.screen.servo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.data.model.ServoTelemetry
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.TelemetryRow

@Composable
fun ServoScreen(viewModel: ServoViewModel) {
    val telemetry by viewModel.servoTelemetry.collectAsState()
    ServoScreenContent(telemetry)
}

@Composable
fun ServoScreenContent(telemetry: ServoTelemetry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        StatusIndicator(
            label = "Связь с приводом",
            isActive = telemetry.isConnected,
            activeText = "Есть связь",
            inactiveText = "Нет связи"
        )
        
        TelemetryRow(
            label = "Позиция",
            value = telemetry.position.toString(),
            unit = "/ 1024"
        )
        
        TelemetryRow(
            label = "Нагрузка",
            value = telemetry.load.toString(),
            unit = "%"
        )
        
        TelemetryRow(
            label = "Ток",
            value = telemetry.current.toString(),
            unit = "мА",
            icon = Icons.Default.Bolt
        )
        
        TelemetryRow(
            label = "Напряжение",
            value = telemetry.voltage.toString(),
            unit = "В"
        )
        
        TelemetryRow(
            label = "Температура",
            value = telemetry.temperature.toString(),
            unit = "°C",
            icon = Icons.Default.DeviceThermostat
        )
        
        StatusIndicator(
            label = "Движение",
            isActive = telemetry.isMoved,
            activeText = "В движении",
            inactiveText = "Статичен"
        )
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
                load = 25,
                current = 450,
                voltage = 12,
                temperature = 38,
                isMoved = true
            )
        )
    }
}
