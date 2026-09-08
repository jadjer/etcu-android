package by.jadjer.etcu.domain.model.system

enum class SystemState(val value: Int) {
    OFF(0),
    NORMAL(1),
    UPDATE(2),
    UNKNOWN(-1);

    companion object {
        fun fromByte(byte: Int): SystemState {
            val intVal = byte and 0xFF
            return entries.find { it.value == intVal } ?: UNKNOWN
        }
    }
}
