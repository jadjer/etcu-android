package by.jadjer.etcu.ui.features.main

import by.jadjer.etcu.domain.model.ble.ConnectionState
import by.jadjer.etcu.domain.model.telemetry.SystemStatusTelemetry
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.domain.repository.BLERepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val _bleRepository = mockk<BLERepository>(relaxed = true)
    private lateinit var _viewModel: MainViewModel

    private val _connectionStateFlow = MutableStateFlow(ConnectionState.DISCONNECTED)
    private val _connectionDetailFlow = MutableStateFlow<String?>(null)
    private val _telemetryFlow = MutableStateFlow(SystemTelemetry())
    private val _isManualForgetFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        
        every { _bleRepository.connectionState } returns _connectionStateFlow
        every { _bleRepository.connectionDetail } returns _connectionDetailFlow
        every { _bleRepository.telemetry } returns _telemetryFlow
        every { _bleRepository.isManualForget } returns _isManualForgetFlow

        _viewModel = MainViewModel(_bleRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `retryConnection calls bleRepository autoConnect`() {
        _viewModel.retryConnection()
        verify { _bleRepository.autoConnect() }
    }

    @Test
    fun `forgetDevice calls bleRepository forgetDevice`() {
        _viewModel.forgetDevice()
        verify { _bleRepository.forgetDevice() }
    }

    @Test
    fun `isBonded returns true when repository isBonded is true and isManualForget is false`() {
        every { _bleRepository.isBonded() } returns true
        _isManualForgetFlow.value = false
        
        assertTrue(_viewModel.isBonded())
    }

    @Test
    fun `isBonded returns false when repository isBonded is true and isManualForget is true`() {
        every { _bleRepository.isBonded() } returns true
        _isManualForgetFlow.value = true
        
        assertFalse(_viewModel.isBonded())
    }

    @Test
    fun `isBonded returns false when repository isBonded is false`() {
        every { _bleRepository.isBonded() } returns false
        _isManualForgetFlow.value = false
        
        assertFalse(_viewModel.isBonded())
    }

    @Test
    fun `connectionState flow properly emits values from repository`() = runTest {
        assertEquals(ConnectionState.DISCONNECTED, _viewModel.connectionState.value)
        
        _connectionStateFlow.value = ConnectionState.READY
        assertEquals(ConnectionState.READY, _viewModel.connectionState.value)
    }

    @Test
    fun `connectionDetail flow properly emits values from repository`() = runTest {
        assertEquals(null, _viewModel.connectionDetail.value)
        
        _connectionDetailFlow.value = "Error"
        assertEquals("Error", _viewModel.connectionDetail.value)
    }

    @Test
    fun `telemetry flow properly emits values from repository`() = runTest {
        val initialTelemetry = SystemTelemetry()
        assertEquals(initialTelemetry, _viewModel.telemetry.value)
        
        val newTelemetry = SystemTelemetry(status = SystemStatusTelemetry(isGuardActive = true))
        _telemetryFlow.value = newTelemetry
        assertEquals(newTelemetry, _viewModel.telemetry.value)
    }
}
