package by.jadjer.etcu.domain.repository

import by.jadjer.etcu.domain.model.FirmwareRelease
import by.jadjer.etcu.domain.util.Resource

interface OtaRepository {
    suspend fun getLatestRelease(): Resource<FirmwareRelease>
    suspend fun downloadFirmware(url: String): Resource<ByteArray>
}
