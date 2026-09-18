package by.jadjer.etcu.ui.util

import android.content.Context
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.control.ControlData
import by.jadjer.etcu.domain.model.system.SystemWarning
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class TelemetryNotificationMonitor(
    private val context: Context,
    repository: BLERepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val notificationHelper = NotificationHelper(context)

    // State tracking to avoid spam
    private var lastEngineOverheat = false
    private var lastServoOverheat = false
    private var lastVoltageStatus = 0
    private var lastCruiseActivated = false
    private var lastFailReason: String? = null

    // Thresholds
    private val engineOverheatThreshold = 106
    private val engineRecoveryThreshold = 95
    private val servoOverheatThreshold = 70
    private val servoRecoveryThreshold = 65
    private val voltageLowThreshold = 11.5f
    private val voltageHighThreshold = 14.8f
    private val voltageRecoveryLow = 12.0f
    private val voltageRecoveryHigh = 14.5f

    init {
        repository.telemetry
            .combine(repository.controlData) { telemetry, control ->
                telemetry to control
            }
            .onEach { (telemetry, control) ->
                checkTelemetry(telemetry, control)
            }
            .launchIn(scope)
    }

    private fun checkTelemetry(telemetry: SystemTelemetry, control: ControlData) {
        checkEngineTemp(telemetry.ecu.coolantTemp)
        checkServoTemp(telemetry.servo.temperature)
        checkVoltage(telemetry.ecu.battery)
        checkCruiseState(telemetry, control)
    }

    private fun checkEngineTemp(temp: Int) {
        if (temp > engineOverheatThreshold && !lastEngineOverheat) {
            notificationHelper.showNotification(
                NotificationHelper.NOTIF_ID_ENGINE_TEMP,
                NotificationHelper.CHANNEL_ALERTS,
                context.getString(R.string.notif_engine_overheat),
                context.getString(R.string.notif_engine_overheat_desc, temp)
            )
            lastEngineOverheat = true
        } else if (temp < engineRecoveryThreshold && lastEngineOverheat) {
            lastEngineOverheat = false
            notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_ENGINE_TEMP)
        }
    }

    private fun checkServoTemp(temp: Int) {
        if (temp > servoOverheatThreshold && !lastServoOverheat) {
            notificationHelper.showNotification(
                NotificationHelper.NOTIF_ID_SERVO_TEMP,
                NotificationHelper.CHANNEL_ALERTS,
                context.getString(R.string.notif_servo_overheat),
                context.getString(R.string.notif_servo_overheat_desc, temp)
            )
            lastServoOverheat = true
        } else if (temp < servoRecoveryThreshold && lastServoOverheat) {
            lastServoOverheat = false
            notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_SERVO_TEMP)
        }
    }

    private fun checkVoltage(voltage: Float) {
        if (voltage < voltageLowThreshold && lastVoltageStatus != 1) {
            notificationHelper.showNotification(
                NotificationHelper.NOTIF_ID_VOLTAGE,
                NotificationHelper.CHANNEL_ALERTS,
                context.getString(R.string.notif_voltage_low),
                context.getString(R.string.notif_voltage_low_desc, voltage)
            )
            lastVoltageStatus = 1
        } else if (voltage > voltageHighThreshold && lastVoltageStatus != 2) {
            notificationHelper.showNotification(
                NotificationHelper.NOTIF_ID_VOLTAGE,
                NotificationHelper.CHANNEL_ALERTS,
                context.getString(R.string.notif_voltage_high),
                context.getString(R.string.notif_voltage_high_desc, voltage)
            )
            lastVoltageStatus = 2
        } else if (voltage in voltageRecoveryLow..voltageRecoveryHigh && lastVoltageStatus != 0) {
            lastVoltageStatus = 0
            notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_VOLTAGE)
        }
    }

    private fun checkCruiseState(telemetry: SystemTelemetry, control: ControlData) {
        val currentActivated = telemetry.cruise.isActivated

        // Notify on On/Off
        if (currentActivated != lastCruiseActivated) {
            val title = if (currentActivated) {
                context.getString(R.string.notif_cruise_on)
            } else {
                context.getString(R.string.notif_cruise_off)
            }
            notificationHelper.showNotification(
                NotificationHelper.NOTIF_ID_CRUISE,
                NotificationHelper.CHANNEL_CRUISE,
                title,
                ""
            )
            lastCruiseActivated = currentActivated
            if (currentActivated) {
                lastFailReason = null
                notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_CRUISE_FAIL)
            }
        }

        // Notify on activation failure based strictly on incoming warning channel data
        val activeWarning = telemetry.status.activeWarnings.firstOrNull()
        if (activeWarning != null) {
            val failReason = when (activeWarning) {
                SystemWarning.SPEED_LOW_FOR_CRUISE -> context.getString(
                    R.string.notif_cruise_failed_speed_low,
                    telemetry.ecu.speed
                )

                SystemWarning.SPEED_FAST_FOR_CRUISE -> context.getString(
                    R.string.notif_cruise_failed_speed_high,
                    telemetry.ecu.speed
                )

                SystemWarning.RPM_LOW_FOR_CRUISE -> context.getString(
                    R.string.notif_cruise_failed_rpm_low,
                    telemetry.ecu.rpm
                )

                SystemWarning.RPM_FAST_FOR_CRUISE -> context.getString(
                    R.string.notif_cruise_failed_rpm_high,
                    telemetry.ecu.rpm
                )

                SystemWarning.CRUISE_NOT_SET -> context.getString(R.string.notif_cruise_failed_not_set)
                SystemWarning.SAFETY_ENABLE -> context.getString(R.string.notif_cruise_failed_safety)
            }

            if (failReason != lastFailReason) {
                notificationHelper.showNotification(
                    NotificationHelper.NOTIF_ID_CRUISE_FAIL,
                    NotificationHelper.CHANNEL_CRUISE,
                    context.getString(R.string.notif_cruise_failed),
                    failReason
                )
                lastFailReason = failReason
            }
        } else {
            if (lastFailReason != null) {
                notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_CRUISE_FAIL)
                lastFailReason = null
            }
        }
    }
}
