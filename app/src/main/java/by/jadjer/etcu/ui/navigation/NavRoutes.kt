package by.jadjer.etcu.ui.navigation

sealed class NavScreen(val route: String) {
    data object Permissions : NavScreen("permissions")
    data object Main : NavScreen("main")
}
