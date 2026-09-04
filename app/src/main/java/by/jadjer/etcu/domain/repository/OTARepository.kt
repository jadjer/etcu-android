package by.jadjer.etcu.domain.repository

import by.jadjer.etcu.domain.model.ota.FirmwareRelease
import by.jadjer.etcu.domain.util.Resource

interface OTARepository {
    suspend fun getLatestRelease(): Resource<FirmwareRelease>
    suspend fun downloadFirmware(
        url: String, 
        expectedSize: Long = -1,
        onProgress: (Float) -> Unit
    ): Resource<ByteArray>
}
