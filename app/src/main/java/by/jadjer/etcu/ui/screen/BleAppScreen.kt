package by.jadjer.etcu.ui.screen

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.data.ble.BleManager
import by.jadjer.etcu.ui.component.ErrorsBlock
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BleAppScreen() {
    val context = LocalContext.current
    val bleManager = remember { BleManager(context.applicationContext) }

    val connectionState by bleManager.connectionState.collectAsState()
    val receivedData by bleManager.receivedData.collectAsState()

    var macAddress by remember { mutableStateOf("00:11:22:33:44:55") } // Замените на MAC вашего BLE-модуля

    // Список необходимых разрешений в зависимости от версии Android
    val blePermissions =
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )

    val permissionState = rememberMultiplePermissionsState(permissions = blePermissions)
    val telemetry by bleManager.telemetryState.collectAsState()

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (permissionState.allPermissionsGranted) {
            OutlinedTextField(
                value = macAddress,
                onValueChange = { macAddress = it },
                label = { Text("MAC Адрес устройства") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { bleManager.connect(macAddress) }) {
                    Text("Подключить")
                }
                Button(onClick = { bleManager.disconnect() }) {
                    Text("Отключить")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "Статус: $connectionState", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = receivedData, style = MaterialTheme.typography.titleMedium)

            Text("Обороты ДВС (RPM): ${telemetry.ecu.rpm}")
            Text("Положение дросселя: ${telemetry.throttlePosition} / 1000")
            Text("Напряжение серво: ${telemetry.servo.voltage} V")

            ErrorsBlock(telemetry.activeErrors)

        } else {
            Text(text = "Для работы BLE необходимы разрешения")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                Text("Предоставить разрешения")
            }
        }
    }
}
