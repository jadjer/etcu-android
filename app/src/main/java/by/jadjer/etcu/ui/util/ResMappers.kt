package by.jadjer.etcu.ui.util

import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.control.OperatingMode
import by.jadjer.etcu.domain.model.system.SystemError
import by.jadjer.etcu.domain.model.system.SystemState

val OperatingMode.labelResId: Int
    get() = when (this) {
        OperatingMode.RAIN -> R.string.mode_rain
        OperatingMode.NORMAL -> R.string.mode_normal
        OperatingMode.SPORT -> R.string.mode_sport
        OperatingMode.CUSTOM -> R.string.mode_custom
    }

val SystemState.labelResId: Int
    get() = when (this) {
        SystemState.OFF -> R.string.sys_state_off
        SystemState.NORMAL -> R.string.sys_state_normal
        SystemState.UPDATE -> R.string.sys_state_update
        SystemState.UNKNOWN -> R.string.unknown
    }

val SystemError.labelResId: Int
    get() = when (this) {
        SystemError.ECU_INIT_FAILED -> R.string.error_ecu_init
        SystemError.ECU_READ_FAILED -> R.string.error_ecu_read
        SystemError.ECU_WRITE_FAILED -> R.string.error_ecu_write
        SystemError.ECU_VOLTAGE_FAILED -> R.string.error_ecu_voltage
        SystemError.ECU_ENGINE_OVERHEAT -> R.string.error_ecu_overheat
        SystemError.GUARD_LOCKED -> R.string.error_guard_lock
        SystemError.SERVO_INIT_FAILED -> R.string.error_servo_init
        SystemError.SERVO_READ_FAILED -> R.string.error_servo_read
        SystemError.SERVO_WRITE_FAILED -> R.string.error_servo_write
        SystemError.SERVO_ENCODER_FAILED -> R.string.error_servo_encoder
        SystemError.SERVO_VOLTAGE_FAILED -> R.string.error_servo_voltage
        SystemError.SERVO_OVERHEAT -> R.string.error_servo_overtemp
        SystemError.SERVO_OVERLOAD -> R.string.error_servo_overload
        SystemError.BLUETOOTH_INIT_FAILED -> R.string.error_bluetooth_init
        SystemError.BLUETOOTH_POWER_FAILED -> R.string.error_bluetooth_power
        SystemError.BLUETOOTH_MTU_FAILED -> R.string.error_bluetooth_mtu
        SystemError.BLUETOOTH_CONN_FAILED -> R.string.error_bluetooth_conn
        SystemError.BLUETOOTH_SEND_FAILED -> R.string.error_bluetooth_send
        SystemError.INDICATOR_INIT_FAILED -> R.string.error_indicator_init
        SystemError.PERIPHERAL_INIT_FAILED -> R.string.error_button_init
        SystemError.ACCELERATOR_INIT_FAILED -> R.string.error_accel_init
        SystemError.ACCELERATOR_READ_FAILED -> R.string.error_accel_read
        SystemError.ACCELERATOR_MISMATCH -> R.string.error_accel_mismatch
    }
