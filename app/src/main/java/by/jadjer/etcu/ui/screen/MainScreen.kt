package by.jadjer.etcu.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.EcuTelemetry
import by.jadjer.etcu.ui.component.ErrorsBlock
import by.jadjer.etcu.ui.navigation.ScreenItem
import by.jadjer.etcu.ui.screen.ecu.EcuScreen
import by.jadjer.etcu.ui.screen.ecu.EcuScreenContent
import by.jadjer.etcu.ui.screen.ecu.EcuViewModel
import by.jadjer.etcu.ui.screen.ota.OtaScreen
import by.jadjer.etcu.ui.screen.ota.OtaViewModel
import by.jadjer.etcu.ui.screen.scan.ScanScreen
import by.jadjer.etcu.ui.screen.scan.ScanViewModel
import by.jadjer.etcu.ui.screen.servo.ServoScreen
import by.jadjer.etcu.ui.screen.servo.ServoViewModel
import by.jadjer.etcu.ui.screen.settings.SettingsScreen
import by.jadjer.etcu.ui.screen.settings.SettingsViewModel
import by.jadjer.etcu.ui.screen.system.SystemScreen
import by.jadjer.etcu.ui.screen.system.SystemViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val isConnected by viewModel.isConnected.collectAsState()
    val connectionStatus by viewModel.connectionState.collectAsState()
    val savedMac by viewModel.savedMac.collectAsState()

    if (!isConnected) {
        if (savedMac != null) {
            ConnectingStubScreen(
                connectionStatus = connectionStatus,
                onRetryClick = { viewModel.retryConnection() },
                onResetClick = { viewModel.clearLastMac() }
            )
        } else {
            val context = LocalContext.current
            val app = context.applicationContext as ETCUApplication
            ScanScreen(
                viewModel = viewModel(factory = ScanViewModel.Factory(app))
            )
        }
    } else {
        MainScreenContent(
            viewModel = viewModel,
            connectionStatus = connectionStatus
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(
    viewModel: MainViewModel,
    connectionStatus: String
) {
    var currentScreen by remember { mutableStateOf<ScreenItem>(ScreenItem.Ecu) }
    val telemetry by viewModel.telemetry.collectAsState()
    val activeErrors = telemetry.activeErrors
    
    var showErrorsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    val context = LocalContext.current
    val app = context.applicationContext as ETCUApplication

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
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
        },
        floatingActionButton = {
            if (activeErrors.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showErrorsSheet = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    BadgedBox(
                        badge = {
                            Badge { Text(activeErrors.size.toString()) }
                        }
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Errors")
                    }
                }
            }
        }
    ) { innerPadding ->
        if (showErrorsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showErrorsSheet = false },
                sheetState = sheetState
            ) {
                Box(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                    ErrorsBlock(activeErrors = activeErrors)
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (currentScreen) {
                ScreenItem.Ecu -> EcuScreen(viewModel(factory = EcuViewModel.Factory(app)))
                ScreenItem.Servo -> ServoScreen(viewModel(factory = ServoViewModel.Factory(app)))
                ScreenItem.System -> SystemScreen(viewModel(factory = SystemViewModel.Factory(app)))
                ScreenItem.Ota -> OtaScreen(viewModel(factory = OtaViewModel.Factory(app)))
                ScreenItem.Settings -> SettingsScreen(
                    viewModel = viewModel(factory = SettingsViewModel.Factory(app)),
                    onOtaClick = { currentScreen = ScreenItem.Ota }
                )
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
        ScreenItem.Ecu,
        ScreenItem.Servo,
        ScreenItem.System,
        ScreenItem.Settings,
    )
    NavigationBar {
        items.forEach { screen ->
            val title = stringResource(screen.titleResId)
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = title) },
                label = { Text(title) },
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
                    telemetry = EcuTelemetry(isConnected = true, rpm = 1200, isStarted = true)
                )
            }
        }
    }
}
