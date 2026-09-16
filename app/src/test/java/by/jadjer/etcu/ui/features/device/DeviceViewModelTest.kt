package by.jadjer.etcu.ui.features.device

import by.jadjer.etcu.domain.model.control.ControlConstants
import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.control.Cruise
import by.jadjer.etcu.domain.model.control.OperatingMode
import by.jadjer.etcu.domain.model.control.PID
import by.jadjer.etcu.domain.model.system.SystemInfo
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.domain.model.telemetry.TelemetryHistory
import by.jadjer.etcu.domain.repository.BLERepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceViewModelTest {

    private val _bleRepository = mockk<BLERepository>(relaxed = true)
    private lateinit var _viewModel: DeviceViewModel

    private val _controlDataFlow = MutableStateFlow(ControlData())
    private val _systemInfoFlow = MutableStateFlow(SystemInfo())
    private val _telemetryFlow = MutableStateFlow(SystemTelemetry())
    private val _historyFlow = MutableStateFlow(TelemetryHistory())

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())

        every { _bleRepository.controlData } returns _controlDataFlow
        every { _bleRepository.systemInfo } returns _systemInfoFlow
        every { _bleRepository.telemetry } returns _telemetryFlow
        every { _bleRepository.history } returns _historyFlow

        _viewModel = DeviceViewModel(_bleRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `forgetDevice calls repository forgetDevice`() {
        _viewModel.forgetDevice()
        verify { _bleRepository.forgetDevice() }
    }

    @Test
    fun `flows correctly reflect changes in repository`() = runTest {
        // Collect flows to make sure they are active due to SharingStarted.WhileSubscribed
        backgroundScope.launch { _viewModel.controlData.collect {} }
        backgroundScope.launch { _viewModel.operatingMode.collect {} }
        backgroundScope.launch { _viewModel.errorCount.collect {} }

        assertEquals(ControlData(), _viewModel.controlData.value)
        assertEquals(OperatingMode.CUSTOM, _viewModel.operatingMode.value)
        assertEquals(0, _viewModel.errorCount.value)

        // Update upstream flows
        val newControlData = ControlData(servoMax = 600) // 600 maps to OperatingMode.NORMAL
        _controlDataFlow.value = newControlData
        
        val newTelemetry = SystemTelemetry() // empty errors
        _telemetryFlow.value = newTelemetry

        // Advance time to allow stateIn to process
        advanceTimeBy(60.milliseconds)

        assertEquals(newControlData, _viewModel.controlData.value)
        assertEquals(OperatingMode.NORMAL, _viewModel.operatingMode.value)
        assertEquals(0, _viewModel.errorCount.value)
    }

    @Test
    fun `updateServoRange updates controlData and schedules repository call`() = runTest {
        _viewModel.updateServoRange(10, 100)
        
        // Before delay
        advanceTimeBy((ControlConstants.UPDATE_DELAY_MS - 10).milliseconds)
        verify(exactly = 0) { _bleRepository.sendControlData(any()) }
        
        // After delay
        advanceTimeBy(20.milliseconds)
        verify(exactly = 1) { _bleRepository.sendControlData(ControlData(servoMin = 10, servoMax = 100)) }
    }

    @Test
    fun `updateOperatingMode updates controlData and schedules repository call`() = runTest {
        _viewModel.updateOperatingMode(OperatingMode.SPORT) // SPORT max is 900
        
        advanceTimeBy((ControlConstants.UPDATE_DELAY_MS + 10).milliseconds)
        verify(exactly = 1) { _bleRepository.sendControlData(ControlData(servoMax = 900)) }
    }

    @Test
    fun `updateAccRange updates controlData and schedules repository call`() = runTest {
        _viewModel.updateAccRange(20, 200)
        
        advanceTimeBy((ControlConstants.UPDATE_DELAY_MS + 10).milliseconds)
        verify(exactly = 1) { _bleRepository.sendControlData(ControlData(acceleratorMin = 20, acceleratorMax = 200)) }
    }

    @Test
    fun `updateCruisePID updates controlData and schedules repository call`() = runTest {
        _viewModel.updateCruisePID(1f, 2f, 3f)
        
        advanceTimeBy((ControlConstants.UPDATE_DELAY_MS + 10).milliseconds)
        val expected = ControlData(cruise = Cruise(pid = PID(1f, 2f, 3f)))
        verify(exactly = 1) { _bleRepository.sendControlData(expected) }
    }

    @Test
    fun `updateCruiseRPMRange updates controlData and schedules repository call`() = runTest {
        _viewModel.updateCruiseRPMRange(1000, 4000)
        
        advanceTimeBy((ControlConstants.UPDATE_DELAY_MS + 10).milliseconds)
        val expected = ControlData(cruise = Cruise(rpmMin = 1000, rpmMax = 4000))
        verify(exactly = 1) { _bleRepository.sendControlData(expected) }
    }

    @Test
    fun `updateCruiseSpeedRange updates controlData and schedules repository call`() = runTest {
        _viewModel.updateCruiseSpeedRange(30, 130)
        
        advanceTimeBy((ControlConstants.UPDATE_DELAY_MS + 10).milliseconds)
        val expected = ControlData(cruise = Cruise(speedMin = 30, speedMax = 130))
        verify(exactly = 1) { _bleRepository.sendControlData(expected) }
    }

    @Test
    fun `updateCruiseLimiterUp updates controlData and schedules repository call`() = runTest {
        _viewModel.updateCruiseLimiterUp(5)
        
        advanceTimeBy((ControlConstants.UPDATE_DELAY_MS + 10).milliseconds)
        val expected = ControlData(cruise = Cruise(limiterUp = 5))
        verify(exactly = 1) { _bleRepository.sendControlData(expected) }
    }

    @Test
    fun `updateCruiseLimiterDown updates controlData and schedules repository call`() = runTest {
        _viewModel.updateCruiseLimiterDown(10)
        
        advanceTimeBy((ControlConstants.UPDATE_DELAY_MS + 10).milliseconds)
        val expected = ControlData(cruise = Cruise(limiterDown = 10))
        verify(exactly = 1) { _bleRepository.sendControlData(expected) }
    }
}
