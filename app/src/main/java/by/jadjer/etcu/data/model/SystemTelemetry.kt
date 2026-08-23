package by.jadjer.etcu.data.model

data class SystemTelemetry(
    val servo: ServoTelemetry = ServoTelemetry(),
    val ecu: EcuTelemetry = EcuTelemetry(),
    val acceleratorPosition: Int = 0,
    val acceleratorOffset: Int = 0,
    val throttlePosition: Int = 0,
    val targetSpeed: Int = 0,
    val guardActive: Boolean = false,
    val brakeEnabled: Boolean = false,
    val clutchEnabled: Boolean = false,
    val systemState: SystemState = SystemState.UNKNOWN,
    val activeErrors: List<SystemError> = emptyList()
)
