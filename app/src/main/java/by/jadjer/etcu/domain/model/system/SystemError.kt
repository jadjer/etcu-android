package by.jadjer.etcu.domain.model.system

import by.jadjer.etcu.R

enum class SystemError(val bitMask: Int, val resId: Int) {
    ECU_INIT_ERROR(1 shl 0, R.string.error_ecu_init),
    ECU_READ_ERROR(1 shl 1, R.string.error_ecu_read),

    GUARD_LOCK(1 shl 2, R.string.error_guard_lock),

    SERVO_INIT_ERROR(1 shl 3, R.string.error_servo_init),
    SERVO_READ_ERROR(1 shl 4, R.string.error_servo_read),
    SERVO_WRITE_ERROR(1 shl 5, R.string.error_servo_write),

    BLUETOOTH_INIT_ERROR(1 shl 6, R.string.error_bluetooth_init),
    BLUETOOTH_SET_POWER_ERROR(1 shl 7, R.string.error_bluetooth_power),
    BLUETOOTH_SET_MTU_ERROR(1 shl 8, R.string.error_bluetooth_mtu),
    BLUETOOTH_CONNECTED_ERROR(1 shl 9, R.string.error_bluetooth_connected),

    INDICATOR_INIT_ERROR(1 shl 10, R.string.error_indicator_init),

    PERIPHERAL_INIT_ERROR(1 shl 11, R.string.error_button_init),

    ACCELERATOR_INIT_ERROR(1 shl 12, R.string.error_accel_init),
    ACCELERATOR_READ_ERROR(1 shl 13, R.string.error_accel_read),
    ACCELERATOR_MISMATCH(1 shl 14, R.string.error_accel_mismatch);

    companion object {
        fun parseErrors(errorsMask: Int): List<SystemError> {
            if (errorsMask == 0) return emptyList()
            return entries.filter { (errorsMask and it.bitMask) != 0 }
        }
    }
}
