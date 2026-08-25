package by.jadjer.etcu.domain.repository

import by.jadjer.etcu.domain.model.FirmwareRelease

interface OtaRepository {
    suspend fun getLatestRelease(): FirmwareRelease?
    suspend fun downloadFirmware(url: String): ByteArray?
}
