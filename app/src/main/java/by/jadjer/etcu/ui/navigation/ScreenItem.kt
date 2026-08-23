package by.jadjer.etcu.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ScreenItem(val title: String, val icon: ImageVector) {
    object Ecu : ScreenItem("ECU", Icons.Default.DirectionsCar)
    object Servo : ScreenItem("Servo", Icons.Default.SettingsInputComponent)
    object System : ScreenItem("System", Icons.Default.Dns)
    object Errors : ScreenItem("Errors", Icons.Default.Warning)
    object Ota : ScreenItem("OTA", Icons.Default.SystemUpdate)
    object Settings : ScreenItem("Settings", Icons.Default.Settings)
}
