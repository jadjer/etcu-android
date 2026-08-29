package by.jadjer.etcu.data.ble

import by.jadjer.etcu.domain.model.ControlData
import by.jadjer.etcu.domain.model.OTAStatus
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
        val bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(100.toShort())
            .putShort(200.toShort())
            .putShort(300.toShort())
            .putShort(400.toShort())
            .array()
        
        val result = parser.parseControlData(bytes)
        
        assertEquals(100, result.accMin)
        assertEquals(200, result.accMax)
        assertEquals(300, result.servoMin)
        assertEquals(400, result.servoMax)
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
