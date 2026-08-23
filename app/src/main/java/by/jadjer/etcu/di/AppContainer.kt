package by.jadjer.etcu.di

import android.content.Context
import by.jadjer.etcu.data.repository.BleRepository
import by.jadjer.etcu.data.repository.OtaRepository
import by.jadjer.etcu.data.source.ble.BleManager
import by.jadjer.etcu.data.source.local.BlePreferenceManager

/**
 * Manual Dependency Injection container.
 * In larger projects, this would be replaced by Hilt or Koin.
 */
class AppContainer(context: Context) {

    private val preferenceManager: BlePreferenceManager by lazy {
        BlePreferenceManager(context)
    }

    private val bleManager: BleManager by lazy {
        BleManager(context, preferenceManager)
    }

    val bleRepository: BleRepository by lazy {
        BleRepository(bleManager)
    }

    val otaRepository: OtaRepository by lazy {
        OtaRepository()
    }
}
