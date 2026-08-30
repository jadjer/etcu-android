package by.jadjer.etcu.ui.features.main

object MainNavRoutes {
    object Routes {
        const val ROOT = "main_root" // Точка, где живет HorizontalPager
        const val OTA = "main_ota"   // Точка, куда переходят из настроек
    }

    // 2. Идентификаторы вкладок (То, что переключает HorizontalPager по индексу)
    object Tabs {
        const val ECU = "tab_ecu"
        const val SERVO = "tab_servo"
        const val SYSTEM = "tab_system"
        const val SETTINGS = "tab_settings"
    }
}
