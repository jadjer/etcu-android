package by.jadjer.etcu.ui.screen.scan

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.DiscoveredDevice
import by.jadjer.etcu.ui.screen.toDisplayString

@Composable
fun ScanScreen(viewModel: ScanViewModel) {
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    
    val connectionStatus = connectionState.toDisplayString()

    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        viewModel.startScanning()
        onDispose {
            viewModel.stopScanning()
        }
    }

    LaunchedEffect(connectionState) {
        if (connectionState.isError) {
            snackbarHostState.showSnackbar(
                message = connectionStatus,
                duration = SnackbarDuration.Short
            )
        }
    }

    ScanScreenContent(
        discoveredDevices = discoveredDevices,
        isScanning = isScanning,
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
    discoveredDevices: List<DiscoveredDevice>,
    isScanning: Boolean,
    snackbarHostState: SnackbarHostState,
    onRefreshClick: () -> Unit,
    onDeviceClick: (DiscoveredDevice) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_title)) },
                actions = {
                    IconButton(onClick = onRefreshClick) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.scan_controllers_detected),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleSmall
                )

                if (discoveredDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                stringResource(R.string.scan_not_found),
                                modifier = Modifier.padding(24.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    discoveredDevices.forEach { device ->
                        DeviceItem(device) { onDeviceClick(device) }
                    }
                }
            }

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.btn_bluetooth_settings))
            }
        }
    }
}

@Composable
private fun DeviceItem(device: DiscoveredDevice, onClick: () -> Unit) {
    val deviceName = device.name.ifEmpty { stringResource(R.string.unknown) }
    ListItem(
        headlineContent = { Text(deviceName) },
        supportingContent = { 
            Column {
                Text(device.macAddress)
                if (device.rssi != 0) {
                    Text(
                        text = stringResource(R.string.scan_signal_info, device.rssi, device.distance),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        trailingContent = {
            if (device.rssi != 0) {
                SignalIcon(rssi = device.rssi)
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider()
}

@Composable
private fun SignalIcon(rssi: Int) {
    val color = when {
        rssi > -60 -> Color(0xFF4CAF50) // Green
        rssi > -70 -> Color(0xFF8BC34A) // Light Green
        rssi > -80 -> Color(0xFFFFC107) // Yellow
        else -> Color(0xFFF44336) // Red
    }
    
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color, shape = androidx.compose.foundation.shape.CircleShape)
    )
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    MaterialTheme {
        ScanScreenContent(
            discoveredDevices = listOf(
                DiscoveredDevice(
                    isPaired = false,
                    name = "ETCU-New-Device",
                    macAddress = "11:22:33:44:55:66",
                    rssi = -55,
                    distance = 1.2
                ),
                DiscoveredDevice(
                    isPaired = false,
                    name = "Unknown",
                    macAddress = "77:88:99:00:11:22",
                    rssi = -85,
                    distance = 15.5
                )
            ),
            isScanning = false,
            snackbarHostState = remember { SnackbarHostState() },
            onRefreshClick = {},
            onDeviceClick = {}
        )
    }
}
