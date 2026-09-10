package by.jadjer.etcu.data.ble

import by.jadjer.etcu.domain.model.calibration.CalibrationData
import by.jadjer.etcu.domain.model.calibration.CalibrationRange
import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.control.PositionRange
import by.jadjer.etcu.domain.model.ota.OTAChunk
import by.jadjer.etcu.domain.model.ota.OTAStatus
import by.jadjer.etcu.domain.model.system.SystemState
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals

class BLEDataParserTest {

    private val parser = BLEDataParser()

    @Test
    fun `parseControlData returns default on short array`() {
        val result = parser.parseControlData(byteArrayOf(1, 2, 3))
        assertEquals(ControlData(), result)
    }

    @Test
    fun `parseControlData parses correctly`() {
        val bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
            // Servo
            .putShort(100.toShort()) // min
            .putShort(600.toShort()) // max
            // Accel
            .putShort(150.toShort()) // min
            .putShort(850.toShort()) // max
            .array()

        val result = parser.parseControlData(bytes)

        assertEquals(100, result.servo.min)
        assertEquals(600, result.servo.max)
        assertEquals(150, result.accelerator.min)
        assertEquals(850, result.accelerator.max)
    }

    @Test
    fun `parseCalibrationData parses correctly`() {
        val bytes = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(1000.toShort()).putShort(2000.toShort()) // HallA
            .putShort(1100.toShort()).putShort(2100.toShort()) // HallB
            .putShort(1200.toShort()).putShort(2200.toShort()) // Servo
            .array()

        val result = parser.parseCalibrationData(bytes)

        assertEquals(1000, result.hallA.min)
        assertEquals(2000, result.hallA.max)
        assertEquals(1100, result.hallB.min)
        assertEquals(2100, result.hallB.max)
        assertEquals(1200, result.servo.min)
        assertEquals(2200, result.servo.max)
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

    @Test
    fun `parseSystemTelemetry parses correctly`() {
        val bytes = ByteBuffer.allocate(35).order(ByteOrder.LITTLE_ENDIAN)
            .put(1.toByte()) // isGuardActive
            .put(0.toByte()) // isBrakeEnabled
            // ECU Telemetry (12 bytes)
            .put(1.toByte()) // isConnected
            .put(1.toByte()) // isStarted
            .put(0.toByte()) // isNeutral
            .putShort(3000.toShort()) // rpm
            .put(14.toByte()) // battery
            .put(60.toByte()) // speed
            .put(90.toByte()) // map
            .putShort(2500.toShort()) // tps
            .put(45.toByte()) // airTemp
            .put(85.toByte()) // coolantTemp
            // Servo Telemetry (9 bytes)
            .put(1.toByte()) // isConnected
            .put(1.toByte()) // isEnabled
            .put(0.toByte()) // isMoved
            .put(12.toByte()) // voltage
            .putShort(500.toShort()) // current
            .putShort(1500.toShort()) // position
            .put(50.toByte()) // temperature
            // Accelerator Telemetry (6 bytes)
            .putShort(1000.toShort()) // hallA
            .putShort(2000.toShort()) // hallB
            .putShort(1500.toShort()) // position
            // System level fields
            .put(100.toByte()) // targetSpeed
            .putShort(300.toShort()) // throttlePosition
            .put(SystemState.NORMAL.value.toByte()) // systemState
            .putShort(0.toShort()) // activeErrors (None)
            .array()

        val result = parser.parseSystemTelemetry(bytes)

        assertEquals(true, result.isGuardActive)
        assertEquals(false, result.isBrakeEnabled)

        // ECU
        assertEquals(true, result.ecu.isConnected)
        assertEquals(true, result.ecu.isStarted)
        assertEquals(false, result.ecu.isNeutral)
        assertEquals(3000, result.ecu.rpm)
        assertEquals(14, result.ecu.battery)
        assertEquals(60, result.ecu.speed)
        assertEquals(90, result.ecu.map)
        assertEquals(2500, result.ecu.tps)
        assertEquals(45, result.ecu.airTemp)
        assertEquals(85, result.ecu.coolantTemp)

        // Servo
        assertEquals(true, result.servo.isConnected)
        assertEquals(true, result.servo.isEnabled)
        assertEquals(false, result.servo.isMoved)
        assertEquals(12, result.servo.voltage)
        assertEquals(500, result.servo.current)
        assertEquals(1500, result.servo.position)
        assertEquals(50, result.servo.temperature)

        // Accelerator
        assertEquals(1000, result.accelerator.hallA)
        assertEquals(2000, result.accelerator.hallB)
        assertEquals(1500, result.accelerator.position)

        // General
        assertEquals(100, result.targetSpeed)
        assertEquals(300, result.throttlePosition)
        assertEquals(SystemState.NORMAL, result.systemState)
        assertEquals(emptyList(), result.activeErrors)
    }

    @Test
    fun `parseOtaFeedback parses correctly`() {
        assertEquals(OTAStatus.READY_FOR_NEXT, parser.parseOtaFeedback(byteArrayOf(1)))
        assertEquals(OTAStatus.COMPLETED, parser.parseOtaFeedback(byteArrayOf(2)))
        assertEquals(OTAStatus.ERROR, parser.parseOtaFeedback(byteArrayOf(0)))
    }

    @Test
    fun `serializeControlData serializes correctly`() {
        val data = ControlData(
            servo = PositionRange(100, 600),
            accelerator = PositionRange(150, 850)
        )
        val bytes = parser.serializeControlData(data)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

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
        val bytes = parser.serializeCalibrationData(data)
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
        val chunkData = ByteArray(500) { it.toByte() }
        val chunk = OTAChunk(
            firmwareSize = 10000L,
            totalChunks = 20,
            chunkIndex = 5,
            data = chunkData
        )
        val bytes = parser.serializeOtaChunk(chunk)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        assertEquals(10000, buffer.int)
        assertEquals(20.toShort(), buffer.short)
        assertEquals(5.toShort(), buffer.short)

        val actualData = ByteArray(500)
        buffer.get(actualData)
        assertEquals(chunkData.toList(), actualData.toList())
    }
}
