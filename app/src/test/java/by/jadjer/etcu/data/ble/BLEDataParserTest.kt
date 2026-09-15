package by.jadjer.etcu.data.ble

import by.jadjer.etcu.domain.model.calibration.CalibrationData
import by.jadjer.etcu.domain.model.calibration.CalibrationRange
import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.ota.OTAChunk
import by.jadjer.etcu.domain.model.ota.OTAStatus
import by.jadjer.etcu.domain.model.system.SystemState
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.assertEquals

class BLEDataParserTest {

    private val _parser = BLEDataParser()

    @Test
    fun `parseControlData returns default on short array`() {
        val result = _parser.parseControlData(byteArrayOf(1, 2, 3))
        assertEquals(ControlData(), result)
    }

    @Test
    fun `parseControlData parses correctly`() {
        val buffer = ByteBuffer.allocate(BLEConstants.CONTROL_DATA_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(34) // Skip cruise
        buffer.putShort(100.toShort()) // min
        buffer.putShort(600.toShort()) // max
        buffer.putShort(150.toShort()) // min
        buffer.putShort(850.toShort()) // max
        val bytes = buffer.array()

        val result = _parser.parseControlData(bytes)

        assertEquals(100, result.servoMin)
        assertEquals(600, result.servoMax)
        assertEquals(150, result.acceleratorMin)
        assertEquals(850, result.acceleratorMax)
    }

    @Test
    fun `parseCalibrationData parses correctly`() {
        val bytes = ByteBuffer.allocate(BLEConstants.CALIBRATION_DATA_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(1000.toShort()).putShort(2000.toShort()) // HallA
            .putShort(1100.toShort()).putShort(2100.toShort()) // HallB
            .putShort(1200.toShort()).putShort(2200.toShort()) // Servo
            .array()

        val result = _parser.parseCalibrationData(bytes)

        assertEquals(1000, result.hallA.min)
        assertEquals(2000, result.hallA.max)
        assertEquals(1100, result.hallB.min)
        assertEquals(2100, result.hallB.max)
        assertEquals(1200, result.servo.min)
        assertEquals(2200, result.servo.max)
    }

    @Test
    fun `parseSystemInfo parses strings correctly`() {
        val bytes = ByteArray(BLEConstants.SYSTEM_INFO_SIZE)
        "2026-08-29".toByteArray().copyInto(bytes, 0)
        "V1.0".toByteArray().copyInto(bytes, 16)
        "FW-2.0".toByteArray().copyInto(bytes, 32)

        val result = _parser.parseSystemInfo(bytes)

        assertEquals("2026-08-29", result.buildDate)
        assertEquals("V1.0", result.boardVersion)
        assertEquals("FW-2.0", result.firmwareVersion)
    }

    @Test
    fun `parseSystemTelemetry parses correctly`() {
        val buffer = ByteBuffer.allocate(BLEConstants.TELEMETRY_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(1.toByte()) // isGuardActive
        buffer.put(0.toByte()) // isBrakeEnabled
        buffer.putShort(300.toShort()) // throttlePosition
        
        // ECU Telemetry (15 bytes)
        buffer.put(1.toByte()) // isConnected
        buffer.put(1.toByte()) // isStarted
        buffer.put(0.toByte()) // isNeutral
        buffer.putShort(3000.toShort()) // rpm
        buffer.put(60.toByte()) // speed
        buffer.put(90.toByte()) // map
        buffer.putShort(2500.toShort()) // tps
        buffer.putFloat(14.5f) // battery
        buffer.put(45.toByte()) // airTemp
        buffer.put(85.toByte()) // coolantTemp
        
        // Servo Telemetry (12 bytes)
        buffer.put(1.toByte()) // isConnected
        buffer.put(1.toByte()) // isEnabled
        buffer.put(0.toByte()) // isMoved
        buffer.putShort(500.toShort()) // current
        buffer.putFloat(12.2f) // voltage
        buffer.putShort(1500.toShort()) // position
        buffer.put(50.toByte()) // temperature
        
        // Cruise Telemetry (16 bytes)
        buffer.put(1.toByte()) // isEnabled
        buffer.put(0.toByte()) // isActivated
        buffer.putFloat(1.5f) // error
        buffer.putFloat(2.5f) // correction
        buffer.put(100.toByte()) // targetSpeed
        buffer.put(70.toByte()) // currentSpeed
        buffer.putShort(1200.toShort()) // basePosition
        buffer.putShort(1300.toShort()) // currentPosition
        
        // Accelerator Telemetry (6 bytes)
        buffer.putShort(1000.toShort()) // hallA
        buffer.putShort(2000.toShort()) // hallB
        buffer.putShort(1500.toShort()) // position
        
        // System level fields
        buffer.put(SystemState.NORMAL.value.toByte()) // systemState
        buffer.putShort(0.toShort()) // activeErrors (None)
        
        val bytes = buffer.array()
        val result = _parser.parseSystemTelemetry(bytes)

        assertEquals(true, result.status.isGuardActive)
        assertEquals(false, result.status.isBrakeEnabled)
        assertEquals(300, result.status.throttlePosition)

        // ECU
        assertEquals(true, result.ecu.isConnected)
        assertEquals(true, result.ecu.isStarted)
        assertEquals(false, result.ecu.isNeutral)
        assertEquals(3000, result.ecu.rpm)
        assertEquals(14.5f, result.ecu.battery)
        assertEquals(60, result.ecu.speed)
        assertEquals(90, result.ecu.map)
        assertEquals(2500, result.ecu.tps)
        assertEquals(45, result.ecu.airTemp)
        assertEquals(85, result.ecu.coolantTemp)

        // Servo
        assertEquals(true, result.servo.isConnected)
        assertEquals(true, result.servo.isEnabled)
        assertEquals(false, result.servo.isMoved)
        assertEquals(12.2f, result.servo.voltage)
        assertEquals(500, result.servo.current)
        assertEquals(1500, result.servo.position)
        assertEquals(50, result.servo.temperature)

        // Cruise
        assertEquals(true, result.cruise.isEnabled)
        assertEquals(100, result.cruise.targetSpeed)

        // Accelerator
        assertEquals(1000, result.accelerator.hallA)
        assertEquals(2000, result.accelerator.hallB)
        assertEquals(1500, result.accelerator.position)

        // General
        assertEquals(SystemState.NORMAL, result.status.systemState)
        assertEquals(emptyList(), result.status.activeErrors)
    }

    @Test
    fun `parseOtaFeedback parses correctly`() {
        assertEquals(OTAStatus.READY_FOR_NEXT, _parser.parseOtaFeedback(byteArrayOf(1)))
        assertEquals(OTAStatus.COMPLETED, _parser.parseOtaFeedback(byteArrayOf(2)))
        assertEquals(OTAStatus.ERROR, _parser.parseOtaFeedback(byteArrayOf(0)))
    }

    @Test
    fun `serializeControlData serializes correctly`() {
        val data = ControlData(
            servoMin = 100,
            servoMax = 600,
            acceleratorMin = 150,
            acceleratorMax = 850
        )
        val bytes = _parser.serializeControlData(data)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        
        buffer.position(34) // Skip cruise
        assertEquals(100, buffer.short.toInt() and 0xFFFF)
        assertEquals(600, buffer.short.toInt() and 0xFFFF)
        assertEquals(150, buffer.short.toInt() and 0xFFFF)
        assertEquals(850, buffer.short.toInt() and 0xFFFF)
    }

    @Test
    fun `serializeCalibrationData serializes correctly`() {
        val data = CalibrationData(
            hallA = CalibrationRange(1000, 2000),
            hallB = CalibrationRange(1100, 2100),
            servo = CalibrationRange(1200, 2200)
        )
        val bytes = _parser.serializeCalibrationData(data)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        assertEquals(1000, buffer.short.toInt() and 0xFFFF)
        assertEquals(2000, buffer.short.toInt() and 0xFFFF)
        assertEquals(1100, buffer.short.toInt() and 0xFFFF)
        assertEquals(2100, buffer.short.toInt() and 0xFFFF)
        assertEquals(1200, buffer.short.toInt() and 0xFFFF)
        assertEquals(2200, buffer.short.toInt() and 0xFFFF)
    }

    @Test
    fun `serializeOtaChunk serializes correctly`() {
        val chunkData = ByteArray(BLEConstants.OTA_PAYLOAD_SIZE) { it.toByte() }
        val chunk = OTAChunk(
            firmwareSize = 10000L,
            totalChunks = 20,
            chunkIndex = 5,
            data = chunkData
        )
        val bytes = _parser.serializeOtaChunk(chunk)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        assertEquals(10000, buffer.int)
        assertEquals(20.toShort(), buffer.short)
        assertEquals(5.toShort(), buffer.short)

        val actualData = ByteArray(BLEConstants.OTA_PAYLOAD_SIZE)
        buffer.get(actualData)
        assertEquals(chunkData.toList(), actualData.toList())
    }
}
