package by.jadjer.etcu.data.model

enum class SystemError(val bitMask: Long, val description: String) {
    GUARD_LOCK(1L shl 0, "Блокировка защиты (Guard Lock)"),

    SERVO_INIT_ERROR(1L shl 1, "Ошибка инициализации сервопривода"),
    SERVO_COMMS_ERROR(1L shl 2, "Ошибка связи с сервоприводом"),
    SERVO_PROTOCOL_ERROR(1L shl 3, "Ошибка протокола сервопривода"),
    SERVO_CHECKSUM_ERROR(1L shl 4, "Ошибка контрольной суммы сервопривода"),
    SERVO_READ_ERROR(1L shl 5, "Ошибка чтения данных сервопривода"),
    SERVO_WRITE_ERROR(1L shl 6, "Ошибка записи данных сервопривода"),
    SERVO_MODE_ERROR(1L shl 7, "Неверный режим работы сервопривода"),
    SERVO_SPEED_ERROR(1L shl 8, "Ошибка скорости сервопривода"),
    SERVO_POSITION_ERROR(1L shl 9, "Ошибка позиционирования сервопривода"),
    SERVO_CURRENT_ERROR(1L shl 10, "Ошибка тока сервопривода"),
    SERVO_TORQUE_ERROR(1L shl 11, "Ошибка крутящего момента сервопривода"),
    SERVO_OVERCURRENT(1L shl 12, "Превышение допустимого тока сервопривода"),
    SERVO_OVERTEMP(1L shl 13, "Перегрев сервопривода"),
    SERVO_CALIBRATE_ERROR(1L shl 14, "Ошибка калибровки сервопривода"),
    SERVO_POWER_FAIL(1L shl 15, "Сбой питания сервопривода"),

    ACCELERATOR_INIT_FAULT(1L shl 16, "Сбой инициализации акселератора"),
    ACCELERATOR_CALIBRATE_FAULT(1L shl 17, "Сбой калибровки акселератора"),
    ACCELERATOR_READ_FAULT(1L shl 18, "Сбой чтения датчика акселератора"),
    ACCELERATOR_MISMATCH(1L shl 19, "Рассогласование каналов акселератора"),
    BUTTON_INIT_FAULT(1L shl 20, "Сбой инициализации кнопок"),
    BUTTON_READ_FAULT(1L shl 21, "Сбой чтения состояния кнопок"),
    ECU_INIT_FAULT(1L shl 22, "Сбой инициализации ЭБУ (ECU)"),
    INDICATOR_INIT_FAULT(1L shl 23, "Сбой инициализации индикатора"),
    BLUETOOTH_INIT_FAULT(1L shl 24, "Сбой инициализации Bluetooth на контроллере"),
    BLUETOOTH_SET_POWER_FAULT(1L shl 25, "Ошибка настройки мощности Bluetooth"),
    BLUETOOTH_SET_MTU_FAULT(1L shl 26, "Ошибка настройки MTU Bluetooth"),
    BLUETOOTH_CONNECTED_FAULT(1L shl 27, "Сбой соединения Bluetooth");

    companion object {
        fun parseErrors(errorsMask: Long): List<SystemError> {
            if (errorsMask == 0L) return emptyList()
            return entries.filter { (errorsMask and it.bitMask) != 0L }
        }
    }
}
