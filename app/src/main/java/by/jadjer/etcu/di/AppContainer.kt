package by.jadjer.etcu.di

import android.content.Context
import by.jadjer.etcu.data.ble.BleManager
import by.jadjer.etcu.data.local.BlePreferenceManager
import by.jadjer.etcu.data.network.GitHubService
import by.jadjer.etcu.data.repository.BLERepositoryImpl
import by.jadjer.etcu.data.repository.OTARepositoryImpl
import by.jadjer.etcu.domain.repository.BLERepository
import by.jadjer.etcu.domain.repository.OTARepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {

    private val preferenceManager: BlePreferenceManager by lazy {
        BlePreferenceManager(context)
    }

    private val bleManager: BleManager by lazy {
        BleManager(context, preferenceManager)
    }

    private val githubService: GitHubService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubService::class.java)
    }

    val bleRepository: BLERepository by lazy {
        BLERepositoryImpl(bleManager)
    }

    val otaRepository: OTARepository by lazy {
        OTARepositoryImpl(githubService, context)
    }
}
