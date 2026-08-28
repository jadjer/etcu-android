package by.jadjer.etcu.data.ble

import by.jadjer.etcu.domain.model.ControlData
import by.jadjer.etcu.domain.model.EcuTelemetry
import by.jadjer.etcu.domain.model.ServoTelemetry
import by.jadjer.etcu.domain.model.SystemError
import by.jadjer.etcu.domain.model.SystemInfo
import by.jadjer.etcu.domain.model.SystemState
import by.jadjer.etcu.domain.model.SystemTelemetry
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BleDataParser {

    fun parseControlData(bytes: ByteArray): ControlData {
        if (bytes.size < 8) return ControlData()

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        val accMin = buffer.short.toInt() and 0xFFFF
        val accMax = buffer.short.toInt() and 0xFFFF
        val servoMin = buffer.short.toInt() and 0xFFFF
        val servoMax = buffer.short.toInt() and 0xFFFF

        return ControlData(
            accMin = accMin,
            accMax = accMax,
            servoMin = servoMin,
            servoMax = servoMax,
        )
    }

    fun parseSystemInfo(bytes: ByteArray): SystemInfo {
        if (bytes.size < 48) return SystemInfo()

        val buildDate = String(bytes, 0, 16, Charsets.UTF_8).trim { it <= '\u0000' }
        val boardVersion = String(bytes, 16, 16, Charsets.UTF_8).trim { it <= '\u0000' }
        val firmwareVersion = String(bytes, 32, 16, Charsets.UTF_8).trim { it <= '\u0000' }

        return SystemInfo(
            boardVersion = boardVersion,
            buildDate = buildDate,
            firmwareVersion = firmwareVersion
        )
    }

    fun parseSystemTelemetry(bytes: ByteArray): SystemTelemetry {
        if (bytes.size < 30) return SystemTelemetry()

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        // 1. System-level flags (bools)
        val isGuardActive = buffer.get().toInt() != 0
        val isBrakeEnabled = buffer.get().toInt() != 0

        // 2. ECUTelemetry (9 bytes)
        val ecu = parseEcuTelemetry(buffer)

        // 3. ServoTelemetry (13 bytes)
        val servo = parseServoTelemetry(buffer)

        // 4. SystemTelemetry Remaining fields
        val targetSpeed = buffer.get().toInt() and 0xFF
        val throttlePos = buffer.short.toInt() and 0xFFFF
        val accelPos = buffer.short.toInt() and 0xFFFF

        val systemState = SystemState.fromByte(buffer.get())

        val rawErrorsMask = buffer.int.toLong() and 0xFFFFFFFFL
        val activeErrorsList = SystemError.parseErrors(rawErrorsMask)

        return SystemTelemetry(
            isGuardActive = isGuardActive,
            isBrakeEnabled = isBrakeEnabled,
            ecu = ecu,
            servo = servo,
            acceleratorPosition = accelPos,
            throttlePosition = throttlePos,
            targetSpeed = targetSpeed,
            systemState = systemState,
            activeErrors = activeErrorsList
        )
    }

    private fun parseEcuTelemetry(buffer: ByteBuffer): EcuTelemetry {
        val isConnected = buffer.get().toInt() != 0
        val isStarted = buffer.get().toInt() != 0
        val isClutchEnabled = buffer.get().toInt() != 0

        val rpm = buffer.short.toInt() and 0xFFFF
        val speed = buffer.get().toInt() and 0xFF
        val tps = buffer.short.toInt() and 0xFFFF

        return EcuTelemetry(
            isConnected = isConnected,
            isStarted = isStarted,
            isClutchEnabled = isClutchEnabled,
            rpm = rpm,
            speed = speed,
            tps = tps,
        )
    }

    private fun parseServoTelemetry(buffer: ByteBuffer): ServoTelemetry {
        val isConnected = buffer.get().toInt() != 0
        val isEnabled = buffer.get().toInt() != 0
        val isMoved = buffer.get().toInt() != 0

        val speed = buffer.get().toInt() and 0xFF
        val voltage = buffer.get().toInt() and 0xFF
        val current = buffer.short.toInt() and 0xFFFF
        val position = buffer.short.toInt() and 0xFFFF
        val temperature = buffer.get().toInt() and 0xFF

        return ServoTelemetry(
            isConnected = isConnected,
            isEnabled = isEnabled,
            isMoved = isMoved,
            speed = speed,
            current = current,
            voltage = voltage,
            position = position,
            temperature = temperature
        )
    }



    fun parseOtaFeedback(bytes: ByteArray): Int? {
        return if (bytes.size >= 2) {
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF
        } else null
    }
}