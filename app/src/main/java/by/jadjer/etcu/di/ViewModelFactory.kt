package by.jadjer.etcu.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.ui.features.device.DeviceViewModel
import by.jadjer.etcu.ui.features.main.MainViewModel
import by.jadjer.etcu.ui.features.ota.OtaViewModel
import by.jadjer.etcu.ui.features.scan.ScanViewModel

object ViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as ETCUApplication
        val container = application.container

        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> 
                MainViewModel(container.bleRepository) as T
            
            modelClass.isAssignableFrom(DeviceViewModel::class.java) -> 
                DeviceViewModel(container.bleRepository) as T
            
            modelClass.isAssignableFrom(ScanViewModel::class.java) -> 
                ScanViewModel(container.bleRepository) as T
            
            modelClass.isAssignableFrom(OtaViewModel::class.java) -> 
                OtaViewModel(application, container.bleRepository, container.otaRepository) as T
            
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
