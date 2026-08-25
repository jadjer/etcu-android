package by.jadjer.etcu.data.repository

import by.jadjer.etcu.data.network.GitHubService
import by.jadjer.etcu.data.network.NetworkConstants
import by.jadjer.etcu.domain.model.FirmwareRelease
import by.jadjer.etcu.domain.repository.OtaRepository
import by.jadjer.etcu.domain.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OtaRepositoryImpl(
    private val service: GitHubService
) : OtaRepository {

    override suspend fun getLatestRelease(): Resource<FirmwareRelease> = withContext(Dispatchers.IO) {
        try {
            val response = service.getLatestRelease(NetworkConstants.GITHUB_OWNER, NetworkConstants.GITHUB_REPO)
            if (response.isSuccessful) {
                val body = response.body()
                val asset = body?.assets?.find { it.name.endsWith(".bin") } ?: body?.assets?.firstOrNull()
                
                if (body != null && asset != null) {
                    Resource.Success(FirmwareRelease(
                        version = body.tagName,
                        name = body.name,
                        downloadUrl = asset.downloadUrl,
                        size = asset.size
                    ))
                } else {
                    Resource.Error("Релиз не найден или отсутствует .bin файл")
                }
            } else {
                Resource.Error("Ошибка сервера: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка сети: ${e.message}", e)
        }
    }

    override suspend fun downloadFirmware(url: String): Resource<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val response = service.downloadFile(url)
            if (response.isSuccessful) {
                val bytes = response.body()?.bytes()
                if (bytes != null) {
                    Resource.Success(bytes)
                } else {
                    Resource.Error("Пустой ответ от сервера")
                }
            } else {
                Resource.Error("Ошибка загрузки: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка сети: ${e.message}", e)
        }
    }
}
