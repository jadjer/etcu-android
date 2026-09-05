package by.jadjer.etcu.domain.model.ota

enum class OTAStatus(val value: Int) {
    ERROR(0),
    READY_FOR_NEXT(1),
    COMPLETED(2),
    UNKNOWN(-1);

    companion object {
        fun fromByte(byte: Byte): OTAStatus {
            val intVal = byte.toInt() and 0xFF
            return entries.find { it.value == intVal } ?: UNKNOWN
        }
    }
}
