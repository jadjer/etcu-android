package by.jadjer.etcu.ui.screen.ota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.domain.model.OtaChunk
import by.jadjer.etcu.domain.repository.BleRepository
import by.jadjer.etcu.domain.repository.OtaRepository
import by.jadjer.etcu.domain.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class OtaState {
    object Idle : OtaState()
    object CheckingUpdates : OtaState()
    data class UpdateAvailable(val version: String, val downloadUrl: String) : OtaState()
    object Downloading : OtaState()
    data class Uploading(val progress: Float, val currentChunk: Int, val totalChunks: Int) : OtaState()
    object Success : OtaState()
    data class Error(val message: String) : OtaState()
}

class OtaViewModel(
    private val bleRepository: BleRepository,
    private val otaRepository: OtaRepository
) : ViewModel() {

    private val _state = MutableStateFlow<OtaState>(OtaState.Idle)
    val state: StateFlow<OtaState> = _state

    private var firmwareData: ByteArray? = null
    private var totalChunks = 0

    init {
        // Listen for chunk acceptance from BLE device
        viewModelScope.launch {
            bleRepository.otaFeedback.collectLatest { acceptedChunkNumber ->
                if (acceptedChunkNumber != null && _state.value is OtaState.Uploading) {
                    sendNextChunk(acceptedChunkNumber + 1)
                }
            }
        }

        // Auto check for updates on connection
        viewModelScope.launch {
            combine(bleRepository.isConnected, bleRepository.systemInfo) { connected, info ->
                connected to info
            }.collectLatest { (connected, info) ->
                if (connected && info.firmwareVersion != "0.0.0" && _state.value == OtaState.Idle) {
                    checkForUpdates(currentVersion = info.firmwareVersion)
                }
            }
        }
    }

    fun checkForUpdates(currentVersion: String? = null) {
        viewModelScope.launch {
            _state.value = OtaState.CheckingUpdates
            when (val result = otaRepository.getLatestRelease()) {
                is Resource.Success -> {
                    val release = result.data
                    val isNewer = currentVersion == null || isVersionNewer(release.version, currentVersion)
                    if (isNewer) {
                        _state.value = OtaState.UpdateAvailable(release.version, release.downloadUrl)
                    } else {
                        _state.value = OtaState.Idle
                    }
                }
                is Resource.Error -> {
                    _state.value = OtaState.Error(result.message)
                }
            }
        }
    }

    private fun isVersionNewer(newVersion: String, currentVersion: String): Boolean {
        return newVersion.replace("v", "") != currentVersion.replace("v", "")
    }

    fun startUpdate(downloadUrl: String) {
        viewModelScope.launch {
            _state.value = OtaState.Downloading
            when (val result = otaRepository.downloadFirmware(downloadUrl)) {
                is Resource.Success -> {
                    val data = result.data
                    firmwareData = data
                    val packageSize = 200 
                    totalChunks = (data.size + packageSize - 1) / packageSize
                    _state.value = OtaState.Uploading(0f, 0, totalChunks)
                    sendNextChunk(0)
                }
                is Resource.Error -> {
                    _state.value = OtaState.Error(result.message)
                }
            }
        }
    }

    private fun sendNextChunk(chunkNumber: Int) {
        val data = firmwareData ?: return
        val packageSize = 200
        
        if (chunkNumber >= totalChunks) {
            _state.value = OtaState.Success
            return
        }

        val start = chunkNumber * packageSize
        val end = minOf(start + packageSize, data.size)
        val chunkData = data.sliceArray(start until end)

        val chunk = OtaChunk(
            data = chunkData,
            chunkNumber = chunkNumber,
            totalChunks = totalChunks,
            firmwareSize = data.size.toLong()
        )

        _state.value = OtaState.Uploading(
            progress = chunkNumber.toFloat() / totalChunks,
            currentChunk = chunkNumber,
            totalChunks = totalChunks
        )
        
        bleRepository.sendOtaChunk(chunk)
    }

    class Factory(private val app: ETCUApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OtaViewModel(app.container.bleRepository, app.container.otaRepository) as T
        }
    }
}
