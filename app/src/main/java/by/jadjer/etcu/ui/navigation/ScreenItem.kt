package by.jadjer.etcu.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.ui.graphics.vector.ImageVector
import by.jadjer.etcu.R

sealed class ScreenItem(val titleResId: Int, val icon: ImageVector) {
    object Ecu : ScreenItem(R.string.nav_ecu, Icons.Default.DirectionsCar)
    object Servo : ScreenItem(R.string.nav_servo, Icons.Default.SettingsInputComponent)
    object System : ScreenItem(R.string.nav_system, Icons.Default.Dns)
    object Ota : ScreenItem(R.string.nav_ota, Icons.Default.SystemUpdate)
    object Settings : ScreenItem(R.string.nav_settings, Icons.Default.Settings)
}
