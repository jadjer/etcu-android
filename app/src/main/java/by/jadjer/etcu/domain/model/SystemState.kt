package by.jadjer.etcu.domain.model

enum class SystemState(val value: Int) {
    OFF(0),
    NORMAL(1),
    CALIBRATION(2),
    UPDATE(3),
    UNKNOWN(-1);

    companion object {
        fun fromByte(byte: Byte): SystemState {
            val intVal = byte.toInt() and 0xFF
            return entries.find { it.value == intVal } ?: UNKNOWN
        }
    }
}
