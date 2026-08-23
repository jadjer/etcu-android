package by.jadjer.etcu.ui.navigation

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.data.source.ble.BleConstants
import by.jadjer.etcu.ui.screen.MainScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val app = context.applicationContext as ETCUApplication
    val navController = rememberNavController()

    val permissions = listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val permissionState = rememberMultiplePermissionsState(permissions)

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
        app.container.bleRepository.autoConnect()
    }

    NavHost(
        navController = navController,
        startDestination = BleConstants.ROUTE_MAIN
    ) {
        composable(BleConstants.ROUTE_MAIN) {
            MainScreen()
        }
    }
}
