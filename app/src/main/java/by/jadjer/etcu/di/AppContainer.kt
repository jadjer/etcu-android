package by.jadjer.etcu.di

import android.app.Application
import by.jadjer.etcu.data.ble.BLEManager
import by.jadjer.etcu.data.local.BLEPreferenceManager
import by.jadjer.etcu.data.network.GitHubService
import by.jadjer.etcu.data.repository.BLERepositoryImpl
import by.jadjer.etcu.data.repository.OTARepositoryImpl
import by.jadjer.etcu.domain.repository.BLERepository
import by.jadjer.etcu.domain.repository.OTARepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(private val app: Application) {

    private val preferenceManager by lazy { BLEPreferenceManager(app) }

    private val bleManager by lazy { BLEManager(app, preferenceManager) }

    private val githubService by lazy {
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

        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubService::class.java)
    }

    val bleRepository: BLERepository by lazy { BLERepositoryImpl(bleManager) }
    val otaRepository: OTARepository by lazy { OTARepositoryImpl(githubService, app) }
}
