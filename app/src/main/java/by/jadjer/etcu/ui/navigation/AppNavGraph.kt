package by.jadjer.etcu.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.di.ViewModelFactory
import by.jadjer.etcu.ui.features.main.MainScreen
import by.jadjer.etcu.ui.features.permission.PermissionsScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppNavGraph() {
    val appContext = LocalContext.current.applicationContext
    val app = appContext as ETCUApplication
    val navController = rememberNavController()

    val requiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
    )

    var hasPermissions by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(appContext, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions) {
            app.container.bleRepository.autoConnect()
        }
    }

    val startRoute = remember(hasPermissions) {
        if (hasPermissions) NavRoutes.MAIN else NavRoutes.PERMISSIONS
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(NavRoutes.PERMISSIONS) {
            PermissionsScreen(
                onPermissionsGranted = {
                    hasPermissions = true
                }
            )
        }

        composable(NavRoutes.MAIN) {
            MainScreen(viewModel = viewModel(factory = ViewModelFactory))
        }
    }
}
