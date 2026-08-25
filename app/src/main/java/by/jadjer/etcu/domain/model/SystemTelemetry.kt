package by.jadjer.etcu.domain.model

data class SystemTelemetry(
    val isGuardActive: Boolean = false,
    val isBrakeEnabled: Boolean = false,

    val ecu: EcuTelemetry = EcuTelemetry(),
    val servo: ServoTelemetry = ServoTelemetry(),
    val acceleratorPosition: Int = 0,
    val acceleratorOffset: Int = 0,
    val throttlePosition: Int = 0,
    val targetSpeed: Int = 0,

    val systemState: SystemState = SystemState.UNKNOWN,
    val activeErrors: List<SystemError> = emptyList()
)
