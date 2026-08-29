package by.jadjer.etcu.domain.model

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    SCANNING,
    CONNECTED_DISCOVERING,
    SERVICES_DISCOVERED,
    OTA_SETUP,
    READING_INFO,
    READING_SETTINGS,
    READY,
    BLUETOOTH_OFF,
    INVALID_MAC,
    ERROR_PROTOCOL,
    ERROR_CONNECTION,
    ERROR_SERVICES,
    ERROR_MTU,
    ERROR_DESCRIPTOR_WRITE,
    ERROR_INFO_NOT_FOUND,
    ERROR_READ_CHAR,
    ERROR_WRITE_CHAR;

    val isProcessing: Boolean
        get() = when (this) {
            CONNECTING,
            SCANNING -> true

            else -> false
        }

    val isActive: Boolean
        get() = when (this) {
            CONNECTED_DISCOVERING,
            SERVICES_DISCOVERED,
            OTA_SETUP,
            READING_INFO,
            READING_SETTINGS,
            READY,
            ERROR_MTU,
            ERROR_READ_CHAR,
            ERROR_WRITE_CHAR -> true

            else -> false
        }

    val isError: Boolean
        get() = when (this) {
            ERROR_PROTOCOL,
            ERROR_CONNECTION,
            ERROR_SERVICES,
            ERROR_MTU,
            ERROR_DESCRIPTOR_WRITE,
            ERROR_INFO_NOT_FOUND,
            ERROR_READ_CHAR,
            ERROR_WRITE_CHAR,
            INVALID_MAC -> true

            else -> false
        }
}
