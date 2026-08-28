package by.jadjer.etcu.data.repository

import android.content.Context
import by.jadjer.etcu.R
import by.jadjer.etcu.data.network.GitHubService
import by.jadjer.etcu.data.network.NetworkConstants
import by.jadjer.etcu.domain.model.FirmwareRelease
import by.jadjer.etcu.domain.repository.OTARepository
import by.jadjer.etcu.domain.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class OTARepositoryImpl(
    private val service: GitHubService,
    private val context: Context
) : OTARepository {

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
                    Resource.Error(context.getString(R.string.ota_error_no_release))
                }
            } else {
                Resource.Error(context.getString(R.string.ota_error_server, response.code()))
            }
        } catch (e: Exception) {
            Resource.Error(context.getString(R.string.ota_error_network, e.message ?: ""))
        }
    }

    override suspend fun downloadFirmware(
        url: String,
        expectedSize: Long,
        onProgress: (Float) -> Unit
    ): Resource<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val response = service.downloadFile(url)
            if (response.isSuccessful) {
                val body = response.body() ?: return@withContext Resource.Error(context.getString(R.string.ota_error_empty))
                
                // Используем размер из заголовка, если он есть, иначе берем переданный ожидаемый размер
                val contentLength = if (body.contentLength() > 0) body.contentLength() else expectedSize
                
                val inputStream = body.byteStream()
                val outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalBytesRead: Long = 0

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    
                    if (contentLength > 0) {
                        onProgress(totalBytesRead.toFloat() / contentLength)
                    }
                }

                Resource.Success(outputStream.toByteArray())
            } else {
                Resource.Error(context.getString(R.string.ota_error_server, response.code()))
            }
        } catch (e: Exception) {
            Resource.Error(context.getString(R.string.ota_error_network, e.message ?: ""))
        }
    }
}
