package by.jadjer.etcu.ui.features.ota

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.jadjer.etcu.R
import by.jadjer.etcu.data.ble.BLEConstants
import by.jadjer.etcu.domain.model.ota.OTAChunk
import by.jadjer.etcu.domain.model.ota.OTAStatus
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
        val description: String?,
        val downloadUrl: String,
        val size: Long
    ) : OTAState()

    data object UpToDate : OTAState()
    data class Downloading(val progress: Float) : OTAState()
    data class Uploading(
        val progress: Float,
        val currentChunk: Int,
        val totalChunks: Int,
        val firmwareSize: Long,
        val estimatedTimeMs: Long
    ) : OTAState()

    data object Success : OTAState()
    data class Error(val message: String) : OTAState()
}

class OtaViewModel(
    private val _app: Application,
    private val _bleRepository: BLERepository,
    private val _otaRepository: OTARepository
) : ViewModel() {

    private val _state = MutableStateFlow<OTAState>(OTAState.Idle)
    val state = _state.asStateFlow()

    private var _firmwareData: ByteArray? = null
    private var _totalChunks = 0
    private var _currentChunkIndex = 0
    private var _hasCheckedUpdates = false

    init {
        _bleRepository.connectionState
            .onEach { state -> if (!state.isActive) resetOtaState() }
            .launchIn(viewModelScope)

        _bleRepository.otaFeedback
            .onEach { handleFeedback(it) }
            .launchIn(viewModelScope)

        _bleRepository.connectionState
            .onEach { connState ->
                if (connState.isError && _state.value is OTAState.Uploading) {
                    _state.value = OTAState.Error(
                        _app.getString(
                            R.string.ota_error_transmission,
                            connState.toString()
                        )
                    )
                }
            }
            .launchIn(viewModelScope)

        combine(_bleRepository.connectionState, _bleRepository.systemInfo) { state, info ->
            if (state.isActive && info.firmwareVersion != "0.0.0" && _state.value == OTAState.Idle && !_hasCheckedUpdates) {
                _hasCheckedUpdates = true
                checkForUpdates(info.firmwareVersion)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleFeedback(status: OTAStatus) {
        if (_state.value !is OTAState.Uploading) return
        when (status) {
            OTAStatus.READY_FOR_NEXT -> {
                val nextIndex = _currentChunkIndex + 1
                if (nextIndex < _totalChunks) sendNextChunk(nextIndex)
            }

            OTAStatus.COMPLETED -> _state.value = OTAState.Success
            OTAStatus.ERROR -> _state.value =
                OTAState.Error(_app.getString(R.string.ota_error_device_firmware))

            else -> {}
        }
    }

    private fun resetOtaState() {
        _state.value = OTAState.Idle
        _firmwareData = null
        _currentChunkIndex = 0
        _hasCheckedUpdates = false
    }

    fun checkForUpdates(currentVersion: String? = null) {
        viewModelScope.launch {
            _state.value = OTAState.CheckingUpdates
            val current = currentVersion ?: _bleRepository.systemInfo.value.firmwareVersion

            when (val result = _otaRepository.getLatestRelease()) {
                is Resource.Success -> {
                    val release = result.data
                    val cleanRelease = release.version.removePrefix("v")
                    val cleanCurrent = current.removePrefix("v").ifEmpty { "0.0.0" }

                    if (isNewer(cleanRelease, cleanCurrent)) {
                        _state.value = OTAState.UpdateAvailable(
                            currentVersion = current,
                            latestVersion = release.version,
                            description = release.description,
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
                val result = _otaRepository.downloadFirmware(url, size) { progress ->
                    if (_state.value is OTAState.Downloading) _state.value =
                        OTAState.Downloading(progress)
                }

                when (result) {
                    is Resource.Success -> {
                        val downloadedData = result.data
                        if (downloadedData.isEmpty()) {
                            _state.value =
                                OTAState.Error(_app.getString(R.string.ota_error_empty_file))
                            return@launch
                        }
                        _state.value = OTAState.Downloading(1f)
                        _firmwareData = downloadedData
                        _totalChunks =
                            (downloadedData.size + BLEConstants.OTA_PAYLOAD_SIZE - 1) / BLEConstants.OTA_PAYLOAD_SIZE
                        sendNextChunk(0)
                    }

                    is Resource.Error -> _state.value = OTAState.Error(result.message)
                }
            }.onFailure { e ->
                _state.value = OTAState.Error(
                    _app.getString(
                        R.string.ota_error_system,
                        e.localizedMessage ?: ""
                    )
                )
            }
        }
    }

    private fun sendNextChunk(index: Int) {
        val data = _firmwareData ?: return
        if (index >= _totalChunks) return

        _currentChunkIndex = index
        val start = index * BLEConstants.OTA_PAYLOAD_SIZE
        val end = minOf(start + BLEConstants.OTA_PAYLOAD_SIZE, data.size)

        if (start >= data.size) return

        val payload = data.sliceArray(start until end)

        _bleRepository.sendOtaChunk(
            OTAChunk(
                firmwareSize = data.size.toLong(),
                totalChunks = _totalChunks,
                chunkIndex = index,
                data = payload
            )
        )

        val showIndex = index + 1
        val uploadProgress = showIndex.toFloat() / _totalChunks
        val remainingChunks = _totalChunks - showIndex
        val estimatedTimeMs = remainingChunks * 100L

        _state.value = OTAState.Uploading(
            progress = uploadProgress,
            currentChunk = showIndex,
            totalChunks = _totalChunks,
            firmwareSize = data.size.toLong(),
            estimatedTimeMs = estimatedTimeMs
        )
    }
}
