package by.jadjer.etcu.ui.features.main

import by.jadjer.etcu.domain.model.ble.ConnectionState
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

    private val bleRepository = mockk<BLERepository>(relaxed = true)
    private lateinit var viewModel: MainViewModel

    private val connectionStateFlow = MutableStateFlow(ConnectionState.DISCONNECTED)
    private val connectionDetailFlow = MutableStateFlow<String?>(null)
    private val telemetryFlow = MutableStateFlow(SystemTelemetry())
    private val isManualForgetFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        
        every { bleRepository.connectionState } returns connectionStateFlow
        every { bleRepository.connectionDetail } returns connectionDetailFlow
        every { bleRepository.telemetry } returns telemetryFlow
        every { bleRepository.isManualForget } returns isManualForgetFlow

        viewModel = MainViewModel(bleRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `retryConnection calls bleRepository autoConnect`() {
        viewModel.retryConnection()
        verify { bleRepository.autoConnect() }
    }

    @Test
    fun `forgetDevice calls bleRepository forgetDevice`() {
        viewModel.forgetDevice()
        verify { bleRepository.forgetDevice() }
    }

    @Test
    fun `isBonded returns true when repository isBonded is true and isManualForget is false`() {
        every { bleRepository.isBonded() } returns true
        isManualForgetFlow.value = false
        
        assertTrue(viewModel.isBonded())
    }

    @Test
    fun `isBonded returns false when repository isBonded is true and isManualForget is true`() {
        every { bleRepository.isBonded() } returns true
        isManualForgetFlow.value = true
        
        assertFalse(viewModel.isBonded())
    }

    @Test
    fun `isBonded returns false when repository isBonded is false`() {
        every { bleRepository.isBonded() } returns false
        isManualForgetFlow.value = false
        
        assertFalse(viewModel.isBonded())
    }

    @Test
    fun `connectionState flow properly emits values from repository`() = runTest {
        assertEquals(ConnectionState.DISCONNECTED, viewModel.connectionState.value)
        
        connectionStateFlow.value = ConnectionState.READY
        assertEquals(ConnectionState.READY, viewModel.connectionState.value)
    }

    @Test
    fun `connectionDetail flow properly emits values from repository`() = runTest {
        assertEquals(null, viewModel.connectionDetail.value)
        
        connectionDetailFlow.value = "Error"
        assertEquals("Error", viewModel.connectionDetail.value)
    }

    @Test
    fun `telemetry flow properly emits values from repository`() = runTest {
        val initialTelemetry = SystemTelemetry()
        assertEquals(initialTelemetry, viewModel.telemetry.value)
        
        val newTelemetry = SystemTelemetry(isGuardActive = true)
        telemetryFlow.value = newTelemetry
        assertEquals(newTelemetry, viewModel.telemetry.value)
    }
}
