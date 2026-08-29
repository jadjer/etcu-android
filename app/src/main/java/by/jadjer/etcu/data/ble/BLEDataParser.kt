package by.jadjer.etcu.data.ble

import by.jadjer.etcu.domain.model.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BLEDataParser {

    companion object {
        const val MIN_CONTROL_DATA_SIZE = 8
        const val SYSTEM_INFO_SIZE = 48
        const val MIN_TELEMETRY_SIZE = 30
        
        const val INFO_STR_LEN = 16
    }

    fun parseControlData(bytes: ByteArray): ControlData {
        if (bytes.size < MIN_CONTROL_DATA_SIZE) return ControlData()

        return try {
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val accMin = buffer.short.toInt() and 0xFFFF
            val accMax = buffer.short.toInt() and 0xFFFF
            val servoMin = buffer.short.toInt() and 0xFFFF
            val servoMax = buffer.short.toInt() and 0xFFFF

            ControlData(
                accMin = accMin,
                accMax = accMax,
                servoMin = servoMin,
                servoMax = servoMax,
            )
        } catch (_: Exception) {
            ControlData()
        }
    }

    fun parseSystemInfo(bytes: ByteArray): SystemInfo {
        if (bytes.size < SYSTEM_INFO_SIZE) return SystemInfo()

        return try {
            val buildDate = String(bytes, 0, INFO_STR_LEN, Charsets.UTF_8).trim { it <= '\u0000' }
            val boardVersion = String(bytes, INFO_STR_LEN, INFO_STR_LEN, Charsets.UTF_8).trim { it <= '\u0000' }
            val firmwareVersion = String(bytes, INFO_STR_LEN * 2, INFO_STR_LEN, Charsets.UTF_8).trim { it <= '\u0000' }

            SystemInfo(
                boardVersion = boardVersion,
                buildDate = buildDate,
                firmwareVersion = firmwareVersion
            )
        } catch (_: Exception) {
            SystemInfo()
        }
    }

    fun parseSystemTelemetry(bytes: ByteArray): SystemTelemetry {
        if (bytes.size < MIN_TELEMETRY_SIZE) return SystemTelemetry()

        return try {
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

            SystemTelemetry(
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
        } catch (_: Exception) {
            SystemTelemetry()
        }
    }

    private fun parseEcuTelemetry(buffer: ByteBuffer): ECUTelemetry {
        val isConnected = buffer.get().toInt() != 0
        val isStarted = buffer.get().toInt() != 0
        val isClutchEnabled = buffer.get().toInt() != 0

        val rpm = buffer.short.toInt() and 0xFFFF
        val speed = buffer.get().toInt() and 0xFF
        val tps = buffer.short.toInt() and 0xFFFF

        return ECUTelemetry(
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

    fun parseOtaFeedback(bytes: ByteArray): OTAStatus {
        if (bytes.isEmpty()) return OTAStatus.ERROR

        return try {
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            OTAStatus.fromByte(buffer.get())
        } catch (_: Exception) {
            OTAStatus.ERROR
        }
    }
}
