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
        if (bytes.size < 35) return SystemTelemetry()

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        // 1. ServoTelemetry
        val servoConnected = buffer.get().toInt() != 0
        val servoMoved = buffer.get().toInt() != 0
        val servoLoad = buffer.short.toInt() and 0xFFFF
        val servoSpeed = buffer.short.toInt() and 0xFFFF
        val servoCurrent = buffer.short.toInt() and 0xFFFF
        val servoVoltage = buffer.get().toInt() and 0xFF
        val servoPosition = buffer.short.toInt() and 0xFFFF
        val servoTemperature = buffer.short.toInt() and 0xFFFF

        val servo = ServoTelemetry(
            isConnected = servoConnected, isMoved = servoMoved, load = servoLoad,
            speed = servoSpeed, current = servoCurrent, voltage = servoVoltage,
            position = servoPosition, temperature = servoTemperature
        )

        // 2. ECUTelemetry
        val ecuConnected = buffer.get().toInt() != 0
        val ecuRpm = buffer.short.toInt() and 0xFFFF
        val ecuSpeed = buffer.short.toInt() and 0xFFFF
        val ecuTps = buffer.short.toInt() and 0xFFFF
        val ecuStarted = buffer.get().toInt() != 0
        val ecuClutch = buffer.get().toInt() != 0

        val ecu = EcuTelemetry(
            isConnected = ecuConnected, rpm = ecuRpm, speed = ecuSpeed,
            tps = ecuTps, started = ecuStarted, clutchEnabled = ecuClutch
        )

        // 3. SystemTelemetry
        val accelPos = buffer.short.toInt() and 0xFFFF
        val accelOffset = buffer.short.toInt() and 0xFFFF
        val throttlePos = buffer.short.toInt() and 0xFFFF
        val targetSpeed = buffer.short.toInt() and 0xFFFF

        val guardActive = buffer.get().toInt() != 0
        val brakeEnabled = buffer.get().toInt() != 0
        val sysClutchEnabled = buffer.get().toInt() != 0

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

    fun parseOtaFeedback(bytes: ByteArray): Int? {
        return if (bytes.size >= 2) {
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF
        } else null
    }
}
