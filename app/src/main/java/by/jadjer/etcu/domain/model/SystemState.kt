package by.jadjer.etcu.domain.model

import by.jadjer.etcu.R

enum class SystemState(val value: Int, val resId: Int) {
    OFF(0, R.string.sys_state_off),
    NORMAL(1, R.string.sys_state_normal),
    CALIBRATION(2, R.string.sys_state_calibration),
    UPDATE(3, R.string.sys_state_update),
    UNKNOWN(-1, R.string.unknown);

    companion object {
        fun fromByte(byte: Byte): SystemState {
            val intVal = byte.toInt() and 0xFF
            return entries.find { it.value == intVal } ?: UNKNOWN
        }
    }
}
