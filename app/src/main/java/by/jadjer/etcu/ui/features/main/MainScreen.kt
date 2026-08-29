package by.jadjer.etcu.ui.features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.ECUTelemetry
import by.jadjer.etcu.domain.model.SystemError
import by.jadjer.etcu.ui.component.ErrorsBlock
import by.jadjer.etcu.ui.features.device.DeviceViewModel
import by.jadjer.etcu.ui.features.device.EcuScreen
import by.jadjer.etcu.ui.features.device.EcuScreenContent
import by.jadjer.etcu.ui.features.device.ServoScreen
import by.jadjer.etcu.ui.features.device.SettingsScreen
import by.jadjer.etcu.ui.features.device.SystemScreen
import by.jadjer.etcu.ui.features.ota.OtaScreen
import by.jadjer.etcu.ui.features.scan.ScanScreen
import by.jadjer.etcu.ui.navigation.NavRoutes
import by.jadjer.etcu.ui.navigation.ScreenItem
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    val savedMac by viewModel.savedMac.collectAsState()
    
    val connectionStatus = connectionState.toDisplayString(savedMac ?: "")

    when {
        !connectionState.isActive && savedMac != null -> {
            ConnectingStubScreen(
                connectionStatus = connectionStatus,
                connectionState = connectionState,
                onRetryClick = viewModel::retryConnection,
                onResetClick = viewModel::clearLastMac
            )
        }
        !connectionState.isActive -> {
            ScanScreen(viewModel = viewModel(factory = MainViewModel.Factory))
        }
        else -> {
            val deviceViewModel: DeviceViewModel = viewModel(factory = MainViewModel.Factory)
            MainScreenContent(
                deviceViewModel = deviceViewModel,
                connectionStatus = connectionStatus
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(
    deviceViewModel: DeviceViewModel,
    connectionStatus: String
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = ScreenItem.fromRoute(currentRoute)
    
    val coroutineScope = rememberCoroutineScope()
    val telemetry by deviceViewModel.telemetry.collectAsState()
    val activeErrors = telemetry.activeErrors
    
    val navItems = ScreenItem.mainItems
    val pagerState = rememberPagerState(pageCount = { navItems.size })

    var showErrorsSheet by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalNavController provides navController) {
        Scaffold(
            topBar = {
                MainTopAppBar(connectionStatus = connectionStatus)
            },
            bottomBar = {
                if (currentScreen != ScreenItem.Ota) {
                    MainNavigationBar(
                        pagerState = pagerState,
                        navItems = navItems,
                        onScreenSelected = { screen ->
                            val index = navItems.indexOf(screen)
                            if (index != -1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (activeErrors.isNotEmpty()) {
                    ErrorFab(
                        errorCount = activeErrors.size,
                        onClick = { showErrorsSheet = true }
                    )
                }
            }
        ) { innerPadding ->
            if (showErrorsSheet) {
                ErrorsBottomSheet(
                    activeErrors = activeErrors,
                    onDismiss = { showErrorsSheet = false }
                )
            }

            MainNavigationHost(
                navController = navController,
                navItems = navItems,
                pagerState = pagerState,
                deviceViewModel = deviceViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopAppBar(connectionStatus: String) {
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
}

@Composable
private fun ErrorFab(errorCount: Int, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        BadgedBox(
            badge = {
                Badge { Text(errorCount.toString()) }
            }
        ) {
            Icon(Icons.Default.Warning, contentDescription = "Errors")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorsBottomSheet(
    activeErrors: List<SystemError>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Box(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            ErrorsBlock(activeErrors = activeErrors)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainNavigationHost(
    navController: NavHostController,
    navItems: List<ScreenItem>,
    pagerState: PagerState,
    deviceViewModel: DeviceViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = NavRoutes.MAIN
        ) {
            composable(NavRoutes.MAIN) {
                val pagerScrollEnabled = remember { mutableStateOf(true) }
                CompositionLocalProvider(LocalPagerScrollEnabled provides pagerScrollEnabled) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = pagerScrollEnabled.value
                    ) { page ->
                        MainTabContent(navItems[page], deviceViewModel)
                    }
                }
            }
            composable(ScreenItem.Ota.route) {
                OtaScreen(viewModel(factory = MainViewModel.Factory))
            }
        }
    }
}

@Composable
private fun MainTabContent(item: ScreenItem, deviceViewModel: DeviceViewModel) {
    when (item) {
        ScreenItem.Ecu -> EcuScreen(deviceViewModel)
        ScreenItem.Servo -> ServoScreen(deviceViewModel)
        ScreenItem.System -> SystemScreen(deviceViewModel)
        ScreenItem.Settings -> {
            val navController = LocalNavController.current
            SettingsScreen(
                viewModel = deviceViewModel,
                onOtaClick = { navController.navigate(ScreenItem.Ota.route) }
            )
        }
        else -> {}
    }
}

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No NavController provided")
}

val LocalPagerScrollEnabled = compositionLocalOf {
    mutableStateOf(true)
}

@Composable
private fun MainNavigationBar(
    pagerState: PagerState,
    navItems: List<ScreenItem>,
    onScreenSelected: (ScreenItem) -> Unit
) {
    NavigationBar {
        navItems.forEachIndexed { index, screen ->
            val title = stringResource(screen.titleResId)
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = title) },
                label = { Text(title) },
                selected = pagerState.currentPage == index,
                onClick = { onScreenSelected(screen) }
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MainScreenPreview() {
    val navItems = ScreenItem.mainItems
    val pagerState = rememberPagerState(pageCount = { navItems.size })

    MaterialTheme {
        Scaffold(
            bottomBar = {
                MainNavigationBar(
                    pagerState = pagerState,
                    navItems = navItems,
                    onScreenSelected = {}
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                EcuScreenContent(
                    telemetry = ECUTelemetry(isConnected = true, rpm = 1200, isStarted = true)
                )
            }
        }
    }
}
