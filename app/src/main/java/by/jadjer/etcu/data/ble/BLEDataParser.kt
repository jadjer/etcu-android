package by.jadjer.etcu.data.ble

import by.jadjer.etcu.data.ble.BLEConstants.CALIBRATION_DATA_SIZE
import by.jadjer.etcu.data.ble.BLEConstants.CONTROL_DATA_SIZE
import by.jadjer.etcu.data.ble.BLEConstants.INFO_STR_LEN
import by.jadjer.etcu.data.ble.BLEConstants.SYSTEM_INFO_SIZE
import by.jadjer.etcu.data.ble.BLEConstants.TELEMETRY_SIZE
import by.jadjer.etcu.domain.model.calibration.CalibrationData
import by.jadjer.etcu.domain.model.calibration.CalibrationRange
import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.control.Cruise
import by.jadjer.etcu.domain.model.control.PID
import by.jadjer.etcu.domain.model.ota.OTAChunk
import by.jadjer.etcu.domain.model.ota.OTAStatus
import by.jadjer.etcu.domain.model.system.SystemError
import by.jadjer.etcu.domain.model.system.SystemInfo
import by.jadjer.etcu.domain.model.system.SystemState
import by.jadjer.etcu.domain.model.telemetry.AcceleratorTelemetry
import by.jadjer.etcu.domain.model.telemetry.CruiseTelemetry
import by.jadjer.etcu.domain.model.telemetry.ECUTelemetry
import by.jadjer.etcu.domain.model.telemetry.ServoTelemetry
import by.jadjer.etcu.domain.model.telemetry.SystemStatusTelemetry
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BLEDataParser {
    fun parseControlData(bytes: ByteArray): ControlData {
        if (bytes.size < CONTROL_DATA_SIZE) return ControlData()

        return try {
            val buffer = bytes.toLittleEndianBuffer()

            ControlData(
                cruise = buffer.parseCruise(),
                servoMin = buffer.uShort,
                servoMax = buffer.uShort,
                acceleratorMin = buffer.uShort,
                acceleratorMax = buffer.uShort,
            )
        } catch (_: Exception) {
            ControlData()
        }
    }

    fun parseCalibrationData(bytes: ByteArray): CalibrationData {
        if (bytes.size < CALIBRATION_DATA_SIZE) return CalibrationData()

        return try {
            val buffer = bytes.toLittleEndianBuffer()

            CalibrationData(
                hallA = CalibrationRange(min = buffer.uShort, max = buffer.uShort),
                hallB = CalibrationRange(min = buffer.uShort, max = buffer.uShort),
                servo = CalibrationRange(min = buffer.uShort, max = buffer.uShort)
            )
        } catch (_: Exception) {
            CalibrationData()
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

            val isGuardActive = buffer.bool
            val isBrakeEnabled = buffer.bool
            val throttlePosition = buffer.uShort
            val ecu = buffer.parseEcuTelemetry()
            val servo = buffer.parseServoTelemetry()
            val cruise = buffer.parseCruiseTelemetry()
            val accelerator = buffer.parseAcceleratorTelemetry()
            val systemState = SystemState.fromByte(buffer.uByte)
            val activeErrors = SystemError.parseErrors(buffer.uShort)

            SystemTelemetry(
                status = SystemStatusTelemetry(
                    isGuardActive = isGuardActive,
                    isBrakeEnabled = isBrakeEnabled,
                    throttlePosition = throttlePosition,
                    systemState = systemState,
                    activeErrors = activeErrors
                ),
                ecu = ecu,
                servo = servo,
                cruise = cruise,
                accelerator = accelerator
            )
        } catch (_: Exception) {
            SystemTelemetry()
        }
    }

    private fun ByteBuffer.parseCruise() = Cruise(
        pid = PID(p = float, i = float, d = float),
        rpmMin = uShort,
        rpmMax = uShort,
        speedMin = uByte,
        speedMax = uByte,
        limiterUp = uShort,
        limiterDown = uShort,
    )

    private fun ByteBuffer.parseEcuTelemetry() = ECUTelemetry(
        isConnected = bool,
        isStarted = bool,
        isNeutral = bool,
        rpm = uShort,
        speed = uByte,
        map = uByte,
        tps = uShort,
        battery = float,
        airTemp = uByte,
        coolantTemp = uByte,
    )

    private fun ByteBuffer.parseServoTelemetry() = ServoTelemetry(
        isConnected = bool,
        isEnabled = bool,
        isMoved = bool,
        current = uShort,
        voltage = float,
        position = uShort,
        temperature = uByte,
    )

    private fun ByteBuffer.parseCruiseTelemetry() = CruiseTelemetry(
        isEnabled = bool,
        isActivated = bool,
        error = float,
        correction = float,
        targetSpeed = uByte,
        currentSpeed = uByte,
        basePosition = uShort,
        currentPosition = uShort,
    )

    private fun ByteBuffer.parseAcceleratorTelemetry() = AcceleratorTelemetry(
        hallA = uShort,
        hallB = uShort,
        position = uShort,
    )

    fun parseOtaFeedback(bytes: ByteArray): OTAStatus {
        val firstByte = bytes.getOrNull(0) ?: return OTAStatus.ERROR
        return runCatching { OTAStatus.fromByte(firstByte) }.getOrDefault(OTAStatus.ERROR)
    }

    fun serializeControlData(data: ControlData): ByteArray {
        return ByteBuffer.allocate(CONTROL_DATA_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            // Cruise
            .putFloat(data.cruise.pid.p)
            .putFloat(data.cruise.pid.i)
            .putFloat(data.cruise.pid.d)
            .putShort(data.cruise.rpmMin.toShort())
            .putShort(data.cruise.rpmMax.toShort())
            .put(data.cruise.speedMin.toByte())
            .put(data.cruise.speedMax.toByte())
            .putShort(data.cruise.limiterUp.toShort())
            .putShort(data.cruise.limiterDown.toShort())
            // Servo
            .putShort(data.servoMin.toShort())
            .putShort(data.servoMax.toShort())
            // Accelerator
            .putShort(data.acceleratorMin.toShort())
            .putShort(data.acceleratorMax.toShort())
            .array()
    }

    fun serializeCalibrationData(data: CalibrationData): ByteArray {
        return ByteBuffer.allocate(CALIBRATION_DATA_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(data.hallA.min.toShort())
            .putShort(data.hallA.max.toShort())
            .putShort(data.hallB.min.toShort())
            .putShort(data.hallB.max.toShort())
            .putShort(data.servo.min.toShort())
            .putShort(data.servo.max.toShort())
            .array()
    }

    fun serializeOtaChunk(chunk: OTAChunk): ByteArray {
        return ByteBuffer.allocate(BLEConstants.OTA_PACKAGE_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(chunk.firmwareSize.toInt())
            .putShort(chunk.totalChunks.toShort())
            .putShort(chunk.chunkIndex.toShort())
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
}
