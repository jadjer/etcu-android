package by.jadjer.etcu.domain.model.system

enum class SystemError(val bitMask: Int) {
    ECU_INIT_ERROR(1 shl 0),
    ECU_READ_ERROR(1 shl 1),

    GUARD_LOCK(1 shl 2),

    SERVO_INIT_ERROR(1 shl 3),
    SERVO_READ_ERROR(1 shl 4),
    SERVO_WRITE_ERROR(1 shl 5),

    BLUETOOTH_INIT_ERROR(1 shl 6),
    BLUETOOTH_SET_POWER_ERROR(1 shl 7),
    BLUETOOTH_SET_MTU_ERROR(1 shl 8),
    BLUETOOTH_CONNECTED_ERROR(1 shl 9),

    INDICATOR_INIT_ERROR(1 shl 10),

    PERIPHERAL_INIT_ERROR(1 shl 11),

    ACCELERATOR_INIT_ERROR(1 shl 12),
    ACCELERATOR_READ_ERROR(1 shl 13),
    ACCELERATOR_MISMATCH(1 shl 14);

    companion object {
        fun parseErrors(errorsMask: Int): List<SystemError> {
            if (errorsMask == 0) return emptyList()
            return entries.filter { (errorsMask and it.bitMask) != 0 }
        }
    }
}
