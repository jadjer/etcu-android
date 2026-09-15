package by.jadjer.etcu.domain.model.telemetry

object TelemetryConstants {
    // ECU
    val RPM_RANGE = 0f..10000f
    val SPEED_RANGE = 0f..250f
    val TPS_RANGE = 0f..1000f
    val BATTERY_RANGE = 10f..16f
    val MAP_RANGE = 0f..150f
    val TEMPERATURE_RANGE = -20f..120f

    // Cruise
    val CRUISE_ERROR_RANGE = -100f..100f
    val POSITION_RANGE = 0f..1000f

    // Servo
    val SERVO_POSITION_RANGE = 0f..1024f
    val SERVO_CURRENT_RANGE = 0f..2500f
    val SERVO_VOLTAGE_RANGE = 0f..16f
    // History
    const val HISTORY_DURATION_MINUTES = 60
    const val HISTORY_DURATION_MS = HISTORY_DURATION_MINUTES * 60 * 1000L
}
