package by.jadjer.etcu.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.ui.graphics.vector.ImageVector
import by.jadjer.etcu.R

sealed class ScreenItem(val titleResId: Int, val icon: ImageVector, val route: String) {
    data object Ecu : ScreenItem(R.string.nav_ecu, Icons.Default.DirectionsCar, NavRoutes.ECU)
    data object Servo : ScreenItem(R.string.nav_servo, Icons.Default.SettingsInputComponent, NavRoutes.SERVO)
    data object System : ScreenItem(R.string.nav_system, Icons.Default.Dns, NavRoutes.SYSTEM)
    data object Ota : ScreenItem(R.string.nav_ota, Icons.Default.SystemUpdate, NavRoutes.OTA)
    data object Settings : ScreenItem(R.string.nav_settings, Icons.Default.Settings, NavRoutes.SETTINGS)

    companion object {
        val mainItems = listOf(Ecu, Servo, System, Settings)
        private val allItems = mainItems + Ota
        
        fun fromRoute(route: String?): ScreenItem? = allItems.find { it.route == route }
    }
}
