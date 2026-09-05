package by.jadjer.etcu.data.ble

import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.ota.OTAStatus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BLEDataParserTest {

    private lateinit var parser: BLEDataParser

    @Before
    fun setUp() {
        parser = BLEDataParser()
    }

    @Test
    fun `parseControlData returns default on short array`() {
        val result = parser.parseControlData(byteArrayOf(1, 2, 3))
        assertEquals(ControlData(), result)
    }

    @Test
    fun `parseControlData parses correctly`() {
        val bytes = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
            // CruiseAutoSet
            .put(1.toByte()) // enabled
            .put(5.toByte()) // delay_sec
            .put(40.toByte()) // threshold_kmh
            .put(5.toByte()) // tolerance_kmh
            // Servo
            .putShort(100.toShort()) // min
            .putShort(600.toShort()) // max
            // Accel
            .putShort(150.toShort()) // min
            .putShort(850.toShort()) // max
            .array()
        
        val result = parser.parseControlData(bytes)
        
        assertEquals(true, result.cruise.enabled)
        assertEquals(5, result.cruise.delaySec)
        assertEquals(40, result.cruise.thresholdKmh)
        assertEquals(5, result.cruise.toleranceKmh)
        assertEquals(100, result.servo.min)
        assertEquals(600, result.servo.max)
        assertEquals(150, result.accelerator.min)
        assertEquals(850, result.accelerator.max)
    }

    @Test
    fun `parseOtaFeedback parses correctly`() {
        assertEquals(OTAStatus.READY_FOR_NEXT, parser.parseOtaFeedback(byteArrayOf(1)))
        assertEquals(OTAStatus.COMPLETED, parser.parseOtaFeedback(byteArrayOf(2)))
        assertEquals(OTAStatus.ERROR, parser.parseOtaFeedback(byteArrayOf(0)))
    }
    
    @Test
    fun `parseSystemInfo parses strings correctly`() {
        val bytes = ByteArray(48)
        "2026-08-29".toByteArray().copyInto(bytes, 0)
        "V1.0".toByteArray().copyInto(bytes, 16)
        "FW-2.0".toByteArray().copyInto(bytes, 32)
        
        val result = parser.parseSystemInfo(bytes)
        
        assertEquals("2026-08-29", result.buildDate)
        assertEquals("V1.0", result.boardVersion)
        assertEquals("FW-2.0", result.firmwareVersion)
    }
}
