package by.jadjer.etcu.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.domain.model.BleControlData
import by.jadjer.etcu.domain.model.SystemInfo
import by.jadjer.etcu.ui.component.TelemetryRow

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOtaClick: () -> Unit
) {
    val controlData by viewModel.controlData.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()

    SettingsScreenContent(
        controlData = controlData,
        systemInfo = systemInfo,
        onSyncChange = { viewModel.updateSyncEnabled(it) },
        onOffsetChange = { viewModel.updateAcceleratorOffset(it.toInt()) },
        onDisconnectClick = { viewModel.disconnect() },
        onForgetClick = { viewModel.forgetDevice() },
        onOtaClick = onOtaClick
    )
}

@Composable
fun SettingsScreenContent(
    controlData: BleControlData,
    systemInfo: SystemInfo,
    onSyncChange: (Boolean) -> Unit,
    onOffsetChange: (Float) -> Unit,
    onDisconnectClick: () -> Unit,
    onForgetClick: () -> Unit,
    onOtaClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Информация об устройстве", style = MaterialTheme.typography.titleLarge)

        TelemetryRow(
            label = "Версия платы",
            value = systemInfo.boardVersion
        )

        TelemetryRow(
            label = "Дата прошивки",
            value = systemInfo.buildDate
        )

        TelemetryRow(
            label = "Версия прошивки",
            value = systemInfo.firmwareVersion
        )

        HorizontalDivider()

        Text("Управление", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Синхронизация")
                    Switch(
                        checked = controlData.syncEnabled,
                        onCheckedChange = onSyncChange
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Смещение акселератора: ${controlData.acceleratorOffset}")
                Slider(
                    value = controlData.acceleratorOffset.toFloat(),
                    onValueChange = onOffsetChange,
                    valueRange = 0f..1000f,
                    steps = 100
                )
            }
        }

        HorizontalDivider()

        Text("Настройки устройства", style = MaterialTheme.typography.titleLarge)
        
        Button(
            onClick = onDisconnectClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Отключить устройство")
        }

        Button(
            onClick = onForgetClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Забыть устройство")
        }
        
        OutlinedButton(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Запустить калибровку")
        }
        
        OutlinedButton(
            onClick = onOtaClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Проверить обновления")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreenContent(
            controlData = BleControlData(
                syncEnabled = true,
                acceleratorOffset = 150
            ),
            systemInfo = SystemInfo(
                boardVersion = "v2.1",
                buildDate = "2023-08-20",
                firmwareVersion = "1.2.3"
            ),
            onSyncChange = {},
            onOffsetChange = {},
            onDisconnectClick = {},
            onForgetClick = {},
            onOtaClick = {}
        )
    }
}
