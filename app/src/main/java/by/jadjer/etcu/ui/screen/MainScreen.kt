package by.jadjer.etcu.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.data.model.EcuTelemetry
import by.jadjer.etcu.ui.navigation.ScreenItem
import by.jadjer.etcu.ui.screen.ecu.*
import by.jadjer.etcu.ui.screen.errors.*
import by.jadjer.etcu.ui.screen.ota.*
import by.jadjer.etcu.ui.screen.scan_screen.ScanScreen
import by.jadjer.etcu.ui.screen.scan_screen.ScanViewModel
import by.jadjer.etcu.ui.screen.servo.*
import by.jadjer.etcu.ui.screen.settings.*
import by.jadjer.etcu.ui.screen.system.*

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as ETCUApplication
    val isConnected by app.container.bleRepository.isConnected.collectAsState()
    val connectionStatus by app.container.bleRepository.connectionState.collectAsState()

    if (!isConnected) {
        ScanScreen(
            viewModel = viewModel(factory = ScanViewModel.Factory(app)),
            onConnected = { /* Handled by isConnected state */ }
        )
    } else {
        MainScreenContent(
            app = app,
            connectionStatus = connectionStatus
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(
    app: ETCUApplication,
    connectionStatus: String
) {
    var currentScreen by remember { mutableStateOf<ScreenItem>(ScreenItem.Ecu) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ETCU Контроллер") },
                actions = {
                    Text(
                        text = connectionStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            MainNavigationBar(
                selectedScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (currentScreen) {
                ScreenItem.Ecu -> EcuScreen(viewModel(factory = EcuViewModel.Factory(app)))
                ScreenItem.Servo -> ServoScreen(viewModel(factory = ServoViewModel.Factory(app)))
                ScreenItem.System -> SystemScreen(viewModel(factory = SystemViewModel.Factory(app)))
                ScreenItem.Errors -> ErrorsScreen(viewModel(factory = ErrorsViewModel.Factory(app)))
                ScreenItem.Ota -> OtaScreen(viewModel(factory = OtaViewModel.Factory(app)))
                ScreenItem.Settings -> SettingsScreen(viewModel(factory = SettingsViewModel.Factory(app)))
            }
        }
    }
}

@Composable
private fun MainNavigationBar(
    selectedScreen: ScreenItem,
    onScreenSelected: (ScreenItem) -> Unit
) {
    val items = listOf(
        ScreenItem.Ecu, ScreenItem.Servo, ScreenItem.System,
        ScreenItem.Errors, ScreenItem.Ota, ScreenItem.Settings
    )
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = selectedScreen == screen,
                onClick = { onScreenSelected(screen) }
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme {
        Scaffold(
            bottomBar = {
                MainNavigationBar(
                    selectedScreen = ScreenItem.Ecu,
                    onScreenSelected = {}
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                EcuScreenContent(
                    telemetry = EcuTelemetry(isConnected = true, rpm = 1200, started = true)
                )
            }
        }
    }
}
