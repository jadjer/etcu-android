package by.jadjer.etcu.di

import android.app.Application
import by.jadjer.etcu.data.ble.BLEManager
import by.jadjer.etcu.data.network.GitHubService
import by.jadjer.etcu.data.repository.BLERepositoryImpl
import by.jadjer.etcu.data.repository.OTARepositoryImpl
import by.jadjer.etcu.domain.repository.BLERepository
import by.jadjer.etcu.domain.repository.OTARepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AppContainer(private val _application: Application) {

    private val _bleManager by lazy { BLEManager(_application) }
    private val _appScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _githubService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "ETCU-Android-App")
                    .header("Accept", "application/vnd.github+json")
                    .build()
                chain.proceed(request)
            }
            .build()

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GitHubService::class.java)
    }

    val bleRepository: BLERepository by lazy {
        BLERepositoryImpl(_bleManager, _appScope)
    }

    val otaRepository: OTARepository by lazy {
        OTARepositoryImpl(_githubService, _application)
    }
}
