package by.jadjer.etcu.domain.model.telemetry

import by.jadjer.etcu.domain.model.system.SystemError
import by.jadjer.etcu.domain.model.system.SystemState
import by.jadjer.etcu.domain.model.system.SystemWarning

data class SystemStatusTelemetry(
    val isGuardActive: Boolean = false,
    val isBrakeEnabled: Boolean = false,
    val throttlePosition: Int = 0,
    val systemState: SystemState = SystemState.UNKNOWN,
    val activeErrors: List<SystemError> = emptyList(),
    val activeWarnings: List<SystemWarning> = emptyList()
)
