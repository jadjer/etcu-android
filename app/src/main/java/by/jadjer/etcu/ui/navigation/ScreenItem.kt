package by.jadjer.etcu.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector
import by.jadjer.etcu.R
import by.jadjer.etcu.ui.features.main.MainNavRoutes

sealed class ScreenItem(val titleResId: Int, val icon: ImageVector, val route: String) {
    data object ECU :
        ScreenItem(R.string.nav_ecu, Icons.Default.TwoWheeler, MainNavRoutes.Tabs.ECU)

    data object Servo : ScreenItem(
        R.string.nav_servo,
        Icons.Default.Engineering,
        MainNavRoutes.Tabs.SERVO
    )

    data object System :
        ScreenItem(R.string.nav_system, Icons.Default.Dns, MainNavRoutes.Tabs.SYSTEM)

    data object Settings :
        ScreenItem(R.string.nav_settings, Icons.Default.Settings, MainNavRoutes.Tabs.SETTINGS)

    data object OTA :
        ScreenItem(R.string.nav_ota, Icons.Default.SystemUpdate, MainNavRoutes.Routes.OTA)

    companion object {
        val mainItems = listOf(ECU, Servo, System, Settings)
        fun fromRoute(route: String?): ScreenItem? {
            return when (route) {
                MainNavRoutes.Routes.OTA -> OTA
                MainNavRoutes.Routes.ROOT -> null
                else -> null
            }
        }
    }
}
