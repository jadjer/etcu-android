package by.jadjer.etcu.domain.model.system

enum class SystemError(val bitMask: Long) {
    ECU_INIT_FAILED(1L shl 0),
    ECU_READ_FAILED(1L shl 1),
    ECU_WRITE_FAILED(1L shl 2),
    ECU_VOLTAGE_FAILED(1L shl 3),
    ECU_ENGINE_OVERHEAT(1L shl 4),

    GUARD_LOCKED(1L shl 5),

    SERVO_INIT_FAILED(1L shl 6),
    SERVO_READ_FAILED(1L shl 7),
    SERVO_WRITE_FAILED(1L shl 8),
    SERVO_ENCODER_FAILED(1L shl 9),
    SERVO_VOLTAGE_FAILED(1L shl 10),
    SERVO_OVERHEAT(1L shl 11),
    SERVO_OVERLOAD(1L shl 12),

    BLUETOOTH_INIT_FAILED(1L shl 13),
    BLUETOOTH_POWER_FAILED(1L shl 14),
    BLUETOOTH_MTU_FAILED(1L shl 15),
    BLUETOOTH_CONN_FAILED(1L shl 16),
    BLUETOOTH_SEND_FAILED(1L shl 17),

    INDICATOR_INIT_FAILED(1L shl 18),

    PERIPHERAL_INIT_FAILED(1L shl 19),

    ACCELERATOR_INIT_FAILED(1L shl 20),
    ACCELERATOR_READ_FAILED(1L shl 21),
    ACCELERATOR_MISMATCH(1L shl 22);

    companion object {
        fun parseErrors(errorsMask: Long): List<SystemError> {
            if (errorsMask == 0L) return emptyList()
            return entries.filter { (errorsMask and it.bitMask) != 0L }
        }
    }
}
