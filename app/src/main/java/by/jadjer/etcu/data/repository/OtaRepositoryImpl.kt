package by.jadjer.etcu.data.repository

import by.jadjer.etcu.data.model.GitHubReleaseDto
import by.jadjer.etcu.data.network.GitHubService
import by.jadjer.etcu.data.network.NetworkConstants
import by.jadjer.etcu.domain.model.FirmwareRelease
import by.jadjer.etcu.domain.repository.OtaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OtaRepositoryImpl(
    private val service: GitHubService
) : OtaRepository {

    override suspend fun getLatestRelease(): FirmwareRelease? = withContext(Dispatchers.IO) {
        try {
            val response = service.getLatestRelease(NetworkConstants.GITHUB_OWNER, NetworkConstants.GITHUB_REPO)
            if (response.isSuccessful) {
                val body = response.body()
                val asset = body?.assets?.find { it.name.endsWith(".bin") } ?: body?.assets?.firstOrNull()
                
                if (body != null && asset != null) {
                    FirmwareRelease(
                        version = body.tagName,
                        name = body.name,
                        downloadUrl = asset.downloadUrl,
                        size = asset.size
                    )
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun downloadFirmware(url: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val response = service.downloadFile(url)
            if (response.isSuccessful) {
                response.body()?.bytes()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
