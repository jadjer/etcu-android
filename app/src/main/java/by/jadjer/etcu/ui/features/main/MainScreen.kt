package by.jadjer.etcu.ui.features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import by.jadjer.etcu.di.ViewModelFactory
import by.jadjer.etcu.ui.component.ErrorFab
import by.jadjer.etcu.ui.component.ErrorsBottomSheet
import by.jadjer.etcu.ui.component.MainNavigationBar
import by.jadjer.etcu.ui.component.MainTopAppBar
import by.jadjer.etcu.ui.features.device.DeviceViewModel
import by.jadjer.etcu.ui.features.device.screens.EcuScreen
import by.jadjer.etcu.ui.features.device.screens.ServoScreen
import by.jadjer.etcu.ui.features.device.screens.SettingsScreen
import by.jadjer.etcu.ui.features.device.screens.SystemScreen
import by.jadjer.etcu.ui.features.ota.OtaScreen
import by.jadjer.etcu.ui.features.ota.OtaViewModel
import by.jadjer.etcu.ui.features.scan.ScanScreen
import by.jadjer.etcu.ui.features.scan.ScanViewModel
import by.jadjer.etcu.ui.navigation.ScreenItem
import by.jadjer.etcu.ui.theme.ETCUTheme
import kotlinx.coroutines.launch

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No NavController provided")
}

val LocalPagerScrollEnabled = compositionLocalOf {
    mutableStateOf(true)
}

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
            val scanViewModel: ScanViewModel = viewModel(factory = ViewModelFactory)
            ScanScreen(viewModel = scanViewModel)
        }
        else -> {
            val deviceViewModel: DeviceViewModel = viewModel(factory = ViewModelFactory)
            MainScreenContent(deviceViewModel, connectionStatus)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ETCUTheme {
        Box(Modifier.fillMaxSize()) {
            Text("Main Screen Preview", Modifier.padding(16.dp))
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
            topBar = { MainTopAppBar(connectionStatus) },
            bottomBar = {
                if (currentScreen != ScreenItem.OTA) {
                    MainNavigationBar(
                        pagerState = pagerState,
                        navItems = navItems,
                        onScreenSelected = { screen ->
                            val index = navItems.indexOf(screen)
                            if (index != -1) {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (activeErrors.isNotEmpty() && currentScreen != ScreenItem.OTA) {
                    ErrorFab(errorCount = activeErrors.size, onClick = { showErrorsSheet = true })
                }
            }
        ) { innerPadding ->
            if (showErrorsSheet) {
                ErrorsBottomSheet(activeErrors, onDismiss = { showErrorsSheet = false })
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

@Composable
fun MainNavigationHost(
    navController: NavHostController,
    navItems: List<ScreenItem>,
    pagerState: PagerState,
    deviceViewModel: DeviceViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = MainNavRoutes.Routes.ROOT
        ) {
            composable(MainNavRoutes.Routes.ROOT) {
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
            composable(MainNavRoutes.Routes.OTA) {
                val otaViewModel: OtaViewModel = viewModel(factory = ViewModelFactory)
                OtaScreen(viewModel = otaViewModel)
            }
        }
    }
}

@Composable
private fun MainTabContent(item: ScreenItem, deviceViewModel: DeviceViewModel) {
    when (item.route) {
        MainNavRoutes.Tabs.ECU -> EcuScreen(deviceViewModel)
        MainNavRoutes.Tabs.SERVO -> ServoScreen(deviceViewModel)
        MainNavRoutes.Tabs.SYSTEM -> SystemScreen(deviceViewModel)
        MainNavRoutes.Tabs.SETTINGS -> {
            val navController = LocalNavController.current
            SettingsScreen(
                viewModel = deviceViewModel,
                onOtaClick = { navController.navigate(MainNavRoutes.Routes.OTA) }
            )
        }
    }
}
