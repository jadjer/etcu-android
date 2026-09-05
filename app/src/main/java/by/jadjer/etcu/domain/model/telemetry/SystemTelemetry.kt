package by.jadjer.etcu.domain.model.telemetry

import by.jadjer.etcu.domain.model.system.SystemState
import by.jadjer.etcu.domain.model.system.SystemError

data class SystemTelemetry(
    val isGuardActive: Boolean = false,
    val isBrakeEnabled: Boolean = false,

    val ecu: ECUTelemetry = ECUTelemetry(),
    val servo: ServoTelemetry = ServoTelemetry(),
    val acceleratorPosition: Int = 0,
    val throttlePosition: Int = 0,
    val targetSpeed: Int = 0,

    val systemState: SystemState = SystemState.UNKNOWN,
    val activeErrors: List<SystemError> = emptyList()
)
