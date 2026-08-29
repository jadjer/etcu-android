package by.jadjer.etcu.di

import android.content.Context
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

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val preferenceManager: BLEPreferenceManager by lazy {
        BLEPreferenceManager(appContext)
    }

    private val bleManager: BLEManager by lazy {
        BLEManager(appContext, preferenceManager)
    }

    private val githubService: GitHubService by lazy {
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

    val bleRepository: BLERepository by lazy {
        BLERepositoryImpl(bleManager)
    }

    val otaRepository: OTARepository by lazy {
        OTARepositoryImpl(githubService, appContext)
    }
}
