package by.jadjer.etcu.data.ble

import by.jadjer.etcu.data.model.EcuTelemetry
import by.jadjer.etcu.data.model.ServoTelemetry
import by.jadjer.etcu.data.model.SystemError
import by.jadjer.etcu.data.model.SystemInfo
import by.jadjer.etcu.data.model.SystemState
import by.jadjer.etcu.data.model.SystemTelemetry
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BleDataParser {

    fun parseSystemInfo(bytes: ByteArray): SystemInfo {
        if (bytes.size < 48) return SystemInfo()

        val boardVersion = String(bytes, 0, 16, Charsets.UTF_8).trim { it <= '\u0000' }
        val buildDate = String(bytes, 16, 16, Charsets.UTF_8).trim { it <= '\u0000' }
        val firmwareVersion = String(bytes, 32, 16, Charsets.UTF_8).trim { it <= '\u0000' }

        return SystemInfo(
            boardVersion = boardVersion,
            buildDate = buildDate,
            firmwareVersion = firmwareVersion
        )
    }

    fun parseTelemetry(bytes: ByteArray): SystemTelemetry {
        // SystemTelemetry size = 37 bytes (1+1 + 9 + 13 + 2+2+2+2 + 1 + 4)
        if (bytes.size < 37) return SystemTelemetry()

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        // 1. System-level flags (bools)
        val isGuardActive = buffer.get().toInt() != 0
        val isBrakeEnabled = buffer.get().toInt() != 0

        // 2. ECUTelemetry (9 bytes)
        val ecu = parseEcuTelemetry(buffer)

        // 3. ServoTelemetry (13 bytes)
        val servo = parseServoTelemetry(buffer)

        // 4. SystemTelemetry Remaining fields
        val accelPos = buffer.short.toInt() and 0xFFFF
        val accelOffset = buffer.short.toInt() and 0xFFFF
        val throttlePos = buffer.short.toInt() and 0xFFFF
        val targetSpeed = buffer.short.toInt() and 0xFFFF

        val systemState = SystemState.fromByte(buffer.get())
        val rawErrorsMask = buffer.int.toLong() and 0xFFFFFFFFL
        val activeErrorsList = SystemError.parseErrors(rawErrorsMask)

        return SystemTelemetry(
            isGuardActive = isGuardActive,
            isBrakeEnabled = isBrakeEnabled,
            ecu = ecu,
            servo = servo,
            acceleratorPosition = accelPos,
            acceleratorOffset = accelOffset,
            throttlePosition = throttlePos,
            targetSpeed = targetSpeed,
            systemState = systemState,
            activeErrors = activeErrorsList
        )
    }

    private fun parseServoTelemetry(buffer: ByteBuffer): ServoTelemetry {
        val isConnected = buffer.get().toInt() != 0
        val isMoved = buffer.get().toInt() != 0
        val load = buffer.short.toInt() and 0xFFFF
        val speed = buffer.short.toInt() and 0xFFFF
        val current = buffer.short.toInt() and 0xFFFF
        val voltage = buffer.get().toInt() and 0xFF
        val position = buffer.short.toInt() and 0xFFFF // uint16_t in new struct
        val temperature = buffer.short.toInt() and 0xFFFF

        return ServoTelemetry(
            isConnected = isConnected,
            isMoved = isMoved,
            load = load,
            speed = speed,
            current = current,
            voltage = voltage,
            position = position,
            temperature = temperature
        )
    }

    private fun parseEcuTelemetry(buffer: ByteBuffer): EcuTelemetry {
        val isConnected = buffer.get().toInt() != 0
        val isStarted = buffer.get().toInt() != 0
        val isClutchEnabled = buffer.get().toInt() != 0
        val rpm = buffer.short.toInt() and 0xFFFF
        val speed = buffer.short.toInt() and 0xFFFF
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

    fun parseOtaFeedback(bytes: ByteArray): Int? {
        return if (bytes.size >= 2) {
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF
        } else null
    }
}