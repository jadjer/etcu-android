package by.jadjer.etcu.ui.screen.scan_screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.data.model.DiscoveredDevice

@Composable
fun ScanScreen(viewModel: ScanViewModel, onConnected: () -> Unit) {
    val pairedDevices by viewModel.pairedDevices.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val connectionStatus by viewModel.connectionState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.updatePairedDevices()
        viewModel.startScanning()
    }

    LaunchedEffect(connectionStatus) {
        if (connectionStatus != "Отключено") {
            snackbarHostState.showSnackbar(
                message = connectionStatus,
                duration = SnackbarDuration.Short
            )
        }
    }

    ScanScreenContent(
        pairedDevices = pairedDevices,
        discoveredDevices = discoveredDevices,
        snackbarHostState = snackbarHostState,
        onRefreshClick = { viewModel.startScanning() },
        onDeviceClick = { device ->
            viewModel.connect(device)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreenContent(
    pairedDevices: List<DiscoveredDevice>,
    discoveredDevices: List<DiscoveredDevice>,
    snackbarHostState: SnackbarHostState,
    onRefreshClick: () -> Unit,
    onDeviceClick: (DiscoveredDevice) -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Выберите устройство") },
                actions = {
                    IconButton(onClick = onRefreshClick) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (pairedDevices.isNotEmpty()) {
                item {
                    Text(
                        "Сопряженные устройства",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                items(pairedDevices) { device ->
                    DeviceItem(device) { onDeviceClick(device) }
                }
            }

            item {
                Text(
                    "Доступные устройства",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            if (discoveredDevices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            } else {
                items(discoveredDevices) { device ->
                    DeviceItem(device) { onDeviceClick(device) }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Настройки Bluetooth")
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(device: DiscoveredDevice, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(device.name) },
        supportingContent = { Text(device.macAddress) },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider()
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    MaterialTheme {
        ScanScreenContent(
            pairedDevices = listOf(
                DiscoveredDevice("ETCU-Controller-1", "AA:BB:CC:DD:EE:FF", true)
            ),
            discoveredDevices = listOf(
                DiscoveredDevice("ETCU-New-Device", "11:22:33:44:55:66", false),
                DiscoveredDevice("Unknown", "77:88:99:00:11:22", false)
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onRefreshClick = {},
            onDeviceClick = {}
        )
    }
}
