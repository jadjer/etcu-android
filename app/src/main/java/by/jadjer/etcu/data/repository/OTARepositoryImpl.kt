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
                val code = response.code()
                val errorBody = response.errorBody()?.string() ?: ""
                val rateRemaining = response.headers()["X-RateLimit-Remaining"]
                
                val message = if (code == 403 && rateRemaining == "0") {
                    context.getString(R.string.ota_error_github_rate_limit)
                } else {
                    context.getString(R.string.ota_error_github_generic, code, errorBody)
                }
                Resource.Error(message)
            }
        } catch (e: Exception) {
            Resource.Error(context.getString(R.string.ota_error_network, e.message ?: ""), e)
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
                
                val contentLength = if (body.contentLength() > 0L) body.contentLength() else expectedSize
                
                val inputStream = body.byteStream()
                val outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalBytesRead: Long = 0

                inputStream.use { input ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        if (contentLength > 0) {
                            onProgress(totalBytesRead.toFloat() / contentLength)
                        }
                    }
                }

                Resource.Success(outputStream.toByteArray())
            } else {
                val code = response.code()
                val errorBody = response.errorBody()?.string() ?: ""
                Resource.Error(context.getString(R.string.ota_error_download_generic, code, errorBody))
            }
        } catch (e: Exception) {
            Resource.Error(context.getString(R.string.ota_error_network, e.message ?: ""), e)
        }
    }
}
