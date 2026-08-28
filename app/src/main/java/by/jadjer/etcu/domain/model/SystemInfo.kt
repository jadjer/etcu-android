package by.jadjer.etcu.domain.model

data class SystemInfo(
    val buildDate: String = "Unknown",
    val boardVersion: String = "Unknown",
    val firmwareVersion: String = "0.0.0"
)
