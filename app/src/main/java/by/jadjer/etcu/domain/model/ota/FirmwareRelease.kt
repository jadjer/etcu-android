package by.jadjer.etcu.domain.model.ota

data class FirmwareRelease(
    val version: String,
    val name: String,
    val description: String?,
    val downloadUrl: String,
    val size: Long
)
