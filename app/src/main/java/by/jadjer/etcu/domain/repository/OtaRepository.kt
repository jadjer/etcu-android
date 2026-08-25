package by.jadjer.etcu.domain.repository

import by.jadjer.etcu.data.network.GitHubRelease

interface OtaRepository {
    suspend fun getLatestRelease(): GitHubRelease?
    suspend fun downloadFirmware(url: String): ByteArray?
}
