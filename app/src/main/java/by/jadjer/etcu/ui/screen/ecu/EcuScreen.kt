package by.jadjer.etcu.ui.screen.ecu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.data.model.EcuTelemetry
import by.jadjer.etcu.ui.component.StatusIndicator
import by.jadjer.etcu.ui.component.TelemetryRow

@Composable
fun EcuScreen(viewModel: EcuViewModel) {
    val telemetry by viewModel.ecuTelemetry.collectAsState()
    EcuScreenContent(telemetry)
}

@Composable
fun EcuScreenContent(telemetry: EcuTelemetry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        StatusIndicator(
            label = "Связь с ЭБУ",
            isActive = telemetry.isConnected,
            activeText = "Есть связь",
            inactiveText = "Нет связи"
        )
        
        TelemetryRow(
            label = "Обороты двигателя",
            value = telemetry.rpm.toString(),
            unit = "RPM",
            icon = Icons.Default.Timer
        )
        
        TelemetryRow(
            label = "Скорость автомобиля",
            value = telemetry.speed.toString(),
            unit = "км/ч",
            icon = Icons.Default.Speed
        )
        
        TelemetryRow(
            label = "Положение TPS",
            value = telemetry.tps.toString(),
            unit = "/ 1000",
            icon = Icons.Default.DirectionsCar
        )
        
        StatusIndicator(
            label = "Двигатель запущен",
            isActive = telemetry.isStarted,
            activeText = "Запущен",
            inactiveText = "Остановлен"
        )
        
        StatusIndicator(
            label = "Статус сцепления (ECU)",
            isActive = telemetry.isClutchEnabled,
            activeText = "Нажато",
            inactiveText = "Отпущено"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EcuScreenPreview() {
    MaterialTheme {
        EcuScreenContent(
            telemetry = EcuTelemetry(
                isConnected = true,
                rpm = 2500,
                speed = 60,
                tps = 150,
                isStarted = true,
                isClutchEnabled = false
            )
        )
    }
}
