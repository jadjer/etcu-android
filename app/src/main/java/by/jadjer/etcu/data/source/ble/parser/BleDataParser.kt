package by.jadjer.etcu.data.source.ble.parser

import by.jadjer.etcu.data.model.*
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
        // SystemTelemetry size = 38 bytes (1+1+1 + 13 + 9 + 2+2+2+2 + 1 + 4)
        if (bytes.size < 38) return SystemTelemetry()

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        // 1. System-level flags (bools)
        val guardActive = buffer.get().toInt() != 0
        val brakeEnabled = buffer.get().toInt() != 0
        val sysClutchEnabled = buffer.get().toInt() != 0

        // 2. ServoTelemetry (13 bytes)
        val servo = parseServoTelemetry(buffer)

        // 3. ECUTelemetry (9 bytes)
        val ecu = parseEcuTelemetry(buffer)

        // 4. SystemTelemetry Remaining fields
        val accelPos = buffer.short.toInt() and 0xFFFF
        val accelOffset = buffer.short.toInt() and 0xFFFF
        val throttlePos = buffer.short.toInt() and 0xFFFF
        val targetSpeed = buffer.short.toInt() and 0xFFFF

        val systemState = SystemState.fromByte(buffer.get())
        val rawErrorsMask = buffer.int.toLong() and 0xFFFFFFFFL
        val activeErrorsList = SystemError.parseErrors(rawErrorsMask)

        return SystemTelemetry(
            servo = servo,
            ecu = ecu,
            acceleratorPosition = accelPos,
            acceleratorOffset = accelOffset,
            throttlePosition = throttlePos,
            targetSpeed = targetSpeed,
            guardActive = guardActive,
            brakeEnabled = brakeEnabled,
            clutchEnabled = sysClutchEnabled,
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
            rpm = rpm,
            speed = speed,
            tps = tps,
            started = isStarted,
            clutchEnabled = isClutchEnabled
        )
    }

    fun parseOtaFeedback(bytes: ByteArray): Int? {
        return if (bytes.size >= 2) {
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF
        } else null
    }
}
