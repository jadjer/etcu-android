package by.jadjer.etcu.data.ble

import by.jadjer.etcu.data.ble.BLEConstants.CONTROL_DATA_SIZE
import by.jadjer.etcu.data.ble.BLEConstants.INFO_STR_LEN
import by.jadjer.etcu.data.ble.BLEConstants.SYSTEM_INFO_SIZE
import by.jadjer.etcu.data.ble.BLEConstants.TELEMETRY_SIZE
import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.telemetry.ECUTelemetry
import by.jadjer.etcu.domain.model.ota.OTAChunk
import by.jadjer.etcu.domain.model.ota.OTAStatus
import by.jadjer.etcu.domain.model.telemetry.ServoTelemetry
import by.jadjer.etcu.domain.model.system.SystemError
import by.jadjer.etcu.domain.model.system.SystemInfo
import by.jadjer.etcu.domain.model.system.SystemState
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BLEDataParser {
    fun parseControlData(bytes: ByteArray): ControlData {
        if (bytes.size < CONTROL_DATA_SIZE) return ControlData()

        return try {
            val buffer = bytes.toLittleEndianBuffer()
            ControlData(
                servoMin = buffer.uShort,
                servoMax = buffer.uShort,
                acceleratorMin = buffer.uShort,
                acceleratorMax = buffer.uShort,
            )
        } catch (_: Exception) {
            ControlData()
        }
    }

    fun parseSystemInfo(bytes: ByteArray): SystemInfo {
        if (bytes.size < SYSTEM_INFO_SIZE) return SystemInfo()

        return try {
            val buildDate = bytes.readString(0, INFO_STR_LEN)
            val boardVersion = bytes.readString(INFO_STR_LEN, INFO_STR_LEN)
            val firmwareVersion = bytes.readString(INFO_STR_LEN * 2, INFO_STR_LEN)

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
        if (bytes.size < TELEMETRY_SIZE) return SystemTelemetry()

        return try {
            val buffer = bytes.toLittleEndianBuffer()

            SystemTelemetry(
                isGuardActive = buffer.bool,
                isBrakeEnabled = buffer.bool,
                ecu = buffer.parseEcuTelemetry(),
                servo = buffer.parseServoTelemetry(),
                targetSpeed = buffer.uByte,
                throttlePosition = buffer.uShort,
                acceleratorPosition = buffer.uShort,
                systemState = SystemState.fromByte(buffer.get()),
                activeErrors = SystemError.parseErrors(buffer.uInt)
            )
        } catch (_: Exception) {
            SystemTelemetry()
        }
    }

    private fun ByteBuffer.parseEcuTelemetry() = ECUTelemetry(
        isConnected = bool,
        isStarted = bool,
        isClutchEnabled = bool,
        rpm = uShort,
        battery = uByte,
        speed = uByte,
        map = uByte,
        tps = uShort,
        airTemp = uByte,
        coolantTemp = uByte,
    )

    private fun ByteBuffer.parseServoTelemetry() = ServoTelemetry(
        isConnected = bool,
        isEnabled = bool,
        isMoved = bool,
        voltage = uByte,
        current = uShort,
        position = uShort,
        temperature = uByte
    )

    fun parseOtaFeedback(bytes: ByteArray): OTAStatus {
        val firstByte = bytes.getOrNull(0) ?: return OTAStatus.ERROR
        return runCatching { OTAStatus.fromByte(firstByte) }.getOrDefault(OTAStatus.ERROR)
    }

    fun serializeControlData(data: ControlData): ByteArray {
        return ByteBuffer.allocate(CONTROL_DATA_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(data.servoMin.toShort())
            .putShort(data.servoMax.toShort())
            .putShort(data.acceleratorMin.toShort())
            .putShort(data.acceleratorMax.toShort())
            .array()
    }

    fun serializeOtaChunk(chunk: OTAChunk): ByteArray {
        return ByteBuffer.allocate(BLEConstants.OTA_PACKAGE_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(chunk.firmwareSize.toInt())
            .putShort(chunk.totalChunks.toShort())
            .putShort(chunk.chunkNumber.toShort())
            .put(chunk.data)
            .array()
    }

    // Helper Extensions
    private fun ByteArray.toLittleEndianBuffer() =
        ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)

    private fun ByteArray.readString(offset: Int, length: Int) =
        String(this, offset, length, Charsets.UTF_8).trim { it <= '\u0000' }

    private val ByteBuffer.bool get() = get().toInt() != 0
    private val ByteBuffer.uByte get() = get().toInt() and 0xFF
    private val ByteBuffer.uShort get() = short.toInt() and 0xFFFF
    private val ByteBuffer.uInt get() = int.toLong() and 0xFFFFFFFFL
}
