package by.jadjer.etcu.ui.features.ota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.data.ble.BLEConstants
import by.jadjer.etcu.domain.model.OTAChunk
import by.jadjer.etcu.domain.model.OTAStatus
import by.jadjer.etcu.domain.repository.BLERepository
import by.jadjer.etcu.domain.repository.OTARepository
import by.jadjer.etcu.domain.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class OTAState {
    data object Idle : OTAState()
    data object CheckingUpdates : OTAState()
    data class UpdateAvailable(
        val currentVersion: String,
        val latestVersion: String,
        val downloadUrl: String,
        val size: Long
    ) : OTAState()
    data object UpToDate : OTAState()
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
        bleRepository.connectionState
            .onEach { state -> if (!state.isActive) resetOtaState() }
            .launchIn(viewModelScope)

        bleRepository.otaFeedback
            .onEach { handleFeedback(it) }
            .launchIn(viewModelScope)

        bleRepository.connectionState
            .onEach { connState ->
                if (connState.isError && _state.value is OTAState.Uploading) {
                    _state.value = OTAState.Error("Ошибка передачи данных: $connState")
                }
            }
            .launchIn(viewModelScope)

        combine(bleRepository.connectionState, bleRepository.systemInfo) { state, info ->
            if (state.isActive && info.firmwareVersion != "0.0.0" && _state.value == OTAState.Idle && !hasCheckedUpdates) {
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
            val current = currentVersion ?: bleRepository.systemInfo.value.firmwareVersion
            
            when (val result = otaRepository.getLatestRelease()) {
                is Resource.Success -> {
                    val release = result.data
                    val cleanRelease = release.version.removePrefix("v")
                    val cleanCurrent = current.removePrefix("v").ifEmpty { "0.0.0" }

                    if (isNewer(cleanRelease, cleanCurrent)) {
                        _state.value = OTAState.UpdateAvailable(
                            currentVersion = current,
                            latestVersion = release.version,
                            downloadUrl = release.downloadUrl,
                            size = release.size
                        )
                    } else {
                        _state.value = OTAState.UpToDate
                    }
                }
                is Resource.Error -> _state.value = OTAState.Error(result.message)
            }
        }
    }

    private fun isNewer(latest: String, current: String): Boolean {
        if (current == "0.0.0") return true
        val latestParts = latest.split('.').mapNotNull { it.toIntOrNull() }
        val currentParts = current.split('.').mapNotNull { it.toIntOrNull() }

        val length = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until length) {
            val l = latestParts.getOrNull(i) ?: 0
            val c = currentParts.getOrNull(i) ?: 0
            if (l > c) return true
            if (l < c) return false
        }
        return false
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
                        totalChunks = (downloadedData.size + BLEConstants.OTA_PAYLOAD_SIZE - 1) / BLEConstants.OTA_PAYLOAD_SIZE
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
        val start = index * BLEConstants.OTA_PAYLOAD_SIZE
        val end = minOf(start + BLEConstants.OTA_PAYLOAD_SIZE, data.size)

        if (start >= data.size) return

        val payload = data.sliceArray(start until end)

        bleRepository.sendOtaChunk(
            OTAChunk(
                firmwareSize = data.size.toLong(),
                totalChunks = totalChunks,
                chunkNumber = index,
                data = payload
            )
        )

        val showIndex = index + 1
        val uploadProgress = showIndex.toFloat() / totalChunks

        _state.value = OTAState.Uploading(
            progress = uploadProgress,
            currentChunk = showIndex,
            totalChunks = totalChunks,
            firmwareSize = data.size.toLong()
        )
    }
}
