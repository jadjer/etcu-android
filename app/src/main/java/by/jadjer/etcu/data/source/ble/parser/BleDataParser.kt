package by.jadjer.etcu.data.source.ble.parser

import by.jadjer.etcu.data.model.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BleDataParser {

    fun parseSystemInfo(bytes: ByteArray): SystemInfo {
        val str = String(bytes, Charsets.UTF_8).trim()
        val parts = str.split("|", ";", ",")
        return if (parts.size >= 3) {
            SystemInfo(
                boardVersion = parts[0],
                buildDate = parts[1],
                firmwareVersion = parts[2]
            )
        } else {
            SystemInfo(boardVersion = str)
        }
    }

    fun parseTelemetry(bytes: ByteArray): SystemTelemetry {
        // Based on the provided C++ hierarchy, the total size is 37 bytes.
        if (bytes.size < 37) return SystemTelemetry()

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        // 1. ServoTelemetry (12 bytes)
        val servo = parseServoTelemetry(buffer)

        // 2. ECUTelemetry (9 bytes)
        val ecu = parseEcuTelemetry(buffer)

        // 3. SystemTelemetry Remaining fields
        val accelPos = buffer.short.toInt() and 0xFFFF    // Position (uint16_t)
        val accelOffset = buffer.short.toInt() and 0xFFFF // Position (uint16_t)
        val throttlePos = buffer.short.toInt() and 0xFFFF // Position (uint16_t)
        val targetSpeed = buffer.short.toInt() and 0xFFFF // Speed (uint16_t)

        val guardActive = buffer.get().toInt() != 0       // bool
        val brakeEnabled = buffer.get().toInt() != 0      // bool
        val sysClutchEnabled = buffer.get().toInt() != 0  // bool

        val systemState = SystemState.fromByte(buffer.get()) // SystemState (uint8_t)

        val rawErrorsMask = buffer.int.toLong() and 0xFFFFFFFFL // SystemError (uint32_t)
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
        val isConnected = buffer.get().toInt() != 0   // bool
        val isMoved = buffer.get().toInt() != 0       // bool
        val load = buffer.short.toInt() and 0xFFFF     // uint16_t
        val speed = buffer.short.toInt() and 0xFFFF    // uint16_t
        val current = buffer.short.toInt() and 0xFFFF  // uint16_t
        val voltage = buffer.get().toInt() and 0xFF    // uint8_t
        val position = buffer.get().toInt() and 0xFF   // uint8_t
        val temperature = buffer.short.toInt() and 0xFFFF // uint16_t

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
        val isConnected = buffer.get().toInt() != 0   // bool
        val rpm = buffer.short.toInt() and 0xFFFF      // uint16_t
        val speed = buffer.short.toInt() and 0xFFFF    // uint16_t
        val tps = buffer.short.toInt() and 0xFFFF      // uint16_t
        val started = buffer.get().toInt() != 0        // bool
        val clutchEnabled = buffer.get().toInt() != 0  // bool

        return EcuTelemetry(
            isConnected = isConnected,
            rpm = rpm,
            speed = speed,
            tps = tps,
            started = started,
            clutchEnabled = clutchEnabled
        )
    }

    fun parseOtaFeedback(bytes: ByteArray): Int? {
        return if (bytes.size >= 2) {
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF
        } else null
    }
}
