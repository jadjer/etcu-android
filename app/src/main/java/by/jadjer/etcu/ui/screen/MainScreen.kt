package by.jadjer.etcu.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import by.jadjer.etcu.ETCUApplication
import kotlinx.coroutines.launch
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.ECUTelemetry
import by.jadjer.etcu.domain.model.SystemError
import by.jadjer.etcu.ui.component.ErrorsBlock
import by.jadjer.etcu.ui.navigation.NavRoutes
import by.jadjer.etcu.ui.navigation.ScreenItem
import by.jadjer.etcu.ui.screen.ecu.EcuScreen
import by.jadjer.etcu.ui.screen.ecu.EcuScreenContent
import by.jadjer.etcu.ui.screen.ecu.ECUViewModel
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
    val connectionState by viewModel.connectionState.collectAsState()
    val savedMac by viewModel.savedMac.collectAsState()
    
    val connectionStatus = connectionState.toDisplayString(savedMac ?: "")

    if (!connectionState.isActive) {
        if (savedMac != null) {
            ConnectingStubScreen(
                connectionStatus = connectionStatus,
                connectionState = connectionState,
                onRetryClick = { viewModel.retryConnection() },
                onResetClick = { viewModel.clearLastMac() }
            )
        }
 else {
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
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = ScreenItem.fromRoute(currentRoute)
    
    val coroutineScope = rememberCoroutineScope()
    val telemetry by viewModel.telemetry.collectAsState()
    val activeErrors = telemetry.activeErrors
    
    val navItems = ScreenItem.mainItems
    val pagerState = rememberPagerState(pageCount = { navItems.size })

    var showErrorsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    CompositionLocalProvider(LocalNavController provides navController) {
        MainScaffold(
            connectionStatus = connectionStatus,
            currentScreen = currentScreen,
            activeErrors = activeErrors,
            navItems = navItems,
            pagerState = pagerState,
            onShowErrors = { showErrorsSheet = true },
            onTabSelected = { screen ->
                val index = navItems.indexOf(screen)
                if (index != -1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            }
        ) {
            if (showErrorsSheet) {
                ErrorsBottomSheet(
                    activeErrors = activeErrors,
                    sheetState = sheetState,
                    onDismiss = { showErrorsSheet = false }
                )
            }

            MainNavigationHost(
                navController = navController,
                navItems = navItems,
                pagerState = pagerState,
                modifier = Modifier.padding(it)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    connectionStatus: String,
    currentScreen: ScreenItem?,
    activeErrors: List<SystemError>,
    navItems: List<ScreenItem>,
    pagerState: PagerState,
    onShowErrors: () -> Unit,
    onTabSelected: (ScreenItem) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            MainTopAppBar(connectionStatus = connectionStatus)
        },
        bottomBar = {
            if (currentScreen != ScreenItem.Ota) {
                MainNavigationBar(
                    pagerState = pagerState,
                    navItems = navItems,
                    onScreenSelected = onTabSelected
                )
            }
        },
        floatingActionButton = {
            if (activeErrors.isNotEmpty()) {
                ErrorFab(
                    errorCount = activeErrors.size,
                    onClick = onShowErrors
                )
            }
        },
        content = content
    )
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
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as ETCUApplication

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
                        MainTabContent(navItems[page], app)
                    }
                }
            }
            composable(ScreenItem.Ota.route) {
                OtaScreen(viewModel(factory = OtaViewModel.Factory(app)))
            }
        }
    }
}

@Composable
private fun MainTabContent(item: ScreenItem, app: ETCUApplication) {
    when (item) {
        ScreenItem.Ecu -> EcuScreen(viewModel(factory = ECUViewModel.Factory(app)))
        ScreenItem.Servo -> ServoScreen(viewModel(factory = ServoViewModel.Factory(app)))
        ScreenItem.System -> SystemScreen(viewModel(factory = SystemViewModel.Factory(app)))
        ScreenItem.Settings -> {
            val navController = LocalNavController.current
            SettingsScreen(
                viewModel = viewModel(factory = SettingsViewModel.Factory(app)),
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
