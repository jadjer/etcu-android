package by.jadjer.etcu.domain.model.telemetry

import by.jadjer.etcu.domain.model.system.SystemError
import by.jadjer.etcu.domain.model.system.SystemState

data class SystemStatusTelemetry(
    val isGuardActive: Boolean = false,
    val isBrakeEnabled: Boolean = false,
    val throttlePosition: Int = 0,
    val systemState: SystemState = SystemState.UNKNOWN,
    val activeErrors: List<SystemError> = emptyList()
)
