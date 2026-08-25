package by.jadjer.etcu.di

import android.content.Context
import by.jadjer.etcu.data.ble.BleManager
import by.jadjer.etcu.data.local.BlePreferenceManager
import by.jadjer.etcu.data.repository.BleRepositoryImpl
import by.jadjer.etcu.data.repository.OtaRepositoryImpl
import by.jadjer.etcu.domain.repository.BleRepository
import by.jadjer.etcu.domain.repository.OtaRepository

class AppContainer(context: Context) {

    private val preferenceManager: BlePreferenceManager by lazy {
        BlePreferenceManager(context)
    }

    private val bleManager: BleManager by lazy {
        BleManager(context, preferenceManager)
    }

    val bleRepository: BleRepository by lazy {
        BleRepositoryImpl(bleManager)
    }

    val otaRepository: OtaRepository by lazy {
        OtaRepositoryImpl()
    }
}
