package by.jadjer.etcu.domain.model.telemetry

object TelemetryConstants {
    // ECU
    val RPM_RANGE = 0f..10000f
    val SPEED_RANGE = 0f..200f
    val TPS_RANGE = 0f..1000f
    val VOLTAGE_RANGE = 0f..15f
    val MAP_RANGE = 0f..100f
    val TEMPERATURE_RANGE = -20f..120f

    // Cruise
    val CRUISE_ERROR_RANGE = -1000f..1000f
    val POSITION_RANGE = 0f..1000f

    // Servo
    val SERVO_POSITION_RANGE = 0f..4095f
    val SERVO_CURRENT_RANGE = 0f..2500f

    // History
    const val HISTORY_DURATION_MINUTES = 60
    const val HISTORY_DURATION_MS = HISTORY_DURATION_MINUTES * 60 * 1000L
}
