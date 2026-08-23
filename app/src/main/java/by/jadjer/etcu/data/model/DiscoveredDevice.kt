package by.jadjer.etcu.data.model

data class DiscoveredDevice(
    val name: String,
    val macAddress: String,
    val isPaired: Boolean = false
)
