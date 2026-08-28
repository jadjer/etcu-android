package by.jadjer.etcu.ui.screen.ota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import by.jadjer.etcu.ETCUApplication
import by.jadjer.etcu.data.ble.BLEConstants
import by.jadjer.etcu.domain.model.OTAChunk
import by.jadjer.etcu.domain.model.OTAStatus
import by.jadjer.etcu.domain.repository.BLERepository
import by.jadjer.etcu.domain.repository.OTARepository
import by.jadjer.etcu.domain.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class OTAState {
    data object Idle : OTAState()
    data object CheckingUpdates : OTAState()
    data class UpdateAvailable(val version: String, val downloadUrl: String, val size: Long) : OTAState()
    data class Downloading(val progress: Float) : OTAState()
    data class Uploading(val progress: Float, val currentChunk: Int, val totalChunks: Int, val firmwareSize: Long) : OTAState()
    data object Success : OTAState()
    data class Error(val message: String) : OTAState()
}

class OtaViewModel(
    private val bleRepository: BLERepository,
    private val otaRepository: OTARepository
) : ViewModel() {

    private val _state = MutableStateFlow<OTAState>(OTAState.Idle)
    val state = _state.asStateFlow()

    private var firmwareData: ByteArray? = null
    private var totalChunks = 0
    private var currentChunkIndex = 0
    private var hasCheckedUpdates = false

    init {
        bleRepository.isConnected
            .onEach { connected -> if (!connected) resetOtaState() }
            .launchIn(viewModelScope)

        bleRepository.otaFeedback
            .onEach { handleFeedback(it) }
            .launchIn(viewModelScope)

        bleRepository.connectionState
            .onEach { connState ->
                if (connState.name.startsWith("ERROR") && _state.value is OTAState.Uploading) {
                    _state.value = OTAState.Error("Ошибка передачи данных: $connState")
                }
            }
            .launchIn(viewModelScope)

        combine(bleRepository.isConnected, bleRepository.systemInfo) { connected, info ->
            if (connected && info.firmwareVersion != "0.0.0" && _state.value == OTAState.Idle && !hasCheckedUpdates) {
                hasCheckedUpdates = true
                checkForUpdates(info.firmwareVersion)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleFeedback(status: OTAStatus) {
        if (_state.value !is OTAState.Uploading) return
        when (status) {
            OTAStatus.READY_FOR_NEXT -> {
                val nextIndex = currentChunkIndex + 1
                if (nextIndex < totalChunks) sendNextChunk(nextIndex)
            }
            OTAStatus.COMPLETED -> _state.value = OTAState.Success
            OTAStatus.ERROR -> _state.value = OTAState.Error("Ошибка прошивки на устройстве")
            else -> {}
        }
    }

    private fun resetOtaState() {
        _state.value = OTAState.Idle
        firmwareData = null
        currentChunkIndex = 0
        hasCheckedUpdates = false
    }

    fun checkForUpdates(currentVersion: String? = null) {
        viewModelScope.launch {
            _state.value = OTAState.CheckingUpdates
            when (val result = otaRepository.getLatestRelease()) {
                is Resource.Success -> {
                    val release = result.data
                    val cleanRelease = release.version.removePrefix("v")
                    val cleanCurrent = currentVersion?.removePrefix("v").orEmpty()

                    if (cleanRelease != cleanCurrent) {
                        _state.value = OTAState.UpdateAvailable(release.version, release.downloadUrl, release.size)
                    } else {
                        _state.value = OTAState.Idle
                    }
                }
                is Resource.Error -> _state.value = OTAState.Error(result.message)
            }
        }
    }

    fun startUpdate(url: String, size: Long) {
        viewModelScope.launch {
            runCatching {
                _state.value = OTAState.Downloading(0f)
                val result = otaRepository.downloadFirmware(url, size) { progress ->
                    if (_state.value is OTAState.Downloading) _state.value = OTAState.Downloading(progress)
                }

                when (result) {
                    is Resource.Success -> {
                        val downloadedData = result.data
                        if (downloadedData.isEmpty()) {
                            _state.value = OTAState.Error("Файл прошивки пуст")
                            return@launch
                        }
                        _state.value = OTAState.Downloading(1f)
                        firmwareData = downloadedData
                        totalChunks = Math.ceilDiv(downloadedData.size, BLEConstants.MAX_OTA_PAYLOAD_SIZE)
                        sendNextChunk(0)
                    }
                    is Resource.Error -> _state.value = OTAState.Error(result.message)
                }
            }.onFailure { e ->
                _state.value = OTAState.Error("Системная ошибка: ${e.localizedMessage}")
            }
        }
    }

    private fun sendNextChunk(index: Int) {
        val data = firmwareData ?: return
        if (index >= totalChunks) return

        currentChunkIndex = index
        val payloadSize = BLEConstants.MAX_OTA_PAYLOAD_SIZE
        val start = index * payloadSize
        if (start >= data.size) return
        val end = minOf(start + payloadSize, data.size)

        viewModelScope.launch(Dispatchers.Default) {
            val payload = data.sliceArray(start until end)
            val uploadProgress = ((index + 1).toFloat() / totalChunks).coerceIn(0f, 1f)

            val otaChunk = OTAChunk(
                firmwareSize = data.size.toLong(),
                totalChunks = totalChunks,
                chunkNumber = index,
                data = payload
            )

            withContext(Dispatchers.Main) {
                bleRepository.sendOtaChunk(otaChunk)
                _state.value = OTAState.Uploading(
                    progress = uploadProgress,
                    currentChunk = index + 1,
                    totalChunks = totalChunks,
                    firmwareSize = data.size.toLong()
                )
            }
        }
    }

    companion object {
        fun Factory(app: ETCUApplication) = viewModelFactory {
            initializer {
                OtaViewModel(app.container.bleRepository, app.container.otaRepository)
            }
        }
    }
}
