package by.jadjer.etcu.domain.model

data class SystemInfo(
    val boardVersion: String = "Unknown",
    val buildDate: String = "Unknown",
    val firmwareVersion: String = "0.0.0"
)
