package by.jadjer.etcu.domain.model

import by.jadjer.etcu.R

enum class SystemError(val bitMask: Long, val resId: Int) {
    GUARD_LOCK(1L shl 0, R.string.error_guard_lock),

    SERVO_INIT_ERROR(1L shl 1, R.string.error_servo_init),
    SERVO_COMMS_ERROR(1L shl 2, R.string.error_servo_comms),
    SERVO_PROTOCOL_ERROR(1L shl 3, R.string.error_servo_protocol),
    SERVO_CHECKSUM_ERROR(1L shl 4, R.string.error_servo_checksum),
    SERVO_READ_ERROR(1L shl 5, R.string.error_servo_read),
    SERVO_WRITE_ERROR(1L shl 6, R.string.error_servo_write),
    SERVO_MODE_ERROR(1L shl 7, R.string.error_servo_mode),
    SERVO_SPEED_ERROR(1L shl 8, R.string.error_servo_speed),
    SERVO_POSITION_ERROR(1L shl 9, R.string.error_servo_position),
    SERVO_CURRENT_ERROR(1L shl 10, R.string.error_servo_current),
    SERVO_TORQUE_ERROR(1L shl 11, R.string.error_servo_torque),
    SERVO_OVERCURRENT(1L shl 12, R.string.error_servo_overcurrent),
    SERVO_OVERTEMP(1L shl 13, R.string.error_servo_overtemp),
    SERVO_CALIBRATE_ERROR(1L shl 14, R.string.error_servo_calibrate),
    SERVO_POWER_FAIL(1L shl 15, R.string.error_servo_power_fail),

    ACCELERATOR_INIT_FAULT(1L shl 16, R.string.error_accel_init),
    ACCELERATOR_CALIBRATE_FAULT(1L shl 17, R.string.error_accel_calibrate),
    ACCELERATOR_READ_FAULT(1L shl 18, R.string.error_accel_read),
    ACCELERATOR_MISMATCH(1L shl 19, R.string.error_accel_mismatch),
    BUTTON_INIT_FAULT(1L shl 20, R.string.error_button_init),
    BUTTON_READ_FAULT(1L shl 21, R.string.error_button_read),
    ECU_INIT_FAULT(1L shl 22, R.string.error_ecu_init),
    INDICATOR_INIT_FAULT(1L shl 23, R.string.error_indicator_init),
    BLUETOOTH_INIT_FAULT(1L shl 24, R.string.error_bluetooth_init),
    BLUETOOTH_SET_POWER_FAULT(1L shl 25, R.string.error_bluetooth_power),
    BLUETOOTH_SET_MTU_FAULT(1L shl 26, R.string.error_bluetooth_mtu),
    BLUETOOTH_CONNECTED_FAULT(1L shl 27, R.string.error_bluetooth_connected);

    companion object {
        fun parseErrors(errorsMask: Long): List<SystemError> {
            if (errorsMask == 0L) return emptyList()
            return entries.filter { (errorsMask and it.bitMask) != 0L }
        }
    }
}
