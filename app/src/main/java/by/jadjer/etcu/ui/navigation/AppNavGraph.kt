package by.jadjer.etcu.ui.navigation

import android.Manifest
import android.content.Context
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
        mutableStateOf(checkPermissions(appContext, requiredPermissions))
    }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions) {
            app.container.bleRepository.autoConnect()
        }
    }

    val startRoute = remember(hasPermissions) {
        if (hasPermissions) NavScreen.Main.route else NavScreen.Permissions.route
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(NavScreen.Permissions.route) {
            PermissionsScreen(
                onPermissionsGranted = { hasPermissions = true }
            )
        }

        composable(NavScreen.Main.route) {
            MainScreen(viewModel = viewModel(factory = ViewModelFactory))
        }
    }
}

private fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
