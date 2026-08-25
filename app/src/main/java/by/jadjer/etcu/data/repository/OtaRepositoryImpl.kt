package by.jadjer.etcu.data.repository

import by.jadjer.etcu.data.ble.BleConstants
import by.jadjer.etcu.data.network.GitHubRelease
import by.jadjer.etcu.data.network.GitHubService
import by.jadjer.etcu.domain.repository.OtaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OtaRepositoryImpl : OtaRepository {
    private val service: GitHubService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(GitHubService::class.java)
    }

    override suspend fun getLatestRelease(): GitHubRelease? = withContext(Dispatchers.IO) {
        try {
            val response = service.getLatestRelease(BleConstants.GITHUB_OWNER, BleConstants.GITHUB_REPO)
            if (response.isSuccessful) response.body() else null
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
