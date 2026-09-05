package by.jadjer.etcu.domain.model.ble

data class DiscoveredDevice(
    val isPaired: Boolean = false,
    val name: String,
    val macAddress: String,
    val rssi: Int = 0,
)
