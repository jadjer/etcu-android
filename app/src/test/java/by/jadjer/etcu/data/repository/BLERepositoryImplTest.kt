package by.jadjer.etcu.data.repository

import by.jadjer.etcu.data.ble.BLEManager
import by.jadjer.etcu.data.ble.BLEScanner
import by.jadjer.etcu.domain.model.ble.DiscoveredDevice
import by.jadjer.etcu.domain.model.calibration.CalibrationData
import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.ota.OTAChunk
import by.jadjer.etcu.domain.model.ota.OTAStatus
import by.jadjer.etcu.domain.model.telemetry.SystemStatusTelemetry
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class BLERepositoryImplTest {

    private val _bleManager = mockk<BLEManager>(relaxed = true)
    private val _bleScanner = mockk<BLEScanner>(relaxed = true)
    private lateinit var _repository: BLERepositoryImpl

    private val _telemetryFlow = MutableStateFlow(SystemTelemetry())
    private val _discoveredDevicesFlow = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    private val _isScanningFlow = MutableStateFlow(false)
    private val _otaFeedbackFlow = MutableSharedFlow<OTAStatus>()

    @Before
    fun setUp() {
        every { _bleManager.scanner } returns _bleScanner
        every { _bleManager.telemetry } returns _telemetryFlow
        every { _bleScanner.discoveredDevices } returns _discoveredDevicesFlow
        every { _bleScanner.isScanning } returns _isScanningFlow
        every { _bleManager.otaFeedback } returns _otaFeedbackFlow
    }

    @Test
    fun `history is updated when new telemetry arrives`() = runTest {
        _repository = BLERepositoryImpl(_bleManager, backgroundScope)
        
        // Initially empty
        assertEquals(0, _repository.history.value.status.size)

        val newTelemetry = SystemTelemetry(status = SystemStatusTelemetry(isGuardActive = true))
        _telemetryFlow.value = newTelemetry

        // Wait for sample interval (200ms)
        advanceTimeBy(210.milliseconds)
        assertEquals(1, _repository.history.value.status.size)
        
        val anotherTelemetry = SystemTelemetry(status = SystemStatusTelemetry(isGuardActive = false))
        _telemetryFlow.value = anotherTelemetry
        advanceTimeBy(210.milliseconds)
        assertEquals(2, _repository.history.value.status.size)
    }

    @Test
    fun `history filtering logic retains recent records`() = runTest {
        _repository = BLERepositoryImpl(_bleManager, backgroundScope)
        
        val newTelemetry = SystemTelemetry(status = SystemStatusTelemetry(isGuardActive = true))
        _telemetryFlow.value = newTelemetry
        advanceTimeBy(210.milliseconds)
        
        val recordTimestamp = _repository.history.value.status.first().timestamp
        val current = System.currentTimeMillis()
        
        // Verify that the record timestamp is recent (real time)
        assertTrue(recordTimestamp <= current)
        assertTrue(recordTimestamp > current - 1000)
    }

    @Test
    fun `other repository methods delegate to BLEManager correctly`() = runTest {
        _repository = BLERepositoryImpl(_bleManager, backgroundScope)
        
        _repository.startScan()
        verify { _bleScanner.startScan() }

        _repository.stopScan()
        verify { _bleScanner.stopScan() }

        _repository.connect("MAC")
        verify { _bleManager.connect("MAC") }

        _repository.autoConnect()
        verify { _bleManager.autoConnect() }

        _repository.forgetDevice()
        verify { _bleManager.forgetDevice() }

        _repository.isBonded()
        verify { _bleManager.isBonded() }

        val controlData = ControlData()
        _repository.sendControlData(controlData)
        verify { _bleManager.writeControlData(controlData) }

        val calibrationData = CalibrationData()
        _repository.sendCalibrationData(calibrationData)
        verify { _bleManager.writeCalibrationData(calibrationData) }

        val chunk = OTAChunk(1000L, 10, 0, byteArrayOf(1))
        _repository.sendOtaChunk(chunk)
        verify { _bleManager.writeOtaChunk(chunk) }
    }
}
