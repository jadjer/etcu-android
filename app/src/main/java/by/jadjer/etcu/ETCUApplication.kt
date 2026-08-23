package by.jadjer.etcu

import android.app.Application
import by.jadjer.etcu.di.AppContainer

class ETCUApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}
